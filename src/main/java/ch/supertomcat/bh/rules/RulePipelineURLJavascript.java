package ch.supertomcat.bh.rules;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLJavascript extends RulePipeline {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Make Package for logging accessable in javascript
	 */
	private static String javascriptProvidedFunctions = "var jsContext = JavaImporter(Packages.ch.supertomcat.bh.rules);\n\n";

	static {
		/*
		 * Define javascript functions for logging
		 * The functions will call Java Functions which will log the errors
		 */
		String javascriptPrintFunctionArr[] = { "function logDebug (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logDebug(message.toString(), '$SOURCE'); } }\n\n", "function logWarn (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logWarn(message.toString(), '$SOURCE'); } }\n\n", "function logError (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logError(message.toString(), '$SOURCE'); } }\n\n" };
		for (String function : javascriptPrintFunctionArr) {
			javascriptProvidedFunctions += function;
		}
	}

	private String javascriptCode = "";

	/**
	 * Amount of milliseconds to wait before getURL is executed
	 */
	private int waitBeforeExecute = 0;

	/**
	 * Flag if the result should be decoded (application/x-www-form-urlencoded)
	 */
	private boolean urlDecodeResult = false;

	/**
	 * Flag if cookies should be sent
	 */
	private boolean sendCookies = true;

	/**
	 * Constructor
	 * 
	 * @param mode Rule-Mode
	 */
	public RulePipelineURLJavascript(int mode) {
		super(mode);
	}

	/**
	 * Constructor
	 * 
	 * @param e Element
	 */
	public RulePipelineURLJavascript(Element e) {
		super(e);

		String javascriptCode = e.getChildText("javascript");
		if (javascriptCode != null) {
			this.javascriptCode = javascriptCode;
		}

		String strWaitBeforeExecute = e.getAttributeValue("waitBeforeExecute");
		if (strWaitBeforeExecute != null) {
			try {
				this.waitBeforeExecute = Integer.parseInt(e.getAttributeValue("waitBeforeExecute"));
			} catch (NumberFormatException ex) {
				logger.error("Could not parse waitBeforeExecute: {}", strWaitBeforeExecute, ex);
			}
		}

		String strUrlDecodeResult = e.getAttributeValue("urlDecodeResult");
		if (strUrlDecodeResult != null) {
			urlDecodeResult = Boolean.parseBoolean(strUrlDecodeResult);
		}

		String strSendCookies = e.getAttributeValue("sendCookies");
		if (strSendCookies != null) {
			sendCookies = Boolean.parseBoolean(strSendCookies);
		}
	}

	@Override
	public Element getXmlElement() {
		Element e = new Element("pipeline");
		e.setAttribute("mode", String.valueOf(this.mode));
		e.setAttribute("waitBeforeExecute", String.valueOf(this.waitBeforeExecute));
		e.setAttribute("urlDecodeResult", String.valueOf(urlDecodeResult));
		e.setAttribute("sendCookies", String.valueOf(sendCookies));
		Element elJavascript = new Element("javascript");
		elJavascript.setText(javascriptCode);
		e.addContent(elJavascript);
		return e;
	}

	@Override
	public String getURLByJavascript(String url, String thumbURL, String htmlcode, URLParseObject upo) throws HostException {
		Context context = Context.enter();

		// Restrict access to all java classes except a few
		context.setClassShutter(new ClassShutter() {
			@Override
			public boolean visibleToScripts(String className) {
				return JavascriptMethodProvider.class.getName().equals(className);
			}
		});
		try {
			// Prepare log functions
			String javascriptContextProvidedFunctions = javascriptProvidedFunctions.replace("$SOURCE", "{" + this.getClass().getName() + ";Thread:" + Thread.currentThread().getId() + "}");

			// Initialize standard objects
			ScriptableObject scope = context.initStandardObjects();

			InfoMap infoMap = new InfoMap(upo.getInfos());
			infoMap.setParentScope(scope);

			Method methodGetInfo = infoMap.getClass().getMethod("getInfo", String.class);
			FunctionObject functionObjGetInfo = new MyFunctionObject("getInfo", methodGetInfo, infoMap);

			Method methodSetInfo = infoMap.getClass().getMethod("setInfo", String.class, Object.class);
			FunctionObject functionObjSetInfo = new MyFunctionObject("setInfo", methodSetInfo, infoMap);

			// Provide functions to javascript
			scope.put("downloadContainerPage", scope, JavascriptMethodProvider.getJavascriptFunction("downloadContainerPage", scope));
			scope.put("getInfo", scope, functionObjGetInfo);
			scope.put("setInfo", scope, functionObjSetInfo);

			// Provide input variables to javascript
			ScriptableObject.putProperty(scope, "containerURL", Context.javaToJS(url, scope));
			ScriptableObject.putProperty(scope, "thumbURL", Context.javaToJS(thumbURL, scope));
			ScriptableObject.putProperty(scope, "htmlCode", Context.javaToJS(htmlcode, scope));
			ScriptableObject.putProperty(scope, "firstContainerURL", Context.javaToJS(upo.getFirstContainerURL(), scope));
			// ScriptableObject.putProperty(scope, "containerURLs", Context.javaToJS(upo.getContainerURLs(), scope));

			// Provide output variables to javascript
			ScriptableObject.putProperty(scope, "directLink", Context.javaToJS(upo.getDirectLink(), scope));
			ScriptableObject.putProperty(scope, "correctedFilename", Context.javaToJS(upo.getCorrectedFilename(), scope));

			// Execute javascript code
			context.evaluateString(scope, javascriptContextProvidedFunctions + javascriptCode, this.toString(), 1, null);

			// Read out output properties from javascript
			String retval = (String)Context.jsToJava(ScriptableObject.getProperty(scope, "directLink"), String.class);
			String correctedFilename = (String)Context.jsToJava(ScriptableObject.getProperty(scope, "correctedFilename"), String.class);
			if (correctedFilename.length() > 0) {
				upo.setCorrectedFilename(correctedFilename);
			}
			Iterator<Entry<String, Object>> it = infoMap.getMapOut().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				upo.addInfo(entry.getKey(), entry.getValue());
			}
			if (retval == null || retval.isEmpty()) {
				throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
			}
			return retval;
		} catch (Exception e) {
			logger.error("Could not execute javascript pipeline", e);
			throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
		} finally {
			Context.exit();
		}
	}

	@Override
	public int getWaitBeforeExecute() {
		return waitBeforeExecute;
	}

	@Override
	public void setWaitBeforeExecute(int waitBeforeExecute) {
		this.waitBeforeExecute = waitBeforeExecute;
	}

	@Override
	public boolean isUrlDecodeResult() {
		return urlDecodeResult;
	}

	@Override
	public void setUrlDecodeResult(boolean urlDecodeResult) {
		this.urlDecodeResult = urlDecodeResult;
	}

	@Override
	public boolean isSendCookies() {
		return sendCookies;
	}

	@Override
	public void setSendCookies(boolean sendCookies) {
		this.sendCookies = sendCookies;
	}

	@Override
	public String getJavascript() {
		return javascriptCode;
	}

	@Override
	public void setJavascript(String javascriptCode) {
		this.javascriptCode = javascriptCode;
	}

	private static class InfoMap extends ScriptableObject {
		private static final long serialVersionUID = 1L;
		private Map<String, Object> mapIn = null;
		private Map<String, Object> mapOut = new HashMap<>();

		public InfoMap(Map<String, Object> mapIn) {
			this.mapIn = mapIn;
		}

		@SuppressWarnings("unused")
		public void setInfo(String key, Object value) {
			mapIn.put(key, value);
			mapOut.put(key, value);
		}

		@SuppressWarnings("unused")
		public Object getInfo(String key) {
			return mapIn.get(key);
		}

		/**
		 * Returns the mapOut
		 * 
		 * @return mapOut
		 */
		public Map<String, Object> getMapOut() {
			return mapOut;
		}

		@Override
		public String getClassName() {
			return getClass().getName();
		}
	}

	private static class MyFunctionObject extends FunctionObject {
		private static final long serialVersionUID = 1L;

		private MyFunctionObject(String name, Member methodOrConstructor, Scriptable parentScope) {
			super(name, methodOrConstructor, parentScope);
		}

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return super.call(cx, scope, getParentScope(), args);
		}
	}
}

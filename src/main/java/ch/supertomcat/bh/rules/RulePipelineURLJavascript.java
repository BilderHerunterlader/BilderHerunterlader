package ch.supertomcat.bh.rules;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostImageUrlNotFoundException;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.hostimpl.HostRules;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.xml.URLJavascriptPipeline;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * RulePipeline
 */
public class RulePipelineURLJavascript extends RuleURLPipeline<URLJavascriptPipeline> {
	/**
	 * Make Package for logging accessable in javascript
	 */
	private static final String JAVASCRIPT_PROVIDED_FUNCTIONS;

	static {
		StringBuilder sb = new StringBuilder();
		sb.append("var jsContext = JavaImporter(Packages.ch.supertomcat.bh.rules);\n\n");
		/*
		 * Define javascript functions for logging
		 * The functions will call Java Functions which will log the errors
		 */
		sb.append("function logDebug (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logDebug(message.toString(), '$SOURCE'); } }\n\n");
		sb.append("function logWarn (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logWarn(message.toString(), '$SOURCE'); } }\n\n");
		sb.append("function logError (message) { with(jsContext) { if (typeof message === 'undefined') { return; } JavascriptMethodProvider.logError(message.toString(), '$SOURCE'); } }\n\n");

		JAVASCRIPT_PROVIDED_FUNCTIONS = sb.toString();
	}

	/**
	 * Constructor
	 */
	public RulePipelineURLJavascript() {
		super(new URLJavascriptPipeline());
		definition.setWaitBeforeExecute(0);
		definition.setUrlDecodeResult(false);
		definition.setSendCookies(true);
		definition.setJavascriptCode("");
	}

	/**
	 * Constructor
	 * 
	 * @param definition Definition
	 */
	public RulePipelineURLJavascript(URLJavascriptPipeline definition) {
		super(definition);
	}

	@Override
	public String downloadContainerPage(RuleContext ruleContext, int step) throws HostException {
		return null;
	}

	@SuppressWarnings("resource")
	@Override
	public String getURL(RuleContext ruleContext) throws HostImageUrlNotFoundException {
		String url = ruleContext.getPipelineURL();
		String thumbURL = ruleContext.getPipelineThumbURL();
		String htmlcode = ruleContext.getHtmlCodeLast().getHtmlCode();
		URLParseObject upo = ruleContext.getUpo();

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
			String javascriptContextProvidedFunctions = JAVASCRIPT_PROVIDED_FUNCTIONS.replace("$SOURCE", "{" + this.getClass().getName() + ";Thread:" + Thread.currentThread().getId() + "}");

			// Initialize standard objects
			ScriptableObject scope = context.initStandardObjects();

			InfoMap infoMap = new InfoMap(upo.getInfos());
			infoMap.setParentScope(scope);

			Method methodGetInfo = infoMap.getClass().getMethod("getInfo", String.class);
			FunctionObject functionObjGetInfo = new MyFunctionObject("getInfo", methodGetInfo, infoMap);

			Method methodSetInfo = infoMap.getClass().getMethod("setInfo", String.class, Object.class);
			FunctionObject functionObjSetInfo = new MyFunctionObject("setInfo", methodSetInfo, infoMap);

			RuleContextScriptWrapper ruleContextScriptWrapper = new RuleContextScriptWrapper(ruleContext);
			ruleContextScriptWrapper.setParentScope(scope);
			Method methodDownloadContainerPage = RuleContextScriptWrapper.class.getMethod("downloadContainerPage", String.class, String.class);
			FunctionObject functionObjDownloadContainerPage = new MyFunctionObject("downloadContainerPage", methodDownloadContainerPage, ruleContextScriptWrapper);

			// Provide functions to javascript
			scope.put("downloadContainerPage", scope, functionObjDownloadContainerPage);
			scope.put("getInfo", scope, functionObjGetInfo);
			scope.put("setInfo", scope, functionObjSetInfo);

			// Provide input variables to javascript
			ScriptableObject.putProperty(scope, "containerURL", Context.javaToJS(url, scope));
			ScriptableObject.putProperty(scope, "thumbURL", Context.javaToJS(thumbURL, scope));
			ScriptableObject.putProperty(scope, "htmlCode", Context.javaToJS(htmlcode, scope));
			ScriptableObject.putProperty(scope, "firstContainerURL", Context.javaToJS(upo.getFirstContainerURL(), scope));

			// Provide output variables to javascript
			ScriptableObject.putProperty(scope, "directLink", Context.javaToJS(upo.getDirectLink(), scope));
			ScriptableObject.putProperty(scope, "correctedFilename", Context.javaToJS(upo.getCorrectedFilename(), scope));

			// Execute javascript code
			context.evaluateString(scope, javascriptContextProvidedFunctions + definition.getJavascriptCode(), this.toString(), 1, null);

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
			ruleContext.setPipelineResult(retval);
			return retval;
		} catch (Exception e) {
			logger.error("Could not execute javascript pipeline", e);
			throw new HostImageUrlNotFoundException(HostRules.NAME + ": " + Localization.getString("ErrorImageURL"));
		} finally {
			Context.exit();
		}
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

	/**
	 * Rule Context Script Wrapper
	 */
	private static class RuleContextScriptWrapper extends ScriptableObject {
		private static final long serialVersionUID = 1L;

		/**
		 * Rule Context
		 */
		private final RuleContext ruleContext;

		/**
		 * Constructor
		 * 
		 * @param ruleContext Rule Context
		 */
		public RuleContextScriptWrapper(RuleContext ruleContext) {
			this.ruleContext = ruleContext;
		}

		/**
		 * Method invoked from javascript to download container page
		 * 
		 * @param url URL
		 * @param referrer Referrer
		 * @return Container Page
		 * @throws HostException
		 */
		@SuppressWarnings("unused")
		public String downloadContainerPage(String url, String referrer) throws HostException {
			DownloadContainerPageOptions downloadContainerPageOptions = ruleContext.createDefaultDownloadContainerPageOptions(false);
			return ruleContext.downloadContainerPage(url, referrer, downloadContainerPageOptions);
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

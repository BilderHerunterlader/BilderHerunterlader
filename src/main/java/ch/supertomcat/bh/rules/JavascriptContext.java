package ch.supertomcat.bh.rules;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * Class containing classes for setting up the Rhino javascript context
 */
public class JavascriptContext {

	static {
		ContextFactory.initGlobal(new JavascriptContext.SandboxContextFactory());
	}

	/**
	 * 
	 */
	public static class SandboxContextFactory extends ContextFactory {
		@Override
		protected Context makeContext() {
			Context cx = super.makeContext();
			SandboxWrapFactory sandboxWrapFactory = new SandboxWrapFactory();
			sandboxWrapFactory.setJavaPrimitiveWrap(false);
			cx.setWrapFactory(sandboxWrapFactory);
			return cx;
		}
	}

	/**
	 * 
	 */
	public static class SandboxWrapFactory extends WrapFactory {
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
			return new SandboxNativeJavaObject(scope, javaObject, staticType);
		}
	}

	/**
	 * 
	 */
	public static class SandboxNativeJavaObject extends NativeJavaObject {
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param scope Scope
		 * @param javaObject Java Object
		 * @param staticType Static Type
		 */
		public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType) {
			super(scope, javaObject, staticType);
		}

		@Override
		public Object get(String name, Scriptable start) {
			if (name.equals("getClass")) {
				return NOT_FOUND;
			}
			return super.get(name, start);
		}
	}
}

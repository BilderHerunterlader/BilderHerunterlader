package ch.supertomcat.bh.settings.oldsettingsconverter;

import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

/**
 * Class which provides methods needed by SettingsManager
 */
public final class SettingsToolkit {
	private static final Set<String> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add("boolean");
		SUPPORTED_TYPES.add("int");
		SUPPORTED_TYPES.add("long");
		SUPPORTED_TYPES.add("string");
		SUPPORTED_TYPES.add("byte");
		SUPPORTED_TYPES.add("short");
		SUPPORTED_TYPES.add("float");
		SUPPORTED_TYPES.add("double");
	}

	/**
	 * Constructor
	 */
	private SettingsToolkit() {
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static int parseIntValue(String value, int defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static long parseLongValue(String value, long defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static boolean parseBooleanValue(String value, boolean defVal) {
		if (value == null) {
			return defVal;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static short parseShortValue(String value, short defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Short.parseShort(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static byte parseByteValue(String value, byte defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Byte.parseByte(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static float parseFloatValue(String value, float defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static double parseDoubleValue(String value, double defVal) {
		if (value == null) {
			return defVal;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	private static String[] getPathArray(String path) {
		if (path.contains(".")) {
			return path.split("\\.");
		} else {
			return new String[] { path };
		}
	}

	/**
	 * Returns the first element found with the given path or null if not found
	 * 
	 * @param path Path
	 * @param root Root
	 * @return first element found with the given path or null if not found
	 */
	protected static Element getElementByPath(String path, Element root) {
		if (path.isEmpty()) {
			return null;
		}
		String[] pathArr = getPathArray(path);

		Element e = root;
		for (int i = 0; i < pathArr.length; i++) {
			Element ex = e.getChild(pathArr[i]);
			if (ex == null) {
				return null;
			}
			e = ex;
		}
		return e;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @return Wert
	 */
	protected static String readValue(String path, Element root) {
		Element e = getElementByPath(path, root);
		if (e != null) {
			return e.getValue();
		}
		return null;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static int readIntValue(String path, Element root, int defVal) {
		String result = readValue(path, root);
		return parseIntValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static long readLongValue(String path, Element root, long defVal) {
		String result = readValue(path, root);
		return parseLongValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static String readStringValue(String path, Element root, String defVal) {
		String retval = readValue(path, root);
		if (retval == null) {
			return defVal;
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static boolean readBooleanValue(String path, Element root, boolean defVal) {
		String result = readValue(path, root);
		return parseBooleanValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static float readFloatValue(String path, Element root, float defVal) {
		String result = readValue(path, root);
		return parseFloatValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static double readDoubleValue(String path, Element root, double defVal) {
		String result = readValue(path, root);
		return parseDoubleValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static short readShortValue(String path, Element root, short defVal) {
		String result = readValue(path, root);
		return parseShortValue(result, defVal);
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static byte readByteValue(String path, Element root, byte defVal) {
		String result = readValue(path, root);
		return parseByteValue(result, defVal);
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 * @param datatype Data Type
	 */
	protected static void addElement(String path, String val, Element root, String datatype) {
		if (path.isEmpty() || !SUPPORTED_TYPES.contains(datatype)) {
			return;
		}

		String[] pathArr = getPathArray(path);

		Element e = root;
		for (int i = 0; i < pathArr.length; i++) {
			Element child = e.getChild(pathArr[i]);
			if (child == null) {
				child = new Element(pathArr[i]);
				e.addContent(child);
			}
			if (i == (pathArr.length - 1)) {
				child.setText(val);
				child.setAttribute("datatype", datatype);
			}
			e = child;
		}
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addSubElement(String path, Element val, Element root) {
		if (path.isEmpty()) {
			return;
		}

		String[] pathArr = getPathArray(path);

		Element e = root;
		for (int i = 0; i < pathArr.length; i++) {
			Element child = e.getChild(pathArr[i]);
			if (child == null) {
				child = new Element(pathArr[i]);
				e.addContent(child);
			}
			if (i == (pathArr.length - 1)) {
				child.addContent(val);
			}
			e = child;
		}
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addIntValue(String path, int val, Element root) {
		addElement(path, String.valueOf(val), root, "int");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addLongValue(String path, long val, Element root) {
		addElement(path, String.valueOf(val), root, "long");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addByteValue(String path, byte val, Element root) {
		addElement(path, String.valueOf(val), root, "byte");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addShortValue(String path, short val, Element root) {
		addElement(path, String.valueOf(val), root, "short");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addFloatValue(String path, float val, Element root) {
		addElement(path, String.valueOf(val), root, "float");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addDoubleValue(String path, double val, Element root) {
		addElement(path, String.valueOf(val), root, "double");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addStringValue(String path, String val, Element root) {
		addElement(path, val, root, "string");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addBooleanValue(String path, boolean val, Element root) {
		addElement(path, String.valueOf(val), root, "boolean");
	}
}

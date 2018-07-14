package ch.supertomcat.bh.settings;

import org.jdom2.Element;

/**
 * Class which provides methods needed by SettingsManager
 */
public class SettingsToolkit {
	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static int parseIntValue(String value, int defVal) {
		if (value == null)
			return defVal;
		int retval = defVal;
		try {
			retval = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static long parseLongValue(String value, long defVal) {
		if (value == null)
			return defVal;
		long retval = defVal;
		try {
			retval = Long.parseLong(value);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static boolean parseBooleanValue(String value, boolean defVal) {
		if (value == null)
			return defVal;
		boolean retval = defVal;
		retval = Boolean.parseBoolean(value);
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static short parseShortValue(String value, short defVal) {
		if (value == null)
			return defVal;
		short retval = defVal;
		try {
			retval = Short.parseShort(value);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static byte parseByteValue(String value, byte defVal) {
		if (value == null)
			return defVal;
		byte retval = defVal;
		try {
			retval = Byte.parseByte(value);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static float parseFloatValue(String value, float defVal) {
		if (value == null)
			return defVal;
		float retval = defVal;
		try {
			retval = Float.parseFloat(value);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param value Wert
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static double parseDoubleValue(String value, double defVal) {
		if (value == null)
			return defVal;
		double retval = defVal;
		try {
			retval = Double.parseDouble(value);
		} catch (NumberFormatException e) {
		}
		return retval;
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
		String pathArr[];
		if (path.contains(".")) {
			pathArr = path.split("\\.");
		} else {
			pathArr = new String[] { path };
		}
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
		if (result == null)
			return defVal;
		int retval = defVal;
		try {
			retval = Integer.parseInt(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static long readLongValue(String path, Element root, long defVal) {
		String result = readValue(path, root);
		if (result == null)
			return defVal;
		long retval = defVal;
		try {
			retval = Long.parseLong(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static String readStringValue(String path, Element root, String defVal) {
		String retval = readValue(path, root);
		if (retval == null)
			return defVal;
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
		if (result == null)
			return defVal;
		boolean retval = defVal;
		retval = Boolean.parseBoolean(result);
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static float readFloatValue(String path, Element root, float defVal) {
		String result = readValue(path, root);
		if (result == null)
			return defVal;
		float retval = defVal;
		try {
			retval = Float.parseFloat(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static double readDoubleValue(String path, Element root, double defVal) {
		String result = readValue(path, root);
		if (result == null)
			return defVal;
		double retval = defVal;
		try {
			retval = Double.parseDouble(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static short readShortValue(String path, Element root, short defVal) {
		String result = readValue(path, root);
		if (result == null)
			return defVal;
		short retval = defVal;
		try {
			retval = Short.parseShort(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Pfad
	 * @param root Root-Element
	 * @param defVal Standard-Wert
	 * @return Wert
	 */
	protected static byte readByteValue(String path, Element root, byte defVal) {
		String result = readValue(path, root);
		if (result == null)
			return defVal;
		byte retval = defVal;
		try {
			retval = Byte.parseByte(result);
		} catch (NumberFormatException e) {
		}
		return retval;
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 * @param datatype Data Type
	 */
	protected static void addElement(String path, String val, Element root, String datatype) {
		if (path.equals(""))
			return;
		if (datatype.equals("boolean") == false && datatype.equals("int") == false && datatype.equals("long") == false && datatype.equals("string") == false && datatype.equals("byte") == false
				&& datatype.equals("short") == false && datatype.equals("float") == false && datatype.equals("double") == false) {
			return;
		}
		String pathArr[];
		if (path.contains(".")) {
			pathArr = path.split("\\.");
		} else {
			pathArr = new String[1];
			pathArr[0] = path;
		}
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
		if (path.equals(""))
			return;

		String pathArr[];
		if (path.contains(".")) {
			pathArr = path.split("\\.");
		} else {
			pathArr = new String[1];
			pathArr[0] = path;
		}
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
		String s = String.valueOf(val);
		addElement(path, s, root, "int");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addLongValue(String path, long val, Element root) {
		String s = String.valueOf(val);
		addElement(path, s, root, "long");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addByteValue(String path, byte val, Element root) {
		String s = String.valueOf(val);
		addElement(path, s, root, "byte");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addShortValue(String path, short val, Element root) {
		String s = String.valueOf(val);
		addElement(path, s, root, "short");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addFloatValue(String path, float val, Element root) {
		String s = String.valueOf(val);
		addElement(path, s, root, "float");
	}

	/**
	 * @param path Path
	 * @param val Value
	 * @param root Root
	 */
	protected static void addDoubleValue(String path, double val, Element root) {
		String s = String.valueOf(val);
		addElement(path, s, root, "double");
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
		String s = String.valueOf(val);
		addElement(path, s, root, "boolean");
	}
}

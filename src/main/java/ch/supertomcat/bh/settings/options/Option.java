package ch.supertomcat.bh.settings.options;

/**
 * Class from which all Option-Classes are extended
 * 
 * @param <E> Type of the value
 */
public abstract class Option<E> {
	/**
	 * Type
	 */
	protected final Class<?> type;

	/**
	 * Type Name
	 */
	protected final String typeName;

	/**
	 * Path
	 */
	protected String path = "";

	/**
	 * Value
	 */
	protected E value;

	/**
	 * Constructor
	 * 
	 * @param type Generic Type
	 * @param typeName Type Name
	 */
	public Option(Class<?> type, String typeName) {
		this.type = type;
		this.typeName = typeName;
	}

	/**
	 * Constructor
	 * 
	 * @param type Generic Type
	 * @param typeName Type Name
	 * @param path Path
	 * @param value Value
	 */
	public Option(Class<?> type, String typeName, String path, E value) {
		this.type = type;
		this.typeName = typeName;
		this.path = path;
		this.value = value;
	}

	/**
	 * Returns the Path
	 * 
	 * @return Path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the Path
	 * 
	 * @param path Path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the Value
	 * 
	 * @return Value
	 */
	public E getValue() {
		return value;
	}

	/**
	 * Sets the Value
	 * 
	 * @param value Value
	 */
	public void setValue(E value) {
		this.value = value;
	}

	/**
	 * Class of the value
	 * 
	 * @return Class
	 */
	public Class<?> getValueType() {
		return type;
	}

	/**
	 * @return Name of the value type
	 */
	public String getValueTypeName() {
		return typeName;
	}

	/**
	 * Returns the value as String using {@link String#valueOf(Object)}
	 * 
	 * @return value as String
	 */
	public String getValueAsString() {
		return String.valueOf(value);
	}

	/**
	 * Returns a new Option for the class
	 * 
	 * @param cl Class
	 * @param path Path
	 * @return Option
	 */
	@SuppressWarnings("unchecked")
	public static <E> Option<E> getNewOptionByClass(Class<E> cl, String path) {
		if (cl == Boolean.class) {
			return (Option<E>)new OptionBoolean(path);
		} else if (cl == Byte.class) {
			return (Option<E>)new OptionByte(path);
		} else if (cl == Double.class) {
			return (Option<E>)new OptionDouble(path);
		} else if (cl == Float.class) {
			return (Option<E>)new OptionFloat(path);
		} else if (cl == Integer.class) {
			return (Option<E>)new OptionInt(path);
		} else if (cl == Long.class) {
			return (Option<E>)new OptionLong(path);
		} else if (cl == Short.class) {
			return (Option<E>)new OptionShort(path);
		} else if (cl == String.class) {
			return (Option<E>)new OptionString(path);
		}
		return null;
	}

	/**
	 * Checks if the type is ok
	 * 
	 * @param option Option
	 * @param valueClass Class of the value
	 * @return True if ok
	 */
	public static boolean checkType(Option<?> option, Class<?> valueClass) {
		return option.getValueType() == valueClass;
	}
}

package ch.supertomcat.bh.settings;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Look And Feel Setting
 */
public enum LookAndFeelSetting {
	/**
	 * Use java-default-theme
	 */
	LAF_DEFAULT("Default", 0, UIManager.getCrossPlatformLookAndFeelClassName()),

	/**
	 * Use operating-system-theme
	 */
	LAF_OS("OperatingSystem", 1, UIManager.getSystemLookAndFeelClassName()),

	/**
	 * Metal
	 */
	LAF_METAL("Metal", "Metal", 2),

	/**
	 * Windows
	 */
	LAF_WINDOWS("Windows", "Windows", 3),

	/**
	 * Windows Classic
	 */
	LAF_WINDOWS_CLASSIC("Windows Classic", "Windows Classic", 4),

	/**
	 * Motif
	 */
	LAF_MOTIF("Motif", "CDE/Motif", 5),

	/**
	 * GTK
	 */
	LAF_GTK("GTK", "GTK+", 6),

	/**
	 * MacOS
	 */
	LAF_MACOS("Mac OS", "Mac OS X", 7),

	/**
	 * Nimbus (since jre6 update 10)
	 */
	LAF_NIMBUS("Nimbus", "Nimbus", 8);

	/**
	 * Display Name
	 */
	private final String displayName;

	/**
	 * Java Name
	 */
	private final String javaName;

	/**
	 * Class Name
	 */
	private final String className;

	/**
	 * XML Value
	 */
	private final int xmlValue;

	/**
	 * Constructor
	 * 
	 * @param displayName Display Name
	 * @param xmlValue XML Value
	 * @param className Class Name
	 */
	private LookAndFeelSetting(String displayName, int xmlValue, String className) {
		this.displayName = displayName;
		this.xmlValue = xmlValue;
		this.className = className;
		String lafJavaName = null;
		for (LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
			if (lookAndFeel.getClassName().equals(className)) {
				lafJavaName = lookAndFeel.getName();
				break;
			}
		}
		this.javaName = lafJavaName;
	}

	/**
	 * Constructor
	 * 
	 * @param displayName Display Name
	 * @param javaName Java Name
	 * @param xmlValue XML Value
	 */
	private LookAndFeelSetting(String displayName, String javaName, int xmlValue) {
		this.displayName = displayName;
		this.javaName = javaName;
		this.xmlValue = xmlValue;
		String lafClassName = null;
		for (LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
			if (lookAndFeel.getName().equals(javaName)) {
				lafClassName = lookAndFeel.getClassName();
				break;
			}
		}
		this.className = lafClassName;
	}

	/**
	 * Returns the xmlValue
	 * 
	 * @return xmlValue
	 */
	public int getXmlValue() {
		return xmlValue;
	}

	/**
	 * Returns the displayName
	 * 
	 * @return displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the javaName
	 * 
	 * @return javaName
	 */
	public String getJavaName() {
		return javaName;
	}

	/**
	 * Returns the className
	 * 
	 * @return className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return True if Look And Feel is available, false otherwise
	 */
	public boolean isAvailable() {
		return className != null;
	}

	/**
	 * Get LookAndFeelSetting by XML Value
	 * 
	 * @param xmlValue XML Value
	 * @return LookAndFeelSetting
	 */
	public static LookAndFeelSetting getByXMLValue(int xmlValue) {
		for (LookAndFeelSetting setting : values()) {
			if (setting.xmlValue == xmlValue) {
				return setting;
			}
		}
		return LAF_OS;
	}
}

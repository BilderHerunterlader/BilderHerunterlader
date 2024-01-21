package ch.supertomcat.bh.settings;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ch.supertomcat.bh.settings.xml.LookAndFeelSetting;

/**
 * Look And Feel Setting
 */
public enum MappedLookAndFeelSetting {
	/**
	 * Use java-default-theme
	 */
	LAF_DEFAULT("Default", LookAndFeelSetting.LAF_DEFAULT, UIManager.getCrossPlatformLookAndFeelClassName()),

	/**
	 * Use operating-system-theme
	 */
	LAF_OS("OperatingSystem", LookAndFeelSetting.LAF_OS, UIManager.getSystemLookAndFeelClassName()),

	/**
	 * Metal
	 */
	LAF_METAL("Metal", "Metal", LookAndFeelSetting.LAF_METAL),

	/**
	 * Windows
	 */
	LAF_WINDOWS("Windows", "Windows", LookAndFeelSetting.LAF_WINDOWS),

	/**
	 * Windows Classic
	 */
	LAF_WINDOWS_CLASSIC("Windows Classic", "Windows Classic", LookAndFeelSetting.LAF_WINDOWS_CLASSIC),

	/**
	 * Motif
	 */
	LAF_MOTIF("Motif", "CDE/Motif", LookAndFeelSetting.LAF_MOTIF),

	/**
	 * GTK
	 */
	LAF_GTK("GTK", "GTK+", LookAndFeelSetting.LAF_GTK),

	/**
	 * MacOS
	 */
	LAF_MACOS("Mac OS", "Mac OS X", LookAndFeelSetting.LAF_MACOS),

	/**
	 * Nimbus (since jre6 update 10)
	 */
	LAF_NIMBUS("Nimbus", "Nimbus", LookAndFeelSetting.LAF_NIMBUS);

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
	private final LookAndFeelSetting xmlValue;

	/**
	 * Constructor
	 * 
	 * @param displayName Display Name
	 * @param xmlValue XML Value
	 * @param className Class Name
	 */
	private MappedLookAndFeelSetting(String displayName, LookAndFeelSetting xmlValue, String className) {
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
	private MappedLookAndFeelSetting(String displayName, String javaName, LookAndFeelSetting xmlValue) {
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
	 * @return XML Value
	 */
	public LookAndFeelSetting getXMLValue() {
		return xmlValue;
	}

	/**
	 * Get LookAndFeelSetting by XML Value
	 * 
	 * @param xmlValue XML Value
	 * @return LookAndFeelSetting
	 */
	public static MappedLookAndFeelSetting getByXMLValue(LookAndFeelSetting xmlValue) {
		switch (xmlValue) {
			case LAF_DEFAULT:
				return MappedLookAndFeelSetting.LAF_DEFAULT;
			case LAF_OS:
				return MappedLookAndFeelSetting.LAF_OS;
			case LAF_METAL:
				return MappedLookAndFeelSetting.LAF_METAL;
			case LAF_WINDOWS:
				return MappedLookAndFeelSetting.LAF_WINDOWS;
			case LAF_WINDOWS_CLASSIC:
				return MappedLookAndFeelSetting.LAF_WINDOWS_CLASSIC;
			case LAF_MOTIF:
				return MappedLookAndFeelSetting.LAF_MOTIF;
			case LAF_GTK:
				return MappedLookAndFeelSetting.LAF_GTK;
			case LAF_MACOS:
				return MappedLookAndFeelSetting.LAF_MACOS;
			case LAF_NIMBUS:
				return MappedLookAndFeelSetting.LAF_NIMBUS;
		}
		return LAF_OS;
	}
}

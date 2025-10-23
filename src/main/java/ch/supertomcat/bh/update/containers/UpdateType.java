package ch.supertomcat.bh.update.containers;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Update Type enum
 */
public enum UpdateType {
	/**
	 * TYPE_BH
	 */
	TYPE_BH(Localization.getString("UpdateTypeBH")),

	/**
	 * TYPE_RULE
	 */
	TYPE_RULE(Localization.getString("UpdateTypeRule")),

	/**
	 * TYPE_HOST_PLUGIN
	 */
	TYPE_HOST_PLUGIN(Localization.getString("UpdateTypeHostPlugin")),

	/**
	 * TYPE_REDIRECT_PLUGIN
	 */
	TYPE_REDIRECT_PLUGIN(Localization.getString("UpdateTypeRedirectPlugin"));

	/**
	 * Name
	 */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name Name
	 */
	private UpdateType(String name) {
		this.name = name;
	}

	/**
	 * Returns the name
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}
}

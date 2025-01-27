package ch.supertomcat.bh.gui;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * Class which contains a list of icon resources
 */
public final class Icons {
	private static final String BH_ICON_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%s";
	private static final String BH_ICON_SIZE_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%dx%d/%s";

	private Icons() {
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoIcon(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getTangoIcon(resource, size);
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoMultiResIcon(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getTangoMultiResIcon(resource, size);
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static Image getTangoImage(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getTangoImage(resource, size);
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getBHIcon(String resource) {
		return getIcon(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getBHIcon(String resource, int size) {
		return getIcon(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static Image getBHImage(String resource) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getImage(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static Image getBHImage(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getImage(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getIcon(String resource) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getIcon(resource);
	}

	/**
	 * Returns a Mutli Resolution ImageIcon for the given resources
	 * 
	 * @param resources Resources
	 * @return ImageIcon
	 */
	public static ImageIcon getMultiResIcon(String... resources) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getMultiResIcon(resources);
	}
}

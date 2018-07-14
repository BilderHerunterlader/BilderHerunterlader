package ch.supertomcat.bh.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which contains a list of icon resources
 */
public final class Icons {
	private static final String BH_ICON_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%s";
	private static final String BH_ICON_SIZE_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%dx%d/%s";
	private static final String TANGO_ICON_RESOURCE_FORMAT = "/org/freedesktop/tango/%dx%d/%s";

	/**
	 * Resources
	 */
	private static Map<String, Image> iconCache = new HashMap<>();

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Icons.class);

	/**
	 * Dummy Image
	 */
	private static Image dummy = getBHImage("Dummy.png", 16);

	private Icons() {
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static synchronized ImageIcon getTangoIcon(String resource, int size) {
		return getIcon(String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static synchronized Image getTangoImage(String resource, int size) {
		return getImage(String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static synchronized ImageIcon getBHIcon(String resource) {
		return getIcon(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static synchronized ImageIcon getBHIcon(String resource, int size) {
		return getIcon(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static synchronized Image getBHImage(String resource) {
		return getImage(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static synchronized Image getBHImage(String resource, int size) {
		return getImage(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource or null
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static synchronized ImageIcon getIcon(String resource) {
		return new ImageIcon(getImage(resource));
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static synchronized Image getImage(String resource) {
		Image image = iconCache.get(resource);
		if (image == null) {
			image = loadImage(resource);
			if (image != null) {
				iconCache.put(resource, image);
			} else {
				logger.error("Could not load image: {}", resource);
			}
		}
		return image;
	}

	private static Image loadImage(String resource) {
		try {
			URL resourceURL = Icons.class.getResource(resource);
			if (resourceURL != null) {
				return Toolkit.getDefaultToolkit().getImage(resourceURL);
			} else {
				logger.error("Could not load image: {}", resource);
			}
		} catch (Exception e) {
			logger.error("Could not load image: {}", resource);
		}
		return dummy;
	}
}

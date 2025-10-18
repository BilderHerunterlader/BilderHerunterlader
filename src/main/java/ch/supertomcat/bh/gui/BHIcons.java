package ch.supertomcat.bh.gui;

import java.awt.Image;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;

/**
 * Class which contains a list of icon resources
 */
public final class BHIcons {
	private static final String BH_ICON_RESOURCE_FORMAT = "/" + BHIcons.class.getPackage().getName().replace(".", "/") + "/icons/%s";
	private static final String BH_ICON_SIZE_RESOURCE_FORMAT = "/" + BHIcons.class.getPackage().getName().replace(".", "/") + "/icons/%dx%d/%s";
	private static final String BH_ICON_SVG_RESOURCE_FORMAT = "/" + BHIcons.class.getPackage().getName().replace(".", "/") + "/icons/svg/%s";

	private static final int[] BH_ICON_SIZES = { 16, 22, 32, 64, 128, 256 };

	private BHIcons() {
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getBHIcon(String resource) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getIcon(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getBHIcon(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getIcon(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getBHSVGIcon(String resource) {
		return getBHSVGIcon(resource, -1);
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getBHSVGIcon(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getSVGIcon(String.format(BH_ICON_SVG_RESOURCE_FORMAT, resource), size);
	}

	/**
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getBHMultiResIcon(String resource) {
		return new ImageIcon(getBHMultiResImage(resource));
	}

	/**
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getBHMultiResIcon(String resource, int size) {
		return new ImageIcon(getBHMultiResImage(resource, size));
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static Image getBHMultiResImage(String resource) {
		return getBHMultiResImage(resource, 16);
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static Image getBHMultiResImage(String resource, int size) {
		int resourceCount = BH_ICON_SIZES.length;
		if (IntStream.of(BH_ICON_SIZES).noneMatch(x -> x == size)) {
			resourceCount++;
		}

		String[] resources = new String[resourceCount];
		resources[0] = String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource);
		String[] tempArr = IntStream.of(BH_ICON_SIZES).filter(x -> x != size).mapToObj(x -> String.format(BH_ICON_SIZE_RESOURCE_FORMAT, x, x, resource)).toArray(String[]::new);
		System.arraycopy(tempArr, 0, resources, 1, tempArr.length);

		return ch.supertomcat.supertomcatutils.gui.Icons.getMultiResImage(resources);
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static Image getBHImage(String resource) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getImage(String.format(BH_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static Image getBHImage(String resource, int size) {
		return ch.supertomcat.supertomcatutils.gui.Icons.getImage(String.format(BH_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}
}

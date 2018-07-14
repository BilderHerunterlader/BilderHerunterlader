package ch.supertomcat.bh.settings.options;

import ch.supertomcat.bh.settings.SettingsManager;

/**
 * The user can define subdirectory were the files are
 * moved to after download is completed. The files are
 * moved, when the filesize are in the defined range.
 */
public class Subdir {
	/**
	 * Only the lower component of the resolution is checked
	 */
	public static final int RESOLUTION_ONLY_LOWER = 0;
	
	/**
	 * Only the higher component of the resolution is checked
	 */
	public static final int RESOLUTION_ONLY_HIGHER = 1;
	
	/**
	 * Only width is checked
	 */
	public static final int RESOLUTION_ONLY_WIDTH = 2;
	
	/**
	 * Only height is checked
	 */
	public static final int RESOLUTION_ONLY_HEIGHT = 3;
	
	/**
	 * Both
	 */
	public static final int RESOLUTION_BOTH = 4;
	
	/**
	 * WIDTH_IS_HIGHER
	 */
	private static final int WIDTH_IS_HIGHER = 0;
	
	/**
	 * HEIGHT_IS_HIGHER
	 */
	private static final int HEIGHT_IS_HIGHER = 1;
	
	/**
	 * WIDTH_IS_LOWER
	 */
	private static final int WIDTH_IS_LOWER = 0;
	
	/**
	 * HEIGHT_IS_LOWER
	 */
	private static final int HEIGHT_IS_LOWER = 1;
	
	/**
	 * Name of the subdirectory
	 */
	private String subdirName = "";
	
	/**
	 * Min Filesize
	 */
	private long minSize = 0;
	
	/**
	 * Max Filesize
	 */
	private long maxSize = 0;
	
	/**
	 * Min Width
	 */
	private int minWidth = 0;
	
	/**
	 * Max Width
	 */
	private int maxWidth = 0;
	
	/**
	 * Min Height
	 */
	private int minHeight = 0;
	
	/**
	 * Max Height
	 */
	private int maxHeight = 0;
	
	/**
	 * Constructor
	 * @param subdirName Name of the subdirectory
	 * @param minSize Min Filesize
	 * @param maxSize Max Filesize
	 */
	public Subdir(String subdirName, long minSize, long maxSize) {
		this.subdirName = subdirName;
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
	
	
	/**
	 * Constructor
	 * @param subdirName Name of the subdirectory
	 * @param minSize Min Filesize
	 * @param maxSize Max Filesize
	 * @param minWidth Min Width
	 * @param maxWidth Max Width
	 * @param minHeight Min Height
	 * @param maxHeight Max Height
	 */
	public Subdir(String subdirName, long minSize, long maxSize, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		this.subdirName = subdirName;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
	}

	/**
	 * Returns the subdirName
	 * @return subdirName
	 */
	public String getSubdirName() {
		return subdirName;
	}

	/**
	 * Sets the subdirName
	 * @param subdirName subdirName
	 */
	public void setSubdirName(String subdirName) {
		this.subdirName = subdirName;
	}

	/**
	 * Returns the minSize
	 * @return minSize
	 */
	public long getMinSize() {
		return minSize;
	}

	/**
	 * Sets the minSize
	 * @param minSize minSize
	 */
	public void setMinSize(long minSize) {
		if (minSize < 0) minSize = 0;
		this.minSize = minSize;
	}

	/**
	 * Returns the maxSize
	 * @return maxSize
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * Sets the maxSize
	 * @param maxSize maxSize
	 */
	public void setMaxSize(long maxSize) {
		if (maxSize < 0) maxSize = 0;
		this.maxSize = maxSize;
	}


	/**
	 * Returns the minWidth
	 * @return minWidth
	 */
	public int getMinWidth() {
		return minWidth;
	}


	/**
	 * Sets the minWidth
	 * @param minWidth minWidth
	 */
	public void setMinWidth(int minWidth) {
		if (minWidth < 0) minWidth = 0;
		this.minWidth = minWidth;
	}


	/**
	 * Returns the maxWidth
	 * @return maxWidth
	 */
	public int getMaxWidth() {
		return maxWidth;
	}


	/**
	 * Sets the maxWidth
	 * @param maxWidth maxWidth
	 */
	public void setMaxWidth(int maxWidth) {
		if (maxWidth < 0) maxWidth = 0;
		this.maxWidth = maxWidth;
	}


	/**
	 * Returns the minHeight
	 * @return minHeight
	 */
	public int getMinHeight() {
		return minHeight;
	}


	/**
	 * Sets the minHeight
	 * @param minHeight minHeight
	 */
	public void setMinHeight(int minHeight) {
		if (minHeight < 0) minHeight = 0;
		this.minHeight = minHeight;
	}


	/**
	 * Returns the maxHeight
	 * @return maxHeight
	 */
	public int getMaxHeight() {
		return maxHeight;
	}


	/**
	 * Sets the maxHeight
	 * @param maxHeight maxHeight
	 */
	public void setMaxHeight(int maxHeight) {
		if (maxHeight < 0) maxHeight = 0;
		this.maxHeight = maxHeight;
	}
	
	/**
	 * Checks if Filesize is in range
	 * @param filesize Filesize
	 * @return True if Filesize is in range
	 */
	public boolean isInFilesizeRange(long filesize) {
		return (filesize >= minSize) && (filesize <= maxSize || maxSize == 0);
	}
	
	/**
	 * Checks if Width is in range
	 * @param width Width
	 * @return True if Width is in range
	 */
	public boolean isInWidthRange(int width) {
		return (width >= minWidth) && (width <= maxWidth || maxWidth == 0);
	}
	
	/**
	 * Checks if Height is in range
	 * @param height Height
	 * @return True if Height is in range
	 */
	public boolean isInHeightRange(int height) {
		return (height >= minHeight) && (height <= maxHeight || maxHeight == 0);
	}
	
	/**
	 * @param filesize File Size
	 * @param width Width
	 * @param height Height
	 * @param isImage True if is image, false otherwise
	 * @return TRUE if the file matches all required ranges
	 */
	public boolean isInRange(long filesize, int width, int height, boolean isImage) {
		/*
		 * if min and max are both 0, we don't check that values
		 * We setup here some boolean values, for easer use later
		 */
		boolean sizeCheck = !(minSize == 0 && maxSize == 0);
		boolean widthCheck = !(minWidth == 0 && maxWidth == 0);
		boolean heightCheck = !(minHeight == 0 && maxHeight == 0);
		
		/*
		 * And now we setup boolean values, checking if filesize, 
		 * width and height match the ranges defined in this subdir.
		 * We do that for easier use later.
		 */
		boolean inSizeRange = isInFilesizeRange(filesize);
		boolean inWidthRange = isInWidthRange(width);
		boolean inHeightRange = isInHeightRange(height);
		
		if (!isImage) {
			/*
			 * if the file is not an image, resolution doesn't matter, 
			 * and if we also don't have to check the size,
			 * the file is definitly not moved to this subdir!
			 */
			if (!sizeCheck) return false;
			
			/*
			 * Now here we can simply return that boolean value
			 */
			return inSizeRange;
		} else {
			/*
			 * First we prepare values to identify the higher and lower component of the resolution
			 */
			int higher = (width >= height) ? WIDTH_IS_HIGHER : HEIGHT_IS_HIGHER;
			int lower = (width <= height) ? WIDTH_IS_LOWER : HEIGHT_IS_LOWER;
			
			/*
			 * if the file is an image, resolution matters, so we have to look
			 * for the way how to check the resolution.
			 */
			switch(SettingsManager.instance().getSubdirsResolutionMode()) {
				case RESOLUTION_ONLY_LOWER:
					switch(lower) {
						case WIDTH_IS_LOWER:
							if (!widthCheck) return false;
							return (!sizeCheck && inWidthRange) || (inSizeRange && inWidthRange);
						case HEIGHT_IS_LOWER:
							if (!heightCheck) return false;
							return (!sizeCheck && inHeightRange) || (inSizeRange && inHeightRange);
					}
					return false;
				case RESOLUTION_ONLY_HIGHER:
					switch(higher) {
						case WIDTH_IS_HIGHER:
							if (!widthCheck) return false;
							return (!sizeCheck && inWidthRange) || (inSizeRange && inWidthRange);
						case HEIGHT_IS_HIGHER:
							if (!heightCheck) return false;
							return (!sizeCheck && inHeightRange) || (inSizeRange && inHeightRange);
					}
					return false;
				case RESOLUTION_ONLY_WIDTH:
					if (!widthCheck) return false;
					return (!sizeCheck && inWidthRange) || (inSizeRange && inWidthRange);
				case RESOLUTION_ONLY_HEIGHT:
					if (!heightCheck) return false;
					return (!sizeCheck && inHeightRange) || (inSizeRange && inHeightRange);
				case RESOLUTION_BOTH:
					if (!widthCheck || !heightCheck) return false;
					return (!sizeCheck && inWidthRange && inHeightRange) || (inSizeRange && inWidthRange && inHeightRange);
				default:
					return false;
			}
		}
	}
}

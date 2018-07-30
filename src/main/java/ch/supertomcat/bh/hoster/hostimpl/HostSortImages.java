package ch.supertomcat.bh.hoster.hostimpl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcattools.applicationtool.ApplicationProperties;
import ch.supertomcat.supertomcattools.httptools.HTTPTool;

/**
 * Host-class for images that are on the harddisk and should be sorted.
 * Because there is a button on the GUI which allows the user to sort files
 * on the harddisk, i decided to move this class in the program itself.
 * So the check if this host-class exists is not needed anymore.
 * 
 * @version 0.9
 */
public class HostSortImages extends Host implements IHoster {
	/**
	 * Version
	 */
	public static final String VERSION = "0.9";

	/**
	 * Name
	 */
	public static final String NAME = "HostSortImages";

	/**
	 * Compiled pattern
	 */
	private final Pattern urlPattern;

	/**
	 * Compiled patterns
	 * If in a specific textfile are more patterns defined
	 * then they are stored in here
	 */
	private final List<Pattern> urlPatterns = new ArrayList<>();

	/**
	 * Constructor
	 */
	public HostSortImages() {
		super(NAME, VERSION, false);
		// This is the default pattern, allows just images to be sort
		urlPattern = Pattern.compile("^.*\\.(gif|jpg|jpeg|jpe|png|tif|tiff)$", Pattern.CASE_INSENSITIVE);

		File file = new File(ApplicationProperties.getProperty("ApplicationPath"), "hosts/HostxSortImages.txt");
		urlPatterns.addAll(BHUtil.readPatternsFromTextFile(file, Charset.defaultCharset(), true));
	}

	/**
	 * Get-Method
	 * 
	 * @param url URL
	 * @return URL
	 */
	public String getURL(String url) {
		if (isFromThisHoster(url)) {
			return url;
		}
		return "";
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (HTTPTool.isURL(url)) {
			return false;
		}

		for (Pattern pattern : urlPatterns) {
			if (pattern.matcher(url).matches()) {
				return true;
			}
		}
		if (urlPattern.matcher(url).matches()) {
			return true;
		}
		return false;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		if (url.lastIndexOf("/") > -1) {
			String retval = url.substring(url.lastIndexOf("/") + 1);
			return retval;
		}
		if (url.lastIndexOf("\\") > -1) {
			String retval = url.substring(url.lastIndexOf("\\") + 1);
			return retval;
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		upo.setDirectLink(getURL(upo.getContainerURL())); // URL
		upo.setCorrectedFilename(getFilenameFromURL(upo.getContainerURL())); // CorrectedFilename
	}
}

package ch.supertomcat.bh.hoster.hostimpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.tool.BHUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

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
		urlPattern = Pattern.compile("^.*\\.(gif|jpg|jpeg|jpe|png|tif|tiff|webp)$", Pattern.CASE_INSENSITIVE);

		Path file = Paths.get(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_PATH), "hosts/HostxSortImages.txt");
		urlPatterns.addAll(BHUtil.readPatternsFromTextFile(file, StandardCharsets.UTF_8, true));
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
		if (url.startsWith("http")) {
			String encodedURL = HTTPUtil.encodeURL(url, true);
			if (HTTPUtil.isURL(encodedURL)) {
				return false;
			}
		}

		for (Pattern pattern : urlPatterns) {
			if (pattern.matcher(url).matches()) {
				return true;
			}
		}
		return urlPattern.matcher(url).matches();
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		if (url.lastIndexOf("/") > -1) {
			return url.substring(url.lastIndexOf("/") + 1);
		}
		if (url.lastIndexOf("\\") > -1) {
			return url.substring(url.lastIndexOf("\\") + 1);
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		upo.setDirectLink(getURL(upo.getContainerURL())); // URL
		upo.setCorrectedFilename(getFilenameFromURL(upo.getContainerURL())); // CorrectedFilename
	}
}

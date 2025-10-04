import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.containerpage.ContainerPage;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Host class for ImageShack
 * 
 * @version 4.2
 */
public class HostImageShack extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "4.2";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostImageShack";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPatternDirectLinked;

	private RuleRegExp regexComplete;

	private RuleRegExp regexCompleteAlternate;

	/**
	 * Konstruktor
	 */
	public HostImageShack() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^https?://(?:img[0-9]+\\.)?(?:imageshack\\.(?:us|com)|exs\\.cx)/(?:my\\.php\\?(?:loc=img[0-9]+\\&)?image=|i/|photo/my-images/[0-9]+/).+?/?");
		urlPatternDirectLinked = Pattern.compile("^https?://img[0-9]+\\.(?:imageshack\\.(?:us|com)|exs\\.cx)/img[0-9]+/[0-9]+/.*");

		regexComplete = new RuleRegExp();
		regexComplete.setSearch("<img[^>]+?id=\"lp-image\"[^>]+?src=\"([^\"]+)\"");
		regexComplete.setReplace("$1");

		// some imageshack pages provide only a download anchor item
		regexCompleteAlternate = new RuleRegExp();
		regexCompleteAlternate.setSearch("<a href=\"(//imageshack.us/download/[0-9]+/.*?)\".*?data-label=\"Download Image\".*?</a>");
		regexCompleteAlternate.setReplace("http:$1");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		Matcher urlDirectLinkedMatcher = urlPatternDirectLinked.matcher(url);
		return urlMatcher.matches() || urlDirectLinkedMatcher.matches();
	}

	/**
	 * URL parsen
	 * 
	 * @param url Container-URL
	 * @return URL
	 * @throws HostException
	 */
	private String parseURL(String url) throws HostException {
		Matcher urlDirectLinkedMatcher = urlPatternDirectLinked.matcher(url);
		if (urlDirectLinkedMatcher.matches()) {
			return url;
		}

		ContainerPage result = downloadContainerPageEx(url, "");

		if (result.isRedirected()) {
			String redirectedURL = result.getRedirectedURL();
			String homepageURL;
			if (redirectedURL.startsWith("https://")) {
				homepageURL = "https://www.imageshack.com/";
			} else {
				homepageURL = "http://www.imageshack.com/";
			}
			if (homepageURL.equals(redirectedURL)) {
				throw new HostFileNotExistException(Localization.getString("FileNotExistsOnTheServer"));
			}
		}

		String page = result.getPage();

		String parsedURL = regexComplete.doPageSourcecodeReplace(page, 0, url, null);
		if (parsedURL.isEmpty()) {
			parsedURL = regexCompleteAlternate.doPageSourcecodeReplace(page, 0, url, null);
		} else {
			if (parsedURL.startsWith("/")) {
				parsedURL = "http://" + HTTPUtil.getDomainFromURL(url) + parsedURL;
			} else {
				parsedURL = "http:" + parsedURL;
			}

		}
		return parsedURL;
	}

	/**
	 * Dateiname korrigieren
	 * 
	 * @param url URL
	 * @return Korrigierter Dateiname
	 */
	private String correctFilename(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		String retval = "";
		String search = "image=";
		int start = url.lastIndexOf(search);
		if (start > -1) {
			start += search.length();
			retval = url.substring(start);
			String cf = correctFilename("/" + retval);
			if (cf.length() > 0) {
				retval = cf;
			}
		}
		return retval;
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			String s = parseURL(upo.getContainerURL());
			if (s.length() > 0) {
				upo.setDirectLink(s);
				upo.setCorrectedFilename(correctFilename(s));
			}
		}
	}
}

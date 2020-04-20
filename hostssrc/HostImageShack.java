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

/**
 * Host class for ImageShack
 * 
 * @version 3.9
 */
public class HostImageShack extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.9";

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
		urlPattern = Pattern.compile("^http://(img[0-9]+\\.)?(imageshack\\.us|exs\\.cx)/(my\\.php\\?(loc=img[0-9]+\\&)?image=|i/|photo/my-images/[0-9]+/).+?[/]?");
		urlPatternDirectLinked = Pattern.compile("^http://img[0-9]+\\.(imageshack\\.us|exs\\.cx)/img[0-9]+/[0-9]+/.*");

		regexComplete = new RuleRegExp();
		regexComplete.setSearch("<img[^>]+id=\"lp-image\"[^>]+src=\"([^\"]+)\"");
		regexComplete.setReplace("http:$1");

		// some imageshack pages provide only a download anchor item
		regexCompleteAlternate = new RuleRegExp();
		regexCompleteAlternate.setSearch("<a href=\"(//imageshack.us/download/[0-9]+/.*?)\".*?data-label=\"Download Image\".*?</a>");
		regexCompleteAlternate.setReplace("http:$1");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		Matcher urlDirectLinkedMatcher = urlPatternDirectLinked.matcher(url);
		if (urlMatcher.matches() || urlDirectLinkedMatcher.matches()) {
			return true;
		}
		return false;
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
				homepageURL = "https://www.imageshack.us/";
			} else {
				homepageURL = "http://www.imageshack.us/";
			}
			if (homepageURL.equals(redirectedURL)) {
				throw new HostFileNotExistException(Localization.getString("FileNotExistsOnTheServer"));
			}
		}

		String page = result.getPage();
		String parsedURL = regexComplete.doPageSourcecodeReplace(page, 0, url, null);
		if (parsedURL.isEmpty()) {
			parsedURL = regexCompleteAlternate.doPageSourcecodeReplace(page, 0, url, null);
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
		String filename = url.substring(url.lastIndexOf("/") + 1);
		int lp = filename.lastIndexOf(".");
		int fp = lp - 3;
		if ((lp < 0) || (fp < 1)) {
			return "";
		}
		String s1 = filename.substring(0, fp);
		String s2 = filename.substring(lp);
		String result = s1 + s2;
		return result;
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

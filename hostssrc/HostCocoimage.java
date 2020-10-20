import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;

/**
 * Host class for www.cocoimage.com
 * 
 * @version 1.8
 */
public class HostCocoimage extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "1.8";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostCocoimage";

	private RulePipelineURLRegex pipe1 = new RulePipelineURLRegex(URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE);

	private RulePipelineURLRegex pipe2 = new RulePipelineURLRegex(URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE);

	private RuleRegExp regex1 = new RuleRegExp();

	private RuleRegExp regex2 = new RuleRegExp();

	private RuleRegExp regex3 = new RuleRegExp();

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	/**
	 * Konstruktor
	 */
	public HostCocoimage() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^https?://img[0-9]+\\.cocoimage\\.com/img\\.php\\?id=.*");
		regex1.setSearch("window\\.location=\"(https?://img[0-9]+\\.cocoimage\\.com/img\\.php\\?id=.*?)\"");
		regex1.setReplace("$1");
		regex2.setSearch("img.*?id=\"img\".*?src=\"(.*?)\"");
		regex2.setReplace("$1");
		pipe1.addRegExp(regex1);
		pipe2.addRegExp(regex2);

		regex3.setSearch("^https?://img[0-9]+\\.cocoimage\\.com/showimg\\.php\\?id=([0-9]+).*");
		regex3.setReplace("$1.jpg");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
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
	private synchronized String parseURL(String url) throws HostException {
		String htmlCode = "";
		String result = "";
		String referrer = "";
		boolean downloadLoop = true;
		int counter = 0;

		do {
			htmlCode = downloadContainerPage(url, referrer);
			if (htmlCode.contains("window.location")) {
				referrer = url;
				url = pipe1.getURL(url, null, htmlCode, null);
			} else {
				result = pipe2.getURL(url, null, htmlCode, null);
				downloadLoop = false;
			}
			counter++;
		} while (downloadLoop && (counter < 50));

		return result;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		String result = parseURL(upo.getContainerURL()); // URL
		upo.setDirectLink(result);
		String filename = regex3.doURLReplace(result, upo.getPic());
		upo.setCorrectedFilename(filename);
	}
}

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.RuleRegExp;

/**
 * Host class for Picfoco
 * 
 * @version 1.3
 */
public class HostPicfoco extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "1.3";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostPicfoco";

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	private RuleRegExp regexForwarding;

	private RuleRegExp regexFilename;

	private RulePipeline pipeURL;

	/**
	 * Konstruktor
	 */
	public HostPicfoco() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^http://img[0-9]+\\.(picfoco)\\.com/img\\.php\\?id=([0-9]+)(&q=.*)?$");
		regexForwarding = new RuleRegExp();
		regexForwarding.setSearch("window\\.location=\"([^\"]+)\"");
		regexForwarding.setReplace("$1");

		pipeURL = new RulePipelineURLRegex(RuleMode.RULE_MODE_CONTAINER_PAGE_SOURCECODE);
		RuleRegExp regex1 = new RuleRegExp();
		regex1.setSearch("<img id=\"img\"");
		regex1.setReplace("");
		pipeURL.addRegExp(regex1);

		RuleRegExp regex2 = new RuleRegExp();
		regex2.setSearch("src=\"([^\"]+)\"");
		regex2.setReplace("$1");
		pipeURL.addRegExp(regex2);

		regexFilename = new RuleRegExp();
		regexFilename.setSearch("showimg\\.php\\?id=([0-9]+)&v=([0-9]+)&ext=([a-zA-Z0-9]+)(&dt=[0-9]+)?");
		regexFilename.setReplace("$1_$2.$3");
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
	 * @param repeats Repeats
	 * @return URL
	 * @throws HostException
	 */
	private String parseURL(String url, int repeats) throws HostException {
		if (repeats > 9) {
			throw new HostException("");
		}
		String htmlCode = downloadContainerPage(url, "");
		String urlToContinue = regexForwarding.doPageSourcecodeReplace(htmlCode, 0, url, null);
		if (urlToContinue.length() > 0) {
			repeats++;
			return parseURL(urlToContinue, repeats);
		}
		return pipeURL.getURL(url, "", htmlCode, null);
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		Matcher urlMatcher = urlPattern.matcher(url);
		return urlMatcher.replaceAll("$2");
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			String s = parseURL(upo.getContainerURL(), 0);
			if (s.length() > 0) {
				int pos = s.lastIndexOf("/");
				if (pos > -1) {
					String filename = regexFilename.doURLReplace(s.substring(pos + 1), null);
					upo.setCorrectedFilename(filename);
				}
				upo.setDirectLink(s);
			}
		}
	}
}

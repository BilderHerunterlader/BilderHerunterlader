import java.util.regex.Pattern;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RulePipelineURLRegex;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.bh.rules.xml.URLRegexPipelineMode;

/**
 * Host class for ImageVenue
 * 
 * @version 4.4
 */
public class HostImageVenue extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "4.4";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostImageVenue";

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	/**
	 * Alternative Pattern
	 */
	private Pattern urlAlternativePattern;

	private RuleRegExp regexImage = new RuleRegExp();

	RulePipelineURLRegex pipeAlternativeImage = new RulePipelineURLRegex(URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE);
	private RuleRegExp regexAlternativeImage1;
	private RuleRegExp regexAlternativeImage2;

	RulePipelineURLRegex pipeAlternativeFilename = new RulePipelineURLRegex(URLRegexPipelineMode.CONTAINER_PAGE_SOURCECODE);
	private RuleRegExp regexAlternativeFilename;

	private RuleRegExp regexContinue = new RuleRegExp();

	/**
	 * Konstruktor
	 */
	public HostImageVenue() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^(https?://img[0-9]+\\.([a-z]+[0-9]+\\.)?imagevenue\\.com/)(img|view)\\.php\\?(loc=loc[0-9]+\\&image|image)=.*");
		regexImage.setSearch("(?m)(?s)<img.+?id=[\"']thepic[\\\"'].+?(src|SRC)=[\\\"'](.+?)[\\\"']");
		regexImage.setReplace("$2");

		urlAlternativePattern = Pattern.compile("^https?://(?:www\\.)?imagevenue\\.com/ME[0-9A-Z]+$");
		regexAlternativeImage1 = new RuleRegExp("<a href=\".+?full=1\"", "");
		regexAlternativeImage2 = new RuleRegExp("<img src=\"(.+?)\"", "$1");
		pipeAlternativeImage.addRegExp(regexAlternativeImage1);
		pipeAlternativeImage.addRegExp(regexAlternativeImage2);

		regexAlternativeFilename = new RuleRegExp("<img src=\".+?\" alt=\"(.+?)\"", "$1");
		pipeAlternativeFilename.addRegExp(regexAlternativeImage1);
		pipeAlternativeFilename.addRegExp(regexAlternativeFilename);

		regexContinue.setSearch("Continue to (?:your image|ImageVenue)");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (urlPattern.matcher(url).matches() || urlAlternativePattern.matcher(url).matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Erster Teil der URL
	 * 
	 * @param url URL
	 * @return Erster Teil der URL
	 */
	private String getFirstPartURL(String url) {
		String search = "imagevenue.com/";
		int i = url.indexOf(search);
		String s = url.substring(0, i + search.length());
		return s;
	}

	/**
	 * URL parsen
	 * 
	 * @param upo URL Parse Object
	 * @param alternative True if alternative, false otherwise
	 * @throws HostException
	 */
	private void parseURL(URLParseObject upo, boolean alternative) throws HostException {
		String url = upo.getContainerURL();
		try (CloseableHttpClient client = getProxyManager().getHTTPClient()) {
			String page = downloadContainerPage(url, url, null, client);

			if (page.contains("This image does not exist on this server")) {
				throw new HostFileNotExistException("This image does not exist on this server");
			}

			if (regexContinue.doPageSourcecodeSearch(page, 0) >= 0) {
				page = downloadContainerPage(url, url, null, client);
			}

			if (!alternative) {
				/*
				 * Check first if old pattern works. Some of the old pattern links still return the old page design, but some already the new one.
				 */
				String parsedURL = regexImage.doPageSourcecodeReplace(page, 0, url, null);
				if (!parsedURL.isEmpty()) {
					String sf = getFirstPartURL(upo.getContainerURL());
					String result = sf + parsedURL;
					upo.setDirectLink(result);
					upo.setCorrectedFilename(correctFilename(result));
					return;
				}
			}

			String parsedURL = pipeAlternativeImage.getURL(upo.getContainerURL(), upo.getThumbURL(), page, upo.getPic());
			if (!parsedURL.isEmpty()) {
				upo.setDirectLink(parsedURL);
				String filename = pipeAlternativeFilename.getURL(upo.getContainerURL(), upo.getThumbURL(), page, upo.getPic());
				if (!filename.isEmpty()) {
					upo.setCorrectedFilename(filename);
				}
			}
		} catch (HostFileNotExistException e) {
			throw new HostFileNotExistException(NAME + ": Container-Page: " + e.getMessage());
		} catch (Exception e) {
			throw new HostIOException(NAME + ": Container-Page: " + e.getMessage(), e);
		}
	}

	/**
	 * Dateiname korrigieren
	 * 
	 * @param filename URL
	 * @return Korrigierter Dateiname
	 */
	private String correctFilename(String filename) {
		try {
			String cf = filename.substring(filename.lastIndexOf("/") + 1);
			if (cf.matches("[0-9]{9}_.*")) {
				cf = cf.substring(10);
			} else if (cf.matches("[0-9]{5}_.*")) {
				cf = cf.substring(6);
			} else if (cf.matches("[0-9]{5}.*")) {
				cf = cf.substring(5);
			}
			if (cf.matches(".*_[0-9]+_[0-9]+lo\\..*")) {
				String s = "";
				int fp = cf.lastIndexOf("_", cf.lastIndexOf("_") - 1);
				int lp = cf.lastIndexOf(".");
				s = cf.substring(0, fp);
				s += cf.substring(lp);
				cf = s;
			}
			return cf;
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (urlPattern.matcher(url).matches()) {
			String search = "image=";
			int start = url.lastIndexOf(search);
			if (start > -1) {
				start += search.length();
				String retval = url.substring(start);
				String cf = correctFilename("/" + retval);
				if (cf.length() > 0) {
					retval = cf;
				}
				return retval;
			}
			return "";
		} else if (urlAlternativePattern.matcher(url).matches()) {
			int start = url.lastIndexOf('/');
			if (start > -1) {
				return url.substring(start + 1);
			}
			return "";
		} else {
			return "";
		}
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (urlPattern.matcher(upo.getContainerURL()).matches()) {
			parseURL(upo, false);
		} else if (urlAlternativePattern.matcher(upo.getContainerURL()).matches()) {
			parseURL(upo, true);
		}
	}
}

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RuleRegExp;

/**
 * Host class for ImageVenue
 * 
 * @version 3.5
 */
public class HostImageVenue extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "3.5";

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

	private RuleRegExp regexImage = new RuleRegExp();

	/**
	 * Konstruktor
	 */
	public HostImageVenue() {
		super(NAME, VERSION);
		urlPattern = Pattern.compile("^(https?://img[0-9]+\\.([a-z]+[0-9]+\\.)?imagevenue\\.com/)(img|view)\\.php\\?(loc=loc[0-9]+\\&image|image)=.*");
		regexImage.setSearch("(?m)(?s)<img.+?id=[\"']thepic[\\\"'].+?(src|SRC)=[\\\"'](.+?)[\\\"']");
		regexImage.setReplace("$2");
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
	 * @param url Container-URL
	 * @return URL
	 * @throws HostException
	 */
	private String parseURL(String url) throws HostException {
		String parsedURL = "";
		try {
			String page = downloadContainerPage(url, url);

			if (page.contains("This image does not exist on this server")) {
				throw new HostFileNotExistException("This image does not exist on this server");
			}

			parsedURL = regexImage.doPageSourcecodeReplace(page, 0, url, null);
		} catch (HostFileNotExistException e) {
			throw new HostFileNotExistException(NAME + ": Container-Page: " + e.getMessage());
		}
		return parsedURL;
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
			if (!s.isEmpty()) {
				String sf = getFirstPartURL(upo.getContainerURL());
				String result = sf + s;
				upo.setDirectLink(result);
				upo.setCorrectedFilename(correctFilename(result));
			}
		}
	}
}

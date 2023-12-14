import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcatutils.http.HTTPUtil;

/**
 * Host class for IMX.to
 * 
 * @version 0.3
 */
public class HostImxTo extends Host implements IHoster {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "0.3";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostImxTo";

	private Pattern urlContainerPattern;

	private Pattern regexContinue;

	private RuleRegExp regexImage = new RuleRegExp();

	private RuleRegExp regexImageFilename = new RuleRegExp();

	/**
	 * Konstruktor
	 */
	public HostImxTo() {
		super(NAME, VERSION);

		urlContainerPattern = Pattern.compile("https?://imx\\.to/(?:i/|img-)[0-9a-zA-Z]+(\\.html)?");

		regexContinue = Pattern
				.compile("(?s)<form action=['\"]['\"] method=['\"]POST['\"]>.+?<input .+?type=['\"]submit['\"] name=['\"](imgContinue)['\"] value=['\"](Continue to( your)? image *... *)['\"]");

		String imagePattern = "<a href=\"([^\"]+)\" title=\"([^\"]*)\"[^>]*><img class=\"centred\"";

		regexImage.setSearch(imagePattern);
		regexImage.setReplace("$1");

		regexImageFilename.setSearch(imagePattern);
		regexImageFilename.setReplace("$2");
	}

	@Override
	public boolean isFromThisHoster(String url) {
		return urlContainerPattern.matcher(url).matches();
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return url.substring(url.lastIndexOf("/") + 1);
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		String containerURL = upo.getContainerURL();

		String pageSoureCode = downloadContainerPage(containerURL, null);

		Matcher matcherContinue = regexContinue.matcher(pageSoureCode);
		if (matcherContinue.find()) {
			String postFieldName = matcherContinue.group(1);
			String postFieldValue = matcherContinue.group(2);

			List<NameValuePair> postData = new ArrayList<>();
			postData.add(new BasicNameValuePair(postFieldName, postFieldValue));

			DownloadContainerPageOptions options = new DownloadContainerPageOptions(false, true, "POST", postData);
			pageSoureCode = downloadContainerPage(containerURL, containerURL, options);
		}

		String downloadURL = regexImage.doPageSourcecodeReplace(pageSoureCode, 0, containerURL, null);
		if (!downloadURL.isEmpty()) {
			upo.setDirectLink(downloadURL);

			String downloadFilename = regexImageFilename.doPageSourcecodeReplace(pageSoureCode, 0, containerURL, null);
			if (downloadFilename.isEmpty()) {
				downloadFilename = HTTPUtil.getFilenameFromURL(downloadURL, "");
			}

			if (!downloadFilename.isEmpty()) {
				upo.setCorrectedFilename(downloadFilename);
			}
		}
	}
}

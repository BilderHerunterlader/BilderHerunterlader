import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ch.supertomcat.bh.exceptions.HostException;
import ch.supertomcat.bh.exceptions.HostFileNotExistException;
import ch.supertomcat.bh.exceptions.HostIOException;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.hoster.Host;
import ch.supertomcat.bh.hoster.IHoster;
import ch.supertomcat.bh.hoster.IHosterURLAdder;
import ch.supertomcat.bh.hoster.containerpage.ContainerPage;
import ch.supertomcat.bh.hoster.containerpage.DownloadContainerPageOptions;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOptions;
import ch.supertomcat.bh.hoster.hosteroptions.IHosterOverrideDirectoryOption;
import ch.supertomcat.bh.hoster.hosteroptions.OverrideDirectoryOption;
import ch.supertomcat.bh.hoster.linkextract.ExtractTools;
import ch.supertomcat.bh.hoster.linkextract.ILinkExtractFilter;
import ch.supertomcat.bh.hoster.linkextract.LinkExtract;
import ch.supertomcat.bh.hoster.parser.URLParseObject;
import ch.supertomcat.bh.hoster.parser.URLParseObjectFile;
import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.URL;
import ch.supertomcat.bh.queue.Restriction;
import ch.supertomcat.bh.rules.RuleRegExp;
import ch.supertomcat.supertomcatutils.gui.Localization;
import ch.supertomcat.supertomcatutils.gui.copyandpaste.JTextComponentCopyAndPaste;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.html.HTMLUtil;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Host class for Youtube
 * 
 * @version 8.2
 */
public class HostYoutube extends Host implements IHoster, IHosterURLAdder, IHosterOptions, IHosterOverrideDirectoryOption {
	/**
	 * Version dieser Klasse
	 */
	public static final String VERSION = "8.2";

	/**
	 * Name dieser Klasse
	 */
	public static final String NAME = "HostYoutube";

	private static final int QUALITY_STANDARD = 0;
	private static final int QUALITY_OLD_MOBILE = 1;
	private static final int QUALITY_MOBILE = 2;
	private static final int QUALITY_MEDIUM = 3;
	private static final int QUALITY_OLD_HIGH = 4;
	private static final int QUALITY_HIGH = 5;
	private static final int QUALITY_HD = 6;
	private static final int QUALITY_FULL_HD = 7;
	private static final int QUALITY_4K_HD = 8;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Kompiliertes Muster
	 */
	private Pattern urlPattern;

	private Pattern youtubeSearchPatternFirstPage;

	private Pattern youtubeSearchPatternNextPage;

	private RuleRegExp regexTitle;

	private RuleRegExp regexError;

	private RuleRegExp regexErrorFileNotExist;

	private RuleRegExp regexFmtUrlMap;

	private RuleRegExp regexAdaptiveFmts;

	private Map<Integer, String[]> strQualities = new HashMap<>();
	private Map<Integer, String[]> strQualitiesDASHVideo = new HashMap<>();
	private Map<Integer, String[]> strQualitiesDASHAudio = new HashMap<>();
	private Set<Integer> excludedFMT = new HashSet<>();

	private boolean download4KHD = true;

	private boolean downloadFullHD = true;

	private boolean downloadHD = true;

	private boolean downloadHigh = true;

	private boolean downloadMedium = true;

	private boolean downloadMobile = false;

	private boolean downloadDASH = false;

	private boolean preferWEBM = false;

	private boolean prefer3D = false;

	private boolean preferDASH = false;

	private boolean filenameIncludeVideoID = false;

	private OverrideDirectoryOption overrideDirectoryOption;

	/**
	 * Beschraenkung
	 */
	private Restriction restriction = null;

	/**
	 * Konstruktor
	 */
	public HostYoutube() {
		super(NAME, VERSION);
		this.overrideDirectoryOption = new OverrideDirectoryOption(NAME, getSettingsManager());
		/**
		 * Name, Container Format, Video Format, Aspect Ratio, Max Video Resolution,
		 * Audio Format, Audio Channels, Sampling Rate (kHz), File-Extension, QUALITY_...
		 * 
		 * Keys = fmt
		 */
		strQualities.put(13, new String[] { "Mobile (Old)", "3GP", "H.263/AMR", "4:3", "176x144", "AMR", "Mono", "8", ".3gp", String.valueOf(QUALITY_OLD_MOBILE), "2D" });
		strQualities.put(17, new String[] { "Mobile", "3GP", "MPEG-4 Part 2", "11:9", "176x144", "AAC", "Stereo", "44.1", ".3gp", String.valueOf(QUALITY_MOBILE), "2D" });
		strQualities.put(36, new String[] { "Mobile", "3GP", "MPEG-4 Part 2", "4:3", "320x240", "AAC", "Stereo", "22", ".3gp", String.valueOf(QUALITY_MOBILE), "2D" });
		strQualitiesDASHVideo.put(160, new String[] { "Mobile", "MP4", "H.264/MPEG-4 AVC", "16:9", "256x144", "AAC", "Stereo", "22", ".mp4", String.valueOf(QUALITY_MOBILE), "2D" });

		strQualities.put(34, new String[] { "240p", "FLV", "H.264/MPEG-4 AVC", "4:3 / 16:9", "320x240 / 400x226", "AAC", "Stereo", "44.1", ".flv", String.valueOf(QUALITY_STANDARD), "2D" });
		strQualities.put(5, new String[] { "240p", "FLV", "FLV", "Unkown", "?x240p", "MP3", "?", "?", ".flv", String.valueOf(QUALITY_STANDARD), "2D" });
		strQualitiesDASHVideo.put(133, new String[] { "240p", "MP4", "H.264/MPEG-4 AVC", "16:9", "426x240", "AAC", "?", "?", ".mp4", String.valueOf(QUALITY_STANDARD), "2D" });

		strQualities.put(18, new String[] { "360p", "MP4", "H.264/MPEG-4 AVC", "4:3", "480x360", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_MEDIUM), "2D" });
		strQualities.put(6, new String[] { "360p", "FLV", "H.263", "4:3", "480x360", "MP3", "Mono", "44.1", ".flv", String.valueOf(QUALITY_MEDIUM), "2D" });
		strQualities.put(43, new String[] { "360p", "WEBM", "VP8", "4:3", "480x360", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_MEDIUM), "2D" });
		strQualities.put(82, new String[] { "360p", "MP4", "H.264/MPEG-4 AVC", "4:3", "480x360", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_MEDIUM), "3D" });
		strQualities.put(100, new String[] { "360p", "WEBM", "VP8", "4:3", "480x360", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_MEDIUM), "3D" });
		strQualitiesDASHVideo.put(134, new String[] { "360p", "MP4", "H.264/MPEG-4 AVC", "16:9", "640x360", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_MEDIUM), "2D" });

		strQualities.put(35, new String[] { "480p", "FLV", "H.264/MPEG-4 AVC", "4:3", "854x480", "AAC", "Stereo", "44.1", ".flv", String.valueOf(QUALITY_HIGH), "2D" });
		strQualities.put(44, new String[] { "480p", "WEBM", "VP8", "4:3", "854x480", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_HIGH), "2D" });
		strQualitiesDASHVideo.put(135, new String[] { "480p", "MP4", "H.264/MPEG-4 AVC", "4:3", "854x480", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_HIGH), "2D" });

		strQualities.put(22, new String[] { "720p", "MP4", "H.264/MPEG-4 AVC", "16:9", "1280x720", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_HD), "2D" });
		strQualities.put(45, new String[] { "720p", "WEBM", "VP8", "16:9", "1280x720", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_HD), "2D" });
		strQualities.put(84, new String[] { "720p", "MP4", "H.264/MPEG-4 AVC", "16:9", "1280x720", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_HD), "3D" });
		strQualities.put(102, new String[] { "720p", "WEBM", "VP8", "16:9", "1280x720", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_HD), "3D" });
		strQualitiesDASHVideo.put(136, new String[] { "720p", "MP4", "H.264/MPEG-4 AVC", "16:9", "1280x720", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_HD), "2D" });

		strQualities.put(37, new String[] { "1080p", "MP4", "H.264/MPEG-4 AVC", "16:9", "1920x1080", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_FULL_HD), "2D" });
		strQualities.put(46, new String[] { "1080p", "WEBM", "VP8", "16:9", "1920x1080", "Vorbis", "Stereo", "44.1", ".webm", String.valueOf(QUALITY_FULL_HD), "2D" });
		strQualitiesDASHVideo.put(137, new String[] { "1080p", "MP4", "H.264/MPEG-4 AVC", "16:9", "1920x1080", "AAC", "Stereo", "44.1", ".mp4", String.valueOf(QUALITY_FULL_HD), "2D" });

		strQualities.put(38, new String[] { "4KHD", "MP4", "H.264/MPEG-4 AVC", "16:9", "4096x2304", "AAC", "Stereo", "48.0", ".mp4", String.valueOf(QUALITY_4K_HD), "2D" });

		strQualitiesDASHAudio.put(139, new String[] { "Dash Audio 48kbps", "MP4", "None", "None", "None", "AAC", "Stereo", "44.1", ".m4a", String.valueOf(QUALITY_HD), "2D" });
		strQualitiesDASHAudio.put(140, new String[] { "Dash Audio 128kbps", "MP4", "None", "None", "None", "AAC", "Stereo", "44.1", ".m4a", String.valueOf(QUALITY_FULL_HD), "2D" });
		strQualitiesDASHAudio.put(141, new String[] { "Dash Audio 256kbps", "MP4", "None", "None", "None", "AAC", "Stereo", "44.1", ".m4a", String.valueOf(QUALITY_4K_HD), "2D" });

		/*
		 * DASH (Dynamic Adaptive Streaming over HTTP) video formats:
		 * 160, 133, 134, 135, 136, 137 are video only
		 * 139, 140, 141 are audio only
		 */

		/*
		 * WEBM video formats:
		 * 242, 243, 244, 247, 248, 278 are video only
		 * 171, 249, 250, 251 are audio only
		 */
		excludedFMT.add(242);
		excludedFMT.add(243);
		excludedFMT.add(244);
		excludedFMT.add(247);
		excludedFMT.add(248);
		excludedFMT.add(278);
		excludedFMT.add(171);
		excludedFMT.add(249);
		excludedFMT.add(250);
		excludedFMT.add(251);

		urlPattern = Pattern.compile("^https?://(www\\.)?youtube\\.com/(watch\\?(.*?)?v=([^&]+).*|(.*?)?#([0-9a-zA-Z]/)+([^&]+))");

		youtubeSearchPatternFirstPage = Pattern.compile("https?://(www\\.)?youtube\\.com/results\\?(&?search_sort=[^&]+|&?&filters=[^&]+|&?search_type=[^&]+|&?search_query=[^&]+){3,4}");
		youtubeSearchPatternNextPage = Pattern.compile("https?://(www\\.)?youtube\\.com/results\\?(&?search_sort=[^&]+|&?&filters=[^&]+|&?search_type=[^&]+|&?search_query=[^&]+){3,4}&page=[0-9]+");

		regexTitle = new RuleRegExp();
		regexTitle.setSearch("(?m)<title>(.+?) - YouTube");
		regexTitle.setReplace("$1");

		String[] errorMessages = new String[] { "url contained a malformed video id", "Confirm Birth Date", "This video is not available in your country", "This video contains content from [^,]+, who has blocked it in your country on copyright grounds" };
		regexError = new RuleRegExp();
		regexError.setSearch("(" + String.join("|", errorMessages) + ")");
		regexError.setReplace("$1");

		String[] fileNotExitErrorMessages = new String[] { "The video you have requested is not available", "no longer available", "copyright claim", "This video has been removed due to terms of use violation", "This video has been deleted", "This video has been removed by the user", "This video has been removed for violating YouTube&#39;s Terms of Service" };
		regexErrorFileNotExist = new RuleRegExp();
		regexErrorFileNotExist.setSearch("(" + String.join("|", fileNotExitErrorMessages) + ")");
		regexErrorFileNotExist.setReplace("$1");

		regexFmtUrlMap = new RuleRegExp();
		regexFmtUrlMap.setSearch("\"url_encoded_fmt_stream_map\": ?\"(.+?)\"");
		regexFmtUrlMap.setReplace("$1");

		regexAdaptiveFmts = new RuleRegExp("\"adaptive_fmts\": ?\"(.+?)\"", "$1");

		int iMaxConnections = 0;
		try {
			iMaxConnections = getSettingsManager().getIntValue(NAME + ".maxSimultaneousDownloads");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".maxSimultaneousDownloads", 1);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			download4KHD = getSettingsManager().getBooleanValue(NAME + ".download4KHD");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".download4KHD", download4KHD);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadFullHD = getSettingsManager().getBooleanValue(NAME + ".downloadFullHD");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadFullHD", downloadFullHD);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadHD = getSettingsManager().getBooleanValue(NAME + ".downloadHD");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadHD", downloadHD);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadHigh = getSettingsManager().getBooleanValue(NAME + ".downloadHigh");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadHigh", downloadHigh);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadMedium = getSettingsManager().getBooleanValue(NAME + ".downloadMedium");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadMedium", downloadMedium);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadMobile = getSettingsManager().getBooleanValue(NAME + ".downloadMobile");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadMobile", downloadMobile);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			downloadDASH = getSettingsManager().getBooleanValue(NAME + ".downloadDASH");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".downloadDASH", downloadDASH);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			preferWEBM = getSettingsManager().getBooleanValue(NAME + ".preferWEBM");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".preferWEBM", preferWEBM);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			prefer3D = getSettingsManager().getBooleanValue(NAME + ".prefer3D");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".prefer3D", prefer3D);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			preferDASH = getSettingsManager().getBooleanValue(NAME + ".preferDASH");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".preferDASH", preferDASH);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		try {
			filenameIncludeVideoID = getSettingsManager().getBooleanValue(NAME + ".filenameIncludeVideoID");
		} catch (Exception e) {
			try {
				getSettingsManager().setOptionValue(NAME + ".filenameIncludeVideoID", filenameIncludeVideoID);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		restriction = new Restriction("youtube.com", iMaxConnections);
		addRestriction(restriction);
	}

	@Override
	public boolean isFromThisHoster(String url) {
		if (deactivateOption.isDeactivated()) {
			return false;
		}
		Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.matches()) {
			return true;
		}
		Matcher searchUrlMatcher = youtubeSearchPatternFirstPage.matcher(url);
		if (searchUrlMatcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * URL parsen
	 * 
	 * @param url Container-URL
	 * @param upo URLParseObject
	 * @param pic Pic
	 * @throws HostException
	 */
	private void parseURL(String url, URLParseObject upo, Pic pic) throws HostException {
		// Videos in Channel-Pages does not work, so we rewrite the url to a normal video-url to get it working
		String videoID = urlPattern.matcher(url).replaceAll("$4$7");
		url = "http://www.youtube.com/watch?v=" + videoID;

		/*
		 * We check the status code later and get the sourcecode first, to check for
		 * error messages, because it is better to display an error message like
		 * "This video has been removed by the user" instead of just a 404 HTTP Error.
		 */
		ContainerPage result = downloadContainerPageEx(url, null, new DownloadContainerPageOptions(true, false));
		String htmlCode = result.getPage();

		/*
		 * Check for error messages
		 */
		String error = regexError.doPageSourcecodeReplace(htmlCode, 0, url, null);
		if (!error.isEmpty()) {
			throw new HostIOException(error);
		}

		/*
		 * Check for file not exist error messages
		 */
		String errorFileNotExist = regexErrorFileNotExist.doPageSourcecodeReplace(htmlCode, 0, url, null);
		if (!errorFileNotExist.isEmpty()) {
			throw new HostFileNotExistException(errorFileNotExist);
		}

		int statusCode = result.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			throw new HostIOException("HTTP-Error: " + statusCode);
		}

		try {
			/*
			 * Get title and error messages from the page-source-code
			 */
			String title = regexTitle.doPageSourcecodeReplace(htmlCode, 0, url, null);
			title = title.replaceAll("\\\\'", "'");

			/*
			 * First Method of getting the download link, which should always work,
			 * but does not always provide the highest quality
			 */
			String normalFmtsMap = regexFmtUrlMap.doPageSourcecodeReplace(htmlCode, 0, url, null);
			String normalFmts[] = normalFmtsMap.split(",");

			String adaptiveFmtsMap = regexAdaptiveFmts.doPageSourcecodeReplace(htmlCode, 0, url, null);
			String adaptiveFmts[] = adaptiveFmtsMap.split(",");

			List<String> fmtUrls = new ArrayList<>();

			if (normalFmts != null && normalFmts.length > 0) {
				fmtUrls.addAll(Arrays.asList(normalFmts));
			}

			if (adaptiveFmts != null && adaptiveFmts.length > 0) {
				fmtUrls.addAll(Arrays.asList(adaptiveFmts));
			}

			int qualityIndex = 0; // =fmt
			String parsedURL = "";

			int dashAudioQualityIndex = 0;
			String parsedDashAudioURL = "";

			if (!fmtUrls.isEmpty()) {
				String retvalUrl = "";
				int fmt = 0;
				String sig = "";

				String dashAudioRetvalUrl = "";
				int dashAudioFmt = 0;
				String dashAudioSig = "";

				for (String strFmtUrlMap : fmtUrls) {
					Map<String, String> fmtUrlMap = getVideoInfo(strFmtUrlMap);
					try {
						int quality = Integer.parseInt(fmtUrlMap.get("itag"));
						logger.info("Check FMT Quality: {}. FMT-Map: {}", quality, fmtUrlMap);
						if (isHigherQuality(quality, fmt, fmtUrlMap)) {
							fmt = quality;
							retvalUrl = fmtUrlMap.get("url");
							sig = fmtUrlMap.get("sig");
							if (sig == null) {
								// Ciphered Signature Detected
								String cipheredSig = fmtUrlMap.get("s");
								/*
								 * TODO Decrypt Ciphered Signature. Player JS needs to be downloaded and decrypt function used to decrypt the ciphered
								 * signature.
								 */
								sig = cipheredSig;
							}
							logger.info("Higher Quality FMT chosen: {}. FMT-Map: {}", fmt, fmtUrlMap);
						}
						if (isHigherDashAudioQuality(quality, fmt)) {
							dashAudioFmt = quality;
							dashAudioRetvalUrl = fmtUrlMap.get("url");
							dashAudioSig = fmtUrlMap.get("sig");
						}
					} catch (NumberFormatException nfe) {
						logger.error("itag is not an integer: {}", fmtUrlMap.get("itag"), nfe);
					}
				}
				if (!retvalUrl.isEmpty()) {
					qualityIndex = fmt;
					parsedURL = prepareDownloadURL(retvalUrl, sig);
				}
				if (!dashAudioRetvalUrl.isEmpty()) {
					dashAudioQualityIndex = dashAudioFmt;
					parsedDashAudioURL = prepareDownloadURL(dashAudioRetvalUrl, dashAudioSig);
				}
			}

			/*
			 * Get information for filename
			 */
			String[] qualityArray = strQualities.get(qualityIndex);
			boolean dash = false;
			if (qualityArray == null) {
				qualityArray = strQualitiesDASHVideo.get(qualityIndex);
				dash = qualityArray != null;
			}
			if (qualityArray != null) {
				logger.info("Chosen Quality: {}, Dash: {}", Arrays.toString(qualityArray), dash);
			}

			/*
			 * If no title could be read from the page, the video id is used
			 */
			if (title.isEmpty()) {
				title = videoID;
			} else {
				title = HTMLUtil.unescapeHTML(title);
			}

			/*
			 * Generate the filename
			 */
			int titleEndIndex = FileUtil.FILENAME_LENGTH_LIMIT - 24;
			if (titleEndIndex > title.length()) {
				titleEndIndex = title.length();
			}
			StringBuilder sbFilename = new StringBuilder();
			// shorten title so that id and quality are not removed by reduceFilenameLength method
			sbFilename.append(title.substring(0, titleEndIndex));

			if (filenameIncludeVideoID) {
				sbFilename.append("-");
				sbFilename.append(videoID);
			}

			sbFilename.append("-");
			sbFilename.append(qualityArray[0]);

			String filenameAudioDash = null;
			if (dash) {
				String[] qualityArrayDashAudio = strQualitiesDASHAudio.get(dashAudioQualityIndex);
				filenameAudioDash = sbFilename.toString() + qualityArrayDashAudio[8];
			}

			sbFilename.append(qualityArray[8]);

			String filename = filterFilename(sbFilename.toString());
			upo.setCorrectedFilename(filename);

			if (overrideDirectoryOption.isPathOverride()) {
				if (overrideDirectoryOption.isPathOverrideSubdirsAllowed() == false) {
					pic.setTargetPath(overrideDirectoryOption.getPathOverrideVal());
				} else {
					if (FileUtil.checkIsSameOrSubFolder(pic.getTargetPath(), overrideDirectoryOption.getPathOverrideVal()) == false) {
						pic.setTargetPath(overrideDirectoryOption.getPathOverrideVal());
					}
				}
			}

			upo.setDirectLink(parsedURL);

			if (dash && dashAudioQualityIndex != 0) {
				URLParseObjectFile additionalDirectLink = new URLParseObjectFile(parsedDashAudioURL, filenameAudioDash);
				upo.addAdditionalDirectLink(additionalDirectLink);
			}
		} catch (Exception e) {
			throw new HostIOException(NAME + ": Container-Page: " + e.getMessage(), e);
		}
	}

	private String prepareDownloadURL(String fmtURL, String fmtSig) throws DecoderException {
		logger.info("Prepare Download URL. FMT-URL: {}, Signature: {}", fmtURL, fmtSig);
		// Remove the application/x-www-form-urlencoded encoding
		URLCodec urlCodec = new URLCodec("UTF-8");
		String decodedFmtURL = urlCodec.decode(fmtURL);
		return decodedFmtURL + "&signature=" + fmtSig;
	}

	private Map<String, String> getVideoInfo(String strFmtUrlMap) {
		Map<String, String> fmtUrlMap = new HashMap<>();

		String arr[] = strFmtUrlMap.split("\\\\u0026");
		if (arr != null) {
			for (String str : arr) {
				int index = str.indexOf("=");
				if (index > 0 && index < (str.length() - 1)) {
					String key = str.substring(0, index);
					String val = str.substring(index + 1);
					fmtUrlMap.put(key, val);
				}
			}
		}
		return fmtUrlMap;
	}

	private boolean isQualityEnabled(int qualityIndex) {
		switch (qualityIndex) {
			case QUALITY_4K_HD:
				return download4KHD;
			case QUALITY_FULL_HD:
				return downloadFullHD;
			case QUALITY_HD:
				return downloadHD;
			case QUALITY_HIGH:
				return downloadHigh;
			case QUALITY_OLD_HIGH:
				return downloadHigh;
			case QUALITY_MEDIUM:
				return downloadMedium;
			case QUALITY_MOBILE:
				return downloadMobile;
			case QUALITY_OLD_MOBILE:
				return downloadMobile;
			case QUALITY_STANDARD:
				return download4KHD;
			default:
				return false;
		}
	}

	private boolean isHigherQuality(int newFmt, int currentFmt, Map<String, String> fmtUrlMap) {
		String[] qualityArrayNew = strQualities.get(newFmt);
		boolean bNewIsDASH = false;
		if (qualityArrayNew == null) {
			qualityArrayNew = strQualitiesDASHVideo.get(newFmt);
			bNewIsDASH = qualityArrayNew != null;
		}
		if (qualityArrayNew == null) {
			if (strQualitiesDASHAudio.get(newFmt) == null && !excludedFMT.contains(newFmt)) {
				logger.warn("Unrecognized Youtube fmt detected: {}. FMT-Map: {}", newFmt, fmtUrlMap);
			}
			return false;
		}

		boolean bCurrentIsDASH = false;
		String[] qualityArrayCurrent = strQualities.get(currentFmt);
		if (qualityArrayCurrent == null) {
			qualityArrayCurrent = strQualitiesDASHVideo.get(currentFmt);
			bCurrentIsDASH = qualityArrayCurrent != null;
		}

		int newIndex = Integer.parseInt(qualityArrayNew[9]);
		int currentIndex = 0;
		if (qualityArrayCurrent != null) {
			currentIndex = Integer.parseInt(qualityArrayCurrent[9]);
		}

		if (!isQualityEnabled(newIndex)) {
			return false;
		}

		if (bNewIsDASH && !downloadDASH) {
			return false;
		}

		if (newIndex > currentIndex) {
			return true;
		} else if (newIndex == currentIndex) {
			// Quality is the same
			boolean bNewIs3D = qualityArrayNew[10].equals("3D");
			boolean bNewIsWEBM = qualityArrayNew[8].equals(".webm");
			boolean bCurrentIs3D = qualityArrayCurrent != null && qualityArrayCurrent[10].equals("3D");
			boolean bCurrentIsWEBM = qualityArrayCurrent != null && qualityArrayCurrent[8].equals(".webm");

			if (prefer3D == bNewIs3D && prefer3D != bCurrentIs3D) {
				return true;
			}

			if (preferDASH == bNewIsDASH && preferDASH != bCurrentIsDASH) {
				return true;
			}

			if (preferWEBM == bNewIsWEBM && preferWEBM != bCurrentIsWEBM) {
				return true;
			}
		}

		return false;
	}

	private boolean isHigherDashAudioQuality(int newFmt, int currentFmt) {
		String[] qualityArrayNew = strQualitiesDASHAudio.get(newFmt);
		if (qualityArrayNew == null) {
			return false;
		}

		String[] qualityArrayCurrent = strQualitiesDASHAudio.get(currentFmt);

		int newIndex = Integer.parseInt(qualityArrayNew[9]);
		int currentIndex = 0;
		if (qualityArrayCurrent != null) {
			currentIndex = Integer.parseInt(qualityArrayCurrent[9]);
		}

		if (newIndex > currentIndex) {
			return true;
		}

		return false;
	}

	@Override
	public String getFilenameFromURL(String url) {
		if (!isFromThisHoster(url)) {
			return "";
		}
		return "";
	}

	@Override
	public void openOptionsDialog() {
		final JButton btnOK = new JButton("OK");
		final JButton btnCancel = new JButton("Cancel");
		JPanel pnlButtons = new JPanel();
		JPanel pnlCenter = new JPanel();
		JPanel pnlOther = new JPanel();
		JLabel lblMaxConnections = new JLabel(Localization.getString("MaxConnectionCount"));
		final JTextField txtMaxConnections = new JTextField("1", 3);
		JPanel pnlPathOverride = new JPanel();
		final JCheckBox cbPathOverride = new JCheckBox(Localization.getString("PathOverride"), overrideDirectoryOption.isPathOverride());
		final JTextField txtPathOverride = new JTextField(overrideDirectoryOption.getPathOverrideVal(), 30);
		final JButton btnPathOverride = new JButton("...");
		final JCheckBox cbPathOverrideSubdirs = new JCheckBox(Localization.getString("PathOverrideSubdirs"), overrideDirectoryOption.isPathOverrideSubdirsAllowed());
		JPanel pnlQuality = new JPanel();
		final JCheckBox cbDownload4KHD = new JCheckBox(Localization.getString("YoutubeDownload4KHD"), download4KHD);
		final JCheckBox cbDownloadFullHD = new JCheckBox(Localization.getString("YoutubeDownloadFullHD"), downloadFullHD);
		final JCheckBox cbDownloadHD = new JCheckBox(Localization.getString("YoutubeDownloadHD"), downloadHD);
		final JCheckBox cbDownloadHigh = new JCheckBox(Localization.getString("YoutubeDownloadHigh"), downloadHigh);
		final JCheckBox cbDownloadMedium = new JCheckBox(Localization.getString("YoutubeDownloadMedium"), downloadMedium);
		final JCheckBox cbDownloadMobile = new JCheckBox(Localization.getString("YoutubeDownloadMobile"), downloadMobile);
		final JCheckBox cbDownloadStandard = new JCheckBox(Localization.getString("YoutubeDownloadStandard"), true);
		final JCheckBox cbFilenameIncludeVideoID = new JCheckBox(Localization.getString("YoutubeFilenameIncludeVideoID"), filenameIncludeVideoID);
		final JCheckBox cbPreferWEBM = new JCheckBox(Localization.getString("YoutubePreferWEBM"), preferWEBM);
		final JCheckBox cbPrefer3D = new JCheckBox(Localization.getString("YoutubePrefer3D"), prefer3D);
		final JCheckBox cbDownloadDASH = new JCheckBox(Localization.getString("YoutubeDownloadDASH"), downloadDASH);
		final JCheckBox cbPreferDASH = new JCheckBox(Localization.getString("YoutubePreferDASH"), preferDASH);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);

		txtMaxConnections.setToolTipText(Localization.getString("MaxConnectionCountToolTip"));

		pnlOther.setBorder(BorderFactory.createTitledBorder(Localization.getString("Others")));
		pnlOther.setLayout(new SpringLayout());
		pnlOther.add(lblMaxConnections);
		pnlOther.add(txtMaxConnections);
		pnlOther.add(cbFilenameIncludeVideoID);
		pnlOther.add(new JLabel());
		SpringUtilities.makeCompactGrid(pnlOther, 2, 2, 0, 0, 5, 5);

		btnPathOverride.setEnabled(overrideDirectoryOption.isPathOverride());
		txtPathOverride.setEditable(false);
		txtPathOverride.setEnabled(overrideDirectoryOption.isPathOverride());
		cbPathOverrideSubdirs.setEnabled(overrideDirectoryOption.isPathOverride());

		pnlPathOverride.setBorder(BorderFactory.createTitledBorder(Localization.getString("PathOverrideTitle")));
		pnlPathOverride.setLayout(new SpringLayout());
		pnlPathOverride.add(cbPathOverride);
		pnlPathOverride.add(new JLabel());
		pnlPathOverride.add(txtPathOverride);
		pnlPathOverride.add(btnPathOverride);
		pnlPathOverride.add(cbPathOverrideSubdirs);
		pnlPathOverride.add(new JLabel());
		SpringUtilities.makeCompactGrid(pnlPathOverride, 3, 2, 0, 0, 5, 5);

		cbDownloadStandard.setEnabled(false);

		pnlQuality.setBorder(BorderFactory.createTitledBorder(Localization.getString("YoutubeQuality")));
		pnlQuality.setLayout(new SpringLayout());
		pnlQuality.add(cbDownload4KHD);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadFullHD);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadHD);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadHigh);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadMedium);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadMobile);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbDownloadStandard);
		pnlQuality.add(new JLabel(""));
		pnlQuality.add(cbPreferWEBM);
		pnlQuality.add(cbPrefer3D);
		pnlQuality.add(cbDownloadDASH);
		pnlQuality.add(cbPreferDASH);
		SpringUtilities.makeCompactGrid(pnlQuality, 9, 2, 0, 0, 5, 5);

		Insets insets = new Insets(0, 0, 5, 0);
		pnlCenter.setLayout(new GridBagLayout());
		pnlCenter.add(pnlOther, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		pnlCenter.add(pnlPathOverride, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		pnlCenter.add(pnlQuality, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.4, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtMaxConnections);
		JTextComponentCopyAndPaste.addCopyAndPasteMouseListener(txtPathOverride);

		final JDialog dialog = new JDialog(getMainWindow(), NAME, true);
		dialog.setLayout(new BorderLayout());
		dialog.add(pnlButtons, BorderLayout.SOUTH);
		try {
			txtMaxConnections.setText(Integer.toString(getSettingsManager().getIntValue(NAME + ".maxSimultaneousDownloads")));
			cbFilenameIncludeVideoID.setSelected(getSettingsManager().getBooleanValue(NAME + ".filenameIncludeVideoID"));
			cbPathOverride.setSelected(overrideDirectoryOption.isPathOverride());
			cbPathOverrideSubdirs.setSelected(overrideDirectoryOption.isPathOverrideSubdirsAllowed());
			cbDownload4KHD.setSelected(getSettingsManager().getBooleanValue(NAME + ".download4KHD"));
			cbDownloadFullHD.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadFullHD"));
			cbDownloadHD.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadHD"));
			cbDownloadHigh.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadHigh"));
			cbDownloadMedium.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadMedium"));
			cbDownloadMobile.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadMobile"));
			cbPreferWEBM.setSelected(getSettingsManager().getBooleanValue(NAME + ".preferWEBM"));
			cbPrefer3D.setSelected(getSettingsManager().getBooleanValue(NAME + ".prefer3D"));
			cbDownloadDASH.setSelected(getSettingsManager().getBooleanValue(NAME + ".downloadDASH"));
			cbPreferDASH.setSelected(getSettingsManager().getBooleanValue(NAME + ".preferDASH"));
			txtPathOverride.setText(overrideDirectoryOption.getPathOverrideVal());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		dialog.add(pnlCenter, BorderLayout.CENTER);

		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOK) {
					int iVal;
					try {
						iVal = Integer.parseInt(txtMaxConnections.getText());
						overrideDirectoryOption.setPathOverride(cbPathOverride.isSelected());
						overrideDirectoryOption.setPathOverrideVal(txtPathOverride.getText());
						overrideDirectoryOption.setPathOverrideSubdirsAllowed(cbPathOverrideSubdirs.isSelected());
						filenameIncludeVideoID = cbFilenameIncludeVideoID.isSelected();
						download4KHD = cbDownload4KHD.isSelected();
						downloadFullHD = cbDownloadFullHD.isSelected();
						downloadHD = cbDownloadHD.isSelected();
						downloadHigh = cbDownloadHigh.isSelected();
						downloadMedium = cbDownloadMedium.isSelected();
						downloadMobile = cbDownloadMobile.isSelected();
						preferWEBM = cbPreferWEBM.isSelected();
						prefer3D = cbPrefer3D.isSelected();
						downloadDASH = cbDownloadDASH.isSelected();
						preferDASH = cbPreferDASH.isSelected();
					} catch (NumberFormatException nfe) {
						return;
					}
					try {
						getSettingsManager().setOptionValue(NAME + ".maxSimultaneousDownloads", iVal);
						getSettingsManager().setOptionValue(NAME + ".filenameIncludeVideoID", filenameIncludeVideoID);
						getSettingsManager().setOptionValue(NAME + ".download4KHD", download4KHD);
						getSettingsManager().setOptionValue(NAME + ".downloadFullHD", downloadFullHD);
						getSettingsManager().setOptionValue(NAME + ".downloadHD", downloadHD);
						getSettingsManager().setOptionValue(NAME + ".downloadHigh", downloadHigh);
						getSettingsManager().setOptionValue(NAME + ".downloadMedium", downloadMedium);
						getSettingsManager().setOptionValue(NAME + ".downloadMobile", downloadMobile);
						getSettingsManager().setOptionValue(NAME + ".preferWEBM", preferWEBM);
						getSettingsManager().setOptionValue(NAME + ".prefer3D", prefer3D);
						getSettingsManager().setOptionValue(NAME + ".downloadDASH", downloadDASH);
						getSettingsManager().setOptionValue(NAME + ".preferDASH", preferDASH);
						overrideDirectoryOption.saveOptions();
						deactivateOption.saveOption();
						getSettingsManager().writeSettings(true);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
					restriction.setMaxSimultaneousDownloads(iVal);
					dialog.dispose();
				} else if (e.getSource() == btnCancel) {
					dialog.dispose();
				} else if (e.getSource() == btnPathOverride) {
					File folder = FileDialogUtil.showFolderSaveDialog(getMainWindow(), txtPathOverride.getText(), null);
					if (folder != null) {
						if ((folder.getAbsolutePath().endsWith("\\") == false) && (folder.getAbsolutePath().endsWith("/") == false)) {
							txtPathOverride.setText(folder.getAbsolutePath() + FileUtil.FILE_SEPERATOR);
						} else {
							txtPathOverride.setText(folder.getAbsolutePath());
						}
					}
					folder = null;
				}
			}
		};

		btnOK.addActionListener(action);
		btnCancel.addActionListener(action);
		btnPathOverride.addActionListener(action);

		cbPathOverride.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				txtPathOverride.setEnabled(cbPathOverride.isSelected());
				btnPathOverride.setEnabled(cbPathOverride.isSelected());
				cbPathOverrideSubdirs.setEnabled(cbPathOverride.isSelected());
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(getMainWindow());
		dialog.setVisible(true);
	}

	@Override
	public void parseURLAndFilename(URLParseObject upo) throws HostException {
		if (isFromThisHoster(upo.getContainerURL())) {
			parseURL(upo.getContainerURL(), upo, upo.getPic());
		}
	}

	@Override
	public OverrideDirectoryOption getOverrideDirectoryOption() {
		return this.overrideDirectoryOption;
	}

	@Override
	public List<URL> isFromThisHoster(URL url, AtomicBoolean isFromThisHoster, ProgressObserver progress) throws Exception {
		if (overrideDirectoryOption.isPathOverride()) {
			url.setTargetPath(overrideDirectoryOption.getPathOverrideVal());
		}
		isFromThisHoster.set(true);

		Matcher matcherSearch = youtubeSearchPatternFirstPage.matcher(url.getURL());
		if (matcherSearch.matches()) {
			isFromThisHoster.set(false);

			ILinkExtractFilter filter = new ILinkExtractFilter() {
				private Node getParentNode(int level, Node node) {
					if (level < 0) {
						return null;
					}
					if (level == 0) {
						return node;
					}

					Node parentNode = node;
					for (int i = 0; i < level; i++) {
						parentNode = parentNode.getParentNode();
						if (parentNode == null) {
							break;
						}
					}
					return parentNode;
				}

				@Override
				public boolean isLinkAccepted(Node nodeURL, Document nodeRoot, URL url, String containerURL) {
					String link = ExtractTools.getAttributeValueFromNode(nodeURL, "href");
					if (link != null && !link.isEmpty()) {
						URL extractedURL = new URL(link);
						if (!link.startsWith("http://") && !link.startsWith("https://")) {
							// If the link was relative we have to correct that
							extractedURL = ExtractTools.convertURLFromRelativeToAbsolute(containerURL, extractedURL);
						}
						Matcher matcherSearch = youtubeSearchPatternNextPage.matcher(extractedURL.getURL());
						if (matcherSearch.matches()) {
							return true;
						}
					}

					String strClass = null;
					Node parentNode = getParentNode(6, nodeURL);
					if (parentNode != null) {
						strClass = ExtractTools.getAttributeValueFromNode(parentNode, "class");
					}
					if (strClass != null && strClass.contains("item-section")) {
						Matcher matcher = urlPattern.matcher(url.getURL());
						String videoID = matcher.replaceAll("$4$7");

						String title = ExtractTools.getTextValueFromNode(nodeURL);

						String filename = videoID;
						if (title != null && title.length() > 0) {
							title = title.replaceAll("\\\\'", "'");
							title = HTMLUtil.unescapeHTML(title);
							filename = title;
							if (filenameIncludeVideoID) {
								filename += "-id" + videoID;
							}
							filename = filterFilename(filename);
							url.setFilenameCorrected(title);
						}
						url.setFilenameCorrected(filename);
						return true;
					}

					return false;
				}
			};

			List<URL> links = new ArrayList<>();
			links.add(url);

			List<URL> downloadedLinks = new ArrayList<>();
			for (int i = 0; i < links.size(); i++) {
				Matcher matcherSearchFirst = youtubeSearchPatternFirstPage.matcher(links.get(i).getURL());
				Matcher matcherSearchNext = youtubeSearchPatternNextPage.matcher(links.get(i).getURL());

				if (matcherSearchNext.matches() || matcherSearchFirst.matches()) {
					if (downloadedLinks.contains(links.get(i)) == false) {
						progress.progressChanged("Extracting Links from " + links.get(i).getURL() + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");

						List<URL> foundLinks = LinkExtract.getLinks(links.get(i).getURL(), "", filter, getProxyManager(), getSettingsManager(), getCookieManager());
						for (int x = 0; x < foundLinks.size(); x++) {
							if (links.contains(foundLinks.get(x)) == false) {
								links.add(foundLinks.get(x));
							}
						}
						downloadedLinks.add(links.get(i));
					}
					links.remove(i);
					i--;
					progress.progressChanged(0, links.size(), (downloadedLinks.size() + i));
					progress.progressChanged("Extracting Links from " + url + " (" + (downloadedLinks.size() + i) + "/" + (downloadedLinks.size() + links.size()) + ")");
				}
			}

			return links;
		}
		return null;
	}
}

package ch.supertomcat.bh.rules.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule Trace Information
 */
public class RuleTraceInfo {
	/**
	 * URL Trace Infos
	 */
	private final List<RuleTraceInfoURL> urlTraceInfos = new ArrayList<>();

	/**
	 * Filename Trace Info
	 */
	private RuleTraceInfoFilename filenameTraceInfo;

	/**
	 * Filename on Download Selection Trace Info
	 */
	private RuleTraceInfoFilename filenameOnDownloadSelectionTraceInfo;

	/**
	 * Failures Before Replace Trace Infos
	 */
	private final List<RuleTraceInfoFailures> failuresBeforeReplaceTraceInfos = new ArrayList<>();

	/**
	 * Failures After Replace Trace Infos
	 */
	private final List<RuleTraceInfoFailures> failuresAfterReplaceTraceInfos = new ArrayList<>();

	/**
	 * Constructor
	 */
	public RuleTraceInfo() {
	}

	/**
	 * Returns the urlTraceInfos
	 * 
	 * @return urlTraceInfos
	 */
	public List<RuleTraceInfoURL> getUrlTraceInfos() {
		return urlTraceInfos;
	}

	/**
	 * Add URL Trace Info
	 * 
	 * @param urlTraceInfo URL Trace Info
	 */
	public void addURLTraceInfo(RuleTraceInfoURL urlTraceInfo) {
		urlTraceInfos.add(urlTraceInfo);
	}

	/**
	 * Returns the filenameTraceInfo
	 * 
	 * @return filenameTraceInfo
	 */
	public RuleTraceInfoFilename getFilenameTraceInfo() {
		return filenameTraceInfo;
	}

	/**
	 * Sets the filenameTraceInfo
	 * 
	 * @param filenameTraceInfo filenameTraceInfo
	 */
	public void setFilenameTraceInfo(RuleTraceInfoFilename filenameTraceInfo) {
		this.filenameTraceInfo = filenameTraceInfo;
	}

	/**
	 * Returns the filenameOnDownloadSelectionTraceInfo
	 * 
	 * @return filenameOnDownloadSelectionTraceInfo
	 */
	public RuleTraceInfoFilename getFilenameOnDownloadSelectionTraceInfo() {
		return filenameOnDownloadSelectionTraceInfo;
	}

	/**
	 * Sets the filenameOnDownloadSelectionTraceInfo
	 * 
	 * @param filenameOnDownloadSelectionTraceInfo filenameOnDownloadSelectionTraceInfo
	 */
	public void setFilenameOnDownloadSelectionTraceInfo(RuleTraceInfoFilename filenameOnDownloadSelectionTraceInfo) {
		this.filenameOnDownloadSelectionTraceInfo = filenameOnDownloadSelectionTraceInfo;
	}

	/**
	 * Returns the failuresBeforeReplaceTraceInfos
	 * 
	 * @return failuresBeforeReplaceTraceInfos
	 */
	public List<RuleTraceInfoFailures> getFailuresBeforeReplaceTraceInfos() {
		return failuresBeforeReplaceTraceInfos;
	}

	/**
	 * Add Failures Before Replace Trace Info
	 * 
	 * @param urlTraceInfo Failures Before Replace Trace Info
	 */
	public void addFailuresBeforeReplaceTraceInfo(RuleTraceInfoFailures urlTraceInfo) {
		failuresBeforeReplaceTraceInfos.add(urlTraceInfo);
	}

	/**
	 * Returns the failuresAfterReplaceTraceInfos
	 * 
	 * @return failuresAfterReplaceTraceInfos
	 */
	public List<RuleTraceInfoFailures> getFailuresAfterReplaceTraceInfos() {
		return failuresAfterReplaceTraceInfos;
	}

	/**
	 * Add Failures After Replace Trace Info
	 * 
	 * @param urlTraceInfo Failures After Replace Trace Info
	 */
	public void addFailuresAfterReplaceTraceInfo(RuleTraceInfoFailures urlTraceInfo) {
		failuresAfterReplaceTraceInfos.add(urlTraceInfo);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (RuleTraceInfoURL urlTraceInfo : urlTraceInfos) {
			sb.append(urlTraceInfo.toString());
			sb.append("\n");
			sb.append("-----------------------------------\n");
		}
		for (RuleTraceInfoFailures failureTraceInfo : failuresBeforeReplaceTraceInfos) {
			sb.append(failureTraceInfo.toString());
			sb.append("\n");
			sb.append("-----------------------------------\n");
		}
		for (RuleTraceInfoFailures failureTraceInfo : failuresAfterReplaceTraceInfos) {
			sb.append(failureTraceInfo.toString());
			sb.append("\n");
			sb.append("-----------------------------------\n");
		}
		if (filenameTraceInfo != null) {
			sb.append(filenameTraceInfo.toString());
			sb.append("\n");
		}
		if (filenameOnDownloadSelectionTraceInfo != null) {
			sb.append(filenameOnDownloadSelectionTraceInfo.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

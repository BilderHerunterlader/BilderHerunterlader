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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (RuleTraceInfoURL urlTraceInfo : urlTraceInfos) {
			sb.append(urlTraceInfo.toString());
			sb.append("\n");
			sb.append("-----------------------------------\n");
		}
		sb.append(filenameTraceInfo.toString());
		sb.append("\n");
		return sb.toString();
	}
}

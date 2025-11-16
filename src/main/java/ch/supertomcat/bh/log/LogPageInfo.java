package ch.supertomcat.bh.log;

/**
 * Log Page
 * 
 * @param start Start
 * @param adjustedStart Adjusted Start
 * @param end End
 * @param lineCount Line Count
 */
public record LogPageInfo(int start, int adjustedStart, int end, int lineCount) {

}

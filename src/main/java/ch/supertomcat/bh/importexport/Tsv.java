package ch.supertomcat.bh.importexport;

/**
 * One object of this class represent a line of tsv-file
 * For more information see ch.supertomcat.bh.importexport.ImportIradaTsv
 * 
 * @param relativePath Relative path
 * @param containerURL Container-URL
 * @param lastModified Timestamp (last modified)
 * 
 * @see ch.supertomcat.bh.importexport.ImportIradaTsv
 */
public record Tsv(String relativePath, String containerURL, long lastModified) {
}

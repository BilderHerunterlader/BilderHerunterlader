package ch.supertomcat.bh.importexport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.pic.Tsv;

/**
 * Class for import tsv-Files created by Irada
 * Irada is a tool to upload files to imagehosts/filehosts.
 * 
 * A tsv-File is a tab-seperated textfile.
 * A line looks like this:
 * Filename Container-URL TimepointOfLastModification
 * or
 * Path\Filename Container-URL TimepointOfLastModification
 * or
 * Path\Path\Filename Container-URL TimepointOfLastModification
 * and so on...
 * 
 * Why such a file?
 * Because when uploading files to Imageshosts, then most of them are
 * changing the filename. Some of them in way, where it is not possible
 * to restore the original one in BilderHerunterlader.
 * An other problem is, that most people has the files sorted in directories
 * on the harddisk. When uploading only a few files, then that is no problem.
 * But if someone uploads many files, then the people who are downloading them,
 * have it then unsorted on the harddisk.
 * So, with the tsv-Files, BilderHerunterlader can restore the original Filenames
 * and also the directories.
 * At the moment the tsv-Files also contains the Timepoint of the last Modification
 * of the File. This is the only thing, i can change on a file with Java.
 * I hope there will in the future be a method to change also the Timepoint of Creation
 * of the File in Java.
 * 
 * At the moment i don't know any other download-program such BilderHerunterlader,
 * which has support for this tsv-Files nor even such a way to restore this information.
 */
public abstract class ImportIradaTsv {
	/**
	 * Logger fuer diese Klasse
	 */
	private static Logger logger = LoggerFactory.getLogger(ImportIradaTsv.class);

	/**
	 * Import the tsv-File
	 * Here the argument is an InputStream because in BilderHerunterlader the file
	 * could be imported from a file or an URL.
	 * 
	 * @param is InputStream
	 * @return Vector containing Tsvs
	 * @throws IOException
	 */
	public static List<Tsv> importTsv(InputStream is) throws IOException {
		if (is == null) {
			return null;
		}

		// Get a BufferedReader
		InputStreamReader isr = new InputStreamReader(is, "UTF-16");
		BufferedReader br = new BufferedReader(isr);

		// Get the Tsv-Objects
		List<Tsv> retval = importTsv(br);
		br.close();
		br = null;
		return retval;
	}

	/**
	 * Returns an array of Tsv-Objects
	 * 
	 * @param br BufferedReader
	 * @return Vector containing Tsvs
	 * @throws IOException
	 */
	private static List<Tsv> importTsv(BufferedReader br) throws IOException {
		String line;
		String val[];
		String rPath;
		String cURL;
		long lMod;
		List<Tsv> v = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			// Split the line
			val = line.split("\t");
			if (val.length == 3) {
				rPath = val[0];
				cURL = val[1];
				try {
					// try to get a long
					lMod = Long.parseLong(val[2]);
					if (lMod < 0) {
						lMod = 0;
					}
				} catch (NumberFormatException nfe) {
					logger.error(nfe.getMessage(), nfe);
					lMod = 0;
				}
				v.add(new Tsv(rPath, cURL, lMod));
			}
		}
		return v;
	}
}

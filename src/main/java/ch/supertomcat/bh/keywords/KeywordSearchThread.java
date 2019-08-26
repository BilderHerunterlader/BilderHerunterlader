package ch.supertomcat.bh.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.adder.AdderKeywordSelectorFilename;
import ch.supertomcat.bh.gui.adder.AdderKeywordSelectorTitle;
import ch.supertomcat.bh.keywords.KeywordMatch.KeywordMatchType;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * A Thread which searches for Keywords for Title or URLs
 * 
 * @see ch.supertomcat.bh.keywords.Keyword
 */
public class KeywordSearchThread extends Thread {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * True if multiple results should be displayed in one dialog, false if for every result a dialog should be displayed
	 */
	private boolean multiResultDisplay;

	/**
	 * Search-Strings
	 */
	private String strS[];

	/**
	 * Title- or Filename Search
	 */
	private boolean byTitle;

	/**
	 * Ownerframe
	 */
	private JFrame owner;

	/**
	 * Target directories
	 */
	private Keyword retval[];

	/**
	 * Keywords
	 */
	private List<Keyword> keywords;

	/**
	 * Listener
	 */
	private KeywordSearchThreadListener kstl = null;

	/**
	 * The index of the selected Keyword by the user (Search by title)
	 */
	private Keyword chosen = null;

	/**
	 * Don't ask the user again to select a Keyword
	 */
	private boolean chooseDefault = false;

	/**
	 * Search for files on the harddisk
	 */
	private boolean localFiles = false;

	/**
	 * Display dialog to select keyword, when no matches where found
	 */
	private boolean displayKeywordsWhenNoMatches = SettingsManager.instance().isDisplayKeywordsWhenNoMatches();

	/**
	 * Constructor for a Single Searchstring
	 * 
	 * @param strS Searchstring (URL)
	 * @param owner Owner for the dialog
	 * @param multiResultDisplay True if multiple results should be displayed in one dialog, false if for every result a dialog should be displayed
	 * @param byTitle Search by title
	 * @param localFiles Search for files on the harddisk
	 */
	public KeywordSearchThread(String strS, JFrame owner, boolean multiResultDisplay, boolean byTitle, boolean localFiles) {
		this(new String[] { strS }, owner, multiResultDisplay, byTitle, localFiles);
	}

	/**
	 * Constructor for multiple Searchstring
	 * 
	 * @param strS Searchstrings (URLs)
	 * @param owner Owner for the dialog
	 * @param multiResultDisplay True if multiple results should be displayed in one dialog, false if for every result a dialog should be displayed
	 * @param byTitle Search by title
	 * @param localFiles Search for files on the harddisk
	 */
	public KeywordSearchThread(String strS[], JFrame owner, boolean multiResultDisplay, boolean byTitle, boolean localFiles) {
		this.strS = strS;
		this.owner = owner;
		this.multiResultDisplay = multiResultDisplay;
		this.byTitle = byTitle;
		this.localFiles = localFiles;
		this.setName("Keyword-Search-Thread-" + this.getId());
	}

	@Override
	public void run() {
		if (this.strS.length == 0) {
			return;
		}

		try {
			// Create new array for target directories
			this.retval = new Keyword[strS.length];

			if (multiResultDisplay) {
				// if search by URL/Filename
				keywords = KeywordManager.instance().getKeywords();
				this.retval = search(strS);
			} else {
				// if search by title
				for (int i = 0; i < strS.length; i++) {
					// Get keywords every time from KeywordManager in case a keyword was added
					keywords = KeywordManager.instance().getKeywords();
					this.retval[i] = search(strS[i]);
				}
			}

			if (this.retval == null) {
				return;
			}
		} finally {
			progressBarStatusChanged(false); // hide the progressbar
			done(); // let the listeners know that the search is done
		}
	}

	/**
	 * Search for Keywords by title
	 * 
	 * @param search Searchstring
	 * @return Target directory
	 */
	private Keyword search(String search) {
		if (chooseDefault && chosen != null) {
			/*
			 * chosen and chooseDefault are set by the AdderKeywordSelectorTitle
			 * This method is called by the run-Method of this class in a loop
			 * So this is why we check this here
			 */
			return chosen;
		}

		progressBarStatusChanged(true); // Show progressbar
		progressBarChanged(0, keywords.size() - 1, 0); // configure progressbar
		List<KeywordMatch> foundKeywords = new ArrayList<>();

		int keywordMatchMode = SettingsManager.instance().getKeywordMatchMode();
		// Check if we have to match only exact
		boolean exactOnly = keywordMatchMode == KeywordManager.MATCH_ONLY_EXACT;
		// Check if we have to do a strict search
		boolean strict = keywordMatchMode == KeywordManager.MATCH_ALL_STRICT;

		String str = search.toUpperCase(); // Bring the searchstring to UpperCase
		int loopCount = 0; // to set the value of the progressbar
		for (Keyword kw : keywords) {
			String ks = kw.getKeywords().toUpperCase(); // Bring the keywords to UpperCase
			if (ks.isEmpty()) {
				continue; // if there are no keywords we can ignore this
			}

			boolean bExactMatch = false;

			// Check if we have an exact match
			String ksArr[] = ks.split(";"); // Split the keywordgroups
			for (String singleKeyword : ksArr) {
				// Check if the searchstring contains the complete keyword
				if (str.contains(singleKeyword)) {
					bExactMatch = true;
					foundKeywords.add(new KeywordMatch(kw, KeywordMatchType.MATCHED_EXACT));
					break;
				}
			}

			// If we have not an exact match and the user wants only exact matches, we don't need to check for other matches
			if (!bExactMatch && exactOnly) {
				loopCount++;
				progressBarChanged(loopCount); // update the value of the progressbar
				continue;
			}

			if (!bExactMatch) {
				// Check if all or some keywords match
				for (String singleKeyword : ksArr) {
					String ksSingleArr[] = singleKeyword.split(" ");
					int foundCount = 0;

					for (String singleKeywordPart : ksSingleArr) {
						if (singleKeywordPart.isEmpty()) {
							continue; // if this keyword is empty ignore it (I never thought about if this really could happen)
						}

						/*
						 * check first if the searchstring contains the
						 * keyword before compiling the pattern! This is faster!
						 */
						boolean containsKeywordPart = str.contains(singleKeywordPart);

						if (strict) {
							// if we have to do a strict search

							if (!containsKeywordPart) {
								continue;
							}

							/*
							 * We build a regex including the keyword, but the keyword could contain special regex characters,
							 * which could cause a PatternSyntaxException
							 * So we escape here some of these characters
							 */
							String escapedSingleKeywordPart = Pattern.quote(singleKeywordPart);

							/*
							 * So here is wath strict search means:
							 * A keyword is only accepted as match when there are no normal
							 * chars before or after the keyword
							 * 
							 * Example:
							 * The keyword is "Nora"
							 * Not a match -> blablaNora blabla
							 * Not a match -> blabla Norablabla
							 * Is a match -> blabla Nora blabla
							 * 
							 * So the question is why a strict search:
							 * Because the keyword could be found as a part of a word/name
							 * 
							 * Example:
							 * The keyword is "Nina"
							 * The searchstring contains "Janina"
							 * Without strict search the keyword would match, but this is not very nice.
							 * So the strict search has less false positives
							 */
							String regex = "^(?:.*[^a-zA-Z]+|)" + escapedSingleKeywordPart + "(?:[^a-zA-Z]+.*|)$";
							try {
								if (str.matches(regex)) {
									foundCount++;
								}
							} catch (PatternSyntaxException nfe) {
								logger.error("Could not compile pattern: {}", regex, nfe);
							}
						} else {
							if (str.contains(singleKeywordPart)) {
								foundCount++;
							}
						}
					}

					if (foundCount > 0) {
						// if keywords had matched
						// Check if all keywords had matched
						KeywordMatchType matchType = foundCount == ksSingleArr.length ? KeywordMatchType.MATCHED_ALL_KEYWORDS : KeywordMatchType.MATCHED_SOME_KEYWORDS;
						foundKeywords.add(new KeywordMatch(kw, matchType));
					}
				}
			}
			loopCount++;
			progressBarChanged(loopCount); // update the value of the progressbar
			if (kstl == null) {
				// if there is no Listener we can stop, because there is no object to get the result of the search
				break;
			}
		}

		if (kstl == null) {
			// if there is no Listener we can stop, because there is no object to get the result of the search
			return null;
		}

		if (!foundKeywords.isEmpty() || (foundKeywords.isEmpty() && displayKeywordsWhenNoMatches)) {
			/*
			 * We ask the user, which Keyword he will use
			 */
			AdderKeywordSelectorTitle aks = new AdderKeywordSelectorTitle(owner, Localization.getString("KeywordSelection"), true, foundKeywords, keywords, !byTitle);
			if (aks.isOkPressed()) {
				// If the user has selected a Keyword
				this.chooseDefault = aks.isChooseDefault();
				chosen = aks.getSelectedKeyword();
				return aks.getSelectedKeyword();
			} else {
				// If the user canceled the Dialog then return null
				chosen = null;
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Search for Keywords by URL/filenames
	 * 
	 * @param search Searchstring
	 * @return Target directory
	 */
	private Keyword[] search(String search[]) {
		progressBarStatusChanged(true); // Show progressbar
		progressBarChanged(0, search.length, 0); // configure progressbar

		List<KeywordFilenameMatches> resultFoundKeywords = new ArrayList<>();

		int index = 0;
		for (String searchString : search) {
			List<KeywordMatch> foundKeywords = new ArrayList<>();
			String str = searchString.toUpperCase(); // Bring the search string to upper case

			for (Keyword keyword : keywords) {
				String ks = keyword.getKeywords().toUpperCase();
				if (ks.isEmpty()) {
					continue;
				}

				boolean bExactMatch = false; // exakte Uebereinstimmung

				// Check if there are exact matches
				String ksArr[] = ks.split(";");
				for (String singleKeyword : ksArr) {
					if (str.contains(singleKeyword)) {
						bExactMatch = true;
						foundKeywords.add(new KeywordMatch(keyword, KeywordMatchType.MATCHED_EXACT));
						break;
					}
				}

				if (bExactMatch == false) {
					// Check if all or some keywords match
					for (int i = 0; i < ksArr.length; i++) {
						String ksSingleArr[] = ksArr[i].split(" ");
						int foundCount = 0;
						/*
						 * Here we can't do a strict search, because it is not useful in an URL
						 */
						for (String singleKeywordPart : ksSingleArr) {
							if (singleKeywordPart.isEmpty()) {
								continue;
							}
							if (str.contains(singleKeywordPart)) {
								foundCount++;
							}
						}
						if (foundCount > 0) {
							// If keywords had matched
							KeywordMatchType matchType = foundCount == ksSingleArr.length ? KeywordMatchType.MATCHED_ALL_KEYWORDS : KeywordMatchType.MATCHED_SOME_KEYWORDS;
							foundKeywords.add(new KeywordMatch(keyword, matchType));
						}
					}
				}
				if (kstl == null) {
					// if there is no Listener we can stop, because there is no object to get the result of the search
					break;
				}
			}
			// Remove some matches, see the method description for more details
			cleanupMatches(foundKeywords);

			resultFoundKeywords.add(new KeywordFilenameMatches(searchString, foundKeywords));
			index++;
			progressBarChanged(index);
			if (kstl == null) {
				// if there is no Listener we can stop, because there is no object to get the result of the search
				break;
			}
		}
		if (kstl == null) {
			// if there is no Listener we can stop, because there is no object to get the result of the search
			return null;
		}

		/*
		 * Yes, i know i should not do this, but if we have a lot of URLs,
		 * then i think it is not the worst idea to let the Garbage Collector know
		 * that he could reduce the memory usage a bit ;-)
		 */
		System.gc();

		/*
		 * Show the Keyword-Selection-Dialog
		 * Now the dialog will display a table with one entry per URL.
		 * And for every URL a JCombobox, with the matches.
		 * And because this is a lot of data, the heap can overflow now, when we have
		 * a lot of URLs.
		 * I have an idea for a solution, read the method-description of cleanupMatches for
		 * more details.
		 * Maybe i could also catch the heap overflow error and try to reduce then the memory,
		 * but i don't know if i can really do that, because this would again need memory, but
		 * there is no more free ;-)
		 */
		AdderKeywordSelectorFilename dlg = new AdderKeywordSelectorFilename(owner, Localization.getString("KeywordSelection"), true, resultFoundKeywords, localFiles);
		return dlg.getChosenKeywords();
	}

	/**
	 * This method remove matches from the array, if they are better ones in it.
	 * So if we have only some keywords matched then nothing is removed
	 * If we have all keywords matched and some keywords matched, then the some keywords matched
	 * will be removed.
	 * If we have exact matches, the all keywords matched and some keywords matched are removed.
	 * 
	 * This method is used for search by filename. Because when there are a lot of URLs
	 * then we should remove some matches, if they are possible not so good. So we can
	 * reduce the memory usage.
	 * I got Heap overflows when displaying the AdderKeywordSelectorFilename and there were
	 * a lot of URLs.
	 * A solution could possible be, to not display all results in one dialog, but display only
	 * a few and so display multiple dialogs one after the other.
	 * I don't tried that, but i will.
	 * 
	 * @param foundKeywords Found Keywords
	 */
	private void cleanupMatches(List<KeywordMatch> foundKeywords) {
		boolean bExact = false; // Exact match
		boolean bAll = false; // All keywords matched

		// Check if there are exact matches or all keyword matched
		for (int i = 0; i < foundKeywords.size(); i++) {
			KeywordMatchType kindOfMatch = foundKeywords.get(i).getMatchType();
			if (kindOfMatch == KeywordMatchType.MATCHED_EXACT) {
				bExact = true;
				break;
			} else if (kindOfMatch == KeywordMatchType.MATCHED_ALL_KEYWORDS) {
				bAll = true;
				break;
			}
		}

		// If no exact matches or no all keywords machted, then we don't have to do anything
		if ((bExact == false) && (bAll == false)) {
			return;
		}

		// Now we remove some matches
		for (int i = foundKeywords.size() - 1; i > -1; i--) {
			KeywordMatchType kindOfMatch = foundKeywords.get(i).getMatchType();
			if (kindOfMatch == KeywordMatchType.MATCHED_SOME_KEYWORDS) {
				foundKeywords.remove(i);
			} else if ((kindOfMatch == KeywordMatchType.MATCHED_ALL_KEYWORDS) && bExact) {
				foundKeywords.remove(i);
			}
		}
	}

	/**
	 * Let the listener know, that the search is done
	 */
	private void done() {
		if (kstl != null) {
			kstl.searchDone(this.retval);
		}
	}

	/**
	 * Sets the listener
	 * 
	 * @param kstl Listener
	 */
	public void setKeywordSearchThreadListener(KeywordSearchThreadListener kstl) {
		this.kstl = kstl;
	}

	/**
	 * Removes the listener
	 * 
	 * @param kstl Listener
	 */
	public void removeKeywordSearchThreadListener(KeywordSearchThreadListener kstl) {
		if (this.kstl == kstl) {
			this.kstl = null;
		}
	}

	/**
	 * Progressbar changed
	 * 
	 * @param min Minimum
	 * @param max Maximum
	 * @param val Value
	 */
	private void progressBarChanged(int min, int max, int val) {
		if (kstl == null) {
			return;
		}
		kstl.progressBarChanged(min, max, val);
	}

	/**
	 * Progressbar changed
	 * 
	 * @param val Value
	 */
	private void progressBarChanged(int val) {
		if (kstl == null) {
			return;
		}
		kstl.progressBarChanged(val);
	}

	/**
	 * Progressbar changed
	 * 
	 * @param enabled Enabled
	 */
	private void progressBarStatusChanged(boolean enabled) {
		if (kstl == null) {
			return;
		}
		kstl.progressBarStatusChanged(enabled);
	}
}

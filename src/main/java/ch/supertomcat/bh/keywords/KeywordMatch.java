package ch.supertomcat.bh.keywords;

import java.util.Comparator;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Keyword Match
 */
public class KeywordMatch implements Comparable<KeywordMatch> {
	/**
	 * Keyword
	 */
	private final Keyword keyword;

	/**
	 * Match Type
	 */
	private final KeywordMatchType matchType;

	/**
	 * Constructor
	 * 
	 * @param keyword Keyword
	 * @param matchType Match Type
	 */
	public KeywordMatch(Keyword keyword, KeywordMatchType matchType) {
		this.keyword = keyword;
		this.matchType = matchType;
	}

	/**
	 * Returns the keyword
	 * 
	 * @return keyword
	 */
	public Keyword getKeyword() {
		return keyword;
	}

	/**
	 * Returns the matchType
	 * 
	 * @return matchType
	 */
	public KeywordMatchType getMatchType() {
		return matchType;
	}

	@Override
	public int compareTo(KeywordMatch o) {
		if (matchType == o.getMatchType()) {
			return keyword.compareTo(o.getKeyword());
		} else {
			return Integer.compare(matchType.getPriority(), o.getMatchType().getPriority());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(keyword.getTitle());
		sb.append(" (");
		sb.append(matchType.getText());
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Keyword Match Type
	 */
	public enum KeywordMatchType {
		/**
		 * No match
		 */
		MATCHED_NOT("NotMatched", 0),

		/**
		 * Exact match
		 */
		MATCHED_EXACT("ExactMatched", 3),

		/**
		 * All keywords found
		 */
		MATCHED_ALL_KEYWORDS("AllMatched", 2),

		/**
		 * Some keywords found
		 */
		MATCHED_SOME_KEYWORDS("SomeMatched", 1);

		/**
		 * Key for text
		 */
		private final String textKey;

		/**
		 * Priority of match
		 */
		private final int priority;

		/**
		 * Constructor
		 * 
		 * @param textKey Key for text
		 * @param priority Priority of match
		 */
		private KeywordMatchType(String textKey, int priority) {
			this.textKey = textKey;
			this.priority = priority;
		}

		/**
		 * @return Text for match type
		 */
		public String getText() {
			return Localization.getString(textKey);
		}

		/**
		 * @return Priority of match
		 */
		private int getPriority() {
			return priority;
		}
	}

	/**
	 * Comparator for KeywordMatchType
	 */
	public static class KeywordMatchTypeComparator implements Comparator<KeywordMatchType> {
		@Override
		public int compare(KeywordMatchType o1, KeywordMatchType o2) {
			return -Integer.compare(o1.priority, o2.priority);
		}
	}
}

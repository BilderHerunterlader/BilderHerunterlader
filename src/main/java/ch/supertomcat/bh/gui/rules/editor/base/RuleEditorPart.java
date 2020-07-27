package ch.supertomcat.bh.gui.rules.editor.base;

/**
 * Interface for handling updates within rule editor and applying changes
 */
public interface RuleEditorPart {
	/**
	 * Called when redirect option is enabled or disabled
	 * 
	 * @param enabled Enabled
	 */
	public void redirectEnabled(boolean enabled);

	/**
	 * Called when changes should be applied to the rule definition
	 * 
	 * @return True if changes were successfully applied, false otherwise
	 */
	public boolean apply();
}

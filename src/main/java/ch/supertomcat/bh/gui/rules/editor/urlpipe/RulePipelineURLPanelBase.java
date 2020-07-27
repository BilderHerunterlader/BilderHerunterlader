package ch.supertomcat.bh.gui.rules.editor.urlpipe;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.bh.gui.rules.editor.base.RuleEditorPart;
import ch.supertomcat.bh.rules.xml.URLPipeline;

/**
 * Base Class for URL Pipeline Panels
 * 
 * @param <T> URLPipeline Type
 */
public abstract class RulePipelineURLPanelBase<T extends URLPipeline> extends JPanel implements RuleEditorPart {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * RulePipeline
	 */
	protected final T pipe;

	/**
	 * Constructor
	 * 
	 * @param pipe
	 */
	public RulePipelineURLPanelBase(T pipe) {
		this.pipe = pipe;
	}

	/**
	 * Returns the pipe
	 * 
	 * @return pipe
	 */
	public T getPipe() {
		return pipe;
	}
}

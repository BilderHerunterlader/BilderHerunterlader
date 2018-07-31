package ch.supertomcat.bh.gui.rules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.supertomcat.bh.gui.Icons;
import ch.supertomcat.bh.gui.SpringUtilities;
import ch.supertomcat.bh.rules.Rule;
import ch.supertomcat.bh.rules.RuleMode;
import ch.supertomcat.bh.rules.RulePipeline;
import ch.supertomcat.bh.rules.RulePipelineFailures;
import ch.supertomcat.supertomcattools.guitools.Localization;

/**
 * 
 *
 */
public class RulePipesFailuresPanel extends JPanel implements ActionListener, ListSelectionListener {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1391103999859263322L;

	private Rule rule = null;

	private JDialog owner = null;

	/**
	 * Model for List
	 */
	private DefaultListModel<String> modelPipelines = new DefaultListModel<>();

	/**
	 * lstPipelines
	 */
	private JList<String> lstPipelines = new JList<>(modelPipelines);

	/**
	 * pnlCurrentDisplayedPipeline
	 */
	private RulePipelineFailuresPanel pnlCurrentDisplayedPipeline = null;

	/**
	 * pipelines
	 */
	private List<RulePipeline> originalPipelines = null;

	/**
	 * pipelines
	 */
	private List<RulePipeline> pipelines = new ArrayList<>();

	/**
	 * RulePipelinePanel
	 */
	private List<RulePipelineFailuresPanel> pnlPipelines = new ArrayList<>();

	/**
	 * Button
	 */
	private JButton btnPipelineNew = new JButton(Localization.getString("New"), Icons.getTangoIcon("actions/document-new.png", 16));

	/**
	 * Button
	 */
	private JButton btnPipelineUp = new JButton(Localization.getString("Up"), Icons.getTangoIcon("actions/go-up.png", 16));

	/**
	 * Button
	 */
	private JButton btnPipelineDown = new JButton(Localization.getString("Down"), Icons.getTangoIcon("actions/go-down.png", 16));

	/**
	 * Button
	 */
	private JButton btnPipelineDelete = new JButton(Localization.getString("Delete"), Icons.getTangoIcon("actions/edit-delete.png", 16));

	/**
	 * Panel
	 */
	private JPanel pnlPipelineButtons = new JPanel();

	/**
	 * Constructor
	 * 
	 * @param rule Rule
	 * @param owner Owner
	 */
	public RulePipesFailuresPanel(Rule rule, JDialog owner) {
		super();
		this.rule = rule;
		this.owner = owner;

		originalPipelines = this.rule.getPipelinesFailures();
		for (int iPipe = 0; iPipe < originalPipelines.size(); iPipe++) {
			RulePipelineFailuresPanel pnlPipe = new RulePipelineFailuresPanel(this.rule, originalPipelines.get(iPipe), owner);
			pnlPipelines.add(pnlPipe);
			modelPipelines.addElement("Pipeline");
			pipelines.add(originalPipelines.get(iPipe));
		}

		pnlPipelineButtons.setLayout(new SpringLayout());
		pnlPipelineButtons.add(btnPipelineNew);
		pnlPipelineButtons.add(btnPipelineUp);
		pnlPipelineButtons.add(btnPipelineDown);
		pnlPipelineButtons.add(btnPipelineDelete);
		SpringUtilities.makeCompactGrid(pnlPipelineButtons, 4, 1, 0, 0, 5, 5);

		setLayout(new BorderLayout());
		JPanel pnlPipe = new JPanel();
		pnlPipe.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(lstPipelines);

		pnlPipe.add(scrollPane, BorderLayout.CENTER);
		pnlPipe.add(pnlPipelineButtons, BorderLayout.EAST);
		add(pnlPipe, BorderLayout.WEST);

		lstPipelines.addListSelectionListener(this);
		btnPipelineNew.addActionListener(this);
		btnPipelineUp.addActionListener(this);
		btnPipelineDown.addActionListener(this);
		btnPipelineDelete.addActionListener(this);

		if (pnlPipelines.size() > 0) {
			lstPipelines.setSelectedIndex(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == lstPipelines) {
			if (pnlPipelines.size() > 0) {
				int selectedIndex = lstPipelines.getSelectedIndex();
				if (selectedIndex > -1) {
					if (pnlCurrentDisplayedPipeline != null) {
						remove(pnlCurrentDisplayedPipeline);
					}
					add(pnlPipelines.get(selectedIndex), BorderLayout.CENTER);
					pnlCurrentDisplayedPipeline = pnlPipelines.get(selectedIndex);
				} else {
					if (pnlCurrentDisplayedPipeline != null) {
						remove(pnlCurrentDisplayedPipeline);
					}
					pnlCurrentDisplayedPipeline = null;
				}
			} else {
				if (pnlCurrentDisplayedPipeline != null) {
					remove(pnlCurrentDisplayedPipeline);
				}
				pnlCurrentDisplayedPipeline = null;
			}
			revalidate();
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnPipelineNew) {
			RulePipeline pipe = new RulePipelineFailures(RuleMode.RULE_MODE_FAILURES);
			RulePipelineFailuresPanel pnlPipe = new RulePipelineFailuresPanel(this.rule, pipe, owner);
			pnlPipelines.add(pnlPipe);
			modelPipelines.addElement("Pipeline");
			pipelines.add(pipe);
		} else if (e.getSource() == btnPipelineDelete) {
			int selectedIndex = lstPipelines.getSelectedIndex();
			if (selectedIndex > -1) {
				pipelines.remove(selectedIndex);
				pnlPipelines.remove(selectedIndex);
				modelPipelines.removeElementAt(selectedIndex);
				if (modelPipelines.getSize() > 0) {
					if (modelPipelines.getSize() > (selectedIndex)) {
						lstPipelines.setSelectedIndex(selectedIndex);
					} else {
						lstPipelines.setSelectedIndex(selectedIndex - 1);
					}
				}
			}
		} else if (e.getSource() == btnPipelineUp) {
			int selectedIndex = lstPipelines.getSelectedIndex();
			if (selectedIndex > 0) {
				swapPipelines(selectedIndex, selectedIndex - 1);
				lstPipelines.setSelectedIndex(selectedIndex - 1);
			}
		} else if (e.getSource() == btnPipelineDown) {
			int selectedIndex = lstPipelines.getSelectedIndex();
			if (selectedIndex > -1 && selectedIndex < modelPipelines.getSize() - 1) {
				swapPipelines(selectedIndex, selectedIndex + 1);
				lstPipelines.setSelectedIndex(selectedIndex + 1);
			}
		}
	}

	private void swapPipelines(int index1, int index2) {
		// Model
		String o1 = modelPipelines.getElementAt(index1);
		String o2 = modelPipelines.getElementAt(index2);
		modelPipelines.setElementAt(o1, index2);
		modelPipelines.setElementAt(o2, index1);

		// Pipelines
		RulePipeline rp1 = pipelines.get(index1);
		RulePipeline rp2 = pipelines.get(index2);
		pipelines.set(index2, rp1);
		pipelines.set(index1, rp2);

		// PipelinePanels
		RulePipelineFailuresPanel rpp1 = pnlPipelines.get(index1);
		RulePipelineFailuresPanel rpp2 = pnlPipelines.get(index2);
		pnlPipelines.set(index2, rpp1);
		pnlPipelines.set(index1, rpp2);
	}

	/**
	 * Apply
	 */
	public void apply() {
		for (int i = 0; i < pnlPipelines.size(); i++) {
			pnlPipelines.get(i).apply();
		}
		originalPipelines.clear();
		for (int iPipe = 0; iPipe < pipelines.size(); iPipe++) {
			originalPipelines.add(pipelines.get(iPipe));
		}
	}
}

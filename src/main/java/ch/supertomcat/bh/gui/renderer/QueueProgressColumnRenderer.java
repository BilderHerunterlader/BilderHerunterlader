package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicProgress;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.ProgressDisplayMode;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;

/**
 * QueueProgressColumnRenderer
 */
public class QueueProgressColumnRenderer extends QueueColorRowRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Progress String Format
	 */
	private static final String PROGRESS_PERCENT_STRING_FORMAT = "%s%.2f%% %s";

	/**
	 * Progress String Format
	 */
	private static final String PROGRESS_SIZE_STRING_FORMAT = "%s%s %s";

	/**
	 * Complete Color
	 */
	private static final Color COMPLETE_COLOR = Color.decode("#81ff68");

	/**
	 * Waiting Color
	 */
	private static final Color WAITING_COLOR = Color.decode("#30ffff");

	/**
	 * Failed Color
	 */
	private static final Color FAILED_COLOR = Color.decode("#ff9e9e");

	/**
	 * Progress Bar
	 */
	private JProgressBar progressBar = new JProgressBar();

	/**
	 * Constructor
	 * 
	 * @param settingsManager Settings Manager
	 */
	public QueueProgressColumnRenderer(SettingsManager settingsManager) {
		super(settingsManager);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Pic) {
			Pic pic = (Pic)value;

			Component comp;
			if (pic.getStatus() == PicState.DOWNLOADING && pic.getProgress() != null) {
				PicProgress progress = pic.getProgress();
				progressBar.setValue(progress.getPercentInt());

				long bytesTotal = progress.getBytesTotal();
				long bytesDownloaded = progress.getBytesDownloaded();

				ProgressDisplayMode progressDisplayMode = settingsManager.getGUISettings().getProgressDisplayMode();
				if (progressDisplayMode == ProgressDisplayMode.NO_PROGRESSBAR_PERCENT || progressDisplayMode == ProgressDisplayMode.NO_PROGRESSBAR_SIZE) {
					// if the user don't want to see the visual progress, set the value to 0
					bytesDownloaded = 0;
				}

				String rateString;
				if (settingsManager.getGUISettings().isDownloadRate()) {
					rateString = UnitFormatUtil.getBitrateString(progress.getRate());
				} else {
					rateString = "";
				}

				int urlCount = progress.getUrlCount();
				String urlIndexString;
				if (urlCount <= 1) {
					urlIndexString = "";
				} else {
					urlIndexString = progress.getCurrentURLIndex() + "/" + urlCount + " ";
				}

				String progressString;
				if (bytesTotal >= bytesDownloaded) {
					if (progressDisplayMode == ProgressDisplayMode.PROGRESSBAR_PERCENT || progressDisplayMode == ProgressDisplayMode.NO_PROGRESSBAR_PERCENT) {
						progressString = String.format(PROGRESS_PERCENT_STRING_FORMAT, urlIndexString, progress.getPercent(), rateString);
					} else if (progressDisplayMode == ProgressDisplayMode.PROGRESSBAR_SIZE || progressDisplayMode == ProgressDisplayMode.NO_PROGRESSBAR_SIZE) {
						String sizeString = UnitFormatUtil.getSizeString(bytesDownloaded, settingsManager.getSizeView());
						progressString = String.format(PROGRESS_SIZE_STRING_FORMAT, urlIndexString, sizeString, rateString);
					} else {
						String sizeString = UnitFormatUtil.getSizeString(bytesDownloaded, settingsManager.getSizeView());
						progressString = String.format(PROGRESS_SIZE_STRING_FORMAT, urlIndexString, sizeString, rateString);
					}
				} else {
					String sizeString = UnitFormatUtil.getSizeString(bytesDownloaded, settingsManager.getSizeView());
					progressString = String.format(PROGRESS_SIZE_STRING_FORMAT, urlIndexString, sizeString, rateString);
				}

				progressBar.setString(progressString);
				progressBar.setToolTipText(progressString);

				comp = progressBar;
			} else {
				setText(pic.getStatusText());
				setOpaque(true);

				String errMsg = pic.getErrMsg();
				if (errMsg != null && !errMsg.isEmpty()) {
					setToolTipText(errMsg);
				} else {
					setToolTipText(pic.getStatusText());
				}

				prepareForegroundColor(this, table, value, isSelected, hasFocus, row, column);

				comp = this;
			}

			if (isSelected) {
				prepareBackgroundColor(comp, table, value, isSelected, hasFocus, row, column);
			} else {
				switch (pic.getStatus()) {
					case COMPLETE:
						comp.setBackground(COMPLETE_COLOR);
						break;
					case FAILED:
					case FAILED_FILE_NOT_EXIST:
					case FAILED_FILE_TEMPORARY_OFFLINE:
						comp.setBackground(FAILED_COLOR);
						break;
					case WAITING:
						comp.setBackground(WAITING_COLOR);
						break;
					case ABORTING:
					case SLEEPING:
					case DOWNLOADING:
					default:
						prepareBackgroundColor(comp, table, value, isSelected, hasFocus, row, column);
						break;
				}
			}

			return comp;
		} else {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}

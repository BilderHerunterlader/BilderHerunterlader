package ch.supertomcat.bh.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.supertomcat.bh.pic.Pic;
import ch.supertomcat.bh.pic.PicProgress;
import ch.supertomcat.bh.pic.PicState;
import ch.supertomcat.bh.settings.SettingsManager;
import ch.supertomcat.bh.settings.xml.ProgressDisplayMode;
import ch.supertomcat.supertomcatutils.gui.formatter.UnitFormatUtil;
import ch.supertomcat.supertomcatutils.gui.table.renderer.DefaultProgressBarColorRowRenderer;

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
	 * Progress Bar (Use DefaultProgressBarColorRowRenderer instead of JProgressBar directly for performance)
	 */
	private DefaultProgressBarColorRowRenderer progressBar = new DefaultProgressBarColorRowRenderer();

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
		if (value instanceof Pic pic) {
			Component comp;
			if (pic.getStatus() == PicState.DOWNLOADING && pic.getProgress() != null) {
				PicProgress progress = pic.getProgress();
				progressBar.setValue(progress.getPercentInt());

				long bytesTotal = progress.getBytesTotal();
				long bytesDownloaded = progress.getBytesDownloaded();

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

				ProgressDisplayMode progressDisplayMode = settingsManager.getGUISettings().getProgressDisplayMode();
				String progressString;
				if (bytesTotal >= bytesDownloaded) {
					switch (progressDisplayMode) {
						case ProgressDisplayMode.PROGRESSBAR_PERCENT -> progressString = String.format(PROGRESS_PERCENT_STRING_FORMAT, urlIndexString, progress.getPercent(), rateString);

						case ProgressDisplayMode.PROGRESSBAR_SIZE -> {
							String sizeString = UnitFormatUtil.getSizeString(bytesDownloaded, settingsManager.getSizeView());
							progressString = String.format(PROGRESS_SIZE_STRING_FORMAT, urlIndexString, sizeString, rateString);
						}
						default -> {
							String sizeString = UnitFormatUtil.getSizeString(bytesDownloaded, settingsManager.getSizeView());
							progressString = String.format(PROGRESS_SIZE_STRING_FORMAT, urlIndexString, sizeString, rateString);
						}
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
					case FAILED, FAILED_FILE_NOT_EXIST, FAILED_FILE_TEMPORARY_OFFLINE:
						comp.setBackground(FAILED_COLOR);
						break;
					case WAITING:
						comp.setBackground(WAITING_COLOR);
						break;
					case ABORTING, SLEEPING, DOWNLOADING:
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

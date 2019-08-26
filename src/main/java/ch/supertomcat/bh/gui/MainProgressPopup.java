package ch.supertomcat.bh.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * 
 *
 */
public class MainProgressPopup extends JWindow {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -107879928022469206L;

	private ArrayList<ProgressObserver> observers = new ArrayList<>();
	private ArrayList<JProgressBar> progressBars = new ArrayList<>();
	private ArrayList<IProgressObserver> observerListeners = new ArrayList<>();

	/**
	 * Constructor
	 */
	public MainProgressPopup() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		getContentPane().setBackground(Color.WHITE);
	}

	/**
	 * @param progress Progress
	 */
	public synchronized void addProgressObserver(ProgressObserver progress) {
		final JProgressBar pg = new JProgressBar();
		observers.add(progress);
		progressBars.add(pg);
		IProgressObserver ipo = new IProgressObserver() {
			private JProgressBar progressBar = pg;

			@Override
			public void progressChanged(final boolean visible) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisible(visible);
					}
				});
			}

			@Override
			public void progressChanged(final String text) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						progressBar.setStringPainted(true);
						progressBar.setString(text);
					}
				});
			}

			@Override
			public void progressChanged(final int min, final int max, final int val) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (min < 0 || max < 0 || val < 0) {
							progressBar.setIndeterminate(true);
						} else {
							progressBar.setIndeterminate(false);
							progressBar.setMinimum(min);
							progressBar.setMaximum(max);
							progressBar.setValue(val);
						}
					}
				});
			}

			@Override
			public void progressChanged(final int val) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						progressBar.setValue(val);
					}
				});
			}

			@Override
			public void progressIncreased() {
			}

			@Override
			public void progressModeChanged(boolean indeterminate) {
			}

			@Override
			public void progressCompleted() {
			}
		};
		observerListeners.add(ipo);
		progress.addProgressListener(ipo);

		add(pg);
	}

	/**
	 * @param progress Progress
	 */
	public synchronized void removeProgressObserver(ProgressObserver progress) {
		int index = observers.indexOf(progress);
		observers.remove(index);
		IProgressObserver ipo = observerListeners.remove(index);
		progress.removeProgressListener(ipo);
		JProgressBar pg = progressBars.remove(index);

		remove(pg);
	}

	/**
	 * @return ProgressObserverCount
	 */
	public synchronized int getProgressObserverCount() {
		return observers.size();
	}
}

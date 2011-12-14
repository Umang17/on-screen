package onscreen.presentator.utility;

import onscreen.presentator.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FileProgress {
	private View view;
	private TextView textLeft, textRight;
	private ProgressBar progressBar;
	private long fileSize = 0;

	/**
	 * Constructs a new FileProgress with fileSize = 0.
	 * 
	 * @param context
	 */
	public FileProgress(Context context) {
		this(context, 0);
	}

	/**
	 * Constructs a new FileProgress.
	 * 
	 * @param context
	 * @param fileSize
	 *            The size of the file.
	 */
	public FileProgress(Context context, long fileSize) {
		view = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.file_progress_view, null);

		textLeft = (TextView) view.findViewById(R.id.textLeft_file_progress);
		textRight = (TextView) view.findViewById(R.id.textRight_file_progress);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		setFileSize(fileSize);
	}

	/**
	 * Gets the view showing the progress.
	 * 
	 * @return The view that shows the progress.
	 */
	public View getView() {
		return view;
	}

	/**
	 * Changes the file size to show progress for, also sets the progress to 0.
	 * 
	 * @param fileSize
	 *            The size of the file, in bytes.
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
		setProgress(0);
	}

	/**
	 * Sets the progress in the progress bar, also changes the % text.
	 * 
	 * @param percent
	 *            The percentage amount which has been sent.
	 */
	private void setPercent(int percent) {
		textLeft.setText(percent + "%");
		progressBar.setProgress(percent);
	}

	/**
	 * Changes the text of the amount of data transfered e.g 10/100B
	 * 
	 * @param progress
	 *            The amount hat has been sent.
	 * @param prefix
	 *            The prefix before B e.g MB where M is the prefix.
	 * @param divider
	 *            How much to divide the progress when showing it. Like 1000000
	 *            for MB.
	 */
	private void setTransferText(long progress, Character prefix, float divider) {
		textRight.setText(String.format("%.2f", progress / divider) + " / "
				+ String.format("%.2f", fileSize / divider) + " "
				+ (prefix == null ? "" : prefix) + "B");
	}

	/**
	 * Sets the current progress of the transfer.
	 * 
	 * @param progress
	 *            The amount that has been sent.
	 */
	public void setProgress(long progress) {
		if (progress > fileSize) {
			progress = fileSize;
		} else if (progress < 0) {
			progress = 0;
		}

		// Convert to MB and KB
		Character prefix = null;
		float divider = 1f;
		if (fileSize >= 1000000) {
			prefix = 'M';
			divider = 1000000f;
		} else if (fileSize >= 1000) {
			prefix = 'K';
			divider = 1000f;
		}
		setTransferText(progress, prefix, divider);

		if (fileSize == 0) {
			setPercent(100);
		} else {
			setPercent((int) (100 * progress / fileSize));
		}
	}

}

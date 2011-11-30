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

	public FileProgress(Context context) {
		this(context, 0);
	}

	public FileProgress(Context context, long fileSize) {
		view = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.file_progress_view, null);

		textLeft = (TextView) view.findViewById(R.id.textLeft_file_progress);
		textRight = (TextView) view.findViewById(R.id.textRight_file_progress);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		setFileSize(fileSize);
	}

	public View getView() {
		return view;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
		setProgress(0);
	}

	private void setPercent(int percent) {
		textLeft.setText(percent + "%");
		progressBar.setProgress(percent);
	}

	private void setTransferText(long progress, Character prefix, float divider) {
		textRight.setText(String.format("%.2f", progress / divider) + " / "
				+ String.format("%.2f", fileSize / divider) + " "
				+ (prefix == null ? "" : prefix) + "B");
	}

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

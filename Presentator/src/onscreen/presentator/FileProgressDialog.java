package onscreen.presentator;

import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FileProgressDialog extends Dialog {
	private TextView textLeft, textRight;
	private ProgressBar progressBar;
	private long fileSize = 0;

	public FileProgressDialog(Context context, long fileSize) {
		super(context);
		this.fileSize = fileSize;

		setContentView(R.layout.file_progress_dialog);
		setTitle(R.string.file_progress_dialog);

		textLeft = (TextView) findViewById(R.id.textLeft);
		textRight = (TextView) findViewById(R.id.textRight);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

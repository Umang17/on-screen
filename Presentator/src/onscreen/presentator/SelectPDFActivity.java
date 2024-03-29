package onscreen.presentator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import onscreen.presentator.nfc.ConcreteHandleTagDiscover;
import onscreen.presentator.nfc.HandleTagDiscoverWithBlock;
import onscreen.presentator.nfc.ReadNfcTag;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectPDFActivity extends ListActivity {
	private final ArrayList<PdfFile> allPDFs = new ArrayList<PdfFile>();
	private ReadNfcTag readNfcTag;
	private static final String TAG = "SelectPDF";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Bind the PDF array to the list
		setListAdapter(new ArrayAdapter<PdfFile>(this, R.layout.pdf_list_item,
				allPDFs));

		// Load all PDFs on the phone.
		loadPDFs();

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Get the selected file
				PdfFile file = allPDFs.get(position);

				String path = file.getPath();
				Log.d(TAG, "Selected: " + path);

				// Place the selected file path in a new Intent
				Intent data = new Intent();
				data.putExtra("File", path);
				setResult(RESULT_OK, data);
				finish();
			}
		});

		// Using nfc but only for ignoring calls and get no popup about nfc
		// discovered.
		ConcreteHandleTagDiscover concreteHandler = new ConcreteHandleTagDiscover();
		HandleTagDiscoverWithBlock handleTagIDDiscoverWithBlock = new HandleTagDiscoverWithBlock(
				concreteHandler);

		readNfcTag = new ReadNfcTag(handleTagIDDiscoverWithBlock);
		readNfcTag.onCreate(this);
	}

	@Override
	protected void onPause() {
		readNfcTag.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		readNfcTag.onResume(getIntent());
	}

	/**
	 * Adds all PDF files on the phone to the list.
	 */
	private void loadPDFs() {
		allPDFs.clear();

		String storageState = Environment.getExternalStorageState();

		// Make sure that the storage is mounted.
		if (storageState.equals(Environment.MEDIA_MOUNTED)
				|| storageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {

			File fileDir = Environment.getExternalStorageDirectory();

			// Add all PDFs
			addPDFsInside(fileDir, allPDFs);
			// Sort the list
			Collections.sort(allPDFs);

			if (allPDFs.isEmpty()) {
				showAlertDialog(R.string.pdf_none_found);
				Log.d(TAG, "Empty");
			}
		} else {
			showAlertDialog(R.string.pdf_storage_failed);
			Log.w(TAG, storageState);
		}
	}

	/**
	 * Adds all the PDF files in the fileDir and all sub items that is readable
	 * to the result.
	 * 
	 * @param fileDir
	 *            Folder to start the search.
	 * @param result
	 *            The list to append PDF files to.
	 */
	private void addPDFsInside(File fileDir, ArrayList<PdfFile> result) {
		if (fileDir.canRead()) {
			for (File currentFile : fileDir.listFiles()) {
				if (currentFile.isDirectory()) {
					addPDFsInside(currentFile, result);
				} else {
					// Match the file ending to PDF
					if (currentFile.getName().toLowerCase().endsWith(".pdf")) {
						result.add(new PdfFile(currentFile));
					}
				}
			}
		}
	}

	/**
	 * Creates a new AlertDialog and shows it, the PDFActivity will finish with
	 * RESULT_CANCELED once the OK-button is pressed.
	 * 
	 * @param messageId
	 *            The message to place in the dialog.
	 */
	private void showAlertDialog(int messageId) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(messageId);
		alertbox.setNeutralButton(R.string.pdf_ok_text, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
			}
		});
		alertbox.show();
	}

	/**
	 * A class that wraps the File class to provide customized toString and also
	 * to make it comparable for sorting.
	 */
	private class PdfFile implements Comparable<PdfFile> {
		private File file;

		public PdfFile(File file) {
			this.file = file;
		}

		public String getPath() {
			return file.getAbsolutePath();
		}

		@Override
		public String toString() {
			return file.getName();
		}

		public int compareTo(PdfFile another) {
			return toString().compareToIgnoreCase(another.toString());
		}
	}
}
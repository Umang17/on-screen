package onscreen.presentator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ListActivity;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ArrayList<PdfFile> allPDFs = getPDFs();

		setListAdapter(new ArrayAdapter<PdfFile>(this, R.layout.pdf_list_item,
				allPDFs));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PdfFile file = allPDFs.get(position);
				Log.d("SelectPDF", "Selected: " + file.getPath());

				Intent data = new Intent();
				data.putExtra("File", file.getPath());
				setResult(RESULT_OK, data);
				finish();
			}
		});
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
	 * Scans the phone for PDF files. Also sorts the PDFs by name.
	 * 
	 * @return An array list with all the PDFs.
	 */
	private ArrayList<PdfFile> getPDFs() {
		File fileDir = Environment.getExternalStorageDirectory();
		ArrayList<PdfFile> result = new ArrayList<PdfFile>();

		addPDFsInside(fileDir, result);
		Collections.sort(result);

		return result;
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
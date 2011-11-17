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
	private final ArrayList<PdfFile> allPDFs = new ArrayList<PdfFile>();

	private void listPDFs() {
		allPDFs.clear();
		File fileDir = Environment.getExternalStorageDirectory();

		addPDFsInside(fileDir, allPDFs);
		Collections.sort(allPDFs);
		
		Log.d("SD", fileDir.toString());
		
		if(allPDFs.isEmpty()) {
			Log.d("SelectPDF", "Empty");
		}

		// allPDFs.add(0, new PdfFile("Refresh"));

		setListAdapter(new ArrayAdapter<PdfFile>(this, R.layout.pdf_list_item,
				allPDFs));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listPDFs();

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PdfFile file = allPDFs.get(position);

				try {
					String path = file.getPath();
					Log.d("SelectPDF", "Selected: " + path);
					Intent data = new Intent();
					data.putExtra("File", file.getPath());
					setResult(RESULT_OK, data);
					finish();
				} catch (IllegalArgumentException e) {
					listPDFs();
					Log.d("SelectPDF", "Refresh");
				}

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
			Log.d("da", fileDir.toString());
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
	 * A class that wraps the File class to provide customized toString and also
	 * to make it comparable for sorting.
	 */
	private class PdfFile implements Comparable<PdfFile> {
		private File file;
		private String name;

		public PdfFile(File file) {
			this.file = file;
			this.name = file.getName();
		}

		public PdfFile(String name) {
			this.file = null;
			this.name = name;
		}

		public String getPath() throws IllegalArgumentException {
			if (file == null) {
				throw new IllegalArgumentException("Refresh");
			} else {
				return file.getAbsolutePath();
			}
		}

		@Override
		public String toString() {
			return name;
		}

		public int compareTo(PdfFile another) {
			return toString().compareToIgnoreCase(another.toString());
		}
	}
}
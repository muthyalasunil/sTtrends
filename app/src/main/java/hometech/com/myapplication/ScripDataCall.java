package hometech.com.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ScripDataCall extends	AsyncTask<File, Integer, String[]> {

	private static final String TAG = "ScripDataCall";

	private LongRunningIOListener mListener;
	private RestServiceHelper mRestHandler;
	private String mScrip;
	private float closePrice;

	public ScripDataCall(String scrip, Float closePrice, LongRunningIOListener listener) {
		this.mScrip=scrip;
		this.closePrice = closePrice;
		this.mListener = listener;
	}
	@Override
	protected String[] doInBackground(File... params) {

		String[] values = null;
		File scripFile = params[0];
			try {
				Log.d(TAG, scripFile.getName());
				if (scripFile.length() < 1 ) {
					int length = RestServiceHelper.downloadScripData(scripFile, mScrip);
					Log.e(TAG, "scrip data length:" + length);
				}
				values = Utils.computeScripAverages(scripFile, closePrice);

			} catch (Exception e) {
				publishProgress(-1);
				Log.e(TAG, "Error in background call:" + e.getMessage());
			}
		return values;
	}

	@Override
	protected void onPostExecute(String[] results) {
		mListener.onPostScripExecute(results);
		super.onPostExecute(results);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onProgressUpdate(values[0]);
		super.onProgressUpdate(values);
	}


}
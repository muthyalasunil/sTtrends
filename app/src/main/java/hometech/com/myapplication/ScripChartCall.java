package hometech.com.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.List;

public class ScripChartCall extends	AsyncTask<String, Integer, List> {

	private static final String TAG = "ScripChartCall";

	private LongRunningIOListener mListener;
	private RestServiceHelper mRestHandler;

	public ScripChartCall(LongRunningIOListener listener) {
		this.mListener = listener;
	}

	@Override
	protected List doInBackground(String... params) {

		List<float[]> data = null;
		String scrip = params[0];
			try {
				Log.d(TAG, scrip);
				if (scrip.length() > 1 ) {
					data = RestServiceHelper.downloadIntraDay(scrip);
					Log.e(TAG, "intra data length:" + data.get(0).length);
				}

			} catch (Exception e) {
				publishProgress(-1);
				Log.e(TAG, "Error in background call:" + e.getMessage());
			}
		return data;
	}

	@Override
	protected void onPostExecute(List results) {
		mListener.onPostExecute(results);
		super.onPostExecute(results);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onProgressUpdate(values[0]);
		super.onProgressUpdate(values);
	}


}
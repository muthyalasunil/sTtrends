package hometech.com.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LatestPriceCall extends AsyncTask<String, Integer, List> {

	private static final String TAG = "LatestPriceCall";

	private LongRunningIOListener mListener;
	private RestServiceHelper mRestHandler;

	public LatestPriceCall(LongRunningIOListener listener) {
		this.mListener = listener;
	}


	@Override
	protected List doInBackground(String... params) {

		List results = new ArrayList();
		String scripList = params[0];
			try {
				publishProgress(5);
				int status = 5;
				Log.d(TAG, scripList);
				if (scripList.length() > 1 ) {
					Map resultsMap = RestServiceHelper.getLatestPrices(scripList);
					Log.e(TAG, "results length:" + results.size());
					results.addAll(resultsMap.values());
				}
				publishProgress(100);

			} catch (Exception e) {
				publishProgress(-1);
				Log.e(TAG, "Error in background call:" + e.getMessage());
			}

		return results;
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
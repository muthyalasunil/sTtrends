package hometech.com.myapplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class ScripsRefreshCall extends	AsyncTask<File, Integer, List<String[]>> {

	private static final String TAG = "ScripsRefreshCall";

	private LongRunningIOListener mListener;
	private RestServiceHelper mRestHandler;

	public ScripsRefreshCall(LongRunningIOListener listener) {
		this.mListener = listener;
	}


	@Override
	protected List<String[]> doInBackground(File... params) {

		List<String[]> list = new ArrayList<String[]>();
		File scripFile = params[0];
			try {
				publishProgress(5);
				int status = 5;
				Log.d(TAG, scripFile.getName());
				if (scripFile.length() < 1 ) {
					RestServiceHelper.downloadScrips(scripFile);
					Log.e(TAG, "file length:" + scripFile.length());
					publishProgress(50);

				}else {
					list = Utils.readCSVValues(scripFile);
					Log.e(TAG, "list size:" + list.size());
					//12-Apr-2016
					String tradeDate = null;
					try {
						tradeDate = list.get(1)[2];
					} catch (Exception e) {
						java.util.Date now = java.util.Calendar.getInstance().getTime();
						java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy");
						if ( !df.format(now).equalsIgnoreCase(tradeDate) ){
							RestServiceHelper.downloadScrips(scripFile);
							Log.e(TAG, "file length:" + scripFile.length());
						}
						publishProgress(50);
					}
				}

				list = Utils.readCSVValues(scripFile);
				Log.e(TAG, "list size:" + list.size());
				publishProgress(100);

			} catch (Exception e) {
				publishProgress(-1);
				Log.e(TAG, "Error in background call:" + e.getMessage());
			}

		return list;
	}

	@Override
	protected void onPostExecute(List<String[]> results) {
		mListener.onPostExecute(results);
		super.onPostExecute(results);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onProgressUpdate(values[0]);
		super.onProgressUpdate(values);
	}

}
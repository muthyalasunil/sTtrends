package hometech.com.myapplication;

import java.util.List;
import java.util.Map;

// The container Activity must implement this interface so the frag can
// deliver messages
public interface LongRunningIOListener {
	/** Called by classes using a bgrnd process */
	public void onPostExecute(List results);
	public void onProgressUpdate(int status);
	public void onPostScripExecute(String[] results);
}
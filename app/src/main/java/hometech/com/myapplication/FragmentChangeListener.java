package hometech.com.myapplication;

import java.util.List;
import java.util.Map;

// The container Activity must implement this interface so the frag can
// deliver messages
public interface FragmentChangeListener {
	/** Called by classes using a bgrnd process */
	public void onPageChangeRequest(int pageId, Map info);
	public void onScripDataUpdate(List<String[]> results);
}
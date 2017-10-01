package hometech.com.myapplication;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

public class ScripListFragment extends Fragment  {

	private static final String TAG = "ScripListFragment";
	private AutoCompleteTextView scripSelector;

	private ListView myListView;
	private LazyAdapter lzAdapter;
	private LayoutInflater inflater;
	private View rootView;
	private FragmentChangeListener fragmentChangeListener;
	private ProgressDialog progress;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_scrip_list, container, false);
		this.inflater = inflater;
		Log.d(TAG, "onCreateView");
		super.onCreate(savedInstanceState);

		scripSelector = (AutoCompleteTextView) rootView.findViewById(R.id.scripSelector);
		scripSelector.setThreshold(3);
		scripSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				String scrip = ((String) arg0.getItemAtPosition(arg2));
				SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
				String scriplist = settings.getString("scripList",null);
				if (scriplist!=null && scriplist.indexOf(scrip)<0)
					scriplist += ",".concat(scrip);
				else  if (scriplist==null )
					scriplist = scrip;
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("scripList", scriplist);
				editor.apply();
				Snackbar.make(arg1, "Saved scrip to watch list", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
				lzAdapter.updateItems(scriplist);
				scripSelector.setText("");
			}
		});

		myListView = (ListView) rootView.findViewById(R.id.watchListView);
		myListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				Map info = new HashMap();
				info.put("scrip", ScripListFragment.this.lzAdapter.getItem(position).scrip);
				fragmentChangeListener.onPageChangeRequest(1, info);

			}
		});

		java.util.Date now = java.util.Calendar.getInstance().getTime();
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("ddMMMyyyy");
		final String filename = "scrips_" + df.format(now);

		progress = new ProgressDialog(rootView.getContext());
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMessage("Fetching scrips... ");
		progress.show();

		final FileCache fileCache = new FileCache(getContext());
		File scripFile = fileCache.getFile(filename);
		ScripsRefreshCall task = new ScripsRefreshCall(new LongRunningIOListener() {
			@Override
			public void onPostExecute(List results) {

				SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);

				if (results.size()>0){

					SharedPreferences.Editor editor = settings.edit();
					editor.putString("scripFile", filename);
					editor.apply();

				}else{
					String oldScripFile = settings.getString("scripFile", "");
					if ( oldScripFile.length() > 1 ){
						try {
							results = Utils.readCSVValues(fileCache.getFile(filename));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}

				setScripData(results);
				String scriplist = settings.getString("scripList", "");
				lzAdapter = new LazyAdapter(getContext(), ScripListFragment.this.inflater, results);
				lzAdapter.updateItems(scriplist);
				myListView.setAdapter(lzAdapter);
				fragmentChangeListener.onScripDataUpdate(results);

				progress.dismiss();
			}

			@Override
			public void onProgressUpdate(int status) {

			}

			@Override
			public void onPostScripExecute(String[] results) {

			}
		});
		task.execute(scripFile);
		setHasOptionsMenu(true);

		return rootView;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.removeItem(R.id.action_sort);
		inflater.inflate(R.menu.list_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public void setScripData(List<String[]> results) {
		String[] scrips = new String[results.size()];
		int index = 0;
		for (Object strArray : results) {
			scrips[index++] = ((String[])strArray)[0];
		}
		ArrayAdapter<String> scripAdapter = new ArrayAdapter<String>(rootView.getContext(),
				android.R.layout.simple_dropdown_item_1line, scrips);
		scripSelector.setAdapter(scripAdapter);
	}

	public void setFragmentChangeListener(FragmentChangeListener fListener){
		this.fragmentChangeListener = fListener;
	}

	public void sort(){
		this.lzAdapter.sort();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if ( lzAdapter!=null && isVisibleToUser  ){
			SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
			String scriplist = settings.getString("scripList", null);
			if ( scriplist != null ) {
				lzAdapter.updateItems(scriplist);
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

}
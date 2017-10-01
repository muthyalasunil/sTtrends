package hometech.com.myapplication;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter  {

	private LatestScripInfo[] scrips;
	private List<String[]> mScripData;
	private Map latestPrices = new HashMap();

	private LayoutInflater inflater = null;
	private Context ctx;
	private static final String TAG = "LazyAdapter";

	public class LatestScripInfo  {
		public String scrip;
		public float price;
	}

	public LazyAdapter(){}

	public LazyAdapter(Context ctx, LayoutInflater iinflater, List scripData) {

		this.ctx = ctx;
		this.mScripData = scripData;
		this.inflater = iinflater;
	}

	public void updateItems(String scriplist){

		String[] lscrips = scriplist.split(",");
		this.scrips = new LatestScripInfo[lscrips.length];
		int i = 0;
		for(String scrip: lscrips) {
			scrips[i]=new LatestScripInfo();
			scrips[i++].scrip = scrip;
		}

		LatestPriceCall priceCall = new LatestPriceCall(new LongRunningIOListener() {
			@Override
			public void onPostExecute(List results) {
				Log.d(TAG, "results:" + results.size());
				int i = 0;
				for (Object strCsv : results)
				{
					String[] csvValues = strCsv.toString().split(",");
					float fltPClose = Float.parseFloat(csvValues[9]);
					float fltClose = Float.parseFloat(csvValues[2]);
					scrips[i].scrip = csvValues[0];
					scrips[i++].price = (fltClose - fltPClose) * 100 / fltPClose;
					latestPrices.put(((String)strCsv).split(",")[0],strCsv);
				}
				notifyDataSetChanged();
			}

			@Override
			public void onProgressUpdate(int status) {}

			@Override
			public void onPostScripExecute(String[] results) {}
		});
		priceCall.execute(scriplist);

	}

	private boolean ascending=false;

	public void sort(){

		Collections.sort(Arrays.asList(scrips), new Comparator<LatestScripInfo>() {
			@Override
			public int compare(LatestScripInfo lhs, LatestScripInfo rhs) {
				if ( ascending )
					return (lhs.price > rhs.price ? -1 : 1);
				else
					return (lhs.price > rhs.price ? 1 : -1);
			}
		});
		ascending = !ascending;
		notifyDataSetChanged();
	}

	public int getCount() {
		return scrips.length;
	}

	public LatestScripInfo getItem(int position) {return scrips[position];}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		final View	rootView = inflater.inflate(R.layout.scrip_list_row, null);

		TextView txt = (TextView) rootView.findViewById(R.id.txtScripName);
		txt.setText(scrips[position].scrip);
		String[] csvValues =  new String[10];

		if ( latestPrices != null && latestPrices.size() > 0 ) {

			String csvValuesStr = (String) latestPrices.get(scrips[position].scrip);
			csvValues = csvValuesStr.split(",");
			//name+","+ltime+","+price+","+open+","+hi+","+lo+","+hi52+","+lo52+","+vo+","+pcls
 		} else {
			// 2,PREV_CLOSE,OPEN_PRICE,HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY
			// TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER
			for (String[] values : mScripData) {
				if (values[0].equalsIgnoreCase(scrips[position].scrip) ) {
					csvValues[1] = values[2];
					csvValues[2] = values[7];
					csvValues[3] = values[4];
					csvValues[4] = values[5];
					csvValues[5] = values[6];
					csvValues[6] = "";
					csvValues[7] = "";
					csvValues[8] = values[10];
					csvValues[9] = values[3];
					break;
				}
			}

		}

			txt = (TextView) rootView.findViewById(R.id.txtClose);
			txt.setText(csvValues[2]);
			txt = (TextView) rootView.findViewById(R.id.txtDate);
			txt.setText(csvValues[1]);

			txt = (TextView) rootView.findViewById(R.id.txtHigh);
			txt.setText(csvValues[4]);
			txt = (TextView) rootView.findViewById(R.id.txtLow);
			txt.setText(csvValues[5]);
			txt = (TextView) rootView.findViewById(R.id.txtHigh52);
			txt.setText(csvValues[6]);
			txt = (TextView) rootView.findViewById(R.id.txtLow52);
			txt.setText(csvValues[7]);
			txt = (TextView) rootView.findViewById(R.id.txtVolume);
			try {
				float vol = 0;
				vol = Long.parseLong(csvValues[8].trim());
				if ( vol > 1000000 )
					txt.setText(String.valueOf(vol/1000000).substring(0,4)+"MM");
				else
					txt.setText(csvValues[8]);
			} catch (NumberFormatException e) {
				txt.setText(csvValues[8]);
			}
			float fltPClose = Float.parseFloat(csvValues[9]);
			float fltClose = Float.parseFloat(csvValues[2]);
			scrips[position].price = (fltClose - fltPClose) * 100 / fltPClose;
			txt = (TextView) rootView.findViewById(R.id.txtChange);
			if (scrips[position].price > 0) {
				txt.setTextColor(Color.rgb(0, 200, 0));
				txt.setText(String.format("+%.2f", scrips[position].price));
			} else {
				txt.setTextColor(Color.rgb(200, 0, 0));
				txt.setText(String.format("-%.2f", scrips[position].price));
			}

		return rootView;
	}


}
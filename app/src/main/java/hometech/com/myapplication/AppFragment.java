package hometech.com.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view. This fragment
 * would include your content.
 */
/*
public class AppFragment extends Fragment {

    public AppFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.scrip_detail, container, false);
        return rootView;
    }
}*/
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppFragment extends Fragment implements LongRunningIOListener {

    private static final String TAG = "AppFragment";
    private LayoutInflater inflater;
    private ProgressDialog progress;
    private AutoCompleteTextView scripSelector;
    private View rootView;

    private List<String[]> mScripData;
    private List<String[]> mScripNames;

    public AppFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.search_scrip_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.scrip_detail, container, false);
        this.inflater = inflater;
        Log.d(TAG, "onCreateView");
        super.onCreate(savedInstanceState);

        scripSelector = (AutoCompleteTextView) rootView.findViewById(R.id.scripSelector);
        scripSelector.setThreshold(3);
        scripSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                updateScripDetails((String) arg0.getItemAtPosition(arg2));
            }
        });
        displayHeaders(rootView);

        progress = new ProgressDialog(rootView.getContext());
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
        progress.setMessage("Fetching scrips... ");

        java.util.Date now = java.util.Calendar.getInstance().getTime();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("ddMMMyyyy");
        String filename = "scrips_" + df.format(now);

        FileCache fileCache = new FileCache(rootView.getContext());
        File scripFile = fileCache.getFile(filename);
        mScripNames = Utils.readRawCSVFile(rootView.getContext(), R.raw.nsescrips);

        ScripsRefreshCall task = new ScripsRefreshCall(this);
        task.execute(scripFile);

        SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
        Set<String> scriplist = settings.getStringSet("scripList", new HashSet());
        Log.d(TAG, "Scrips:" + scriplist.size());

        return rootView;

    }


    @Override
    public void onPostExecute(List results) {

        mScripData = results;
        String[] scrips = new String[results.size()];
        int index = 0;
        for (String[] row : mScripData) {
            scrips[index++] = row[0];
        }
        ArrayAdapter<String> scripAdapter = new ArrayAdapter<String>(rootView.getContext(),
                android.R.layout.simple_dropdown_item_1line, scrips);
        scripSelector.setAdapter(scripAdapter);

        progress.dismiss();
    }

    @Override
    public void onProgressUpdate(int status) {
        Log.d(TAG, "status:" + status);
        if (status > 0) {
            progress.setProgress(status);
            if (status == 100) {
                progress.dismiss();
            }
        } else {
            Toast.makeText(this.getContext(),
                    "Error searching for scrips", Toast.LENGTH_SHORT).show();
        }

    }


    private void updateScripDetails(String scrip) {

        int pos = 0;
        // 2,PREV_CLOSE,OPEN_PRICE,HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY
        // TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER
        for (String[] values : mScripData) {
            if (values[0] == scrip) {
                break;
            }
            pos++;
        }
        TextView txt = (TextView) rootView.findViewById(R.id.txtClose);
        txt.setText(mScripData.get(pos)[8]);
        txt = (TextView) rootView.findViewById(R.id.txtDate);
        txt.setText(mScripData.get(pos)[2]);

        float fltOpen = Float.parseFloat(mScripData.get(pos)[4]);
        float fltClose = Float.parseFloat(mScripData.get(pos)[8]);
        float percChange = (fltClose - fltOpen) * 100 / fltOpen;
        txt = (TextView) rootView.findViewById(R.id.txtChange);
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }
        txt = (TextView) rootView.findViewById(R.id.txtOpen);
        txt.setText(mScripData.get(pos)[4]);
        txt = (TextView) rootView.findViewById(R.id.txtHigh);
        txt.setText(mScripData.get(pos)[5]);
        txt = (TextView) rootView.findViewById(R.id.txtLow);
        txt.setText(mScripData.get(pos)[6]);
        txt = (TextView) rootView.findViewById(R.id.txtAvg);
        txt.setText(mScripData.get(pos)[9]);
        txt = (TextView) rootView.findViewById(R.id.txtVolume);
        txt.setText(mScripData.get(pos)[10]);
        txt = (TextView) rootView.findViewById(R.id.txtNumTrades);
        txt.setText(mScripData.get(pos)[12]);
        txt = (TextView) rootView.findViewById(R.id.txtDelivery);
        txt.setText(mScripData.get(pos)[14]);

        txt = (TextView) rootView.findViewById(R.id.txtScripName);
        txt.setText("");

        for (String[] values : mScripNames) {
            if (scrip.equalsIgnoreCase(values[0])) {
                txt.setText(values[1]);
                break;
            }
        }

        java.util.Date now = java.util.Calendar.getInstance().getTime();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy");
        String filename = scrip + "_" + df.format(now);

        FileCache fileCache = new FileCache(getContext());
        File scripFile = fileCache.getFile(filename);
        if (scripFile.length() > 1) {
            String[] values = Utils.computeScripAverages(scripFile);
            onPostScripExecute(values);
        } else {
            ScripDataCall scripDataCall = new ScripDataCall(scrip, this);
            scripDataCall.execute(scripFile);
        }

    }

    @Override
    public void onPostScripExecute(String[] values) {

        TextView txtclose = (TextView) rootView.findViewById(R.id.txtClose);
        float fltClose = Float.parseFloat((String) txtclose.getText());

        TextView txt = (TextView) rootView.findViewById(R.id.txt200);
        txt.setText(values[0]);
        float fltvalue = Float.parseFloat(values[0]);
        txt = (TextView) rootView.findViewById(R.id.txt200Perc);
        float percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }

        txt = (TextView) rootView.findViewById(R.id.txt50);
        txt.setText(values[1]);
        fltvalue = Float.parseFloat(values[1]);
        txt = (TextView) rootView.findViewById(R.id.txt50Perc);
        percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }

        txt = (TextView) rootView.findViewById(R.id.txt15);
        txt.setText(values[2]);
        fltvalue = Float.parseFloat(values[2]);
        txt = (TextView) rootView.findViewById(R.id.txt15Perc);
        percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }

        txt = (TextView) rootView.findViewById(R.id.txtBBh);
        txt.setText(values[3]);
        fltvalue = Float.parseFloat(values[3]);
        txt = (TextView) rootView.findViewById(R.id.txtBBhp);
        percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }

        txt = (TextView) rootView.findViewById(R.id.txtBBl);
        txt.setText(values[4]);
        fltvalue = Float.parseFloat(values[4]);
        txt = (TextView) rootView.findViewById(R.id.txtBBlp);
        percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }

        txt = (TextView) rootView.findViewById(R.id.txtSMA20);
        txt.setText(values[5]);
        fltvalue = Float.parseFloat(values[5]);
        txt = (TextView) rootView.findViewById(R.id.txtSMA20p);
        percChange = (fltClose - fltvalue) * 100 / fltClose;
        if (percChange > 0) {
            txt.setTextColor(Color.rgb(0, 200, 0));
            txt.setText(String.format("+%.2f", percChange));
        } else {
            txt.setTextColor(Color.rgb(200, 0, 0));
            txt.setText(String.format("-%.2f", percChange));
        }
    }

    private void displayHeaders(View rootView) {

        TextView txt = (TextView) rootView.findViewById(R.id.lblAvg);
        txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt = (TextView) rootView.findViewById(R.id.lblHigh);
        txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt = (TextView) rootView.findViewById(R.id.lblLow);
        txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt = (TextView) rootView.findViewById(R.id.lblOpen);
        txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    }

    /*
     * @Override protected void onResume() { int resultCode =
     * GooglePlayServicesUtil
     * .isGooglePlayServicesAvailable(getApplicationContext()); if (resultCode
     * != ConnectionResult.SUCCESS) { if (resultCode ==
     * ConnectionResult.SERVICE_MISSING || resultCode ==
     * ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED || resultCode ==
     * ConnectionResult.SERVICE_DISABLED) { Dialog dialog =
     * GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
     * dialog.show(); }} super.onResume(); }
     */


}

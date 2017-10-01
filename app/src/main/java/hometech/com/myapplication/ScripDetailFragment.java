package hometech.com.myapplication;

import android.content.pm.ActivityInfo;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScripDetailFragment extends Fragment implements LongRunningIOListener {

    private static final String TAG = "ScripDetailFragment";
    private LayoutInflater inflater;
    private ProgressDialog progress;
    private View rootView;
    private FragmentChangeListener fragmentChangeListener;

    private ChartPlotter plotMacd, plotBBands;

    private List<String[]> mScripData;
    private List<String[]> mScripNames;

    private String scripName;

    public void setScripData(List<String[]> scripData) {
        this.mScripData = scripData;
    }


    public void setFragmentChangeListener(FragmentChangeListener fListener) {
        this.fragmentChangeListener = fListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_scrip, container, false);
        this.inflater = inflater;
        // initialize our XYPlot reference:
        plotMacd = new ChartPlotter((XYPlot)rootView.findViewById(R.id.dynamicXYPlot)) ;
        plotBBands = new ChartPlotter((XYPlot) rootView.findViewById(R.id.dynamicXYPlot1));

        super.onCreate(savedInstanceState);
        mScripNames = Utils.readRawCSVFile(rootView.getContext(), R.raw.nsescrips);
        setHasOptionsMenu(true);

        return rootView;

    }


    @Override
    public void onPostExecute(List results) {
    }

    @Override
    public void onProgressUpdate(int status) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_delete) {
            SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
            String scriplist = settings.getString("scripList",null);
            if (scriplist!=null && scriplist.indexOf(scripName)>-1) {
                scriplist = scriplist.replace(scripName+",", "");
                scriplist = scriplist.replace(","+scripName, "");
            }
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("scripList", scriplist);
            editor.apply();
            fragmentChangeListener.onPageChangeRequest(0, null);

            Snackbar.make(rootView, "Removed scrip from watch list", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.action_sort);
        inflater.inflate(R.menu.detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
            String scrip = settings.getString(Utils.CURR_SCRIP, null);
            if (scrip != null) {
                updateScripDetails(scrip);
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void updateScripDetails(final String scrip) {

        scripName = scrip;
        int pos = 0;
        // 2,PREV_CLOSE,OPEN_PRICE,HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY
        // TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER
        for (String[] values : mScripData) {
            if (values[0].equalsIgnoreCase(scrip)) {
                break;
            }
            pos++;
        }

        TextView txt = (TextView) rootView.findViewById(R.id.txtAverage);
        txt.setText(mScripData.get(pos)[9]);
        txt = (TextView) rootView.findViewById(R.id.txtNumTrades);
        txt.setText(mScripData.get(pos)[12]);
        txt = (TextView) rootView.findViewById(R.id.txtDelivery);
        txt.setText(mScripData.get(pos)[14]);

        txt = (TextView) rootView.findViewById(R.id.txtScripTitle);
        txt.setText("");

        for (String[] values : mScripNames) {
            if (scrip.equalsIgnoreCase(values[0])) {
                txt.setText(values[1]);
                break;
            }
        }


        final int index = pos;
        LatestPriceCall priceCall = new LatestPriceCall(new LongRunningIOListener() {

            @Override
            public void onPostExecute(List results) {

                float fltClose = 0.0f;
                String[] csvValues = new String[10];
                if ( results != null && results.size() > 0 ) {

                    Log.d(TAG, "LatestPriceCall:" + results.size());
                    String csvValuesStr = (String) results.get(0);
                    csvValues = csvValuesStr.split(",");
                    //name+","+ltime+","+price+","+open+","+hi+","+lo+","+hi52+","+lo52+","+vo+","+pcls
                } else {
                    // 2,PREV_CLOSE,OPEN_PRICE,HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY
                    // TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER
                    for (String[] values : mScripData) {
                        if (values[0].equalsIgnoreCase(scripName) ) {
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

                    TextView txt = (TextView) rootView.findViewById(R.id.txtClose);
                    txt.setText(csvValues[2]);
                    txt = (TextView) rootView.findViewById(R.id.txtDate);
                    txt.setText(csvValues[1]);

                    txt = (TextView) rootView.findViewById(R.id.txtHigh);
                    txt.setText(csvValues[4]);
                    txt = (TextView) rootView.findViewById(R.id.txtLow);
                    txt.setText(csvValues[5]);
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
                    txt = (TextView) rootView.findViewById(R.id.txtPClose);
                    txt.setText(csvValues[9]);
                    txt = (TextView) rootView.findViewById(R.id.txtOpen);
                    txt.setText(csvValues[3]);

                    float fltPClose = Float.parseFloat(csvValues[9]);
                    fltClose = Float.parseFloat(csvValues[2]);
                    float chgprice = (fltClose - fltPClose) * 100 / fltPClose;

                    txt = (TextView) rootView.findViewById(R.id.txtChange);
                    if (chgprice > 0) {
                        txt.setTextColor(Color.rgb(0, 200, 0));
                        txt.setText(String.format("+%.2f", chgprice));
                    } else {
                        txt.setTextColor(Color.rgb(200, 0, 0));
                        txt.setText(String.format("-%.2f", chgprice));
                    }


                java.util.Date now = java.util.Calendar.getInstance().getTime();
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy");
                String filename = scrip + "_" + df.format(now);

                FileCache fileCache = new FileCache(getContext());
                final File scripFile = fileCache.getFile(filename);
                ScripDataCall scripDataCall = new ScripDataCall(scrip, fltClose, ScripDetailFragment.this);
                scripDataCall.execute(scripFile);

            }

            @Override
            public void onProgressUpdate(int status) {
            }

            @Override
            public void onPostScripExecute(String[] results) {
            }
        });
        priceCall.execute(scrip);
/*

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map info = new HashMap();
                info.put("scrip", scrip);
                fragmentChangeListener.onPageChangeRequest(2, info);
            }
        });
*/

    }

    @Override
    public void onPostScripExecute(String[] values) {

        java.util.Date now = java.util.Calendar.getInstance().getTime();
        String filename = scripName + "_" + df.format(now);
        SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy");

        TextView txtclose = (TextView) rootView.findViewById(R.id.txtClose);
        float fltClose = Float.parseFloat((String) txtclose.getText());
        if (values == null || values.length == 0){

            FileCache fileCache = new FileCache(getContext());
            filename = settings.getString(scripName, "");
            if ( filename.length() > 1 ) {
                final File scripFile = fileCache.getFile(filename);
                values = Utils.computeScripAverages(scripFile, fltClose);
            }
            if (values == null || values.length == 0)
                return;

        }else{
            filename = scripName + "_" + df.format(now);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(scripName, filename);
            editor.apply();
        }


        try {

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

            txt = (TextView) rootView.findViewById(R.id.txtMACD);
            txt.setText(values[6]);
            txt = (TextView) rootView.findViewById(R.id.txtSignal);
            txt.setText(values[7]);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        FileCache fileCache = new FileCache(getContext());
        final File scripFile = fileCache.getFile(filename);
        updateSeriesData(scripFile);

    }

    private void updateSeriesData(final File scripFile){

        List<String> list = new ArrayList<String>();
        List<String> dlist = new ArrayList<String>();
        try {
            BufferedReader dataBR = new BufferedReader(new FileReader(scripFile));
            String line = "";

            dataBR.readLine();
            while ((line = dataBR.readLine()) != null) {
                String[] values = line.split(",");
                //Date	Open	High	Low	Close	Volume	Adj Close
                long volume = Long.parseLong(values[5]);
                if ( volume > 0 ) {
                    list.add(values[4]);
                    dlist.add(values[0]);
                }
            }

            List<float[]> rlist = Utils.calculateMACDValues(list,12, 26, 9);
            Number[] s1= new Number[rlist.get(1).length];
            Number[] s2= new Number[rlist.get(1).length];
            Number[] s5= new Number[rlist.get(1).length];

            int i = s1.length-1;
            for (float flt : rlist.get(0)) {
                s1[i--] = flt;
                if (i == 0 ) break;
            }
            i = s2.length-1;
            for (float flt : rlist.get(1)) {
                s2[i--] = flt;
                if (i == 0 ) break;
            }
            i = s5.length-1;
            for (float flt : rlist.get(2)) {
                s5[i--] = df.parse(dlist.get((int)flt)).getTime();
                if (i == 0 ) break;
            }

            // turn the above arrays into XYSeries':
            // (Y_VALS_ONLY means use the element index as the x value)
            List<Number[]> nlist = new ArrayList();
            nlist.add(s1);
            nlist.add(s2);
            plotMacd.updateSeriesData(nlist, s5, new String[]{"MACD", "Signal"}, false);


            rlist = Utils.calculateCalculateBBands(list, 50);
            Number[] s3= new Number[rlist.get(0).length];
            Number[] s4= new Number[rlist.get(2).length];
            s1= new Number[rlist.get(1).length];
            s2= new Number[rlist.get(3).length];

            i = s3.length-1;
            for (float flt : rlist.get(0)) {
                s3[i--] = flt;
                if (i == 0 ) break;
            }
            i = s4.length-1;
            for (float flt : rlist.get(2)) {
                s4[i--] = flt;
                if (i == 0 ) break;
            }
            i = s1.length-1;
            for (float flt : rlist.get(1)) {
                s1[i--] = flt;
                if (i == 0 ) break;
            }
            i = s2.length-1;
            for (float flt : rlist.get(3)) {
                s2[i--] = flt;
                if (i == 0 ) break;
            }
            s5= new Number[rlist.get(4).length];
            i = s5.length-1;
            for (float flt : rlist.get(4)) {
                s5[i--] = df.parse(dlist.get((int)flt)).getTime();
                if (i == 0 ) break;
            }

            nlist = new ArrayList();
            nlist.add(s3);
            nlist.add(s4);
            nlist.add(s1);
            nlist.add(s2);

            plotBBands.updateSeriesData(nlist, s5, new String[]{"High", "Low","Median", "Close"}, false);


        } catch (Exception e) {
            e.printStackTrace();

        }

    }
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");


}
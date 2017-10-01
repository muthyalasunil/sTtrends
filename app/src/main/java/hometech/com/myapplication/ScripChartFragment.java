package hometech.com.myapplication;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple XYPlot
 */
public class ScripChartFragment extends Fragment {

    private View rootView;
    private FragmentChangeListener fragmentChangeListener;

    private ChartPlotter pplotter, vplotter;

    public ScripChartFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize our XYPlot reference:
        setRetainInstance(true);
    }

    public void setFragmentChangeListener(FragmentChangeListener fListener){
        this.fragmentChangeListener = fListener;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            SharedPreferences settings = rootView.getContext().getSharedPreferences(Utils.PREFS_NAME, 0);
            //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            String scrip = settings.getString(Utils.CURR_SCRIP, null);
            if (scrip != null) {
                updateSeriesData(scrip);
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    static SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");

    public void updateSeriesData(String scrip){

        ScripChartCall scripCall = new ScripChartCall(new LongRunningIOListener(){

            @Override
            public void onPostExecute(List results) {

                if (results.size()==0)
                    return;

                float[] f1 = (float[])results.get(0);
                float[] f2 = (float[])results.get(1);
                float[] f3 = (float[])results.get(2);
                Number[] s1= new Number[f1.length];
                Number[] s2= new Number[f2.length];
                Number[] s5= new Number[f3.length];
                int i = 0;
                for (float flt : f1)
                    s1[i++] = flt;
                i = 0;
                for (float flt : f2)
                    s2[i++] = flt/100000;

                i = 0;
                for (float flt : f3) {
                    s5[i++] = (flt) * 1000;
                }

                List nlist = new ArrayList();
                nlist.add(s1);

                pplotter.updateSeriesData(nlist, s5, new String[]{"Price"}, true);
                nlist = new ArrayList();
                nlist.add(s2);
                vplotter.updateSeriesData(nlist, s5, new String[]{"Volume"}, true);

            }

            @Override
            public void onProgressUpdate(int status) {

            }

            @Override
            public void onPostScripExecute(String[] results) {

            }
        });
        scripCall.execute(scrip);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chart_intra, container, false);
        pplotter = new ChartPlotter((XYPlot) rootView.findViewById(R.id.pricePlot));
        vplotter = new ChartPlotter((XYPlot) rootView.findViewById(R.id.volumePlot), true);
        return rootView;
    }
}
package hometech.com.myapplication;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by sunil on 5/8/2016.
 */
public class ChartPlotter {

    private XYPlot mPlot;
    private SimpleXYSeries[] mseries;
    private boolean isBarChart;

    public ChartPlotter(XYPlot plot, boolean isBarChart){
        this.mPlot = plot;
        this.isBarChart = isBarChart;
    }

    public ChartPlotter(XYPlot plot){
        this.mPlot = plot;
        this.isBarChart = false;
    }

    final static Integer[] colors = new Integer[]{Color.rgb(0, 0, 0), Color.rgb(255, 0, 0),
            Color.rgb(0, 255, 0), Color.rgb(0, 0, 255), Color.rgb(128, 128, 128),
            Color.rgb(128, 0, 0), Color.rgb(0, 128, 0), Color.rgb(0, 0, 128),Color.rgb(255, 255, 255)};

    private void drawChart(){

        int i = 0;
        for (SimpleXYSeries s: mseries){
            if ( isBarChart ){
                // setup our line fill paint to be a slightly transparent gradient:
                Paint lineFill = new Paint();
                lineFill.setAlpha(200);
                lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.DKGRAY, Color.GRAY, Shader.TileMode.MIRROR));
                StepFormatter stepFormatter  = new StepFormatter(Color.BLUE, Color.BLUE);
                stepFormatter.getLinePaint().setStrokeWidth(1);
                stepFormatter.getLinePaint().setAntiAlias(false);
                stepFormatter.setFillPaint(lineFill);
                //stepFormatter.getFillPaint().setAlpha(220);
                mPlot.addSeries(s, stepFormatter);
            }else {
                LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                        colors[i++], null, null, null);
                formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
                formatter1.getLinePaint().setStrokeWidth(5);
                //formatter1.getFillPaint().setAlpha(220);
                mPlot.addSeries(s, formatter1);
            }
        }

        // uncomment this line to freeze the range boundaries:
        //mPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        mPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        mPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);

        // reduce the number of range labels
        mPlot.setTicksPerDomainLabel(3);
        mPlot.setTicksPerRangeLabel(4);
/*
        mPlot.getBackgroundPaint().setColor(Color.WHITE);
        mPlot.setBorderStyle(XYPlot.BorderStyle.SQUARE, null, null);
        mPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
*/
        //mPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.LTGRAY);

        // By default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mPlot.removeMarkers();

        mPlot.getBackgroundPaint().setAlpha(0);
        mPlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
        mPlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);

        mPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        mPlot.getGraphWidget().getRangeTickLabelPaint().setColor(Color.BLACK);
        mPlot.getGraphWidget().getDomainTickLabelPaint().setColor(Color.BLACK);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        //mPlot.getGraphWidget().setDomainLabelOrientation(-45);
        mPlot.getGraphWidget().setRangeLabelOrientation(-45);
        mPlot.redraw();
    }

    public void updateSeriesData(List<Number[]> seriesList, String[] label){
        updateSeriesData(seriesList, null, label, false);
    }

    public void updateSeriesData(List<Number[]> seriesList, final Number[] xList, String[] label, final boolean isRangeTime){

            if (mseries!=null)
            for (SimpleXYSeries series: mseries) {
                mPlot.removeSeries(series);
                series = null;
            }

            int i = 0;
            mseries = new SimpleXYSeries[seriesList.size()];
            for (Number[] num : seriesList) {
                // turn the above arrays into XYSeries':
                // (Y_VALS_ONLY means use the element index as the x value)
                if ( xList == null )
                    mseries[i] = new SimpleXYSeries(Arrays.asList(num),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, label[i]);
                else {
                    mseries[i] = new SimpleXYSeries(Arrays.asList(num), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, label[i]);
                    mPlot.setDomainValueFormat(new Format() {

                        // create a simple date format that draws on the year portion of our timestamp.
                        // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
                        // for a full description of SimpleDateFormat.
                       // private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");

                        @Override
                        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                            // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                            // we multiply our timestamp by 1000:
                            long timestamp = xList[((Double)obj).intValue()].longValue();
                            Date date = new Date(timestamp);
                            Log.d("Chart", tf.format(date));
                            return (isRangeTime ? tf.format(date, toAppendTo, pos) :df.format(date, toAppendTo, pos));
                        }

                        @Override
                        public Object parseObject(String source, ParsePosition pos) {
                            return null;

                        }
                    });
                }
                i++;
            }
            drawChart();

    }

    static SimpleDateFormat df = new SimpleDateFormat("mm/yy");
    static SimpleDateFormat tf = new SimpleDateFormat("HH:MM");

}

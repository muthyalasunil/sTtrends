package hometech.com.myapplication;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestServiceHelper {

    static final String TAG = "RestServiceHelper";

    static final String REST_GOOGL_QUOTE = "http://www.google.com/finance/info?infotype=infoquoteall&q=";
    //"http://finance.google.com/finance/info?client=ig&q=";
    //http://www.google.com/finance/getprices?q=RPOWER&x=NSE&i=60&p=1d&f=d,o,h,l,c,v
    static final String REST_NSE_QUOTE = "http://www.nseindia.com/live_market/dynaContent/live_watch/get_quote/ajaxGetQuoteJSON.jsp?symbol=AXISBANK";
    static final String REST_YHOO_QUOTE="http://chartapi.finance.yahoo.com/instrument/1.0/_SCRIP_.NS/chartdata;type=quote;range=1d/csv/";
    static final String REST_SERVICE_EPOINT_SCRIP = "http://real-chart.finance.yahoo.com/table.csv?s=_SCRIP_.NS&then&now&g=d&ignore=.csv";
    static final String REST_SERVICE_EPOINT_SCRIPS = "https://www.nseindia.com/products/content/sec_bhavdata_full.csv";

    private static final int BUFFER_SIZE = 4096;
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    //"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070802 SeaMonkey/1.1.4";

    public static List<float[]> downloadIntraDay(String scrip) throws Exception {

        List<float[]> datalist = new ArrayList();

        BufferedReader r  = null;
        try {
            String url = REST_YHOO_QUOTE.replace("_SCRIP_", scrip);
            Log.e(TAG, "downloading trades:"+url);

            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept","*/*");
            connection.setDoOutput(false);
            connection.connect();

            // opens an output stream to save into file
            BufferedReader bis = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line="";
            boolean isdataline = false;
            long time1 = 0;
            long time2 = 0;

            while ((line = bis.readLine()) != null) {
                if ( isdataline ){
                    //Timestamp,close,high,low,open,volume
                    //1462506301,1169.0000,1169.4500,1159.8500,1161.0000,57600
                    String[] splits = line.split(",");
                    time1 = Long.parseLong(splits[0]);
                    if ( time1 - time2 > 30){
                        datalist.add(new float[]{Float.parseFloat(splits[1]),Float.parseFloat(splits[5]),Float.parseFloat(splits[0])});
                        time2 = Long.parseLong(splits[0]);
                    }

                }else if ( line.indexOf("volume:")>-1){
                    isdataline = true;
                }
            }

            float[] s1 = new float[datalist.size()];
            float[] s2 = new float[datalist.size()];
            float[] s3 = new float[datalist.size()];
            int i = 0;
            Iterator<float[]> iter = datalist.iterator();
            while (iter.hasNext()) {
                float temp[] = iter.next();
                s1[i] = temp[0];
                s2[i] = temp[1];
                s3[i++] = temp[2];

                iter.remove();
            }

            datalist.add(s1);
            datalist.add(s2);
            datalist.add(s3);
            bis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return datalist;
    }

    public static int downloadScrips(File outFile) throws Exception {

        int scrips=-1;
            BufferedReader r  = null;
            try {
                URLConnection connection = new URL(REST_SERVICE_EPOINT_SCRIPS).openConnection();
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept","*/*");
                connection.setDoOutput(false);
                connection.connect();
                Log.e(TAG, "downloading scrips.. "+REST_SERVICE_EPOINT_SCRIPS);
                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(outFile);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                    Log.e(TAG, ". "+bytesRead);
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e(TAG, "length;"+outFile.length());


        return scrips;
    }

    public static int downloadScripData(File outFile, String scrip) throws Exception {

        Log.e(TAG, "downloading scrip data.. ");

        BufferedReader r  = null;
        try {
            //"10-01-2016-TO-08-04-2016LUPINEQN.csv";
            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar then = java.util.Calendar.getInstance();
            then.add(Calendar.YEAR, -1);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy");
            String[] values = sdf.format(then.getTime()).split("-");
            String fromdate = String.format("a=%s&b=%s&c=%s", values[0], values[1], values[2]);
            values = sdf.format(now.getTime()).split("-");
            String todate = String.format("d=%s&e=%s&f=%s", values[0], values[1], values[2]);

            String query = REST_SERVICE_EPOINT_SCRIP;
            query = query.replaceFirst("then", fromdate);
            query = query.replaceFirst("now", todate);
            query = query.replaceFirst("_SCRIP_", scrip);
            //a=05&b=17&c=2015&d=03&e=16&f=2016

            URLConnection connection = new URL(query).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept","*/*");
            connection.setDoOutput(false);
            connection.connect();
            Log.e(TAG, query);
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(outFile);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            Log.e(TAG, "csv file;"+outFile.length());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return (int)outFile.length();
    }

    //http://finance.google.com/finance/info?client=ig&q=AXISBANK,ICICIBANK
    public static Map getLatestPrices(String scripList){
    // [ { "id": "3261730" ,"t" : "AXISBANK" ,"e" : "NSE" ,"l" : "474.80" ,"l_fix" : "474.80" ,"l_cur" : "Rs.474.80" ,"s": "0" ,"ltt":"3:59PM GMT+5:30" ,"lt" : "Apr 22, 3:59PM GMT+5:30" ,"lt_dts" : "2016-04-22T15:59:06Z" ,"c" : "+6.85" ,"c_fix" : "6.85" ,"cp" : "1.46" ,"cp_fix" : "1.46" ,"ccol" : "chg" ,"pcls_fix" : "467.95" } ,
    // { "id": "16345036" ,"t" : "ICICIBANK" ,"e" : "NSE" ,"l" : "252.20" ,"l_fix" : "252.20" ,"l_cur" : "Rs.252.20" ,"s": "0" ,"ltt":"3:46PM GMT+5:30" ,"lt" : "Apr 22, 3:46PM GMT+5:30" ,"lt_dts" : "2016-04-22T15:46:47Z" ,"c" : "-0.85" ,"c_fix" : "-0.85" ,"cp" : "-0.34" ,"cp_fix" : "-0.34" ,"ccol" : "chr" ,"pcls_fix" : "253.05" } ]
//
        Map data= new HashMap();

        try {
            String parameters = "";
            for (String scrip : scripList.split(","))
                parameters += "NSE:"+scrip+",";
            String query = REST_GOOGL_QUOTE+parameters.substring(0, parameters.length()-1);
            URLConnection connection = new URL(query).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept","*/*");
            connection.setDoOutput(false);
            connection.connect();
            Log.e(TAG, query);


            String strJson = convertStreamToString(connection.getInputStream());
            //Log.e(TAG, strJson);
            JSONArray  jsonArray = new JSONArray(strJson.substring(3));
            //Get the instance of JSONArray that contains JSONObjects
            Log.e(TAG, "data:"+jsonArray.length());
            //Iterate the jsonArray and print the info of JSONObjects
            for(int i=0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //"op" : "104.10","hi" : "105.40","lo" : "101.50","vo" : "13.28M","avvo" : "","hi52" : "233.40","lo52" : "58.15"

                String name = jsonObject.optString("t").toString();
                String ltime = jsonObject.optString("lt_dts").toString();

                String price = jsonObject.optString("l").toString().replaceAll(",","");
                String open = jsonObject.optString("op").toString().replaceAll(",","");
                String hi = jsonObject.optString("hi").toString().replaceAll(",","");
                String lo = jsonObject.optString("lo").toString().replaceAll(",","");
                String hi52 = jsonObject.optString("hi52").toString().replaceAll(",","");
                String lo52 = jsonObject.optString("lo52").toString().replaceAll(",","");
                String vo = jsonObject.optString("vo").toString().replaceAll(",","");
                String pcls = jsonObject.optString("pcls_fix").toString().replaceAll(",","");

                data.put(name, name+","+ltime+","+price+","+open+","+hi+","+lo+","+hi52+","+lo52+","+vo+","+pcls);
                //Log.e(TAG,"Node"+i+" :  id= "+ id +"  Name= "+ name +"  Price= "+ price);
            }

        } catch (Exception e) {e.printStackTrace();}
        return data;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
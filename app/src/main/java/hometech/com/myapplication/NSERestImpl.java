package hometech.com.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Calendar;

public class NSERestImpl {

    static final String TAG = "NSERestImpl";

    static final String REST_SERVICE_EPOINT_SCRIP = "http://real-chart.finance.yahoo.com/table.csv?s=_SCRIP_.NS&then&now&g=d&ignore=.csv";
    static final String REST_SERVICE_EPOINT_SCRIPS = "http://www.nseindia.com/products/content/sec_bhavdata_full.csv";

    private static final int BUFFER_SIZE = 4096;
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    //"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070802 SeaMonkey/1.1.4";

    public static int downloadScrips(File outFile) throws Exception {

        int scrips=-1;
            Log.e(TAG, "downloading scrips.. ");

            BufferedReader r  = null;
            try {
                URLConnection connection = new URL(REST_SERVICE_EPOINT_SCRIPS).openConnection();
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept","*/*");
                connection.setDoOutput(false);
                connection.connect();

                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(outFile);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
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
            then.add(Calendar.DATE, -300);
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

}
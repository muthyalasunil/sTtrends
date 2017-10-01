package hometech.com.myapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;


public class Utils {

	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String CURR_SCRIP = "CURR_SCRIP";
	static final String TAG = "Utils";

	public static List<String[]> readRawCSVFile(Context ctx, int id) {
		InputStream inputStream = ctx.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		List<String[]> dataArr = new ArrayList<String[]>();
		try {

			while ((line = buf.readLine()) != null) {
				String[] values = line.split(",");
				dataArr.add(values);
			}
		} catch (IOException e) {
			return new ArrayList<String[]>();
		}
		return dataArr;
	}

	public static String[] computeScripAverages(File csvFile, float closeprc) {

		String[] dataArr = new String[8];
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader dataBR = new BufferedReader(new FileReader(csvFile));
			String line = "";

			dataBR.readLine();
			while ((line = dataBR.readLine()) != null) {
                String[] values = line.split(",");
				//Date	Open	High	Low	Close	Volume	Adj Close
				long volume = Long.parseLong(values[5]);
				if ( volume > 0 )
					list.add(values[4]);
            }
			//update close price
			list.remove(0);
			list.add(0, String.valueOf(closeprc));
		} catch (Exception e) {
			e.printStackTrace();
		}
		float sma200=0.0f, sma50=0.0f, sma15=0.0f;
		int j = 1;
		for (int i = 0; i<list.size() ;i++,j++){
			String token = list.get(i).replace("\"","");
			float value = Float.parseFloat(token);
			if (j<=200)
				sma200 += value;
			if (j<=50)
				sma50 += value;
			if (j<=15)
				sma15 += value;
		}


		sma200 = (j>=200 ? sma200/200 : 0);
		sma50 = (j>=50 ? sma50/50 : 0);
		sma15 = (j>=15 ? sma15/15 : 0);

		dataArr[0]=String.format("%.2f", sma200);
		dataArr[1]=String.format("%.2f", sma50);
		dataArr[2]=String.format("%.2f", sma15);

		List<float[]> rlist = calculateCalculateBBands(list, 20);
		dataArr[3]=String.format("%.2f", rlist.get(0)[0]);
		dataArr[4]=String.format("%.2f", rlist.get(2)[0]);
		dataArr[5]=String.format("%.2f", rlist.get(1)[0]);

		rlist = calculateMACDValues(list, 12, 26, 9);
		dataArr[6]=String.format("%.2f", rlist.get(0)[0]);
		dataArr[7]=String.format("%.2f", rlist.get(1)[0]);


		return dataArr;
	}

	public static List<float[]> calculateCalculateBBands(List<String> numlist, int period){

		int end = period*2;
		if (numlist.size() < period+5 )
			return Collections.EMPTY_LIST;
		else if (numlist.size() < end )
			end = numlist.size() - period;

		float sma=0.0f;
		float[] sma20= new float[end];
		float[] bbh= new float[end];
		float[] bbl= new float[end];
		float[] cls= new float[end];
		float[] time = new float[end];
		for (int i = 0; i < end ; i++) {
			try {
				float[] numbers= new float[period];
				sma=0.0f;
				cls[i] = Float.parseFloat(numlist.get(i));
				for ( int j = i, k = 0 ; j < i + period ; j++, k++ ){
                    numbers[k] = Float.parseFloat(numlist.get(j));
                    sma += numbers[k];
                }
				sma20[i] = sma / period;
				float stdev = standardDeviation(numbers);
				bbh[i] = sma20[i] + (2*stdev);
				bbl[i] = sma20[i] - (2*stdev);
				time[i]=i;
/*
				if ( dates != null && dates.size()>i ){
					try{
						time[i] = (df.parse(dates.get(i)).getTime());
					}catch(Exception e){
						time[i]=i;
					};
				}
*/
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

		}

		List<float[]> rlist = new ArrayList<>();
		rlist.add(bbh);rlist.add(sma20);rlist.add(bbl);rlist.add(cls);rlist.add(time);
		return rlist;
	}

	public static List<float[]> calculateMACDValues(List<String> numlist, int p1, int p2, int signal){

		int end = p2+3+p2;
		float[] numbers= new float[end];
		if (numlist.size() < end )
			return Collections.emptyList();
		else {
			for (int i = 0 ; i < end ; i++){
				float number = Float.parseFloat(numlist.get(i));
				numbers[i] = number;
			}
		}

		float sma26=0.0f, sma12=0.0f;
		int i12 = 0, i26 =0;
		for (int i = p2+3; i < end  ; i++){
			sma26 += numbers[i];
			if ( i < p2+3+p1 )
				sma12 += numbers[i];

		}
		sma12 = sma12/p1;
		sma26 = sma26/p2;

		float factor1 = 2.0f/(p1+1), factor2 = 2.0f/(p2+1), factor3 = 2.0f/(signal+1);
		float ema12=sma12, ema26=sma26;
		float[] macd = new float[p2+3];
		float[] time = new float[p2+3];
		//factor*(close-ema1)+ema1

		for (int i = p2+2 ; i >= 0 ; i--){
			ema12 = factor1 * (numbers[i]-ema12)+ema12;
			ema26 = factor2 * (numbers[i]-ema26)+ema26;
			macd[i] = ema12 - ema26;
			time[i] = i;
		}

		float sma9[]=new float[p2-5];
		for (int i = macd.length-1, j=0 ; j<signal ; j++, i--){
			sma9[sma9.length-1] += macd[i];
		}
		sma9[sma9.length-1]=sma9[sma9.length-1]/signal;
		for (int i = sma9.length-2; i>=0 ; i--){
			sma9[i] = factor3 * (macd[i]-sma9[i+1])+sma9[i+1];
			//Log.d(TAG, i+":" + sma9[i] + ":" + macd[i]);
		}

		List<float[]> rlist = new ArrayList();
		rlist.add(macd);
		rlist.add(sma9);
		rlist.add(time);
		return rlist;

	}

	public static float standardDeviation(float[] numbers)
	{
		int period = numbers.length;
		float avg = 0.0f;
		for (int i = 0 ; i < period ; i++){
			avg += numbers[i];
		}
		avg = avg / period;
		float sumavg = 0.0f;
		for (int i = 0 ; i < period ; i++){
			sumavg += ((numbers[i] - avg) * (numbers[i] - avg));
		}
		sumavg = sumavg / period;

		return (float)Math.sqrt(sumavg);
	}

	public static List<String[]> readCSVValues(File csvFile) throws Exception {

		BufferedReader dataBR = new BufferedReader(new FileReader(csvFile));
		String line = "";

		List<String[]> dataArr = new ArrayList<String[]>(); //An ArrayList is used because I don't know how many records are in the file.
		dataBR.readLine();
		while ((line = dataBR.readLine()) != null) { // Read a single line from the file until there are no more lines to read
			String[] values = line.split(",");
			dataArr.add(values);
		}
		return dataArr;
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Get average prices
	 * 
	 * @param doc
	 * @return
	 */
	public static double[] getAveragePrices(Document doc) {

		try {
			int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("/nodes/node/lowestPrice");
			NodeList lpricenodes = (NodeList) expr.evaluate(doc,
					XPathConstants.NODESET);
			expr = xpath.compile("/nodes/node/day");
			NodeList daynodes = (NodeList) expr.evaluate(doc,
					XPathConstants.NODESET);

			double mavg = 0.0;
			double wavg = 0.0;
			int weekdaycount = 0;
			for (int i = 0; i < daynodes.getLength(); i++) {
				int iday = (int) getAmountValue(daynodes.item(i)
						.getTextContent());
				if (day >= 7 || iday <= day) {
					int diff = day - iday;
					if (diff >= 0) {
						wavg += getAmountValue(lpricenodes.item(i)
								.getTextContent());
						weekdaycount++;
					}
				} else {
					if (day < 7) {
						int diff = 30 + day - iday;// 5-29=-24, 5-28=-23,
													// 5-27=-22
						if (diff <= 7) {
							wavg += getAmountValue(lpricenodes.item(i)
									.getTextContent());
							weekdaycount++;
						}
					}
				}
				mavg += getAmountValue(lpricenodes.item(i).getTextContent());
			}

			if (daynodes.getLength() > 0)
				mavg = mavg / (daynodes.getLength());

			Log.d(TAG, "Wavg:" + weekdaycount + ":" + wavg);
			if (weekdaycount > 0)
				wavg = wavg / weekdaycount;

			return new double[] { mavg, wavg };

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error in getAveragePrices call", e);
		}
		return null;
	}

	public static String getStringWithNullCheck(final NodeList nlist, int index) {
		if (nlist != null && nlist.item(index) != null)
			return nlist.item(index).getTextContent();
		return "";
	}

	public static double getAmountValue(String amtString) {
		try {
			return Double.parseDouble(amtString);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		return 0.0;
	}

	public static String getAmount(String amtString) {
		try {
			return String.valueOf((Float.parseFloat(amtString)) / 100);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		return "";
	}

	public static String readRawTextFile(Context ctx, int id) {
		InputStream inputStream = ctx.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try {

			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	public static Document parseFile(Context ctx, int id) {

		try {
			InputStream inputStream = ctx.getResources().openRawResource(id);
			InputStreamReader in = new InputStreamReader(inputStream);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputStream);
			return doc;

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		return null;
	}

	public static String csvStringFromList(List<String> list) {
		StringBuilder sbldr = new StringBuilder();
		for (String str : list)
			sbldr.append(str + ",");

		sbldr.deleteCharAt(sbldr.length() - 1);
		return sbldr.toString();
	}

	/**
	 * Takes a query string, separates the constituent name-value pairs
	 * and stores them in a hashmap.
	 * 
	 * @param queryString
	 * @return
	 */
/*	public static Map<String, String> createParameterMap(String queryString) {
	    Map<String, String> map = new HashMap<String, String>();
	    String[] pairs = queryString.split("&");
	
	    for (String pair: pairs) {
	        if (pair.length() < 1) {
	            continue;
	        }
	
	        String[] tokens = pair.split("=",2);
	        for(int j=0; j<tokens.length; j++)
	        {
	            try {
	                tokens[j] = URLDecoder.decode(tokens[j], SignedRequestsHelper.UTF8_CHARSET);
	            } catch (UnsupportedEncodingException e) {
	            }
	        }
	        switch (tokens.length) {
	            case 1: {
	                if (pair.charAt(0) == '=') {
	                    map.put("", tokens[0]);
	                } else {
	                    map.put(tokens[0], "");
	                }
	                break;
	            }
	            case 2: {
	                map.put(tokens[0], tokens[1]);
	                break;
	            }
	        }
	    }
	    return map;
	}*/
}

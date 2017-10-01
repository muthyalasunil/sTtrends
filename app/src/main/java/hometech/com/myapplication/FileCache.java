package hometech.com.myapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class FileCache {

	private static final String TAG = "FileCache";

	private File cacheDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"TempImages");
		else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public File getFile(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;

	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

	public ArrayList<HashMap<String, String>> retrieveItems() throws Exception {

		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(new File(
					cacheDir, "PRODUCT_CART")));
			items = (ArrayList<HashMap<String, String>>) input.readObject();
			input.close();
		} catch (Exception e) {
			Log.e(TAG, "Error in retrieveItems call", e);
		}
		return items;

	}

	public void removeItem(String asin, String siteName) throws Exception {

		ObjectOutput out = null;
		boolean itemfound = false;
		try {
			ArrayList<HashMap<String, String>> items = retrieveItems();
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				Map map = (Map) iter.next();
				if (map.get("asin").equals(asin)
						&& (siteName != null && siteName.equals(map
								.get("siteName")))) {
					itemfound = true;
					iter.remove();
					break;
				} else if (siteName == null && map.get("asin").equals(asin)) {
					itemfound = true;
					iter.remove();
					break;
				}
			}

			if (itemfound) {
				out = new ObjectOutputStream(new FileOutputStream(new File(
						cacheDir, "PRODUCT_CART")));
				Log.d(TAG, "writeObject:" + items.size());
				out.writeObject(items);
				out.close();
			} else
				throw new Exception("Item not found!");
		} catch (Exception e) {
			Log.e(TAG, "Error in removeItem call", e);
			throw e;
		}
	}

	public void saveItems(HashMap<String, String> itemdata) throws Exception {

		ObjectOutput out = null;
		try {
			ArrayList<HashMap<String, String>> items = retrieveItems();
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				Map map = (Map) iter.next();
				if (map.get("asin").equals(itemdata.get("asin"))) {
					iter.remove();
				}
				break;
			}

			if (items.size() >= 10) {
				throw new Exception("Cannot add more than 10 items to myList!");
			}
			out = new ObjectOutputStream(new FileOutputStream(new File(
					cacheDir, "PRODUCT_CART")));
			items.add(itemdata);
			Log.d(TAG, "writeObject:" + items.size());
			out.writeObject(items);
			out.close();
		} catch (Exception e) {
			Log.e(TAG, "Error in saveItems call", e);
			throw e;
		}
	}

}
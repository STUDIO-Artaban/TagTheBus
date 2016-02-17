package com.studio.artaban.tagthebus;

import com.studio.artaban.tagthebus.R;
import com.studio.artaban.tagthebus.AlbumDatabase;
import com.studio.artaban.tagthebus.StationAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends ListActivity implements OnItemClickListener {

	public static AlbumDatabase mDB; // Bus station photo database

	////// Download & Extract JSON file
	private static final String JSON_DATA = "data";
	private static final String JSON_STATIONS = "nearstations";
	private static final String JSON_STATION_ID = "id";
	private static final String JSON_STATION_STREET = "street_name";
	private static final String JSON_STATION_BUSES = "buses";
	private static final String JSON_STATION_LATITUDE = "lat";
	private static final String JSON_STATION_LONGITUDE = "lon";

    public class Station {

    	public int id; // Station ID
    	public String street; // Station street
    	public String buses; // Station buses

    	// Station coordinates
    	public double latitude;
    	public double longitude;
    }
	public static List<Station> mStationList; // Station data list

	private boolean AnalysisData(String data) {

		try {

			JSONObject listJSON = new JSONObject(data);
			JSONArray arrJSON = listJSON.getJSONObject(JSON_DATA).getJSONArray(JSON_STATIONS);

			// Fill station list
			mStationList = new ArrayList<Station>();
			for (int i = 0; i < arrJSON.length(); ++i) {

				JSONObject station = arrJSON.getJSONObject(i);
				Station newStation = new Station();
				newStation.id = Integer.parseInt(station.getString(JSON_STATION_ID));
				newStation.street = station.getString(JSON_STATION_STREET);
				newStation.buses = station.getString(JSON_STATION_BUSES);
				newStation.latitude = Double.parseDouble(station.getString(JSON_STATION_LATITUDE));
				newStation.longitude = Double.parseDouble(station.getString(JSON_STATION_LONGITUDE));
				mStationList.add(newStation);
			}
		}
		catch (JSONException e) {

			Log.e(MainActivity.class.toString(), "Wrong JSON file");
			return false;
		}

		// Sort station list
		Collections.sort(mStationList, new Comparator<Station>() {
	        @Override public int compare(Station stationA, Station stationB) {
	            return (stationA.id > stationB.id)? 1:-1;
	        }
	    });

		// Display station list
		runOnUiThread(new Runnable() {
			@Override public void run() {

				setListAdapter(new StationAdapter(MainActivity.this));

				((RelativeLayout)findViewById(R.id.layoutDownloading)).setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
				mMenuMap.setEnabled(true);
			}
		});
		return true;
	}

	private static final String STATION_DATA_URL = "http://barcelonaapi.marcpous.com/bus/nearstation/latlon/41.3985182/2.1917991/1.json";
	private AsyncTask<Void, Void, Void> mDownloadTask;

	private class DownloadDataTask extends AsyncTask<Void, Void, Void> {
		
		@Override protected Void doInBackground(Void... params) {

			// Check if device is online
			NetworkInfo netInfo = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if ((netInfo == null) || (!netInfo.isConnectedOrConnecting())) {

				Log.e(MainActivity.class.toString(), "Device not connected");
				displayError(R.string.error_no_internet);
				return null;
			}

			// Download JSON file
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(STATION_DATA_URL);

			int errID = R.string.error_download;
			try {

				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == 200) {
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					StringBuilder data = new StringBuilder();
					String line = reader.readLine();
					while (line != null) {

						data.append(line);
						line = reader.readLine();
					}
					reader.close();

					// Analysis JSON file
					if (!AnalysisData(data.toString()))
						errID = R.string.error_json;
					else
						errID = 0; // Succeeded
				}
				else {
					Log.e(MainActivity.class.toString(), "Failed to download data file");
				}
			}
			catch (ClientProtocolException e) {
				Log.e(MainActivity.class.toString(), "Client protocol exception: " + e.getMessage());
			}
			catch (IOException e) {
				Log.e(MainActivity.class.toString(), "IO exception: " + e.getMessage());
			}

			if (errID != 0) // Error
				displayError(errID);
			return null;
		}
	}

	////// Error (display)
	private class ErrorRunnable implements Runnable {

		private int mErrorID;
		public ErrorRunnable(int errorID) {
			mErrorID = errorID;
		}
		@Override public void run() {

			((RelativeLayout)findViewById(R.id.layoutDownloading)).setVisibility(View.GONE);
			getListView().setVisibility(View.GONE);
			((RelativeLayout)findViewById(R.id.layoutError)).setVisibility(View.VISIBLE);

			((TextView)findViewById(R.id.textError)).setText(mErrorID);
		}
	}
	public void displayError(int errorID) {
		runOnUiThread(new ErrorRunnable(errorID));
	}

	////// Activity
	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setListAdapter(null); // Reset list adapter
		getListView().setOnItemClickListener(this);

		mDB = new AlbumDatabase();
		mDB.read(this);
	}
	@Override protected void onPause() {
		
		super.onPause();
		setListAdapter(null);
	}
	@Override protected void onResume() {
		
		super.onResume();

		((RelativeLayout)findViewById(R.id.layoutError)).setVisibility(View.GONE);
		((RelativeLayout)findViewById(R.id.layoutDownloading)).setVisibility(View.VISIBLE);
		getListView().setVisibility(View.GONE);
		if (mMenuMap != null)
			mMenuMap.setEnabled(false);

		// Download station data file (JSON)
		mDownloadTask = new DownloadDataTask();
		mDownloadTask.execute();
	}

	////// Menu
	private MenuItem mMenuMap;

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu
		getMenuInflater().inflate(R.menu.main, menu);
		mMenuMap = menu.getItem(0);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle action bar item clicks here.
		int id = item.getItemId();
		if (id == R.id.action_map) {
			
			try {

				final Intent mapList = new Intent();
				mapList.setComponent(new ComponentName(AQUAFADAS_PACKAGE, AQUAFADAS_PACKAGE + "." + STATION_ACTIVITY));
				startActivity(mapList);
			}
			catch (ActivityNotFoundException e) {
				Log.e(MainActivity.class.toString(), "Station activity not found: " + e.getMessage());
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	////// Station item (select)
	public static final String AQUAFADAS_PACKAGE = "com.studio.artaban.tagthebus";
	public static final String ALBUM_ACTIVITY = "AlbumActivity"; 
	private static final String STATION_ACTIVITY = "StationActivity"; 

	public static final String STATION_KEY_POSITION = "pos";

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		try {
			
			Bundle bundle = new Bundle();
			bundle.putInt(STATION_KEY_POSITION, position);

			final Intent photoList = new Intent();
			photoList.putExtras(bundle);
			photoList.setComponent(new ComponentName(AQUAFADAS_PACKAGE, AQUAFADAS_PACKAGE + "." + ALBUM_ACTIVITY));
			startActivity(photoList);
		}
		catch (ActivityNotFoundException e) {
			Log.e(MainActivity.class.toString(), "Album activity not found: " + e.getMessage());
		}
	}
}

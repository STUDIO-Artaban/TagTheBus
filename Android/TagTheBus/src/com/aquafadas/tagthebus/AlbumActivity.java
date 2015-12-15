package com.aquafadas.tagthebus;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import com.aquafadas.tagthebus.MainActivity;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumActivity extends ListActivity implements OnItemClickListener {

	private int mStationIdx; // Selected station index

	private void addPhoto(Intent camIntent) {
    	try {

    		int stationID = MainActivity.mStationList.get(mStationIdx).id;
    		SimpleDateFormat format = new SimpleDateFormat(CameraActivity.PHOTO_DATE_FORMAT);
    		MainActivity.mDB.write(stationID, camIntent.getExtras().getString(PHOTO_KEY_TITLE),
    				(Date)format.parse(camIntent.getExtras().getString(PHOTO_KEY_DATE)));
    		MainActivity.mDB.save(this);
    		MainActivity.mDB.select(stationID);
    		displayList();
    	}
    	catch (ParseException e) {  
			Log.e(AlbumActivity.class.toString(), "Date parser error: " + e.getMessage());
    	}
	}
	private void displayList() {

		setListAdapter(new AlbumAdapter(this));

		((ImageView)findViewById(R.id.imageAlbum)).setVisibility(View.GONE);
		((TextView)findViewById(R.id.textInfo)).setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
	}

	////// Activity
	public static final String PHOTO_KEY_TITLE = "title"; // Bundle key photo title 
	public static final String PHOTO_KEY_DATE = "date"; // Bundle key photo date

	private static final int TAKE_PHOTO_REQUEST = 23; // Request code to take a photo 

	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		
		setListAdapter(null); // Reset list adapter
		getListView().setOnItemClickListener(this);

		mStationIdx = getIntent().getExtras().getInt(MainActivity.STATION_KEY_POSITION);
		if (MainActivity.mDB.select(MainActivity.mStationList.get(mStationIdx).id))
			displayList();

		final MainActivity.Station station = MainActivity.mStationList.get(mStationIdx);
		getActionBar().setTitle(station.id + " - " + station.street);
	}
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == TAKE_PHOTO_REQUEST) && (resultCode == RESULT_OK)) // Photo taken
           	addPhoto(data);
    }
	@Override protected void onPause() {

		super.onPause();
		setListAdapter(null);
	}
	@Override protected void onResume() {

		super.onResume();
		if ((getListView().getAdapter() == null) && (MainActivity.mDB.select(MainActivity.mStationList.get(mStationIdx).id)))
			displayList();
	}

	////// Menu
	private static final int ADD_PHOTO_ID = 303; // ID of the item to add photo

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu
		MenuItem addItem = menu.add(Menu.NONE, ADD_PHOTO_ID, 1, "Add");
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		addItem.setIcon(android.R.drawable.ic_menu_camera);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here
		int id = item.getItemId();
		if (id == ADD_PHOTO_ID) {

			Intent photoCAM = new Intent(this, CameraActivity.class);
			startActivityForResult(photoCAM, TAKE_PHOTO_REQUEST);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	////// Photo item (select)
	public static final String ALBUM_KEY_INDEX = "idx";
	private static final String PHOTO_ACTIVITY = "PhotoActivity";

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		try {
			
			Bundle bundle = new Bundle();
			bundle.putInt(MainActivity.STATION_KEY_POSITION, mStationIdx);
			bundle.putInt(ALBUM_KEY_INDEX, position);
			
			final Intent photoList = new Intent();
			photoList.putExtras(bundle);
			photoList.setComponent(new ComponentName(MainActivity.AQUAFADAS_PACKAGE, MainActivity.AQUAFADAS_PACKAGE +
					"." + PHOTO_ACTIVITY));
			startActivity(photoList);
		}
		catch (ActivityNotFoundException e) {
			Log.e(AlbumActivity.class.toString(), "Album activity not found: " + e.getMessage());
		}
	}
}

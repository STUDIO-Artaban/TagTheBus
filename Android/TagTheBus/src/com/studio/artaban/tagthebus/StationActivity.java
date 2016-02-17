package com.studio.artaban.tagthebus;

import java.util.HashMap;

import com.studio.artaban.tagthebus.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class StationActivity extends Activity implements OnMarkerClickListener, ConnectionCallbacks, OnClickListener,
		OnConnectionFailedListener, OnMapClickListener {

	private GoogleMap mMap; // Google map
	
	////// Geolocalisation
	private GoogleApiClient mGoogleClient; // Google client used to in geolocation
	private Marker mUserMarker; // User marker

	private void geoLocation() {

		final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
		if (lastLocation != null) {

			LatLng userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18));
			
			if (mUserMarker != null)
				mUserMarker.remove();

			mUserMarker = mMap.addMarker(new MarkerOptions()
				.position(userLatLng)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));		
		}
		else
			Toast.makeText(this, "Géocalisation impossible! Veuillez réessayer ultérieurement, ou vérifier les options" +
					" de géocalisation dans vos rêglages.", Toast.LENGTH_LONG).show();
	}
	@Override public void onConnectionFailed(ConnectionResult arg0) { }
	@Override public void onConnectionSuspended(int arg0) {	}
	@Override public void onConnected(Bundle arg0) {
		geoLocation();
	}
	
	////// Marker (click events)
	private HashMap<Marker, Integer> mMarkerIDs; // Marker <-> Station index hashmap
	private Integer mStationIdx; // Selected station index
	private Button mDisplayAlbum; // Button to display station album

	@Override public boolean onMarkerClick(Marker marker) {
		
		mStationIdx = mMarkerIDs.get(marker);
		if (mStationIdx != null) // Avoid user location marker
			mDisplayAlbum.setVisibility(View.VISIBLE);

		//else // User location marker
		return false;
	}
	@Override public void onClick(View v) {
		
		// Display station album
		Bundle bundle = new Bundle();
		bundle.putInt(MainActivity.STATION_KEY_POSITION, mStationIdx);

		final Intent photoList = new Intent();
		photoList.putExtras(bundle);
		photoList.setComponent(new ComponentName(MainActivity.AQUAFADAS_PACKAGE, MainActivity.AQUAFADAS_PACKAGE + "." +
				MainActivity.ALBUM_ACTIVITY));
		startActivity(photoList);
	}
	@Override public void onMapClick(LatLng arg0) {
		mDisplayAlbum.setVisibility(View.GONE);
	}

	////// Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        mDisplayAlbum = (Button)findViewById(R.id.buttonView);
        mDisplayAlbum.setOnClickListener(this);
        
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.setOnMapClickListener(this);
		mMap.setOnMarkerClickListener(StationActivity.this);
		
		mMarkerIDs = new HashMap<Marker, Integer>();
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
		
        Handler handTask = new Handler();
        Runnable markerTask = new Runnable() {
			
        	@Override public void run() {
				
        		LatLng firstLoc = null;
        		for (int i = 0; i < MainActivity.mStationList.size(); ++i) {

        			MainActivity.Station station = MainActivity.mStationList.get(i);
		        	final LatLng coords = new LatLng(station.latitude, station.longitude);
		        	if (firstLoc == null)
		        		firstLoc = coords;

		        	mMarkerIDs.put(mMap.addMarker(new MarkerOptions().position(coords).title(station.id + " - " +
		        			station.street)), i);
				}
				((RelativeLayout)findViewById(R.id.layoutMarking)).setVisibility(View.GONE);

				// Zoom on the first bus station
			    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, 14));
			}
        };
        handTask.postAtTime(markerTask, 1000);
    }

	////// Menu
	private MenuItem mMenuList;

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu
		getMenuInflater().inflate(R.menu.map, menu);

		mMenuList = menu.getItem(0);
		mMenuList.setTitle(getResources().getText(R.string.action_list));
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle action bar item clicks here.
		switch (item.getItemId()) {

			case R.id.action_list:
				finish(); // Back to activity list
				return true;

			case R.id.action_location:
				
				mDisplayAlbum.setVisibility(View.GONE);
				if (!mGoogleClient.isConnected())
					mGoogleClient.connect();
				else
					geoLocation();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

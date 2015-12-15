package com.aquafadas.tagthebus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class AlbumDatabase {

	private static final String DATABASE_FILE_NAME = "DB.json"; // Local file containing the database (JSON)

	private static final String JSON_STATION_ID = "id"; // Station ID
	private static final String JSON_STATION_PHOTO = "photo"; // Station album
	private static final String JSON_PHOTO_TITLE = "title"; // Photo title
	private static final String JSON_PHOTO_DATE = "date"; // Photo date

	private JSONArray mDatabase; // Local photo album database

	public AlbumDatabase() {
		mAlbum = new ArrayList<Photo>();
	}

	private JSONArray find(int stationID) throws JSONException { // Find album
		if (mDatabase == null)
			return null;

		for (int i = 0; i < mDatabase.length(); ++i) {

			JSONObject station = mDatabase.getJSONObject(i);
			if (station.getInt(JSON_STATION_ID) == stationID)
				return station.getJSONArray(JSON_STATION_PHOTO);
		}
		return null;
	}

	////// DB management
	public boolean read(Context context) {
		try {

			FileInputStream inStream = new FileInputStream(context.getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() +
					"/" + DATABASE_FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		    StringBuilder data = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {

				data.append(line);
				line = reader.readLine();
			}
			reader.close();
			mDatabase = new JSONArray(data.toString());
		}
		catch (FileNotFoundException e) {
			Log.w(AlbumDatabase.class.toString(), "Local DB file not found: " + e.getMessage());
		}
		catch (IOException e) {
			Log.e(AlbumDatabase.class.toString(), "IO exception (read): " + e.getMessage());
			return false;
		}
		catch (JSONException e) {
			Log.e(AlbumDatabase.class.toString(), "JSON exception (read): " + e.getMessage());
			return false;
		}
		return true;
	}
	public void write(int stationID, String title, Date date) {
		try {

			JSONObject newPhoto = new JSONObject();
			newPhoto.put(JSON_PHOTO_TITLE, title);
			newPhoto.put(JSON_PHOTO_DATE, DateFormat.format(CameraActivity.PHOTO_DATE_FORMAT, date).toString());

			if (mDatabase == null)
				mDatabase = new JSONArray();
			
			boolean found = false;
			for (int i = 0; i < mDatabase.length(); ++i) { // Find album

				JSONObject station = mDatabase.getJSONObject(i);
				if (station.getInt(JSON_STATION_ID) == stationID) {

					found = true; // Album found
					station.getJSONArray(JSON_STATION_PHOTO).put(newPhoto); // Add photo
				}
			}
			if (!found) {

				// Add station album
				JSONObject station = new JSONObject();
				station.put(JSON_STATION_ID, stationID);

				JSONArray album = new JSONArray();
				album.put(newPhoto); // Add photo
				station.put(JSON_STATION_PHOTO, album);

				mDatabase.put(station);
			}
		}
		catch (JSONException e) {
			Log.e(AlbumDatabase.class.toString(), "JSON exception (write): " + e.getMessage());
		}
	}
	public boolean save(Context context) {
		if (mDatabase == null)
			return true; // Nothing to save

		try {
			FileOutputStream outStream = new FileOutputStream(context.getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() +
					"/" + DATABASE_FILE_NAME);
			//Log.i(AlbumActivity.class.toString(), mDatabase.toString());
			outStream.write(mDatabase.toString().getBytes());
			outStream.close();
		}
		catch (Exception e) {
			Log.e(AlbumDatabase.class.toString(), "DB File exception: " + e.getMessage());
			return false;
		}
		return true;
	}

	////// Accessors
	public int getPhotoCount(int stationID) {
		try {

			JSONArray album = find(stationID);
			if (album != null) // Album found
				return album.length();
		}
	    catch (JSONException e) {
			Log.e(AlbumDatabase.class.toString(), "JSON exception (getPhotoCount): " + e.getMessage());
		}
		return 0;
	}
	public List<Photo> getAlbum() { return mAlbum; }

	////// Album (select)
	public class Photo {

		public String title; // Photo title
		public Date date; // Photo date
	}
	private List<Photo> mAlbum; // Selected album

	public boolean select(int stationID) {
		try {

			mAlbum.clear();
			JSONArray album = find(stationID);
			if (album != null) { // Album found

				for (int i = 0; i < album.length(); ++i) {

					Photo photo = new Photo();
					photo.title = album.getJSONObject(i).getString(JSON_PHOTO_TITLE);

		    		SimpleDateFormat format = new SimpleDateFormat(CameraActivity.PHOTO_DATE_FORMAT);
		    		try {
		    			photo.date = (Date)format.parse(album.getJSONObject(i).getString(JSON_PHOTO_DATE));
						mAlbum.add(photo);
		    		}
		    		catch (ParseException e) {
		    			Log.e(AlbumDatabase.class.toString(), "Date parser exception: " + e.getMessage());
					}
				}					
				return true;
			}
		}
	    catch (JSONException e) {
			Log.e(AlbumDatabase.class.toString(), "JSON exception (select): " + e.getMessage());
		}
		return false;
	}
}

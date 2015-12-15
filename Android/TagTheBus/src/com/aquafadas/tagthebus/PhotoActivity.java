package com.aquafadas.tagthebus;

import java.io.File;
import java.util.Date;

import com.aquafadas.tagthebus.AlbumDatabase.Photo;
import com.aquafadas.tagthebus.social.EngFacebook;
import com.aquafadas.tagthebus.social.EngGoogle;
import com.aquafadas.tagthebus.social.EngSocial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoActivity extends Activity {

	private int mStationIdx; // Station index
	private int mPhotoIdx; // Photo index
	private Bitmap mPhotoBmp; // Photo bitmap

	public Uri getURI(String file, String title, String date) {

        File picFile = new File(file);
        Cursor curPicture = getContentResolver().query(
        		MediaStore.Video.Media.INTERNAL_CONTENT_URI, new String[] { MediaStore.Video.Media._ID },
                MediaStore.Video.Media.DATA + "=? ", new String[] { picFile.getAbsolutePath() }, null);

        Uri pictureURI = null;
        if ((curPicture != null) && (curPicture.moveToFirst()))
        	pictureURI = Uri.withAppendedPath(MediaStore.Video.Media.INTERNAL_CONTENT_URI,
        			String.valueOf(curPicture.getInt(curPicture.getColumnIndex(MediaStore.MediaColumns._ID))));

        else {

    		ContentValues values = new ContentValues(4);
            values.put(MediaStore.Video.Media.TITLE, title + " (" + date + ")");
            values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Video.Media.DATA, picFile.getAbsolutePath());
            values.put(MediaStore.Video.Media.SIZE, picFile.length());
            pictureURI = getContentResolver().insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
        }
        return pictureURI;
	}
	
	static public final String TITLE_UNDEFINED = "<NON DEFINI>";

	////// Social networks
	private static final short NETWORK_ID_FACEBOOK = 0;
	private static final short NETWORK_ID_GOOGLE = 1;
	private static final short NETWORK_ID_EMAIL = 2;

	private short mNetworkID; // Current network ID

	private EngFacebook mFacebook;
	private EngGoogle mGooglePlus;

	private void share() {

		// Share picture
		Photo picInfo = MainActivity.mDB.getAlbum().get(mPhotoIdx);
		
		String[] share = new String[3];
		share[0] = getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() + "/" +
				DateFormat.format(CameraActivity.PHOTO_DATE_FORMAT, picInfo.date).toString() + ".jpg"; // File
		share[1] = (picInfo.title.length() > 0)? picInfo.title:TITLE_UNDEFINED; // Title
		share[2] = picInfo.date.toString(); // Date

		//Log.i(PhotoActivity.class.toString(), "Share " + share[0] + " picture (" + mNetworkID + ")");
		boolean result = true;
		switch (mNetworkID) {

			case NETWORK_ID_FACEBOOK:
				result = mFacebook.sharePicture(share);
				break;
	
			case NETWORK_ID_GOOGLE:
				result = mGooglePlus.sharePicture(share);
				break;
	
			case NETWORK_ID_EMAIL:
				
				final Intent email = new Intent(Intent.ACTION_SEND);
				email.setData(Uri.parse("mailto:"));
				email.setType("text/plain");
				email.putExtra(android.content.Intent.EXTRA_SUBJECT, share[1]); // Title
				email.putExtra(Intent.EXTRA_STREAM, getURI(share[0], share[1], share[2])); // Picture joined

			    try { startActivity(email); }
			    catch(ActivityNotFoundException e) {

			    	Log.w(PhotoActivity.class.toString(), "No email client installed");
			    	Toast.makeText(this, getResources().getString(R.string.error_email), Toast.LENGTH_LONG).show();
			    }
			    //result = true;
				break;
		}
		if (!result) // Error
			Toast.makeText(this, getResources().getString(R.string.error_shared), Toast.LENGTH_LONG).show();
	}
	
	////// Activity
	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_photo);

		mStationIdx = getIntent().getExtras().getInt(MainActivity.STATION_KEY_POSITION);		
		mPhotoIdx = getIntent().getExtras().getInt(AlbumActivity.ALBUM_KEY_INDEX);

		mFacebook = new EngFacebook(this, false);
		mGooglePlus = new EngGoogle(this, false);
		
		String picFolder = getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() + "/";
		Date picDate = MainActivity.mDB.getAlbum().get(mPhotoIdx).date;
		try {
			mPhotoBmp = BitmapFactory.decodeFile(picFolder +
					DateFormat.format(CameraActivity.PHOTO_DATE_FORMAT,	picDate).toString() + ".jpg");
			((ImageView)findViewById(R.id.imageDisplay)).setImageBitmap(mPhotoBmp);
		}
		catch (OutOfMemoryError e) {
			Log.e(PhotoActivity.class.toString(), "Displaying photo: Out of memory");
		}

		final MainActivity.Station station = MainActivity.mStationList.get(mStationIdx);
		getActionBar().setTitle(station.id + " - " + DateFormat.format(getResources().getString(R.string.date_format),
				picDate).toString());
	}
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        
		//Log.i(PhotoActivity.class.toString(), "Login activity result - Request: " + requestCode + " Result: " + resultCode);
        switch (requestCode) {
        	case EngSocial.REQUEST_LOGIN: {
        		
        		if (resultCode != Activity.RESULT_OK)
        			break; // Not logged

    			mFacebook.launch(requestCode, resultCode, data);
       			share();
        		break;
        	}
        	case EngGoogle.SIGN_IN_REQUEST: {

        		mGooglePlus.launch(requestCode, resultCode, data);
        		share();
        		break;
        	}
        }
    }
    @Override protected void onDestroy() {

    	super.onDestroy();
    	if (mPhotoBmp != null) {
    		mPhotoBmp.recycle();
    		mPhotoBmp = null;
    	}
    }

	////// Menu
	private static final int SHARE_PHOTO_ID = 303; // ID of the item to add photo
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu
		MenuItem addItem = menu.add(Menu.NONE, SHARE_PHOTO_ID, 1, "Share");
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		addItem.setIcon(android.R.drawable.ic_menu_share);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here
		int id = item.getItemId();
		if (id == SHARE_PHOTO_ID) {

            // Dialog with social network choice
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle(getResources().getString(R.string.share_photo));

            final CharSequence[] networks = { "Facebook", "Google+", "Email" };
			dlg.setSingleChoiceItems(networks, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
						case 0: // Facebook

							mNetworkID = NETWORK_ID_FACEBOOK;
							if (mFacebook.isLogged()) {
							
								mFacebook.login(); // Open active session
								share();
							}
							else
								mFacebook.login();
							break;
							
						case 1: // Google+

							mNetworkID = NETWORK_ID_GOOGLE;
							if (!mGooglePlus.isLogged())
								mGooglePlus.login();

							share();
							break;
							
						case 2: // Email
							
							mNetworkID = NETWORK_ID_EMAIL;
							share();
							break;
					}
					dialog.dismiss();   
				}
			});
			dlg.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

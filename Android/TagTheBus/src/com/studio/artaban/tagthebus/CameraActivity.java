package com.studio.artaban.tagthebus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.studio.artaban.tagthebus.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnClickListener, PictureCallback {

	private Camera mCamera; // Camera
	private SurfaceHolder mCamHolder; // Camera preview holder
	
	public static final String PHOTO_DATE_FORMAT = "yyyyMMdd_hhmmss";

	////// Photo
	@Override public void onClick(View v) {
		if (mCamera == null)
			return;

		mCamera.takePicture(null, null, this);
	}
	@Override public void onPictureTaken(byte[] data, Camera camera) {

		final Date picDate = new Date();	
		String picName = getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() + "/" +
				DateFormat.format(PHOTO_DATE_FORMAT, picDate).toString();
		try {

			// Save picture file
			FileOutputStream outStream = new FileOutputStream(picName + ".jpg");
            outStream.write(data);
            outStream.close();

            // Save thumbnail file (to be displayed into a list view)
            Bitmap thumbnail = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picName + ".jpg"), 50, 50, true);
			outStream = new FileOutputStream(picName + "_THMB.jpg");
			thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            outStream.flush();
            outStream.close();
		}
		catch (FileNotFoundException e) {
			Log.e(CameraActivity.class.toString(), "Failed to create file: " + e.getMessage());
			Toast.makeText(this, getResources().getString(R.string.error_photo), Toast.LENGTH_LONG).show();
			return;
		}
		catch (IOException e) {
            Log.e(CameraActivity.class.toString(), "IO exception: " + e.getMessage());
			Toast.makeText(this, getResources().getString(R.string.error_photo), Toast.LENGTH_LONG).show();
			return;
		}
		catch (OutOfMemoryError e) {
            
			Log.e(CameraActivity.class.toString(), "On picture taken: Out of memory");
			
			// Delete files (if any)
			File picFile = new File(picName + ".jpg");
			if (picFile.exists())
				picFile.delete();
			picFile = new File(picName + "_THMB.jpg");
			if (picFile.exists())
				picFile.delete();

			Toast.makeText(this, getResources().getString(R.string.error_photo), Toast.LENGTH_LONG).show();
			return;
		}
    	
    	// Display dialog to set photo title
    	AlertDialog.Builder dlg = new AlertDialog.Builder(CameraActivity.this);
    	dlg.setTitle(getResources().getString(R.string.photo_title));

    	final EditText picTitle = new EditText(CameraActivity.this);
    	picTitle.setInputType(InputType.TYPE_CLASS_TEXT);
    	dlg.setView(picTitle);

    	// Set validation buttons
    	dlg.setOnCancelListener(new OnCancelListener() {
			@Override public void onCancel(DialogInterface dialog) {

    	    	refreshCamera(); // Restart camera
			}
    	});
    	
    	dlg.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() { 
    	    @Override public void onClick(DialogInterface dialog, int which) {

    	    	Intent intent = new Intent();
    			intent.putExtra(AlbumActivity.PHOTO_KEY_TITLE, picTitle.getText().toString());
    			intent.putExtra(AlbumActivity.PHOTO_KEY_DATE, DateFormat.format(PHOTO_DATE_FORMAT, picDate).toString());
    			setResult(RESULT_OK, intent);        
    			finish();
    	    }
    	});
    	dlg.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
    	    @Override public void onClick(DialogInterface dialog, int which) {

    	    	refreshCamera(); // Restart camera
    	    }
    	});
    	dlg.show();            	
	}

	////// Activity
	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    	((ImageView)findViewById(R.id.imageStart)).setOnClickListener(this);

    	mCamHolder = ((SurfaceView)findViewById(R.id.surfaceCamera)).getHolder();
    	mCamHolder.addCallback(this);
	}
	
	////// Camera
	public void refreshCamera() {
		if (mCamHolder.getSurface() == null)
			return;

		try {
			mCamera.stopPreview();
	        mCamera.setPreviewDisplay(mCamHolder);
			mCamera.startPreview();
		}    
		catch (Exception e) {
            Log.e(CameraActivity.class.toString(), "Refresh camera error: " + e.getMessage());
		}
	}
	private void stopCamera() {
		if (mCamera == null)
			return;
		
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override public void surfaceCreated(SurfaceHolder holder) {

		boolean err = true;
        try {
        	mCamera = Camera.open(); // Try to open back-facing camera
        	mCamera.setDisplayOrientation(90);
        	mCamera.setPreviewDisplay(mCamHolder);
            mCamera.setErrorCallback(new ErrorCallback() {

                public void onError(int error, Camera camera) {
                    Log.e(CameraActivity.class.toString(), "Camera error: " + error);
                }
            });
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public synchronized void onPreviewFrame(byte[] data, Camera camera) { }
            });
            mCamera.startPreview();
            err = false;
        }
        catch (IOException e) {
            Log.e(CameraActivity.class.toString(), "Failed to set surface holder: " + e.getMessage());
        }
        catch (RuntimeException e) {
            Log.e(CameraActivity.class.toString(), "Failed to open camera: " + e.getMessage());
        }
        if (err) {

    		Intent intent = new Intent();
    		setResult(RESULT_FIRST_USER, intent);
    		finish();
        }
	}
	@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		refreshCamera();
	}
	@Override public void surfaceDestroyed(SurfaceHolder holder) {
		stopCamera();
	}
}

package com.aquafadas.tagthebus;

import com.aquafadas.tagthebus.CameraActivity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends BaseAdapter {

	private static class ViewHolder {

		ImageView photo;
		TextView title;
		TextView date;
		// See 'album_item' layout
	}
	private LayoutInflater mItemInflater; // Item layout inflater
	private String mPicFolder; // Photo folder
	private String mDateFormat; // Photo date format

	public AlbumAdapter(Context context) {
		mItemInflater = LayoutInflater.from(context);
		mPicFolder = context.getExternalFilesDir(null).getAbsoluteFile().getAbsolutePath() + "/";
		mDateFormat = context.getResources().getString(R.string.date_format);
	}
	@Override public int getCount() {
		return MainActivity.mDB.getAlbum().size();
	}
	@Override public Object getItem(int position) {
		return MainActivity.mDB.getAlbum().get(position);
	}
	@Override public long getItemId(int position) {
		return position;
	}
	@Override public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) { // Check existing cache view
			
			convertView = mItemInflater.inflate(R.layout.album_item, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.textDesc);
			holder.date = (TextView)convertView.findViewById(R.id.textDate);
			holder.photo = (ImageView)convertView.findViewById(R.id.imagePhoto);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder)convertView.getTag();

		// Set album info to display
		AlbumDatabase.Photo photoInfo = MainActivity.mDB.getAlbum().get(position);
		
		try {
			holder.date.setText(DateFormat.format(mDateFormat, photoInfo.date).toString());
			holder.title.setText((photoInfo.title.length() > 0)? photoInfo.title:PhotoActivity.TITLE_UNDEFINED);
			holder.photo.setImageBitmap(BitmapFactory.decodeFile(mPicFolder +
					DateFormat.format(CameraActivity.PHOTO_DATE_FORMAT,	photoInfo.date).toString() + "_THMB.jpg"));
		}
		catch (OutOfMemoryError e) {
			Log.e(AlbumAdapter.class.toString(), "Album adapter: Out of memory");
		}
		return convertView;
	}
}

package com.aquafadas.tagthebus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationAdapter extends BaseAdapter {

	private static class ViewHolder {

		TextView title;
		TextView subtitle;
		// See 'station_item' layout
	}
	private LayoutInflater mItemInflater; // Item layout inflater

	public StationAdapter(Context context) {
		mItemInflater = LayoutInflater.from(context);
	}
	@Override public int getCount() {
		return MainActivity.mStationList.size();
	}
	@Override public Object getItem(int position) {
		return MainActivity.mStationList.get(position);
	}
	@Override public long getItemId(int position) {
		return MainActivity.mStationList.get(position).id;
	}
	@Override public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) { // Check existing cache view

			convertView = mItemInflater.inflate(R.layout.station_item, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.textTitle);
			holder.subtitle = (TextView)convertView.findViewById(R.id.textSubTitle);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder)convertView.getTag();

		// Set station info to display
		MainActivity.Station station = MainActivity.mStationList.get(position);
		holder.title.setText(station.id + " - " + station.street);
		holder.subtitle.setText("BUSES: " + station.buses);

		return convertView;
	}
}

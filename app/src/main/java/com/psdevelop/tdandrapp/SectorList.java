package com.psdevelop.tdandrapp;

import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
//import android.support.v4.app.NavUtils;
//import android.annotation.TargetApi;
import android.content.Context;

public class SectorList extends ListActivity {
	
	private LayoutInflater mInflater;
	private Vector<VectorSectorItem> data;
	VectorSectorItem sect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//savedInstanceState.g
		data = new Vector<VectorSectorItem>();
		sect = new VectorSectorItem("1","sector");
		data.add(sect);
		//setContentView(R.layout.activity_sector_list);
		mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		CustomAdapter adapter = new CustomAdapter(this, R.layout.custom_row,
				R.id.custom_row_item, data);
	    setListAdapter(adapter);        
	    //getListView().setTextFilterEnabled(true);
		// Show the Up button in the action bar.
	}

	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	//	getMenuInflater().inflate(R.menu.sector_list, menu);
	//	return true;
	//}

	//@Override
	//public boolean onOptionsItemSelected(MenuItem item) {
	//	switch (item.getItemId()) {
	//	case R.id.action_settings:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
	//		NavUtils.navigateUpFromSameTask(this);
	//		return true;
	//	}
	//	return super.onOptionsItemSelected(item);
	//}
	
	private class CustomAdapter extends ArrayAdapter<VectorSectorItem> {

		public CustomAdapter(Context context, int resource,
				int textViewResourceId, List<VectorSectorItem> objects) {
			super(context, resource, textViewResourceId, objects);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			//widgets displayed by each item in your list
			TextView item = null;
			//TextView description = null;

			//data from your adapter
			VectorSectorItem rowData= getItem(position);


			//we want to reuse already constructed row views...
			if(null == convertView){
				convertView = mInflater.inflate(R.layout.custom_row, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			// 
			holder = (ViewHolder) convertView.getTag();
			item = holder.getItem();
			item.setText(rowData.sectorName);

			//description = holder.getDescription();		
			//description.setText(rowData.sectorName);

			return convertView;
		}
	}

	/**
	 * Wrapper for row data.
	 *
	 */
	private class ViewHolder {      
	    private View mRow;
	    //private TextView description = null;
	    private TextView item = null;

		public ViewHolder(View row) {
	    	mRow = row;
		}

		//public TextView getDescription() {
		//	if(null == description){
		//		description = (TextView) mRow.findViewById(R.id.description);
		//	}
		//	return description;
		//}

		public TextView getItem() {
			if(null == item){
				item = (TextView) mRow.findViewById(
						R.id.custom_row_item);
			}
			return item;
		}    	
	}

}

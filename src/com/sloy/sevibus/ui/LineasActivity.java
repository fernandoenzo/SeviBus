package com.sloy.sevibus.ui;


import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.flurry.android.FlurryAgent;
import com.jakewharton.activitycompat2.ActivityCompat2;
import com.jakewharton.activitycompat2.ActivityOptionsCompat2;
import com.sloy.sevibus.R;
import com.sloy.sevibus.utils.Datos;

public class LineasActivity extends SherlockActivity  {

	private ListView mList;
	private LineasAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.onStartSession(this, Datos.FLURRY_KEY);
		setContentView(R.layout.list_activity);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setTitle("L�neas");
		mList = (ListView)findViewById(android.R.id.list);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				Intent intent = new Intent(LineasActivity.this,ParadasActivity.class);
				intent.putExtra("linea", mAdapter.getItemId(pos));
				intent.putExtra("nombre", mAdapter.getItem(pos).getString("nombre"));
				ActivityOptionsCompat2 options = ActivityOptionsCompat2.makeScaleUpAnimation(v, v.getWidth()/2, v.getHeight()/2, v.getWidth() ,v.getHeight());
				ActivityCompat2.startActivity(LineasActivity.this, intent, options.toBundle());
			}
		});
		DataFramework db = null;
		try{
			db = DataFramework.getInstance();
			db.open(this, getPackageName());
			mAdapter = new LineasAdapter(this, db.getEntityList("lineas",null,"nombre"));
		}catch(Exception e){
			Log.e("sevibus",e.toString(),e);
		}finally{
			db.close();
		}
		
		if(mAdapter!=null){
			mList.setAdapter(mAdapter);
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	private class LineasAdapter extends BaseAdapter {

		List<Entity> mItems;
		private Context mContext;

		public LineasAdapter(Context context, List<Entity> items) {
			mItems = items;
			mContext = context;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Entity getItem(int pos) {
			return mItems.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return mItems.get(pos).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Entity item = getItem(position);
			if(convertView == null){
				convertView = View.inflate(mContext, R.layout.list_item_linea, null);
			}
			TextView nombre = (TextView)convertView.findViewById(R.id.item_linea_nombre);
			TextView trayecto = (TextView)convertView.findViewById(R.id.item_linea_trayecto);
			nombre.setText(item.getString("nombre"));
			trayecto.setText(item.getString("trayecto"));
			return convertView;
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSherlock().getMenuInflater();
		inflater.inflate(R.menu.lineas, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				startActivity(new Intent(this, HomeActivity.class));
				return true;
			default:
				return false;
		}
	}

}

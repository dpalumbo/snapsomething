package com.example.snapsomething;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stackmob.sdk.api.StackMobGeoPoint;
import com.stackmob.sdk.api.StackMobOptions;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

public class MasterActivity extends Activity {
	private SnapSomethingApplication snapStackApplication;
	private ImageButton profile_button;
	private ImageButton camera_button;
	private GoogleMap map;
	private LinearLayout transparent_cover;
	private PullToRefreshListView pull_refresh_list;
	private ToggleButton toggle_button;
	private List<Snap> snaps = new ArrayList<Snap>();
	private Handler handler = new Handler();
	private SnapAdapter adapter;
	private GPSTracker gps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_master);
		gps = new GPSTracker(this);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());
		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		}

		camera_button = (ImageButton) findViewById(R.id.camera_button);
		camera_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = snapStackApplication.getUser();
				Snap snap = new Snap(user, null);
				snapStackApplication.setSnap(snap);
				Intent intent = new Intent(MasterActivity.this,
						SharePhotoActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		transparent_cover = (LinearLayout) findViewById(R.id.transparent_cover);
		pull_refresh_list = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		adapter = new SnapAdapter(MasterActivity.this, snaps);
		pull_refresh_list.setAdapter(adapter);
		pull_refresh_list.setRefreshing(true);
		pull_refresh_list
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						loadObjects();
					}
				});
		pull_refresh_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Snap snap = snaps.get(position - 1);
				Intent intent = new Intent(MasterActivity.this,
						DetailViewActivity.class);
				snapStackApplication.setSnap(snap);
				startActivityForResult(intent, 0);
			}
		});
		
		//Todo: load ongoing games
		loadObjects();
	}

	private void setMarkers() {
		if (snaps == null || snaps.size() == 0)
			return;
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < snaps.size(); i++) {
			Snap snap = snaps.get(i);
			LatLng point = new LatLng(snap.getLocation().getLatitude(), snap
					.getLocation().getLongitude());
			builder.include(point);
			map.addMarker(new MarkerOptions().position(point).snippet("" + i));
		}
		if (gps.canGetLocation) {
			LatLng location = new LatLng(gps.latitude, gps.longitude);
			builder.include(location);
		}
		LatLngBounds bounds = builder.build();
		map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
	}

	private class ListUpdater implements Runnable {
		public ListUpdater() {
		}

		public void run() {
			if (snaps.size() == 0) {
				Toast.makeText(MasterActivity.this,
						"Couldn't find any Snaps nearby", Toast.LENGTH_LONG)
						.show();
			}
			adapter = new SnapAdapter(MasterActivity.this, snaps);
			pull_refresh_list.onRefreshComplete();
			pull_refresh_list.setAdapter(adapter);
			setMarkers();
			Animation fadeIn = new AlphaAnimation(0, 1);
			fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
			fadeIn.setDuration(1000);
			pull_refresh_list.setAnimation(fadeIn);
		}
	}

	private void loadObjects() {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			loadObjects();
		}
	}
}

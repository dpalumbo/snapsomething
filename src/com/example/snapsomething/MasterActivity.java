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
		profile_button = (ImageButton) findViewById(R.id.profile_button);
		profile_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MasterActivity.this,
						ProfileActivity.class);
				startActivity(intent);
			}
		});
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setMyLocationEnabled(true);
		map.getUiSettings().setAllGesturesEnabled(false);
		camera_button = (ImageButton) findViewById(R.id.camera_button);
		camera_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gps.getLocation() == null) {
					Builder builder = new AlertDialog.Builder(
							MasterActivity.this);
					builder.setTitle("Oh snap!");
					builder.setCancelable(true);
					builder.setMessage("Couldn't get your location.");
					AlertDialog dialog = builder.create();
					dialog.show();
					return;
				}
				Location location = gps.getLocation();
				StackMobGeoPoint point = new StackMobGeoPoint(location
						.getLongitude(), location.getLatitude());
				User user = snapStackApplication.getUser();
				Snap snap = new Snap(user, point);
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
		toggle_button = (ToggleButton) findViewById(R.id.toggle_button);
		toggle_button
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// Save the state here
						if (isChecked) {
							Animation fadeOut = new AlphaAnimation(1, 0);
							fadeOut.setInterpolator(new AccelerateInterpolator()); // and
																					// this
							fadeOut.setDuration(500);
							transparent_cover.setAnimation(fadeOut);
							pull_refresh_list.setAnimation(fadeOut);
							transparent_cover.setVisibility(View.GONE);
							pull_refresh_list.setVisibility(View.GONE);
							map.getUiSettings().setAllGesturesEnabled(true);
						} else {
							transparent_cover.setVisibility(View.VISIBLE);
							pull_refresh_list.setVisibility(View.VISIBLE);
							Animation fadeIn = new AlphaAnimation(0, 1);
							fadeIn.setInterpolator(new DecelerateInterpolator());
							fadeIn.setDuration(500);
							transparent_cover.setAnimation(fadeIn);
							pull_refresh_list.setAnimation(fadeIn);
							map.getUiSettings().setAllGesturesEnabled(false);
						}
					}
				});
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(final Marker marker) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MasterActivity.this);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = inflater.inflate(R.layout.activity_photo, null);
				builder.setView(v);
				int i = Integer.parseInt(marker.getSnippet());
				Snap snap = snaps.get(i);
				ImageView imageView = (ImageView) v
						.findViewById(R.id.photo_image);
				DisplayImageOptions options = new DisplayImageOptions.Builder()
						.showStubImage(R.drawable.placeholder)
						.showImageForEmptyUri(R.drawable.placeholder)
						.showImageOnFail(R.drawable.placeholder)
						.cacheInMemory().cacheOnDisc()
						.bitmapConfig(Bitmap.Config.RGB_565).build();
				ImageLoader imageLoader = ImageLoader.getInstance();
				imageLoader.displayImage(snap.getPhoto().getS3Url(), imageView,
						options);
				imageView.setAdjustViewBounds(true);
				imageView.setMaxHeight(150);
				imageView.setMaxWidth(150);
				imageView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						int i = Integer.parseInt(marker.getSnippet());
						Snap snap = snaps.get(i);
						Intent intent = new Intent(MasterActivity.this,
								DetailViewActivity.class);
						snapStackApplication.setSnap(snap);
						startActivity(intent);
					}
				});
				builder.setPositiveButton("Show Details",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								int i = Integer.parseInt(marker.getSnippet());
								Snap snap = snaps.get(i);
								Intent intent = new Intent(MasterActivity.this,
										DetailViewActivity.class);
								snapStackApplication.setSnap(snap);
								startActivity(intent);
							}
						});
				builder.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				Dialog dialog = builder.create();
				dialog.show();
				return true;
			}
		});
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
		Location location = gps.getLocation();
		if (location == null) {
			pull_refresh_list.onRefreshComplete();
			Toast.makeText(this, "Couldn't get your location",
					Toast.LENGTH_LONG).show();
			return;
		}
		LatLng currentLocation = new LatLng(location.getLatitude(),
				location.getLongitude());
		// Move the camera instantly to the current location with a zoom of 15.
		map.animateCamera(CameraUpdateFactory
				.newLatLngZoom(currentLocation, 15));
		StackMobGeoPoint point = new StackMobGeoPoint(location.getLongitude(),
				location.getLatitude());
		StackMobQuery query = new StackMobQuery();
		query.fieldIsNear("location", point);
		query.fieldIsOrderedBy("createddate", StackMobQuery.Ordering.DESCENDING);
		query.isInRange(0, 50);
		Snap.query(Snap.class, query, StackMobOptions.depthOf(1),
				new StackMobQueryCallback<Snap>() {
					@Override
					public void success(List<Snap> result) {
						snaps = result;
						handler.post(new ListUpdater());
					}

					@Override
					public void failure(StackMobException e) {
						handler.post(new ListUpdater());
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			loadObjects();
		}
	}
}

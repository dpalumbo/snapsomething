package com.example.snapsomething;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stackmob.sdk.api.StackMobOptions;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobNoopCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

public class ProfileActivity extends Activity {
	SnapSomethingApplication snapStackApplication;
	private PullToRefreshListView pull_refresh_list;
	private List<Snap> snaps = new ArrayList<Snap>();
	private Handler handler = new Handler();
	private SnapAdapter adapter;
	private User user;
	private TextView profile_username;
	private ImageView profile_photo_imageview;
	private DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.default_avatar)
				.showImageForEmptyUri(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
		snapStackApplication = (SnapSomethingApplication) getApplication();
		user = snapStackApplication.getUser();
		profile_photo_imageview = (ImageView) findViewById(R.id.profile_photo_imageview);
		if (user.getPhoto() != null) {
			imageLoader.displayImage(user.getPhoto().getS3Url(),
					profile_photo_imageview, options);
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_avatar);
			profile_photo_imageview.setImageBitmap(bitmap);
		}
		profile_photo_imageview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProfileActivity.this,
						ChoosePhotoActivity.class);
				intent.putExtra("calling_activity", 666);
				startActivityForResult(intent, 0);
			}
		});
		profile_username = (TextView) findViewById(R.id.profile_username);
		profile_username.setText(user.getUsername());
		pull_refresh_list = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
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
				Intent intent = new Intent(ProfileActivity.this,
						DetailViewActivity.class);
				snapStackApplication.setSnap(snap);
				startActivity(intent);
			}
		});
		loadObjects();
	}

	private class ListUpdater implements Runnable {
		public ListUpdater() {
		}

		public void run() {
			if (snaps.size() == 0) {
				Toast.makeText(ProfileActivity.this, "No Snaps found",
						Toast.LENGTH_LONG).show();
			}
			adapter = new SnapAdapter(ProfileActivity.this, snaps);
			pull_refresh_list.onRefreshComplete();
			pull_refresh_list.setAdapter(adapter);
			Animation fadeIn = new AlphaAnimation(0, 1);
			fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
			fadeIn.setDuration(1000);
			pull_refresh_list.setAnimation(fadeIn);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.signout_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.signOut:
			SnapSomethingApplication snapStackApplication = (SnapSomethingApplication) this
					.getApplication();
			snapStackApplication.getUser().logout(new StackMobNoopCallback());
			Intent myIntent = new Intent(this, MainActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadObjects() {
		StackMobQuery query = new StackMobQuery();
		query.fieldIsOrderedBy("createddate", StackMobQuery.Ordering.DESCENDING);
		query.fieldIsEqualTo("creator", user.getUsername());
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
			imageLoader.clearDiscCache();
			imageLoader.clearMemoryCache();
			user = snapStackApplication.getUser();
			imageLoader.displayImage(user.getPhoto().getS3Url(),
					profile_photo_imageview, options);
		}
	}
}

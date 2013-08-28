package com.example.snapsomething;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

public class PhotoViewActivity extends Activity {
	private Snap snap;
	SnapSomethingApplication snapStackApplication;
	DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.placeholder)
				.showImageForEmptyUri(R.drawable.placeholder)
				.showImageOnFail(R.drawable.placeholder).cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
		snapStackApplication = (SnapSomethingApplication) getApplication();
		snap = snapStackApplication.getSnap();
		ImageView imageView = (ImageView) findViewById(R.id.photo_image);
		imageLoader
				.displayImage(snap.getPhoto().getS3Url(), imageView, options);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.delete_menu, menu);
		String username = snap.getCreator().getUsername();
		User user = snapStackApplication.getUser();
		if (username.equals(user.getUsername())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		progressDialog = ProgressDialog.show(PhotoViewActivity.this,
				"Deleting...", "Deleting your snap", true);
		snap.destroy(new StackMobModelCallback() {
			@Override
			public void success() {
				// the call succeeded
				progressDialog.dismiss();
				setResult(RESULT_OK, null);
				finish();
			}

			@Override
			public void failure(StackMobException e) {
				// the call failed
				progressDialog.dismiss();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Builder builder = new AlertDialog.Builder(
								PhotoViewActivity.this);
						builder.setTitle("Uh oh...");
						builder.setCancelable(true);
						builder.setMessage("Couldn't delete snap");
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				});
			}
		});
		return true;
	}
}
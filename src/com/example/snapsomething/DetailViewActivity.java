package com.example.snapsomething;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobCountCallback;
import com.stackmob.sdk.exception.StackMobException;

public class DetailViewActivity extends Activity {
	private Snap snap;
	SnapSomethingApplication snapStackApplication;
	private Button commentsButton;
	DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.placeholder)
				.showImageForEmptyUri(R.drawable.placeholder)
				.showImageOnFail(R.drawable.placeholder).cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
		snapStackApplication = (SnapSomethingApplication) getApplication();
		snap = snapStackApplication.getSnap();
		TextView user_name = (TextView) findViewById(R.id.detail_username);
		user_name.setText(snap.getCreator().getUsername());
		ImageView detail_profile_image = (ImageView) findViewById(R.id.detail_profile_image);
		if (snap.getCreator().getPhoto() != null) {
			imageLoader.displayImage(snap.getCreator().getPhoto().getS3Url(),
					detail_profile_image, options);
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_avatar);
			detail_profile_image.setImageBitmap(bitmap);
		}
		ImageView imageView = (ImageView) findViewById(R.id.detail_image);
		imageLoader
				.displayImage(snap.getPhoto().getS3Url(), imageView, options);
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DetailViewActivity.this,
						PhotoViewActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		commentsButton = (Button) findViewById(R.id.detail_comments);
		commentsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DetailViewActivity.this,
						CommentViewActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		commentsButton.setVisibility(View.GONE);
		countComments();
	}

	private void countComments() {
		Snap snap = snapStackApplication.getSnap();
		StackMobQuery query = new StackMobQuery();
		query.fieldIsEqualTo("snap", snap.getID());
		Comment.count(Comment.class, query, new StackMobCountCallback() {
			@Override
			public void success(long count) {
				// TODO Auto-generated method stub
				String commentLabel;
				if (count == 0) {
					return;
				} else if (count > 1) {
					commentLabel = " Comments";
				} else {
					commentLabel = " Comment";
				}
				final String label = "" + (int) count + commentLabel;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						commentsButton.setText(label);
						commentsButton.setVisibility(View.VISIBLE);
						Animation fadeIn = new AlphaAnimation(0, 1);
						fadeIn.setInterpolator(new DecelerateInterpolator());
						fadeIn.setDuration(500);
						commentsButton.setAnimation(fadeIn);
					}
				});
			}

			@Override
			public void failure(StackMobException arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == 0) {
			setResult(RESULT_OK, null);
			finish();
		}
		if (resultCode == Activity.RESULT_OK && requestCode == 1) {
			setResult(RESULT_OK, null);
			countComments();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.comment_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(DetailViewActivity.this,
				ShareCommentActivity.class);
		startActivityForResult(intent, 1);
		return true;
	}
}
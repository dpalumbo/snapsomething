package com.example.snapsomething;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.stackmob.sdk.api.StackMobOptions;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

public class CommentViewActivity extends Activity {
	List<Comment> comments;
	ListView listView;
	private SnapSomethingApplication snapStackApplication;
	private Handler handler = new Handler();
	CommentAdapter adapter;
	DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.placeholder)
				.showImageForEmptyUri(R.drawable.placeholder)
				.showImageOnFail(R.drawable.placeholder).cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
		listView = (ListView) findViewById(R.id.comment_listview);
		comments = new ArrayList<Comment>();
		snapStackApplication = (SnapSomethingApplication) getApplication();
		adapter = new CommentAdapter(CommentViewActivity.this, comments);
		listView.setAdapter(adapter);
		loadComments();
	}

	public void loadComments() {
		progressDialog = ProgressDialog.show(CommentViewActivity.this,
				"Loading...", "Loading comments", true);
		Snap snap = snapStackApplication.getSnap();
		StackMobQuery query = new StackMobQuery();
		query.fieldIsEqualTo("snap", snap.getID());
		Comment.query(Comment.class, query, StackMobOptions.depthOf(1),
				new StackMobQueryCallback<Comment>() {
					@Override
					public void success(List<Comment> result) {
						progressDialog.dismiss();
						comments = result;
						handler.post(new ListUpdater());
					}

					@Override
					public void failure(StackMobException e) {
						progressDialog.dismiss();
						handler.post(new ListUpdater());
						Builder builder = new AlertDialog.Builder(
								CommentViewActivity.this);
						builder.setTitle("Uh oh...");
						builder.setCancelable(true);
						builder.setMessage("There was an error loading comments.");
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				});
	}

	private class ListUpdater implements Runnable {
		public ListUpdater() {
		}

		public void run() {
			adapter = new CommentAdapter(CommentViewActivity.this, comments);
			listView.setAdapter(adapter);
		}
	}

	private class CommentAdapter extends ArrayAdapter<Comment> {
		private List<Comment> objects;

		public CommentAdapter(Context context, List<Comment> objects) {
			super(context, R.layout.listview_comment_item, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) CommentViewActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.listview_comment_item, null);
			}
			if (objects != null) {
				Comment comment = objects.get(position);
				TextView user_name = (TextView) view
						.findViewById(R.id.comment_username);
				user_name.setText(comment.getCreator().getUsername());
				ImageView comment_item_profile_image = (ImageView) view
						.findViewById(R.id.comment_item_profile_image);
				if (comment.getCreator().getPhoto() != null) {
					imageLoader.displayImage(comment.getCreator().getPhoto()
							.getS3Url(), comment_item_profile_image, options);
				} else {
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.default_avatar);
					comment_item_profile_image.setImageBitmap(bitmap);
				}
				TextView text = (TextView) view.findViewById(R.id.comment_text);
				text.setText(comment.getText());
			}
			return view;
		}
	}
}
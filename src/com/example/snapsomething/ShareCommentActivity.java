package com.example.snapsomething;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

public class ShareCommentActivity extends Activity {
	private SnapSomethingApplication snapStackApplication;
	private EditText comment_edittext;
	private Button share_comment_button;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_comment);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		comment_edittext = (EditText) findViewById(R.id.comment_edittext);
		share_comment_button = (Button) findViewById(R.id.share_comment_button);
		share_comment_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String commentText = comment_edittext.getText().toString();
				if (commentText.trim().length() != 0) {
					progressDialog = ProgressDialog.show(
							ShareCommentActivity.this, "Saving",
							"Saving comment", true);
					User user = snapStackApplication.getUser();
					final Snap snap = snapStackApplication.getSnap();
					Comment comment = new Comment(user, commentText, snap);
					comment.save(new StackMobModelCallback() {
						@Override
						public void success() {
							// the call succeeded
							progressDialog.dismiss();
							setResult(RESULT_OK, null);
							finish();
						}

						@Override
						public void failure(StackMobException e) {
							progressDialog.dismiss();
							// the call failed
							threadAgnosticDialog(ShareCommentActivity.this,
									"There was an error saving your comment.");
						}
					});
				}
			}
		});
	}

	private void threadAgnosticDialog(final Context ctx, final String txt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Builder builder = new AlertDialog.Builder(ctx);
				builder.setCancelable(true);
				builder.setMessage(txt);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
}
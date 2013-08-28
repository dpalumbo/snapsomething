package com.example.snapsomething;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.model.StackMobUser;

public class ForgotPasswordActivity extends Activity {
	private EditText username_edittext;
	private Button forgot_password_button;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		username_edittext = (EditText) findViewById(R.id.username_edittext);
		forgot_password_button = (Button) findViewById(R.id.forgot_password_button);
		forgot_password_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = username_edittext.getText().toString();
				if (username.trim().length() != 0) {
					progressDialog = ProgressDialog.show(
							ForgotPasswordActivity.this, "Saving",
							"Sending email", true);
					StackMobUser.sentForgotPasswordEmail(username,
							new StackMobModelCallback() {
								@Override
								public void success() {
									progressDialog.dismiss();
									Intent intent = new Intent(
											ForgotPasswordActivity.this,
											ChangePasswordActivity.class);
									startActivity(intent);
								}

								@Override
								public void failure(StackMobException e) {
									progressDialog.dismiss();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Builder builder = new AlertDialog.Builder(
													ForgotPasswordActivity.this);
											builder.setTitle("Uh oh...");
											builder.setCancelable(true);
											builder.setMessage("Unable to recover password.");
											AlertDialog dialog = builder
													.create();
											dialog.show();
										}
									});
								}
							});
				}
			}
		});
	}
}
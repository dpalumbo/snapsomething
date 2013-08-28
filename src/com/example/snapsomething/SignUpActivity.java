package com.example.snapsomething;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

public class SignUpActivity extends Activity {
	private SnapSomethingApplication snapStackApplication;
	private EditText username_edittext;
	private EditText password_edittext;
	private EditText email_edittext;
	private Button join_button;
	private ProgressDialog progressDialog;
	private Handler handler = new Handler();
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		// Find our views
		username_edittext = (EditText) findViewById(R.id.username_edittext);
		password_edittext = (EditText) findViewById(R.id.password_edittext);
		email_edittext = (EditText) findViewById(R.id.email_edittext);
		join_button = (Button) findViewById(R.id.join_button);
		join_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(username_edittext.getWindowToken(),
						0);
				imm.hideSoftInputFromWindow(password_edittext.getWindowToken(),
						0);
				imm.hideSoftInputFromWindow(email_edittext.getWindowToken(), 0);
				progressDialog = ProgressDialog.show(SignUpActivity.this,
						"Signing up", "Signing up for SnapStack", true);
				String username = username_edittext.getText().toString().trim();
				String password = password_edittext.getText().toString().trim();
				String email = email_edittext.getText().toString().trim();
				if (foundError(username, password, email)) {
					progressDialog.dismiss();
					return;
				}
				User user = new User(username, password, email);
				snapStackApplication.setUser(user);
				user.save(new StackMobModelCallback() {
					@Override
					public void success() {
						handler.post(new UserLogin());
					}

					@Override
					public void failure(StackMobException e) {
						progressDialog.dismiss();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Builder builder = new AlertDialog.Builder(
										SignUpActivity.this);
								builder.setTitle("Uh oh...");
								builder.setCancelable(true);
								builder.setMessage("There was an error signing up.");
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						});
					}
				});
			}
		});
	}

	public boolean foundError(String username, String password, String email) {
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Oops");
		builder.setCancelable(true);
		if (username.equals("")) {
			builder.setMessage("Don't forget to enter a username!");
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		} else if (password.equals("")) {
			builder.setMessage("Don't forget to enter a password!");
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		} else if (email.equals("")) {
			builder.setMessage("Don't forget to enter an email");
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		} else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email)
				.matches()) {
			builder.setMessage("Please enter a valid email address.");
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		}
		return false;
	}

	private class UserLogin implements Runnable {
		public UserLogin() {
		}

		public void run() {
			user = snapStackApplication.getUser();
			user.login(new StackMobModelCallback() {
				@Override
				public void success() {
					progressDialog.dismiss();
					snapStackApplication.setUser(user);
					Intent intent = new Intent(SignUpActivity.this,
							ChoosePhotoActivity.class);
					intent.putExtra("calling_activity", 333);
					startActivity(intent);
					finish();
				}

				@Override
				public void failure(StackMobException e) {
					progressDialog.dismiss();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Builder builder = new AlertDialog.Builder(
									SignUpActivity.this);
							builder.setTitle("Uh oh...");
							builder.setCancelable(true);
							builder.setMessage("There was an error logging in.");
							AlertDialog dialog = builder.create();
							dialog.show();
						}
					});
				}
			});
		}
	}
}
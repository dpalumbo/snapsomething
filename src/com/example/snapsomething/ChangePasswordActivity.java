package com.example.snapsomething;

import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ChangePasswordActivity extends Activity {
	private SnapSomethingApplication snapStackApplication;
	private EditText username_edittext;
	private EditText temporary_password_edittext;
	private EditText password_edittext;
	private Button signin_button;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		// Find our views
		username_edittext = (EditText) findViewById(R.id.username_edittext);
		password_edittext = (EditText) findViewById(R.id.password_edittext);
		temporary_password_edittext = (EditText) findViewById(R.id.temporary_password_edittext);
		signin_button = (Button) findViewById(R.id.signin_button);
		signin_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(username_edittext.getWindowToken(),
						0);
				imm.hideSoftInputFromWindow(
						temporary_password_edittext.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(password_edittext.getWindowToken(),
						0);
				progressDialog = ProgressDialog.show(
						ChangePasswordActivity.this, "Signing in",
						"Signing into SnapStack", true);
				String username = username_edittext.getText().toString().trim();
				String temp = temporary_password_edittext.getText().toString()
						.trim();
				String password = password_edittext.getText().toString().trim();
				if (foundError(username, password)) {
					progressDialog.dismiss();
					return;
				}
				User user = new User(username, temp, null);
				snapStackApplication.setUser(user);
				user.loginResettingTemporaryPassword(password,
						new StackMobModelCallback() {
							@Override
							public void success() {
								progressDialog.dismiss();
								Intent intent = new Intent(
										ChangePasswordActivity.this,
										MasterActivity.class);
								startActivity(intent);
							}

							@Override
							public void failure(StackMobException e) {
								progressDialog.dismiss();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Builder builder = new AlertDialog.Builder(
												ChangePasswordActivity.this);
										builder.setTitle("Uh oh...");
										builder.setCancelable(true);
										builder.setMessage("There was an error signing in.");
										AlertDialog dialog = builder.create();
										dialog.show();
									}
								});
							}
						});
			}
		});
	}

	public boolean foundError(String username, String password) {
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
		}
		return false;
	}
}
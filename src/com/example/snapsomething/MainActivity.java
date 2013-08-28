package com.example.snapsomething;

import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.stackmob.android.sdk.common.StackMobAndroid;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

public class MainActivity extends Activity {
	private Button sign_up;
	private Button sign_in;
	private SnapSomethingApplication snapStackApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StackMobAndroid.init(getApplicationContext(), 0,
				"3d22a61d-a343-43f1-8051-a9b59a97b699");
		StackMob.getStackMob().getSession().getLogger().setLogging(true);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		GPSTracker gps = new GPSTracker(this);
		if (!gps.canGetLocation()) {
			gps.showSettingsAlert();
		}
		// Find our buttons
		sign_up = (Button) findViewById(R.id.signup_button);
		sign_in = (Button) findViewById(R.id.signin_button);
		// Set an OnClickListener for the sign_up button
		sign_up.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SignUpActivity.class);
				startActivity(intent);
			}
		});
		// Set an OnClickListener for the sign_in button
		sign_in.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SignInActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (StackMob.getStackMob().isLoggedIn()) {
			final ProgressDialog progressDialog = ProgressDialog.show(
					MainActivity.this, "Signing in", "Signing back in", true);
			User.getLoggedInUser(User.class, new StackMobQueryCallback<User>() {
				@Override
				public void success(List<User> list) {
					progressDialog.dismiss();
					User user = list.get(0);
					snapStackApplication.setUser(user);
					Intent intent = new Intent(MainActivity.this,
							MasterActivity.class);
					startActivity(intent);
				}

				@Override
				public void failure(StackMobException e) {
					progressDialog.dismiss();
					Toast.makeText(MainActivity.this, "Couldn't sign back in",
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
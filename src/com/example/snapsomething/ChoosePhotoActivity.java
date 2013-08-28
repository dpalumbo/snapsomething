package com.example.snapsomething;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.stackmob.sdk.api.StackMobFile;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

public class ChoosePhotoActivity extends Activity {
	private SnapSomethingApplication snapStackApplication;
	private Uri imageCaptureUri;
	private ImageView choose_photo_imageview;
	private Button choose_photo_button;
	private ProgressDialog progressDialog;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_photo);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		final String[] items = new String[] { "Take from camera",
				"Select from gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) { // pick from
			// camera
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					imageCaptureUri = Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), "tmp_avatar_"
							+ String.valueOf(System.currentTimeMillis())
							+ ".jpg"));
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
							imageCaptureUri);
					try {
						intent.putExtra("return-data", true);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { // pick from file
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent,
							"Complete action using"), PICK_FROM_FILE);
				}
			}
		});
		final AlertDialog dialog = builder.create();
		choose_photo_button = (Button) findViewById(R.id.choose_photo_button);
		choose_photo_button.setEnabled(false);
		choose_photo_imageview = (ImageView) findViewById(R.id.choose_photo_imageview);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_avatar);
		choose_photo_imageview.setImageBitmap(bitmap);
		choose_photo_imageview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});
		choose_photo_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(ChoosePhotoActivity.this,
						"Uploading photo", "Uploading your profile pic", true);
				Bitmap bitmap = ((BitmapDrawable) choose_photo_imageview
						.getDrawable()).getBitmap();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] image = stream.toByteArray();
				User user = snapStackApplication.getUser();
				user.setPhoto(new StackMobFile("image/jpeg",
						"profile_picture.jpg", image));
				user.save(new StackMobModelCallback() {
					@Override
					public void success() {
						progressDialog.dismiss();
						int callingActivity = getIntent().getIntExtra(
								"calling_activity", 0);
						if (callingActivity == 666) {
							setResult(RESULT_OK, null);
							finish();
						} else if (callingActivity == 333) {
							Intent intent = new Intent(
									ChoosePhotoActivity.this,
									MasterActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					}

					@Override
					public void failure(StackMobException e) {
						progressDialog.dismiss();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Builder builder = new AlertDialog.Builder(
										ChoosePhotoActivity.this);
								builder.setTitle("Uh oh...");
								builder.setCancelable(true);
								builder.setMessage("There was an error saving your photo.");
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						});
					}
				});
			}
		});
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case PICK_FROM_CAMERA:
			doCrop();
			break;
		case PICK_FROM_FILE:
			imageCaptureUri = data.getData();
			doCrop();
			break;
		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				choose_photo_imageview.setImageBitmap(photo);
				choose_photo_button.setEnabled(true);
			}
			File f = new File(imageCaptureUri.getPath());
			if (f.exists())
				f.delete();
			break;
		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);
		int size = list.size();
		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			intent.setData(imageCaptureUri);
			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);
			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);
				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));
				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();
					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);
					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));
					cropOptions.add(co);
				}
				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_FROM_CAMERA);
							}
						});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						if (imageCaptureUri != null) {
							getContentResolver().delete(imageCaptureUri, null,
									null);
							imageCaptureUri = null;
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}
}
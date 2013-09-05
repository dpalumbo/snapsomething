package com.example.snapsomething;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SharePhotoActivity extends Activity implements
	SurfaceHolder.Callback {
	SnapSomethingApplication snapStackApplication;
	private Uri imageCaptureUri;
	private ImageView share_photo_imageview;
	private ImageView send_photo;
	private Button share_photo_button;
	private ImageView take_photo;
	private ProgressDialog progressDialog;
	private boolean inPreview = false;
    private boolean cameraConfigured = false;
	
	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean cameraview = false;
	LayoutInflater inflater = null;
	private ToggleButton flip_camera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_photo);
		snapStackApplication = (SnapSomethingApplication) getApplication();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// imageCaptureUri = Uri.fromFile(new File(Environment
		// .getExternalStorageDirectory(), "tmp_avatar_"
		// + String.valueOf(System.currentTimeMillis())
		// + ".jpg"));
		// intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
		// imageCaptureUri);
		// try {
		// intent.putExtra("return-data", true);
		// startActivityForResult(intent, PICK_FROM_CAMERA);
		// } catch (ActivityNotFoundException e) {
		// e.printStackTrace();
		// }
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


		inflater = LayoutInflater.from(getBaseContext());
		View view = inflater.inflate(R.layout.overlay, null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(view, layoutParamsControl);
		flip_camera = (ToggleButton) findViewById(R.id.flip_camera);
		
		take_photo = (ImageView) findViewById(R.id.take_photo);
		send_photo = (ImageView) findViewById(R.id.send_photo);
		send_photo.setVisibility(View.INVISIBLE);
		
		take_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flip_camera.setVisibility(View.INVISIBLE);
				take_photo.setVisibility(View.INVISIBLE);
				send_photo.setVisibility(View.VISIBLE);
				takePicture();
			}
		});
		
		send_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				User user = snapStackApplication.getUser();
				Snap snap = new Snap(user, null);
				snapStackApplication.setSnap(snap);
				//NEEDED: ADD BITMAP TO INTENT TO SEND IT TO CHOOSE USER ACTIVITY
				
				Intent intent = new Intent(SharePhotoActivity.this,
						ChooseUserActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		
		 flip_camera.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		        @Override
		        public void onCheckedChanged(CompoundButton buttonView,
		                boolean isChecked) {
		        	if(camera!=null) {
		              restartPreview(isChecked);
		        	} else {
		        		//TODO: change this to return the oncreate methods
		        		startPreview();		        		
		        	}
		        }
		    });
		 
		// final AlertDialog dialog = builder.create();
		// share_photo_button = (Button) findViewById(R.id.share_photo_button);
		// share_photo_button.setEnabled(false);
		// share_photo_imageview = (ImageView)
		// findViewById(R.id.share_photo_imageview);
		// Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
		// R.drawable.placeholder);
		// share_photo_imageview.setImageBitmap(bitmap);
		// share_photo_imageview.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// dialog.show();
		// }
		// });
		// share_photo_button.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// progressDialog = ProgressDialog.show(SharePhotoActivity.this,
		// "Saving...", "Saving your snap", true);
		// Bitmap bitmap = ((BitmapDrawable) share_photo_imageview
		// .getDrawable()).getBitmap();
		// ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		// byte[] image = stream.toByteArray();
		// Snap snap = snapStackApplication.getSnap();
		// snap.setPhoto(new StackMobFile("image/jpeg",
		// "profile_picture.jpg", image));
		// snap.save(new StackMobModelCallback() {
		// @Override
		// public void success() {
		// progressDialog.dismiss();
		// threadAgnosticDialog(SharePhotoActivity.this,
		// "Your photo was shared to SnapStack!");
		// setResult(RESULT_OK, null);
		// finish();
		// }
		//
		// @Override
		// public void failure(StackMobException e) {
		// progressDialog.dismiss();
		// threadAgnosticDialog(SharePhotoActivity.this,
		// "There was an error saving your photo.");
		// }
		// });
		// }
		// });
	}

	@Override
	public void onResume() {
	    super.onResume();

	    // camera=Camera.open();
	    int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
	    if (Camera.getNumberOfCameras() > 1
	            && camId < Camera.getNumberOfCameras() - 1) {
	        // startCamera(camId + 1);
	        camera = Camera.open(camId + 1);
	    } else {
	        // startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
	        camera = Camera.open(camId);
	    }
	    startPreview();
	}

void restartPreview(boolean isFront) {
    if (inPreview) {
        camera.stopPreview();
    }
    //
    camera.release();

    // camera=null;
    // inPreview=false;
    // /*int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    // if (Camera.getNumberOfCameras() > 1 && camId <
    // Camera.getNumberOfCameras() - 1) {
    // //startCamera(camId + 1);
    // camera = Camera.open(camId + 1);
    // } else {
    // //startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    // camera = Camera.open(camId);
    // }*/
    int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    if (isFront) {
        camera = Camera.open(camId);
        initPreview(0,0);
        startPreview();

    } else {
        camera = Camera.open(camId + 1);
        initPreview(0,0);
        startPreview();

    }
    // startPreview();
}

@Override
public void onPause() {
	if(camera != null) {
	    if (inPreview) {
	        camera.stopPreview();
	    }
	
	    camera.release();
	    camera = null;
	    inPreview = false;
	}
	
    super.onPause();
}

private Camera.Size getBestPreviewSize(int width, int height,
        Camera.Parameters parameters) {
    Camera.Size result = null;

    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
        if (size.width <= width && size.height <= height) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }
    }

    return (result);
}

public void takePicture() {  
	//WHY IS THIS CRASHING MY PHONE
    camera.takePicture(null, null, new PhotoHandler(this));
    camera.stopPreview();
    camera.release();
    camera = null;
}

private void initPreview(int width, int height) {
    if (camera != null && surfaceHolder.getSurface() != null) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Throwable t) {
            Log.e("PreviewDemo-surfaceCallback",
                    "Exception in setPreviewDisplay()", t);
            Toast.makeText(SharePhotoActivity.this, t.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        if (!cameraConfigured) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                cameraConfigured = true;
            }
        }
    }
}

private void startPreview() {
    if (cameraConfigured && camera != null) {
    	camera.setDisplayOrientation(90);
        camera.startPreview();
        inPreview = true;
    }
}

//SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
    public void surfaceCreated(SurfaceHolder holder) {
        // no-op -- wait until surfaceChanged()
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        initPreview(width, height);
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // no-op
        if (camera != null) {
            /*
             * Call stopPreview() to stop updating the preview surface.
             */
            camera.stopPreview();

            /*
             * Important: Call release() to release the camera for use by
             * other applications. Applications should release the camera
             * immediately in onPause() (and re-open() it in onResume()).
             */
            camera.release();

            camera = null;
        }
    }

//};
}
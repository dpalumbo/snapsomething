package com.example.snapsomething;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceView;

public class PhotoHandler implements PictureCallback {

    private final Activity activity;
    private Bitmap bmp; 
    private SurfaceView surfaceView;

    public PhotoHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
    	 surfaceView = (SurfaceView) activity.findViewById(R.id.cameraview);
         bmp = BitmapFactory.decodeByteArray(data , 0, data.length);
         Bitmap bmpMutable = bmp.copy(Bitmap.Config.ARGB_8888, true);
         Canvas canvas = new Canvas(bmpMutable);
         surfaceView.draw(canvas);
    }

}
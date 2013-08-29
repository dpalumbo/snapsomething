package com.example.snapsomething;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {

    private final Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File pictureFileDir =  new File(sdDir, "/CameraAPIDemo");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d("", "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();

            // TODO: Merge the photo
            try {
                Bitmap bottomImage = BitmapFactory.decodeFile(pictureFile.getAbsolutePath()); //blue

                Bitmap bitmap = Bitmap.createBitmap(bottomImage.getWidth(), bottomImage.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                Resources res = context.getResources();

                Bitmap topImage = BitmapFactory.decodeResource(res, topPhotoLong.intValue()); //green
                Drawable drawable1 = new BitmapDrawable(bottomImage);
                Drawable drawable2 = new BitmapDrawable(topImage);


                drawable1.setBounds(0, 0, bottomImage.getWidth(), bottomImage.getHeight());
                drawable2.setBounds(0, 0, bottomImage.getWidth(), bottomImage.getHeight());
                drawable1.draw(c);
                drawable2.draw(c);


            } catch (Exception e) {
            }
            // To write the file out to the SDCard:
            OutputStream os = null;
            try {
                os = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, os);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } catch (Exception error) {
            Log.d("", "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

}
}
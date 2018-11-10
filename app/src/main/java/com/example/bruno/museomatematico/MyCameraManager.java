package com.example.bruno.museomatematico;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class MyCameraManager {

        Activity my_activity;
        ObjRenderer obj_v_frag;

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        protected float changeBitmap(Bitmap b){
            float min_luminance = -1;
            int min_i = 0, min_j = 0;
            for(int i = 0; i < b.getWidth(); i++){
                for(int j = 0; j < b.getHeight(); j++){
                    if(Color.luminance(b.getPixel(i,j)) > min_luminance || min_luminance == -1){
                        min_luminance = Color.luminance(b.getPixel(i,j));
                        min_i = i;
                        min_j = j;
                    }
                }
            }
            Bitmap b2;
            b2 = b.copy(b.getConfig(), true);
            return min_luminance;

        }


        private Camera mCamera = null;
        protected void initCamera(Activity activity, boolean alreadyAsked, ObjRenderer o) {
            my_activity = activity;
            obj_v_frag = o;
            Log.i("h", "AntonioCheca NUMERO DE CAMARAS " + Camera.getNumberOfCameras());
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            // Here, MainActivity is the current activity
            if (ContextCompat.checkSelfPermission(my_activity.getApplicationContext(),
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(my_activity,
                        Manifest.permission.CAMERA)) {

                    if(!alreadyAsked) {
                        //Toast.makeText(my_activity.getApplicationContext(), "No hay acceso a la cámara", Toast.LENGTH_LONG).show();
                    }
                    // Show an expanation to the user *asynchronously* -- don't block
                    // my_activity thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.
                    if(!alreadyAsked) {
                        Log.i("h", "AntonioCheca Pidiendo permiso de cámara");
                        ActivityCompat.requestPermissions(my_activity,
                                new String[]{Manifest.permission.CAMERA},
                                2);
                    }

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            else {
                int camId = 1;
                mCamera = Camera.open();
                /*if (camId == 0) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                }*/
                try {

                    Log.i("h", "AntonioCheca He podido abrir la camara");

                    if(mCamera != null) {
                        Camera.Parameters params = mCamera.getParameters();
                        params.setPreviewSize(320, 240);
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        params.setPictureFormat(ImageFormat.JPEG);
                        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                        mCamera.setPreviewTexture(new SurfaceTexture(8));
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                        //mCamera.reconnect();

                        //   takeFocusedPicture();
                        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                                Log.i("h", "AntonioCheca " + bytes.length);

                                if (bytes.length > 0)
                                    Log.i("h", "AntonioCheca " + bytes[400]);
                                Log.i("h", "AntonioCheca picture-taken");
                                // Convert bytes data into a Bitmap
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                ImageView imageView = new ImageView(my_activity);
                                // Set the Bitmap data to the ImageView

                                obj_v_frag.setCameraLight(changeBitmap(bmp));
                                // Get the Root View of the layout
                                ViewGroup layout = (ViewGroup) my_activity.findViewById(android.R.id.content);
                                // Add the ImageView to the Layout
                                layout.addView(imageView);
                                mCamera.release();
                            }
                        });

                    }
                    //mCamera.release();
                } catch (IOException e) {
                    Log.e(my_activity.getString(R.string.app_name), "AntonioCheca failed to open Camera");
                    e.printStackTrace();
                }
            }
        }

}

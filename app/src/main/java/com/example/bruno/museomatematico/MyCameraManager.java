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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;

/* La clase MyCameraManager se encarga de la gestión de los permisos de la cámara, de hacer las
fotos y de analizarlas. Se la llama con la función "initCamera" desde otros procesos para que
inicie la cámara, pida permisos si es necesario y luego devuelva el valor de más luminancia
al renderer.
La autoría de esta clase es casi enteramente nuestra, salvo algunas partes y líneas
 */
public class MyCameraManager {

        /*Los objetos de la clase son la actividad desde que se la llama (necesaria para pedir permisos
        y el renderer en el que devolveremos los valores de luminancia
         */
        Activity my_activity;
        ObjRenderer obj_renderer;

        /* changeBitmap es una función que dado el Bitmap b devuelve el valor de más luminancia
         */
        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        protected float changeBitmap(Bitmap b){
            float max_luminance = -1;
            int min_i = 0, min_j = 0;
            for(int i = 0; i < b.getWidth(); i++){
                for(int j = 0; j < b.getHeight(); j++){
                    if(Color.luminance(b.getPixel(i,j)) > max_luminance || max_luminance == -1){
                        max_luminance = Color.luminance(b.getPixel(i,j));
                        min_i = i;
                        min_j = j;
                    }
                }
            }
            return max_luminance;

        }


        //Necesitamos también el objeto mCamera, que será la cámara que usemos
        private Camera mCamera = null;
        /*En la función initCamera, aparte de la actividad y el renderer necesitamos un booleano
        que nos avisa si hemos preguntado ya o no sobre los permisos de la cámara
         */
        protected void initCamera(Activity activity, boolean alreadyAsked, ObjRenderer o) {
            /* Iniciamos los objetos que necesitaremos más adelante, la actividad y el renderer
            Por otro lado, liberamos la cámara por si algún otro proceso la seguía teniendo
             */
            my_activity = activity;
            obj_renderer = o;

            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            /*
            A continuación pedimos los permisos de la cámara si aún no los tenemos.
            Este código no es nuestro y proviene de https://developer.android.com/training/permissions/requesting
             */
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
                mCamera = Camera.open();
                try {

                    if(mCamera != null) {
                        /* Iniciamos los parámetros de la cámara con el mínimo de tamaño, sin flash
                        para que no moleste y en formato JPG
                         */
                        Camera.Parameters params = mCamera.getParameters();
                        params.setPreviewSize(320, 240);
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        params.setPictureFormat(ImageFormat.JPEG);
                        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                        /* Asignamos una textura de preview cualquiera y tras asignar los parámetros
                        a la cámara empezamos la preview, para poder hacer la foto
                         */
                        mCamera.setPreviewTexture(new SurfaceTexture(8));
                        mCamera.setParameters(params);
                        mCamera.startPreview();

                        /* Esta es la función que toma una foto y le pasa la luminancia al renderer
                        una vez hecha
                         */
                        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {

                                Log.i("h", "picture-taken");
                                /*Transformamos el vector de bytes en una imagen bitmap mediante la función
                                decodeByteArray
                                 */
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                /* Una vez con el bitmap, llamamos a changeBitmap para conseguir la luminancia
                                y se la pasamos al renderer con la función setCameraLight
                                 */
                                obj_renderer.setCameraLight(changeBitmap(bmp));

                                /* El código comentado es por si queremos ver las fotos que aparecen
                                Este código proviene de https://stackoverflow.com/a/39136852
                                 */
                                // ImageView imageView = new ImageView(my_activity);
                                // Set the Bitmap data to the ImageView
                                // Get the Root View of the layout
                                // ViewGroup layout = (ViewGroup) my_activity.findViewById(android.R.id.content);
                                // Add the ImageView to the Layout
                                // layout.addView(imageView);
                                mCamera.release();
                            }
                        });

                    }
                    //mCamera.release();
                } catch (IOException e) {
                    /* Recogemos la excepción de la preview, en el caso de que no hayamos podido
                    hacer el proceso de coger una foto
                     */
                    Log.e(my_activity.getString(R.string.app_name), "AntonioCheca failed to open Camera");
                    e.printStackTrace();
                }
            }
        }

}

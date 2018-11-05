package com.example.bruno.museomatematico;


import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.signum;


public class ObjRenderer extends Renderer {
    private Sphere mEarthSphere;
    private Object3D mObj;
    private DirectionalLight mDirectionalLigth;
    private Sensor mRotationVectorSensor;
    private double[] mCurrentOrientation = {0.0, 0.0};
    private double[] mReferenceOrientation = null;


    private final SensorEventListener rotationVectorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedRotationMatrix);

            // Convert to orientations
            float[] orientations = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientations);

            if (mReferenceOrientation == null) {
                mReferenceOrientation = new double[3];
                mReferenceOrientation[0] = orientations[1];
                mReferenceOrientation[1] = orientations[0];
            } else {
                mCurrentOrientation[0] = orientations[1];
                mCurrentOrientation[1] = orientations[0];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    public ObjRenderer(Context context, SensorManager sensorManager) {
        super(context);
        this.mContext = context;
        setFrameRate(60);
        mRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(rotationVectorListener, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void initScene() {
        getCurrentScene().setBackgroundColor(0.96f,0.96f,0.96f, 1.0f);
        mDirectionalLigth = new DirectionalLight(1f, .2f, -1.0f);
        mDirectionalLigth.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLigth.setPower(2);
        getCurrentScene().addLight(mDirectionalLigth);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColorInfluence(0);
        /*Texture earthTexture = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try {
            material.addTexture(earthTexture);
        } catch (ATexture.TextureException error) {
            Log.d("ObjRenderer.initScene", error.toString());
        }*/

        mEarthSphere = new Sphere(1, 24, 24);
        /*LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                mTextureManager, R.raw.torus);
        try {
            objParser.parse();
            mObj = objParser.getParsedObject();
            getCurrentScene().addChild(mObj);

        } catch (ParsingException e) {
            e.printStackTrace();
        }*/
        mEarthSphere.setMaterial(material);
        getCurrentScene().addChild(mEarthSphere);
        getCurrentCamera().setZ(4.2f);
    }


    private double[] getRotationSpeed(){
      double[] rotationSpeed = {0.0, 0.0};

      if (mReferenceOrientation != null) {
          rotationSpeed[0] = mCurrentOrientation[0] - mReferenceOrientation[0];
          rotationSpeed[0] = min(pow(abs(rotationSpeed[0]) + 0.5, 2), pow(PI/3 + 0.5, 2))*signum(rotationSpeed[0]);
          rotationSpeed[1] = mCurrentOrientation[1] - mReferenceOrientation[1];
          rotationSpeed[1] = min(pow(abs(rotationSpeed[1]) + 0.5, 2), pow(PI/3 + 0.5, 2))*signum(rotationSpeed[1]);
      }

      return rotationSpeed;
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        double[] rotationSpeed = getRotationSpeed();
        mEarthSphere.rotate(Vector3.Axis.X, rotationSpeed[0]);
        mEarthSphere.rotate(Vector3.Axis.Y, rotationSpeed[1]);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}

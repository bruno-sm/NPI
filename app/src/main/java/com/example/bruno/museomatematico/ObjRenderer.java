package com.example.bruno.museomatematico;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;


public class ObjRenderer extends Renderer {
    private Sphere mEarthSphere;
    private DirectionalLight mDirectionalLigth;


    public ObjRenderer(Context context) {
        super(context);
        this.mContext = context;
        setFrameRate(60);
    }


    @Override
    protected void initScene() {
        mDirectionalLigth = new DirectionalLight(1f, .2f, -1.0f);
        mDirectionalLigth.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLigth.setPower(2);
        getCurrentScene().addLight(mDirectionalLigth);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColorInfluence(0);
        Texture earthTexture = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try {
            material.addTexture(earthTexture);
        } catch (ATexture.TextureException error) {
            Log.d("ObjRenderer.initScene", error.toString());
        }

        mEarthSphere = new Sphere(1, 24, 24);
        mEarthSphere.setMaterial(material);
        getCurrentScene().addChild(mEarthSphere);
        getCurrentCamera().setZ(4.2f);
    }


    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        mEarthSphere.rotate(Vector3.Axis.Y, 1.0);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}

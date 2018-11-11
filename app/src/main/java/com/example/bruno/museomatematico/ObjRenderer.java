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
import org.rajawali3d.lights.ALight;
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
/* La clase que se encarga de renderizar los objetos. Es el que pinta los objetos y hace todos los
cambios sobre la escena como el cambio de luz, rotación o zoom. Para hacer todas estas acciones
necesita la información de sensores que obtiene a partir de un sensorManager.

También se define el touchListener que se utiliza luego en el multitouch, el que se encarga de pasar
los movimientos con los dedos a transformaciones en la escena.
 */

public class ObjRenderer extends Renderer {
    // Una esfera para visualizar
    private Sphere mEarthSphere;
    // Un Object3D llamado mObj, es que el que contendrá los objetos
    private Object3D mObj;
    // Una luz direccional para la escena
    private DirectionalLight mDirectionalLigth;
    // Un sensor de rotación para detectar cuánto debería girar cada objeto.
    private Sensor mRotationVectorSensor;
    // Dos doubles que guardan la orientación actual del móvil
    private double[] mCurrentOrientation = {0.0, 0.0};
    // Dos doubles que guardarán (al principio son null) la rotación de referencia (la inicial)
    private double[] mReferenceOrientation = null;
    // Un ObjInformation para conseguir las diferentes informaciones de los objetos a renderizar
    private ObjInformation mObjInfo;
    // El double que guarda el zoom
    double mZoom;


    /* El listener para el vector de rotación, tiene una función de onSensorChanged que se activa
    cuando se gira el móvil, con la que actualiza la orientación de referencia y la actual
     */
    private final SensorEventListener rotationVectorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            /* Recoge la información de la rotación del sensorManager y le aplica un remapCoordinateSystem
            para transformar la matriz de rotación a una más fácil de utilizar para la rotación del
            móvil. Parte del código es de la web
            https://code.tutsplus.com/es/tutorials/android-sensors-in-depth-proximity-and-gyroscope--cms-28084
             */
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

            //Actualizamos las orientaciones actuales y de referencia
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

    /* Constructor a partir de un contexto, el sensorManager y un ObjInformation.
    Se encarga de asignar las variables de contexto e información de objetos de la clase
    y de activar el listener del vector de rotación definido anteriormente.
     */
    public ObjRenderer(Context context, SensorManager sensorManager, ObjInformation objInfo) {
        super(context);
        this.mContext = context;
        mObjInfo = objInfo;
        setFrameRate(60);
        mRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(rotationVectorListener, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
        mZoom = 15.0;
    }

    /* Cambia la intensidad de la luz. Esta función es la que se llama desde la cámara para que se
    vea la intensidad de la foto en los modelos
     */
    protected void setCameraLight(float camera_light){
        Log.i("h", "AntonioCheca La luminancia es " + camera_light);
        Log.i("h", "AntonioCheca Hay un total de  " + getCurrentScene().getLights().size());


        if(getCurrentScene().getLights().size() > 0){
            /*ALight l = getCurrentScene().getLightsCopy().get(0);
            l.setPower(camera_light);
            getCurrentScene().removeLight(getCurrentScene().getLights().get(0));
            getCurrentScene().addLight(l);*/
            camera_light += 0.5;
            getCurrentScene().getLights().get(0).setPower(camera_light);
        }

        //onRender(0,0);
    }

    /* initScene es la función que se llama por primera vez al renderizar una escena. Se encarga
    de inicializar los valores de luz, el objeto y el material para pintarlo correctamente.
    Parte de esta función (y del resto del renderer) vienen de la web de tutoriales de Rajawali
    https://github.com/Rajawali/Rajawali/wiki/Anchor-01-Basic-Scene-Android-Studio
     */
    @Override
    protected void initScene() {
        // Iniciamos la luz direccional inicial y el color de fondo
        getCurrentScene().setBackgroundColor(0.96f,0.96f,0.96f, 1.0f);
        mDirectionalLigth = new DirectionalLight(1f, .2f, -1.0f);
        mDirectionalLigth.setColor(1.0f, 1.0f, 1.0f);
        mDirectionalLigth.setPower(2f);

        // Añadimos la luz direccional a la escena
        getCurrentScene().addLight(mDirectionalLigth);

        // Creamos un material
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

        /* Cargamos el objeto a pintar con LoaderOBJ y el nombre del fichero del objeto que cogemos
        mediante el mObjInfo
         */
        //mEarthSphere = new Sphere(1, 24, 24);
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, mObjInfo.getObjFile());
        try {
            objParser.parse();
            mObj = objParser.getParsedObject();
            // Añadimos el objeto a la escena
            getCurrentScene().addChild(mObj);

        } catch (ParsingException e) {
            e.printStackTrace();
        }
        /*
        mEarthSphere.setMaterial(material);
        getCurrentScene().addChild(mEarthSphere);
        */
        // Ponemos el zoom correspondiente
        getCurrentCamera().setZ(mZoom);
    }

    /* Función que nos indica la velocidad de rotación, coge la diferencia y aplica una función cuadrática
    (en lugar de lineal, para que la rotación sea mayor cuanto más tiempo lleves rotando) y aplica
    bordes, para que haya un límite en la rotación
     */
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

    // La función que renderiza la escena, simplemente hacemos la rotación conveniente
    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        try {
            super.onRender(elapsedTime, deltaTime);
        } catch (java.lang.IllegalArgumentException e) {

        } catch (java.lang.NullPointerException e) {

        }
        double[] rotationSpeed = getRotationSpeed();
        mObj.rotate(Vector3.Axis.X, rotationSpeed[0]);
        mObj.rotate(Vector3.Axis.Y, rotationSpeed[1]);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    /* El listener que se pasará al Multitouch, aquí se define qué se hace en cada detección de
    patrón
     */
    public MultiTouchViewPager.OnTouchListener getOnTouchListener() {
        return new MultiTouchViewPager.OnTouchListener(){

            @Override
            public void onTouch() {
                mReferenceOrientation = null;
            }

            @Override
            public void onRelease() {
                Log.d("tl", "Renderer Release");
            }

            @Override
            public void onPinchIn() {
                double z = getCurrentCamera().getZ();
                mZoom = Math.max(10.0, Math.min(30.0, mZoom + z/15.0));
                getCurrentCamera().setZ(mZoom);
            }

            @Override
            public void onPinchOut() {
                double z = getCurrentCamera().getZ();
                mZoom = Math.max(10.0, Math.min(30.0, mZoom - z/15.0));
                getCurrentCamera().setZ(mZoom);
            }

            @Override
            public void onMove(int diffX, int diffY) {
                mObj.rotate(Vector3.Axis.Y, -diffX/2.0);
                mObj.rotate(Vector3.Axis.X, -diffY/2.0);
            }
        };
    }
}

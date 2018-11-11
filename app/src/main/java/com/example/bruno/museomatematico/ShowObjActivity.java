package com.example.bruno.museomatematico;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static android.view.View.FOCUS_RIGHT;

/**
 * La actividad en la que se muestran los objetos 3D
 */
public class ShowObjActivity extends FragmentActivity {
    public MultiTouchViewPager mPager;
    private ObjectViewerPageAdapter mPagerAdapter;
    private ArrayList<ObjInformation> mObjsInfo;
    private ArrayList<ObjInformation> mObjsInfoAux;
    int mCurrentObject;
    private float light;
    boolean cam_light;
    private TTS mytts;
    private ASR myasr;
    private AIDialog myai;
    private final static String LOGTAG = "ShowObjActivity";
    private boolean mustChangeObjects = false;


    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;

    /**
     * Los objetos se muestran en un ViewPager. Este es su adapter (Quién le dice que objetos mostrar en cada posición)
     */
    private class ObjectViewerPageAdapter extends FragmentStatePagerAdapter {
        private Activity mActivity;
        private HashMap<Integer, Fragment> mFragments;


        public ObjectViewerPageAdapter(FragmentManager fm, Activity activity) {
            super(fm);
            mActivity = activity;
            mFragments = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments.get(position) == null) {
                Log.d("d", "Creando fragment " + mObjsInfo.get(position).getType());
                ObjectViewerFragment fragment = ObjectViewerFragment.newInstance(mObjsInfo.get(position).getType(), position);
                mFragments.put(position, fragment);
            }

            return mFragments.get(position);
        }


        @Override
        public int getCount() {
            return mObjsInfo.size();
        }


        public void update() {
            mFragments.clear();
            notifyDataSetChanged();
        }
    }


    // Estas son las funcionalidades que AndroidStudio crea automáticamente en una Acticity
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    /**
     * Nuesta función onCreate, donde inicializamos la Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creamos un TTS y un ASR para su posterior uso
        mytts = new TTS(this, false);
        myasr = new ASR(this);

        // Inicializamos el sensor de proximidad
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Hacemos que la actividad se vea a pantalla completa
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Asociamos la actividad con su layout
        setContentView(R.layout.activity_show_obj);
        mVisible = true;

        // Hacemos scrollable el TextView donde aparece la descripción de los objetos
        TextView obj_text_view = (TextView) findViewById(R.id.obj_text_view);
        obj_text_view.setMovementMethod(new ScrollingMovementMethod());

        // Obtenemos los objetos que mostrará esta activity
        mObjsInfoAux = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        int objTypes[] = extras.getIntArray("com.example.museomatematico.ObjTypes");
        for(int i: objTypes) {
            mObjsInfoAux.add(new ObjInformation(ObjInformation.ObjType.from(i)));
        }
        changeObjects(mObjsInfoAux);

        // Inicializamos el boton para hablar con el bot
        setSpeakActionButton();

        // Inicializamos el ViewPager donde se muestran los objetos 3D
        mPager = (MultiTouchViewPager) findViewById(R.id.obj_view_pager);
        mPagerAdapter = new ObjectViewerPageAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mCurrentObject = i;
                setDescriptionText();
                ObjectViewerFragment currentFragment = (ObjectViewerFragment) mPagerAdapter.getItem(i);
                if (currentFragment.callGetOnTouchListener) {
                    mPager.setOnTouchListener(currentFragment.getOnTouchListener());
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        // Asociamos nuestro ViewPager a un indicador que nos permite ver y cambiar la posición
        SpringDotsIndicator indicator = (SpringDotsIndicator) findViewById(R.id.page_indicator);
        indicator.setViewPager(mPager);
    }


    // Inicializa el botón para hablar con el bot
    private void setSpeakActionButton() {
        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);

        final PackageManager packM = getPackageManager();

        // Le decimos que hacer cuando sea clickado
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Comprueba si el reconocimiento del habla está soportado
                List<ResolveInfo> intActivities = packM.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
                if (intActivities.size() != 0) {
                    // Comienza el reconocimiento del habla
                    myasr.launchActivity();
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.asr_notsupported, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(LOGTAG, "ASR not supported");
                }
            }
        });
    }


    // Escribe la descripción del objeto actual en el TextView
    private void setDescriptionText() {
        TextView objTextView = (TextView) findViewById(R.id.obj_text_view);
        ObjInformation objInfo = mObjsInfo.get(mCurrentObject);
        HashMap<String, String> properties = objInfo.getProperties();

        String text = "<p><h2>" + objInfo.getName() + "</h2></p>";
        for (String key : properties.keySet()) {
            text = text + "<p><b>" + key.substring(0, 1).toUpperCase() + key.substring(1) + "</b>: " + properties.get(key) + "</p>";
        }

        objTextView.setText(Html.fromHtml(text));
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == myasr.getRequestCode())  {
            // Cuando
            Pair<ArrayList<String>, float[]> results = myasr.onActivityResult(resultCode, data);
            if (results != null) {
                ArrayList<String> n_best_list = results.first;
                if(n_best_list.size() > 0) {
                    AIlee( n_best_list.get(0) );
                }
                Log.i(LOGTAG, "There were : " + n_best_list.size() + " recognition results");
            }

        } else if (requestCode == mytts.getRequestCode()) {
            mytts.onActivityResult(resultCode, data);
        }
    }


    private void changeObjects(ArrayList<ObjInformation> objs) {
        mObjsInfo = objs;
        if (mPagerAdapter != null) mPagerAdapter.notifyDataSetChanged();
        mCurrentObject = 0;
        setDescriptionText();
        if(mPagerAdapter !=null ) {
            mPagerAdapter.update();
        }
        ViewGroup vg = findViewById (R.id.sliding_layout);
        if (vg != null) vg.refreshDrawableState();
        if(mPager != null) {
            mPager.invalidate();
            mPager.setAdapter(mPagerAdapter);
            SpringDotsIndicator indicator = (SpringDotsIndicator) findViewById(R.id.page_indicator);
            indicator.invalidate();
            indicator.setViewPager(mPager);
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Activamos el listener del sensorManager de nuevo. En la función onSensorChanged hay más info
        // de este código
        mSensorManager.registerListener(listener, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        ObjectViewerFragment currentFragment = (ObjectViewerFragment) mPagerAdapter.getItem(mCurrentObject);
        mPagerAdapter = new ObjectViewerPageAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mCurrentObject = i;
                setDescriptionText();
                ObjectViewerFragment currentFragment = (ObjectViewerFragment) mPagerAdapter.getItem(i);
                mPager.setOnTouchListener(currentFragment.getOnTouchListener());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        if (currentFragment.callGetOnTouchListener) {
            mPager.setOnTouchListener(currentFragment.getOnTouchListener());
        }

        firstProximityActivation = true;
        mPager.setCurrentItem(mCurrentObject, true);
    }

    /* Ponemos en pausa el listener del sensorManager. En la función donde se crea el listener y
    se crea la función onSensorChanged hay más información de este código
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

    }

    /* onSensorChanged se encarga de ver cuándo el sensor de proximidad ha cambiado
    Esta función y los cambios al mSensorManager no son de nuestra autoría, y parte del código
    son de https://stackoverflow.com/a/35743193
    Hemos añadido una funcionalidad de que la primera activación del sensor no se activa. Esto
    es debido a que siempre que se inicializa nos detecta un cambio, por lo que se activa, y solo
    queremos que se active cuando se pase la mano por encima.
     */
    boolean firstProximityActivation = true;
    private SensorEventListener listener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (firstProximityActivation) {
                    firstProximityActivation = false;
                    return;
                }
                if (event.values[0] >= -event.sensor.getMaximumRange() && event.values[0] <= event.sensor.getMaximumRange()) {
                    mPager.arrowScroll(FOCUS_RIGHT);
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private ArrayList<String> objetos = new ArrayList<>();
    private ArrayList<String> propiedades = new ArrayList<>();

    private void ResetObjetos(){
        objetos.clear();
    }
    private void ResetPropiedades(){
        propiedades.clear();
    }

    private void ResetObjetos(ArrayList<String> a){
        objetos.clear();
        objetos.addAll(a);
    }
    private void ResetPropiedades(ArrayList<String> a){
        propiedades.clear();
        propiedades.addAll(a);
    }



    protected void AIlee(String s){
        myai = new AIDialog(this, new Callable<Integer>() {
            public Integer call() {
                AIresponde();
                return 0;
            }
        });
        myai.setOnPostExecute(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if (mustChangeObjects) changeObjects(mObjsInfoAux);
                mustChangeObjects = false;
                return 0;
            }
        });
        myai.initAiDialog();
        myai.execute(s);
    }

    protected void AIresponde() {
        String texto_respuesta;
        String intent = myai.getIntent();
        ArrayList<String> entidades = myai.getEntidades();

        Log.i("h",String.format("Mbot -> Intent: %s",intent));
        if( entidades.contains("Objeto") )
            Log.i("h", String.format("Mbot ->   Parámetros de Objeto: %s", myai.getParams("Objeto")));
        if( entidades.contains("Propiedad") )
            Log.i("h", String.format("Mbot ->   Parámetros de Propiedades: %s", myai.getParams("Propiedad")));

        if(     (intent.equals("Dibujar-Objeto")
                && entidades.contains("Objeto")
                && !myai.getParams("Objeto").isEmpty())
                ||
                (intent.equals("Propiedades-Objeto-Dibuja")
                        && !objetos.isEmpty())
                ||
                (intent.equals("Propiedades-PropiedadObjeto-Dibuja")
                        && !objetos.isEmpty())
                ){
            if(intent.equals("Dibujar-Objeto")) {
                // Vaciamos objetos y guardamos los objetos nuevos como variable, y qué objeto se está mostrando
                ResetObjetos(myai.getParams("Objeto"));
            }
            // Dibujamos los Objetos
            ArrayList<String> params = myai.getParams("Objeto");
            ArrayList<ObjInformation> objs = new ArrayList<>();
            for (String p : params) {
                objs.add(new ObjInformation(p));
                Log.d("MainActivity", "Mostrar " + p);
            }
            mObjsInfoAux = objs;
            mustChangeObjects = true;

            texto_respuesta = myai.getSpeech();
        }
        else if( (intent.equals("Dibujar-Objeto-Cambia")
                || intent.equals("Dibujar-Objeto-Cambia-Cambia"))
                && mObjsInfo.size() >= 2 ){
            if (mCurrentObject < mObjsInfo.size()-1) mCurrentObject += 1;
            mPager.setCurrentItem(mCurrentObject);
            Log.d("ShowObjActivity", "Cambiando de objeto");

            texto_respuesta = myai.getSpeech();
        }
        else if(   (intent.equals("Dibujar-Objeto-HaciaProp")
                && !objetos.isEmpty())
                ||
                (intent.equals("Propiedades-Objeto")
                        && entidades.contains("Objeto")
                        && !myai.getParams("Objeto").isEmpty())){
            if(intent.equals("Propiedades-Objeto")){
                // Entonces los objetos han sido pasados como parámetros
                ResetObjetos( myai.getParams("Objeto") );
            }

            texto_respuesta = myai.getSpeech();
            // Lista de propiedades, iterando por objeto, que puede el usuario preguntar
            for(String s : objetos){
                // Quitamos las comillas
                s = s.substring(1, s.length()-1);
                if(objetos.size() > 1)
                    texto_respuesta += String.format("\n   %s:",s);

                ObjInformation info_s = new ObjInformation(s);
                HashMap<String,String> hmap_s = info_s.getProperties();
                //Map<String,String> map_s = hmap_s;
                // Escribimos bien las propiedades a preguntar
                for(String p_s : hmap_s.keySet()) {
                    if (objetos.size() > 1)
                        texto_respuesta += String.format(" %s,", p_s);
                    else
                        texto_respuesta += String.format("\n%s,", p_s);
                }
            }
        }
        else if(  (intent.equals("Propiedades-Objeto-Propiedad")
                && entidades.contains("Propiedad")
                && !myai.getParams("Propiedad").isEmpty())
                ||
                (intent.equals("Propiedades-PropiedadObjeto")
                        && entidades.contains("Propiedad")
                        && entidades.contains("Objeto")
                        && !myai.getParams("Objeto").isEmpty()
                        && !myai.getParams("Propiedad").isEmpty())  ) {
            // Guardamos las propiedades como variable
            ResetPropiedades( myai.getParams("Propiedad") );
            if(intent.equals("Propiedades-PropiedadObjeto")){
                // Entonces los objetos han sido pasados como parámetros
                ResetObjetos( myai.getParams("Objeto") );
            }

            texto_respuesta = myai.getSpeech();
            // Iterando por objeto, escribimos las propiedades
            for(String s : objetos){
                // Quitamos las comillas
                s = s.substring(1, s.length()-1);

                ObjInformation info_s_p = new ObjInformation(s);
                for(String p_s : propiedades) {
                    // Quitamos las comillas
                    p_s = p_s.substring(1, p_s.length()-1);
                    // Escribimos las propiedades preguntadas, variando el mensaje según
                    // tengamos varios objetos/propiedades o sólo 1 objeto, 1 propiedad.
                    if(   propiedades.size() == 1
                            &&
                            objetos.size() == 1    ){
                        // El usuario puede preguntar por una propiedad que no tenga sentido según el objeto.
                        // Entonces le respondemos que no disponemos de tal información.
                        if( info_s_p.getProperties().get(p_s) != null)
                            texto_respuesta += String.format("\n %s", info_s_p.getProperties().get(p_s));
                        else
                            texto_respuesta += " Pero sintiéndolo mucho, no dispongo de tal información.";
                    }
                    else{
                        texto_respuesta += String.format("\n  %s de %s:,", p_s,s);
                        // El usuario puede preguntar por una propiedad que no tenga sentido según el objeto.
                        // Entonces le respondemos que no disponemos de tal información.
                        if( info_s_p.getProperties().get(p_s) != null)
                            texto_respuesta += String.format(" %s", info_s_p.getProperties().get(p_s));
                        else
                            texto_respuesta += " Pero sintiéndolo mucho, no dispongo de tal información.";
                    }
                }
            }
        }
        else{
            ResetPropiedades();
            ResetObjetos();

            texto_respuesta = myai.getSpeech();
        }

        // Leer en voz alta la respuesta del Bot
        //Toast.makeText(this, texto_respuesta, Toast.LENGTH_LONG).show();
        Log.d("ShowObjectActivity", "Respuesta AI: " + texto_respuesta);
        mytts.launchActivity(texto_respuesta);
    }
}

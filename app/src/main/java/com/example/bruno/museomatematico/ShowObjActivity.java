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

import static android.view.View.FOCUS_RIGHT;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShowObjActivity extends FragmentActivity {
    public MultiTouchViewPager mPager;
    private ObjectViewerPageAdapter mPagerAdapter;
    private ArrayList<ObjInformation> mObjsInfo;
    int mCurrentObject;
    private float light;
    boolean cam_light;
    private TTS mytts;
    private ASR myasr;
    private final static String LOGTAG = "ShowObjActivity";


    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;

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
                Log.d("d", "Creando fragment");
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
            mFragments = new HashMap<>();
        }
    }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mytts = new TTS(this);
        myasr = new ASR(this);

// Start the initial runnable task by posting through the handler
        //handler.post(runnableCode);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_obj);
        mVisible = true;

        TextView obj_text_view = (TextView) findViewById(R.id.obj_text_view);
        obj_text_view.setMovementMethod(new ScrollingMovementMethod());

        // Obtenemos los objetos que mostrará esta activity
        ArrayList<ObjInformation.ObjType> objs = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        int objTypes[] = extras.getIntArray("com.example.museomatematico.ObjTypes");
        for(int i: objTypes) {
            objs.add(ObjInformation.ObjType.from(i));
        }
        changeObjects(objs);

        setSpeakActionButton();

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
        SpringDotsIndicator indicator = (SpringDotsIndicator) findViewById(R.id.page_indicator);
        indicator.setViewPager(mPager);
    }


    private void setSpeakActionButton() {
        //Gain reference to speak button
        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);

        final PackageManager packM = getPackageManager();

        //Set up click listener
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ////To avoid running on a simulated device
                //if("generic".equals(Build.BRAND.toLowerCase())){
                //	Toast toast = Toast.makeText(getApplicationContext(),"Virtual device: "+R.string.asr_notsupported, Toast.LENGTH_SHORT);
                //	toast.show();
                //	Log.d(LOGTAG, "ASR attempt on virtual device");
                // }

                // find out whether speech recognition is supported
                List<ResolveInfo> intActivities = packM.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
                if (intActivities.size() != 0) {
                    //Disable button so that ASR is not launched until the previous recognition result is achieved
                    FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);
                    myasr.launchActivity();                //Set up the recognizer with the parameters and start listening
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
            Pair<ArrayList<String>, float[]> results = myasr.onActivityResult(resultCode, data);
            if (results != null) {
                ArrayList<String> n_best_list = results.first;
                float[] n_best_confidences = results.second;
                if(n_best_list.size() > 0) {
                   // AIDialog ai = new AIDialog(this);
                   // ai.initAiDialog();
                   // ai.execute(n_best_list.get(0));
                }
                Log.i(LOGTAG, "There were : " + n_best_list.size() + " recognition results");
            }

        } else if (requestCode == mytts.getRequestCode()) {
            mytts.onActivityResult(resultCode, data);
        }
    }


    private void changeObjects(ArrayList<ObjInformation.ObjType> objectTypes) {
        mObjsInfo = new ArrayList<>();
        for (ObjInformation.ObjType t : objectTypes) {
            mObjsInfo.add(new ObjInformation(t));
        }
        ViewGroup vg = findViewById (R.id.sliding_layout);
        if(vg != null) vg.invalidate();
        mCurrentObject = 0;
        setDescriptionText();
        if(mPagerAdapter !=null ) mPagerAdapter.update();
        if(mPager !=null ) {
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
                if (currentFragment.callGetOnTouchListener) {
                    mPager.setOnTouchListener(currentFragment.getOnTouchListener());
                }
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
}

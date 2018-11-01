package com.example.bruno.museomatematico;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private TTS mytts;
    private ASR myasr;

    private final static String LOGTAG = "MainActivity";


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
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            if (mControlsView != null) {
                mControlsView.setVisibility(View.VISIBLE);
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
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mContentView = findViewById(R.id.response_text_view);

        mytts = new TTS(this);
        myasr = new ASR(this);

        setSpeakActionButton();


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }


    public void startShowObjActivity(View view) {
        Intent intent = new Intent(this, ShowObjActivity.class);
        intent.putExtra("com.example.museomatematico.ObjID", "#BotellaKlein");
        startActivity(intent);
    }


    /**
     * Sets up the listener for the button that the user
     * must click to start talking
     */
    @SuppressLint("DefaultLocale")
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
                    speak.setEnabled(false);
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

    @SuppressLint("DefaultLocale")
    private void setASRText(ArrayList<String> nBestList, float v[]) {
        //Gain reference to speak button
        TextView asr_text = (TextView) findViewById(R.id.ASRtext);

        if(v[0] > 0.6){
            asr_text.setText(nBestList.get(0)+".");
        }
        else{
            asr_text.setText("No te he entendido. Prueba a decirlo otra vez.");
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == myasr.getRequestCode())  {
            Pair<ArrayList<String>, float[]> results = myasr.onActivityResult(resultCode, data);
            if (results != null) {
                ArrayList<String> n_best_list = results.first;
                float[] n_best_confidences = results.second;
                setASRText(n_best_list, n_best_confidences);
                if(n_best_list.size() > 0)
                    mytts.launchActivity(n_best_list.get(0)+".");
                Log.i(LOGTAG, "There were : " + n_best_list.size() + " recognition results");
            }

        } else if (requestCode == mytts.getRequestCode()) {
            mytts.onActivityResult(resultCode, data);
        }

        //Enable button
        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);
        speak.setEnabled(true);
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
        ActionBar actionBar = getSupportActionBar();
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
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
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
}

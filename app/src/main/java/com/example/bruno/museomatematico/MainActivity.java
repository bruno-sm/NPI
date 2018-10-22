package com.example.bruno.museomatematico;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {

    // Default values for the language model and maximum number of recognition results
    // They are shown in the GUI when the app starts, and they are used when the user selection is not valid
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;


    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private String languageModel = DEFAULT_LANG_MODEL;

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
    private final static String LOGTAG = "SIMPLEASR";
    private final static int ASR_CODE = 123;
    private TextToSpeech mytts;
    private final static int TTS_DATA_CHECK = 12;

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

        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_DATA_CHECK);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mContentView = findViewById(R.id.response_text_view);

        setSpeakActionButton();


    }


    private void listen()  {

        //Disable button so that ASR is not launched until the previous recognition result is achieved
        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);
        speak.setEnabled(false);

        // Check we have permission to record audio
        checkASRPermission();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify language model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

        // Specify mx number of recognition results
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);

        // Start listening
        startActivityForResult(intent, TTS_DATA_CHECK);

    }

    /**
     * Checks whether the user has granted permission to the microphone. If the permission has not been provided,
     * it is requested. The result of the request (whether the user finally grants the permission or not)
     * is processed in the onRequestPermissionsResult method.
     *
     * This is necessary from Android 6 (API level 23), in which users grant permissions to apps
     * while the app is running. In previous versions, the permissions were granted when installing the app
     * See: http://developer.android.com/intl/es/training/permissions/requesting.html
     */
    public void checkASRPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), R.string.asr_permission, Toast.LENGTH_SHORT).show();

            // Request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }


    private void setRecognitionParams()  {

        numberRecoResults = DEFAULT_NUMBER_RESULTS;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
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
                    setRecognitionParams(); //Read speech recognition parameters from GUI
                    listen();                //Set up the recognizer with the parameters and start listening
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
            asr_text.setText(nBestList.get(0));
        }
        else{
            asr_text.setText("No te he entendido. Prueba a decirlo otra vez.");
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE)  {
            if (resultCode == RESULT_OK)  {
                if(data!=null) {
                    //Retrieves the N-best list and the confidences from the ASR result
                    ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    float[] nBestConfidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                    //Creates a collection of strings, each one with a recognition result and its confidence
                    //following the structure "Phrase matched (conf: 0.5)"
                    ArrayList<String> nBestView = new ArrayList<>();

                    for(int i=0; i<nBestList.size(); i++) {
                        if (nBestConfidences != null) {
                            if (nBestConfidences[i] >= 0)
                                nBestView.add(nBestList.get(i) + " (conf: " + String.format(this.getResources().getConfiguration().getLocales().get(0), "%.2f", nBestConfidences[i]) + ")");
                            else
                                nBestView.add(nBestList.get(i) + " (no confidence value available)");
                        } else
                            nBestView.add(nBestList.get(i) + " (no confidence value available)");
                    }

                    Log.i(LOGTAG, "There were : "+ nBestView.size()+" recognition results");
                    if(nBestView.size() > 0) {
                        setASRText(nBestList, nBestConfidences);
                    }
                }
            }
            else {
                //Reports error in recognition error in log
                Log.e(LOGTAG, "Recognition was not successful");
            }

            //Enable button
            FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);
            speak.setEnabled(true);
        }
        else if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mytts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Toast.makeText(MainActivity.this, R.string.tts_initialized, Toast.LENGTH_LONG).show();
                            if (mytts.isLanguageAvailable(Locale.US) >= 0)
                                mytts.setLanguage(Locale.US);
                        }

                    }
                });
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                PackageManager pm = getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (resolveInfo != null) {
                    startActivity(installIntent);
                } else {
                    Toast.makeText(MainActivity.this, R.string.please_install_tts, Toast.LENGTH_LONG).show();
                }
            }
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

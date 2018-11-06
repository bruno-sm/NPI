package com.example.bruno.museomatematico;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class ASR {
    private Activity my_activity;
    private final static int REQUEST_CODE = 123;
    private final static String LOGTAG = "ASR";
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    private String languageModel = DEFAULT_LANG_MODEL;
    // Default values for the language model and maximum number of recognition results
    // They are shown in the GUI when the app starts, and they are used when the user selection is not valid
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;


    public ASR(MainActivity activity){
        my_activity = activity;
        numberRecoResults = DEFAULT_NUMBER_RESULTS;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    }


    public int getRequestCode(){
        return REQUEST_CODE;
    }


    public void launchActivity(){

        // Check we have permission to record audio
        checkASRPermission();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify language model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

        // Specify mx number of recognition results
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);

        // Start listening
        my_activity.startActivityForResult(intent, REQUEST_CODE);
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
        if (ContextCompat.checkSelfPermission(my_activity.getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(my_activity, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(my_activity.getApplicationContext(), R.string.asr_permission, Toast.LENGTH_SHORT).show();

            // Request the permission.
            ActivityCompat.requestPermissions(my_activity, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public Pair<ArrayList<String>, float[]> onActivityResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (data != null) {
                //Retrieves the N-best list and the confidences from the ASR result
                ArrayList<String> n_best_list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                float[] n_best_confidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                return new Pair<ArrayList<String>, float[]>(n_best_list, n_best_confidences);
            }
        }

        return null;
    }
}

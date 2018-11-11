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

/* Esta clase, aunque no es exactamente el mismo archivo que se nos dio en clase, está fuertemente
basado en él. En lugar de una activity lo hemos separado en una clase que hace la gestión del ASR.
Nuestro trabajo ha sido simplemente traducir de Activity a clase.
Más abajo comentamos las partes importantes del código. Hemos dejado los comentarios originales
que explican detalladamente las funciones que hemos dejado.
 */

/**
 * SimpleASR: Basic app with ASR using a RecognizerIntent
 *
 * Simple demo in which the user speaks and the recognition results
 * are showed in a list along with their confidence values
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 3.0, 09/25/18
 *
 */

public class ASR {
    // Activity con la que se comunica el ASR
    private Activity my_activity;
    // Request_Code para la comunicación entre la activity y la clase ASR
    private final static int REQUEST_CODE = 123;
    // LogTag para los logs
    private final static String LOGTAG = "ASR";
    // Variables para la gestión del idioma del ASR
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    private String languageModel = DEFAULT_LANG_MODEL;

    // Default values for the language model and maximum number of recognition results
    // They are shown in the GUI when the app starts, and they are used when the user selection is not valid
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;

    /* Constructor, en él pasamos la activity con la que se comunicará esta clase, gestionamos el
    idioma en el que lo hace y el número máximo por defecto de recognition results (la lista que nos
    devuelve el ASR cuando se le llama)
     */
    public ASR(Activity activity){
        my_activity = activity;
        numberRecoResults = DEFAULT_NUMBER_RESULTS;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    }

    // Función getter del request code
    public int getRequestCode(){
        return REQUEST_CODE;
    }

    /*
    Launch activity es la función que llama y activa el ASR, que empieza a escuchar al usuario.
    Esta función es la que en el código original se llamaba "listen()", salvo que en lugar de
    activar startActivityForResult de la actividad en la que estamos, llamamos a my_activity
     */
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

    /* La función de onActivityResult la hemos cambiado para que solo recoja la lista de las n mejores
    frases que haya reconocido, junto a la lista de confianzas. La gestión de cuál cogemos se ha trasladado
    a otra función (en concreto, a setAsrText en MainActivity)
     */
    /**
     *  Shows the formatted best of N best recognition results (N-best list) from
     *  best to worst in the <code>ListView</code>.
     *  For each match, it will render the recognized phrase and the confidence with
     *  which it was recognized.
     */
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

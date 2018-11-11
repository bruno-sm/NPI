package com.example.bruno.museomatematico;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/* Esta clase, aunque no es exactamente el mismo archivo que se nos dio en clase, está fuertemente
basado en él. En lugar de una activity lo hemos separado en una clase que hace la gestión del TTS,
y eliminando partes del código como la gestión del botón. Abajo comentamos las partes más importantes.
 */
/**
 * SimpleTTS: Basic app with text to speech synthesis
 * <p/>
 * Simple demo in which the user writes a text in a text field
 * and it is synthesized by the system when pressing a button.
 * <p/>
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 3.0, 25/09/18
 *
 */
public class TTS {
    // La activity con la que nos comunicamos
    private Activity my_activity;
    // El objeto TextToSpeech que gestiona el TTS
    private TextToSpeech mytts;
    // El reques_code para comunicarnos con la activity
    private final static int REQUEST_CODE = 12;

    /* Constructor del TTS. Le necesitamos pasar la activity con la que se comunicará y llama
    a startActivityForResult con el código de request de nuestra variable.
     */
    public TTS(Activity activity){
        my_activity = activity;
        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        my_activity.startActivityForResult(checkIntent, REQUEST_CODE);
    }

    // getter del request Code.
    public int getRequestCode(){
        return REQUEST_CODE;
    }

    /* La función que inicia el TTS. Dado el texto text, esta función inicia el mytts para que
    se escuche
    */
    public void launchActivity(String text){
        if (text.length() > 0)
            mytts.speak(text, TextToSpeech.QUEUE_ADD, null, "msg");
    }

    /* Función que hemos mantenido muy parecida a la del código original, salvo ligeros cambios
    Se encarga de comprobar si poemos hacer una llamada al TextToSpeech, gestionar el idioma en el
    que hablará y gestionar los errores, en caso de que no podamos hacerlo.
    Hemos mantenido los comentarios originales.
     */
    /**
     * Callback from check for text to speech engine installed
     * If positive, then creates a new <code>TextToSpeech</code> instance which will be called when user
     * clicks on the 'Speak' button
     * If negative, creates an intent to install a <code>TextToSpeech</code> engine
     */
    public void onActivityResult(int resultCode, Intent data){
        // If the result of the action is CHECK_VOICE_DATA_PASS, there is a TTS Engine
        //available in the device
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // Create a TextToSpeech instance
            mytts = new TextToSpeech(my_activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Locale espa = new Locale("spa", "ESP");
                        // Display Toast
                        Toast.makeText(my_activity, R.string.tts_initialized, Toast.LENGTH_LONG).show();
                        // Set language to spanish if it is available
                        if (mytts.isLanguageAvailable(espa) >= 0)
                            mytts.setLanguage(espa);
                    }

                    }
                });
        } else {
            // The TTS is not available, we will try to install it:
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

            PackageManager pm = my_activity.getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

            //If the install can be started automatically we launch it (startActivity), if not, we
            //ask the user to install the TTS from Google Play (toast)
            if (resolveInfo != null) {
                my_activity.startActivity(installIntent);
            } else {
                Toast.makeText(my_activity, R.string.please_install_tts, Toast.LENGTH_LONG).show();
            }
        }
    }


}

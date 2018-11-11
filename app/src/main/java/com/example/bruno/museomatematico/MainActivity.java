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
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ai.api.model.AIRequest;
import ai.api.model.Result;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity{
    private TTS mytts;
    private ASR myasr;
    private AIDialog myai;
    private TextView botResultTextView;
    private ArrayList<String> objetos = new ArrayList<>();
    private ArrayList<String> propiedades = new ArrayList<>();

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
    private final transient Handler mHideHandler = new Handler();
    private final transient Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
        }
    };

    private transient View mControlsView;
    private final transient Runnable mShowPart2Runnable = new Runnable() {
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
    private final transient Runnable mHideRunnable = new Runnable() {
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
    private final transient View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
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

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mVisible = true;

        mytts = new TTS(this, true);
        myasr = new ASR(this);
        botResultTextView = (TextView) findViewById(R.id.botText);
        botResultTextView.setMovementMethod(new ScrollingMovementMethod());

        setSpeakActionButton();

        TextView asr_text = (TextView) findViewById(R.id.tts_text_view);
        asr_text.setText(Html.fromHtml("<big>Bienvenido al Museo Matemático</big><br/><br/>¿Qué quieres ver?"));

        /* Bot v /*
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(permission != PackageManager.PERMISSION_GRANTED){
            makeRequest();
        }

        final AIConfiguration config = new AIConfiguration(
                "101096c066bd400cb4ff31bb3f723b53",
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
         Bot ^ */
    }

    /* Bot v
    protected void makeRequest(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},101);
    }


    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 101:{
                if(grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED){

                }
                else{

                }
                return;
            }
        }
    }
     Bot ^ */


    public void startShowObjActivityFromButton(View view) {
        ArrayList<ObjInformation> objs = new ArrayList<>();
        objs.add(new ObjInformation(ObjInformation.ObjType.CUBE));
        objs.add(new ObjInformation(ObjInformation.ObjType.SPHERE));
        objs.add(new ObjInformation(ObjInformation.ObjType.CYLINDER));
        objs.add(new ObjInformation(ObjInformation.ObjType.KLEIN_BOTTLE));
        objs.add(new ObjInformation(ObjInformation.ObjType.TORUS));
        objs.add(new ObjInformation(ObjInformation.ObjType.MOBIUS_STRIP));
        startShowObjActivity(objs);
    }


    public void startShowObjActivity(ArrayList<ObjInformation> objs) {
        Intent intent = new Intent(this, ShowObjActivity.class);
        int objTypes[] = new int[objs.size()];
        for (int i=0; i < objs.size(); i++)
            objTypes[i] = objs.get(i).getType().getValue();
        intent.putExtra("com.example.museomatematico.ObjTypes", objTypes);
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
        TextView asr_text = (TextView) findViewById(R.id.asr_text_view);

        if(v[0] > 0.6){
            asr_text.setText(nBestList.get(0)+".");
        }
        else{
            TextView tts_text = (TextView) findViewById(R.id.tts_text_view);
            tts_text.setText("No te he entendido. Prueba a decirlo otra vez.");
            mytts.launchActivity("No te he entendido. Prueba a decirlo otra vez.");
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
                if(n_best_list.size() > 0) {
                    AIlee( n_best_list.get(0) );
                }
                Log.i(LOGTAG, "There were : " + n_best_list.size() + " recognition results");
            }

        } else if (requestCode == mytts.getRequestCode()) {
            mytts.onActivityResult(resultCode, data);
        }


        //Enable button
        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak_action_button);
        speak.setEnabled(true);
    }

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
            startShowObjActivity(objs);

            texto_respuesta = myai.getSpeech();
        }
        else if( (intent.equals("Dibujar-Objeto-Cambia")
                  || intent.equals("Dibuja-Objeto-Cambia-Cambia"))
                && objetos.size() >= 2 ){
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
                texto_respuesta += String.format("\n   (%s):",s);

                ObjInformation info_s = new ObjInformation(s);
                HashMap<String,String> hmap_s = info_s.getProperties();
                //Map<String,String> map_s = hmap_s;
                // Escribimos bien las propiedades a preguntar
                for(String p_s : hmap_s.keySet())
                    texto_respuesta += String.format(" %s,",p_s);
                texto_respuesta = texto_respuesta.substring(0,texto_respuesta.length()-1);
                texto_respuesta += ".";
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
            // todo
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

        // Cambiar el texto de la respuesta del Bot
        botResultTextView.setText(texto_respuesta);
        // Leer en voz alta la respuesta del Bot
        mytts.launchActivity(texto_respuesta);
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

    /* Bot v
    @Override
    public void onResult(AIResponse response) {
        final Result result = response.getResult();
        final Metadata metadata = result.getMetadata();
        if (metadata != null) {
            Log.i(LOGTAG, "Intent id: " + metadata.getIntentId());
            Log.i(LOGTAG, "Intent name: " + metadata.getIntentName());
        }

        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            Log.i(LOGTAG, "Parameters: ");
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                Log.i(LOGTAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
            }
        }

        TextView bot_text = (TextView) findViewById(R.id.botText);
        bot_text.setText("Query: " + result.getResolvedQuery() + " - Action: " + result.getAction());
        //t.setText("Query: " + result.getResolvedQuery() + "- Action: " + result.getAction());
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
     Bot ^ */
}

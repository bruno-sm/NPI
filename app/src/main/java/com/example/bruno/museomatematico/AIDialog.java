package com.example.bruno.museomatematico;

/* Parte de esta clase proviene del repositorio
https://github.com/dialogflow/dialogflow-android-client/tree/master/apiAISampleApp/src/main/java/ai/api/sample

La autoría del código no es nuestra.

Esta clase es la que en el repositorio se llama AIDialogSampleActivity. Nosotros la hemos pasado a una
clase separada (en lugar de una Activity) porque así se integraba mejor en nuestra app. Hemos hecho
varios cambios para hacer esto que se comentan a continuación, pero la estructura es la misma
que en el archivo mencionado
 */

/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;

public class AIDialog extends AsyncTask<String, Void, String> implements ai.api.ui.AIDialog.AIDialogListener  {

    // Ponemos un string TAG para los Logs
    private static final String TAG = AIDialog.class.getName();

    // El objeto AIDialog que será el que gestione el diálogo del bot
    private ai.api.ui.AIDialog aiDialog;
    // El objeto result_ai guardará la respuesta que nos genera el bot de DialogFlow
    private Result result_ai;
    // El objeto de metadata_ai sirve para guardar los metadatos de las llamadas al bot
    private Metadata metadata_ai;
    // El string req_ai guardará el String request que se le mandará al bot
    private String req_ai;
    // La actividad my_activity que llama al bot
    private Activity my_activity;
    // La respuesta que se ejecuta cuando el bot es llamadp
    private Callable<Integer> mAiResponde;

    // Constructor de la clase, le pasamos la activity con la que se va a comunicar, que ha sido
    // la que ha llamado y creado la clase.
    public AIDialog(Activity activity, Callable<Integer> aiResponde){
        my_activity = activity;
        mAiResponde = aiResponde;
    }

    /* Inicialización de valores básicos como la configuración y la conexión con my_activity, para
    cuando necesitemos comunicarnos con ella.
     */
    public void initAiDialog(){

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiDialog = new ai.api.ui.AIDialog(my_activity, config);

    }

    /*
    La función responseAI se encarga de gestionar la respuesta del bot, una vez que le hemos
    pasado nuestro request en la función DoInBackground
     */
    public void ResponseAI(final AIResponse response) throws Exception {
                /*
                // this is example how to get different parts of result object
                final ai.api.model.Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                */

                /* Recogemos el resultado de la respuesta, en el que se encuentra el texto
                de nuestro bot. Avisamos de lo que nos devuelve y asignamos las variables correspondientes
                como result_ai y metadata_ai. Guardamos en speech el texto.
                 */
                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);

                result_ai = response.getResult();
                metadata_ai = result_ai.getMetadata();

                // Gestión de los metadatos
                final Metadata metadata = result.getMetadata();
                if (metadata != null)

                {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                // Miramos los parámetros de la respuesta
                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty())
                {
                    //Log.i(TAG, String.format("Parameters: %s", getParams("Objeto")));
                }

                // Devolvemos la respuesta del bot a la activity
                mAiResponde.call();
    }

    /*
    La función getEntidades se encarga de devolver una lista con las entidades que aparecen
    en el diálogo con el bot para poder gestionar qué objetos se dibujarán o se listarán las propiedades
     */
    protected ArrayList<String> getEntidades(){
        ArrayList<String> list = new ArrayList<String>();
        HashMap<String, JsonElement> params = result_ai.getParameters();
        Map<String,JsonElement> mparams = params;
        for(String s: mparams.keySet()){
            list.add(s);
        }
        return list;
    }

    /*
    La función getParams se encarga de conseguir los parámetros de una respuesta del bot. Los devuelve
    en un ArrayList de String.
     */
    protected ArrayList<String> getParams(String key){
        ArrayList<String> list = new ArrayList<String>();
        if( result_ai.getParameters() != null) {
            JsonElement jsonArray = result_ai.getParameters().get(key).getAsJsonArray();
            if (jsonArray != null) {
                int len = ((JsonArray) jsonArray).size();
                for (int i = 0; i < len; i++) {
                    list.add(((JsonArray) jsonArray).get(i).toString());
                }
            }
        }
        return list;
    }

    // Función que devuelve la query del bot
    protected String getQuery(){
        return result_ai.getResolvedQuery();
    }

    /* Función que devuelve la respuesta en texto del bot. Se la llama desde activity para conseguir
    el resultado
    */
    protected String getSpeech(){
        return result_ai.getFulfillment().getSpeech();
    }

    /* Función que devuelve el nombre del intent de la respuesta, que se encuentra en los metadatos
    de la respuesta
     */
    protected String getIntent(){
        return metadata_ai.getIntentName();
    }

    /* Función necesaria por implementar AIDialogListener. Se la llama automáticamente cuando hay
    un resultado
    */
    @Override
    public void onResult(AIResponse result) {

    }

    /* Función necesaria por implementar AIDialogListener. Se la llama automáticamente cuando hay
    un error
    */
    @Override
    public void onError(AIError error) {

    }

    /* Función que activa el request que nos pasan desde activity y que llama a ResponseAI para
    que el bot pueda responder. También recoge las excepciones por si ha habido algún error en
    la llamada.
    */
    @Override
    protected String doInBackground(String... strings) {
                req_ai = strings[0];

                try {
                    ResponseAI(aiDialog.textRequest(req_ai));
                }
                catch (AIServiceException e) {
                    e.printStackTrace();
                    Log.d("h", "MBot: No he podido responder.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        return null;
    }

    /* Función necesaria por implementar AIDialogListener. Se la llama automáticamente cuando se
    cancela la petición al bot
    */
    @Override
    public void onCancelled() {

    }


}

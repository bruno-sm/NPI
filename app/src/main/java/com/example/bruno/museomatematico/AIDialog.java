package com.example.bruno.museomatematico;

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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

public class AIDialog extends AsyncTask<String, Void, String> implements ai.api.ui.AIDialog.AIDialogListener  {

    private static final String TAG = AIDialog.class.getName();


    private ai.api.ui.AIDialog aiDialog;
    private Result result_ai;
    private Metadata metadata_ai;
    private String req_ai;
    private MainActivity my_activity;

    public AIDialog(MainActivity activity){
        my_activity = activity;
    }

    public void initAiDialog(){

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiDialog = new ai.api.ui.AIDialog(my_activity, config);

    }

    public void ResponseAI(final AIResponse response) {
                /*
                // this is example how to get different parts of result object
                final ai.api.model.Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                */

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                result_ai = response.getResult();
                metadata_ai = result_ai.getMetadata();

                final Metadata metadata = result.getMetadata();
                if (metadata != null)

                {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty())
                {
                    //Log.i(TAG, String.format("Parameters: %s", getParams("Objeto")));
                }

                // Bot responde
                my_activity.AIresponde();
    }

    protected ArrayList<String> getEntidades(){
        ArrayList<String> list = new ArrayList<String>();
        HashMap<String, JsonElement> params = result_ai.getParameters();
        Map<String,JsonElement> mparams = params;
        for(String s: mparams.keySet()){
            list.add(s);
        }
        return list;
    }

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

    protected String getQuery(){
        return result_ai.getResolvedQuery();
    }

    protected String getSpeech(){
        return result_ai.getFulfillment().getSpeech();
    }

    protected String getIntent(){
        return metadata_ai.getIntentName();
    }

    @Override
    public void onResult(AIResponse result) {

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    protected String doInBackground(String... strings) {
                req_ai = strings[0];

                try {
                    ResponseAI(aiDialog.textRequest(req_ai));
                }
                catch (AIServiceException e) {
                    e.printStackTrace();
                    Log.d("h", "MBot: No he podido responder.");
                }
        return null;
    }

    @Override
    public void onCancelled() {

    }


}

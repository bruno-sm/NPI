package com.example.bruno.museomatematico;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;

public class MatematiBotActivity extends AppCompatActivity implements AIListener {
    private AIService aiService;
    private TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t= (TextView) findViewById(R.id.textView);
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
    }
    
    protected void makeRequest(){
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},101);
    }
    
    @Override
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
    
    public void botonClickado(View view){
       aiService.startListening();
    }

    @Override
    public void onResult(AIResponse response) {
        final Result result = response.getResult();
        final Metadata metadata = result.getMetadata();
        if (metadata != null) {
            Log.i("i", "Intent id: " + metadata.getIntentId());
            Log.i("i", "Intent name: " + metadata.getIntentName());
        }
        
        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            Log.i("p", "Parameters: ");
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                Log.i("p", String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
            }
        }
        
        t.setText("Query: " + result.getResolvedQuery() + "- Action: " + result.getAction());
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
}

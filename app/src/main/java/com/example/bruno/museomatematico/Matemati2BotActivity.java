package com.example.bruno.museomatematico;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ai.api.AIListener;

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
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},120);
    }
    
    @Override
    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults){
      switch(requestCode){
        case 120:{
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

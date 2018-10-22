package com.example.bruno.museomatematico;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class TTS {
    private Activity my_activity;
    private TextToSpeech mytts;
    private final static int TTS_DATA_CHECK = 12;


    public void TTS(Activity activity){
        my_activity = activity;
        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        my_activity.startActivityForResult(checkIntent, TTS_DATA_CHECK);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mytts = new TextToSpeech(my_activity, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Toast.makeText(my_activity, R.string.tts_initialized, Toast.LENGTH_LONG).show();
                            if (mytts.isLanguageAvailable(Locale.US) >= 0)
                                mytts.setLanguage(Locale.US);
                        }

                        }
                    });
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                PackageManager pm = my_activity.getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (resolveInfo != null) {
                    my_activity.startActivity(installIntent);
                } else {
                    Toast.makeText(my_activity, R.string.please_install_tts, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

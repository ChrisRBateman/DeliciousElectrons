package org.cbateman.deliciouselectrons;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

/**
 * Service class to convert text string to speech.
 */
public class MyTextToSpeechService extends Service implements OnInitListener {
	public static final String TAG = DeliciousElectronsActivity.TAG;
	public static final String POWER_CONNECTED_KEY = DeliciousElectronsActivity.POWER_CONNECTED_KEY;
	
	public static TextToSpeech mTextToSpeech;
	public static boolean mTextToSpeechInitialized = false;
	
	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "MyTextToSpeechService.onCreate");
		}

		mTextToSpeechInitialized = false;
		mTextToSpeech = new TextToSpeech(this, this);
	}
	
	@Override
	public void onInit(int status) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MyTextToSpeechService.onInit : status > " + status);
        }

		if (status == TextToSpeech.SUCCESS) {
			mTextToSpeechInitialized = true;
		}
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MyTextToSpeechService.onStartCommand :  intent > " + intent);
            Log.d(TAG, "MyTextToSpeechService.onStartCommand :   flags > " + flags);
            Log.d(TAG, "MyTextToSpeechService.onStartCommand : startId > " + startId);
        }

        if (intent != null) {
            String msg = intent.getStringExtra(POWER_CONNECTED_KEY);

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "MyTextToSpeechService.onStartCommand : msg > " + msg);
            }

            if (msg != null) {
                if (mTextToSpeechInitialized && (mTextToSpeech != null)) {
                    mTextToSpeech.speak(msg, TextToSpeech.QUEUE_ADD, null);
                } else {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MyTextToSpeechService.onBind");
        }

		return null;
	}
	
	@Override
	public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MyTextToSpeechService.onDestroy");
        }

		if (mTextToSpeech != null) {
			mTextToSpeech.stop();
			mTextToSpeech.shutdown();
			mTextToSpeechInitialized = false;
            mTextToSpeech = null;
		}
	}
}

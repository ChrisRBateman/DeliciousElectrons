package org.cbateman.deliciouselectrons;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.espiandev.showcaseview.ShowcaseView;

/**
 * Main activity for Delicious Electrons application.
 */
public class DeliciousElectronsActivity extends AppCompatActivity implements
        View.OnClickListener,
        TextWatcher {
	public static final String TAG = "DeliciousElectronsTag";
	public static final String POWER_CONNECTED_KEY = "userPowerConnectedString";
	public static final String SHARED_PREFS_NAME = "myPrefs";
	
	private ImageView mAppImageView;
	private Button mSaveButton;
    private Button mTestButton;
    private EditText mMsgEditText;
    private ImageView mClearImageView;
    
    private ShowcaseView mShowcaseView;
    private int mTutorialState = -1;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "DeliciousElectronsActivity.onCreate");
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delicious_electrons);
		
		mAppImageView = (ImageView)findViewById(R.id.app_image);
		mSaveButton = (Button)findViewById(R.id.msg_save_button);
        mTestButton = (Button)findViewById(R.id.test_button);
        Button tutorialButton = (Button)findViewById(R.id.tutorial_button);
		mMsgEditText = (EditText)findViewById(R.id.power_connected_msg_edit);
        mClearImageView = (ImageView)findViewById(R.id.clear_image);
		
		mSaveButton.setOnClickListener(this);
        mTestButton.setOnClickListener(this);
		if (tutorialButton != null) {
            tutorialButton.setOnClickListener(this);
        }
        mMsgEditText.addTextChangedListener(this);
        mClearImageView.setOnClickListener(this);

		String defaultMsg = getResources().getString(R.string.default_power_connected_msg);
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		String s = prefs.getString(POWER_CONNECTED_KEY, defaultMsg);
		if (s.length() > 0) {
			mMsgEditText.setText(s);
		}
		
		// perform check for speech engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == 1) {
	        if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            // start up google play store so user can install speech engine
	            Intent installIntent = new Intent();
	            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        } else {
	        	Intent i = new Intent(this, MyTextToSpeechService.class);
	        	startService(i);
	        }
	    }
	}

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        	case R.id.msg_save_button:
        		saveUserMessage();
        		break;

            case R.id.test_button:
                testMessage();
                break;

            case R.id.clear_image:
                clearMessage();
                break;
        		
        	case R.id.tutorial_button:
        		startTutorial();
        		break;
        		
        	case R.id.showcase_button:
        		updateShowcaseView();
        		break;
        }
    }

    public void afterTextChanged(Editable s) {
        mClearImageView.setVisibility((s.length() > 0) ? View.VISIBLE : View.INVISIBLE);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
	
	/**
	 * Save the users message. Report error to user if field is blank.
	 */
	private void saveUserMessage() {
		try {
			String s = mMsgEditText.getText().toString().trim();
            if (s.length() > 0) {
            	SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        		SharedPreferences.Editor editor = prefs.edit();
        		editor.putString(POWER_CONNECTED_KEY, s);
        		editor.apply();
        		Toast.makeText(this, getResources().getString(R.string.msg_saved), Toast.LENGTH_SHORT).show();
            }
            else {
                emptyTextFieldMsg();
            }
		} catch (Exception e) {
            Log.e(TAG, "DeliciousElectronsActivity.saveUserMessage : e > " +
                    Log.getStackTraceString(e));
		}
	}

    /**
     * Test message.
     */
    private void testMessage() {
        try {
            String s = mMsgEditText.getText().toString().trim();
            if (s.length() > 0) {
                Intent i = new Intent(this, MyTextToSpeechService.class);
                i.putExtra(POWER_CONNECTED_KEY, s);
                startService(i);
            }
            else {
                emptyTextFieldMsg();
            }
        } catch (Exception e) {
            Log.e(TAG, "DeliciousElectronsActivity.testMessage : e > " +
                    Log.getStackTraceString(e));
        }
    }

    /**
     * Clear the text message field.
     */
    private void clearMessage() {
        mMsgEditText.setText("");
        mClearImageView.setVisibility(View.INVISIBLE);
    }
	
	/**
	 * Start the on screen tutorial.
	 */
	private void startTutorial() {
		if (mTutorialState != -1) {
			return;
		}
		
		if (mShowcaseView == null) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.insert = ShowcaseView.INSERT_TO_VIEW;
			mShowcaseView = ShowcaseView.insertShowcaseView(mMsgEditText, this,
					R.string.tutorial_enter_msg_title, R.string.tutorial_enter_msg_desc, co);
			mShowcaseView.overrideButtonClick(this);
			mShowcaseView.setShowcaseIndicatorScale(0.75f);
		} else {
			mShowcaseView.setShowcaseView(mMsgEditText);
			mShowcaseView.setText(R.string.tutorial_enter_msg_title, R.string.tutorial_enter_msg_desc);
			mShowcaseView.show();
		}
        mTutorialState = 1;
	}
	
	/**
	 * Update the showcaseview. 
	 */
	private void updateShowcaseView() {
		if (mShowcaseView != null) {
			switch (mTutorialState) {
				case 1:
					mShowcaseView.setShowcaseView(mSaveButton);
					mShowcaseView.setText(R.string.tutorial_save_msg_title, R.string.tutorial_save_msg_desc);
                    mTutorialState = 2;
					break;
				case 2:
                    mShowcaseView.setShowcaseView(mTestButton);
                    mShowcaseView.setText(R.string.tutorial_test_msg_title, R.string.tutorial_test_msg_desc);
                    mTutorialState = 3;
                    break;
                case 3:
					mShowcaseView.setShowcaseView(mAppImageView);
					mShowcaseView.setText(R.string.tutorial_hear_msg_title, R.string.tutorial_hear_msg_desc);
                    mTutorialState = 4;
					break;
				case 4:
					mShowcaseView.hide();
					mTutorialState = -1;
					break;
			}
		}
	}

    /**
     * Display empty text dialog window.
     */
    private void emptyTextFieldMsg() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        Resources res = getResources();
        String message = getResources().getString(R.string.msg_blank);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(res.getString(R.string.ok_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.create();
        alertDialog.show();
    }
}

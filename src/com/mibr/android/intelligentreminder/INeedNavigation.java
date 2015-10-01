package com.mibr.android.intelligentreminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class INeedNavigation extends Activity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 22221235;
	private TextToSpeech mTts;
	private String sLatitude;
	private String sLongitude;
	private String sName;
	private WakeLock screenLock=null;
	
	
	private void doDoit() {
//		showToastMessage("hi4");
		int logFilter=3;
		try {
			logFilter = Integer.valueOf(getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
		} catch (Exception e) {
		}
		if(logFilter>=2) {
			Intent navigateMe = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+sLatitude+","+sLongitude));
			navigateMe.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(navigateMe);
		} else {
			Intent intent = new Intent(
					android.content.Intent.ACTION_VIEW, 
					Uri.parse("geo:0,0?q="+sLatitude+","+ sLongitude +" (" + sName + ")"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
//		showToastMessage("bye4");
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ineed_navigation);		
		Bundle bundle = getIntent().getExtras();
		sLatitude=bundle.getString("latitude");
		sLongitude=bundle.getString("longitude");
		sName=bundle.getString("name");
		
		final Button doit = (Button)findViewById(R.id.buttonYes);
		
		doit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doDoit();
			}
		});		
		
		
		/* This makes it happen even if the system is sleeping or locked */
		screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
			     PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
			screenLock.acquire();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		//showToastMessage("hi0");
		try {
			new RetrieveResponse().execute("bubba");
			//showToastMessage("hi1");
		} catch (Exception ee) {
			//showToastMessage("Failed starting RetrieveReponse: " + ee.getMessage());
		}
		//checkVoiceRecognition();
	 }
	/* My belief is that the error I'm having with the voice window sometimes not popping up is because something is happening outside of the view thread.  This will make that happen. */
	class RetrieveResponse extends AsyncTask<String, Integer, Float> {
		@Override
		protected Float doInBackground(String... stringness) { 
			try {
//showToastMessage("hi");
				Thread.currentThread().sleep(1000);
	//SystemClock.sleep(1234);
//showToastMessage("bye yay!");
			} catch (Exception e) {
				//showToastMessage("oh no: "+e.getMessage());
			}
			return new Float(1);
			
		}

		protected void onPostExecute(Float floatness) {
			//showToastMessage("hi2");
			checkVoiceRecognition();
			//showToastMessage("bye2 yay!");
		}
	}
	public void checkVoiceRecognition() {
	  // Check if voice recognition is present
	//	PackageManager pm = getPackageManager();
	//	List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
	//			RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//		if (activities.size() == 0) {
//			Toast.makeText(this, "Voice recognizer not present",
	// //				Toast.LENGTH_SHORT).show();
	//	} else {
		//showToastMessage("hi3");
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
					.getPackage().getName());
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			//showToastMessage("bye3");
//	}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			boolean didit=false;
			if (resultCode == RESULT_OK) {
	//			showToastMessage("Result_OK");
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if(matches!=null && matches.size()>0) {
					for(int i=0;i<matches.size();i++) {
						if(matches.get(i).toLowerCase(Locale.getDefault()).trim().equals("yes")) {
							doDoit();
							didit=true;
							break;
						}
					}
				}	
				if(!didit) {
					finish();
				}
			}
			   //Result code for various error.
		   else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
		    showToastMessage("Audio Error");
		   }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
		    showToastMessage("Client Error");
		   }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
		    showToastMessage("Network Error");
		   }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
		    showToastMessage("No Match");
		   }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
		    showToastMessage("Server Error");
		   }		
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	void showToastMessage(String message){
		  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void finish() {
		//getLogger().log("VoiceHelper: 9 ... finish()",100);
		doneCode();
		super.finish();
	}

	@Override 
	public void onDestroy() {
		doneCode();
		super.onDestroy();
	}
	
	private synchronized void doneCode() {
		try {
			if (screenLock!=null) {
				screenLock.release();
				screenLock=null;
			}
		} catch (Exception e3) {}		
	}	

}

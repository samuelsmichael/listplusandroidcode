package com.mibr.android.intelligentreminder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.mibr.android.intelligentreminder.R;
import com.mibr.android.intelligentreminder.INeedLocationService.IncomingHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.google.android.vending.licensing.LicenseChecker;    
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;    
import com.google.android.vending.licensing.AESObfuscator;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class INeedToo extends TabActivity implements LocationListener,RespondsToNeedByVoiceProgress,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	public static Boolean IS_ANDROID_VERSION=true;
	private static final int JUST_CHECKING_FOR_LICENSE=39111;
	private static final int DIALOG_SPLASH = 33;
	private static final int DIALOG_SAMPLES = 34113;
	private static final int DOING_SAMPLE_NEEDS_DIALOG =1991;
	private static final int DIALOG_REMINDER_CONTACTS_SERVICE_FAILED_LICENSING =1999;
	public String Ad1Text="Press here to purchase the non-trial version of Intelligent Reminder ... only US$ 1.99";
	public String Ad2Text="Press here to purchase the non-trial version of Intelligent Reminder ... only US$ 1.99";
	// TRIAL_VS_REAL DO NOT CHANGE THESE TO .intelligentremindertrial
	public String Ad1URI="market://details?id=com.mibr.android.intelligentreminder";
	public String Ad2URI="market://details?id=com.mibr.android.intelligentreminder";
	
	private int _initialTabIndex = 0;
	private String _doingLocationCompany=null;
	public Boolean _forceNonCompany=false;
	private Location _latestLocation=null;
	public static Boolean doViewCount=false;
	private static Boolean iveCheckedAuthorizationStatusForThisPhone=false;
	public static Boolean isReminderContactsAvailable=false;
	public static Boolean iDidReminderContacts=false;
	public static long DELAYPOSTTTS=100;
	private boolean _didContact=false;
	public static int REQUEST_CAME_FROM_CONTACTS_AVAILABLE=1; 
	public static int REQUEST_CAME_FROM_SEARCH_CONTACTS=2; 
	public static int REQUEST_CAME_FROM_NEEDS_BY_VOICE=3; 
	private static Boolean _doneSplashy = false;
	CheckBox mWantSample=null;
	Dialog jdd=null;
	CheckBox mWalmart=null;
	CheckBox mWalgreens=null;
	CheckBox mSevenEleven=null;
	CheckBox mStarbucks=null;
	Dialog dialogDoingSamples;
	static ArrayList<ArrayList<String>> allItems;
	static int whereImAtInAllItems=0;
    private LocationClient mLocationClient;
    public static Location InitialLocation;


/* When changing version (and "trial" vs "real") 
 *   1. Scan on TRIAL_VS_REAL 
 *   2. In strings, change title (I think it's app name)
 *   3. Change name in manifest (NO, I don't think so)
 *   4a. right click INeed2 Android Tools Rename Application Package
 *   4b. right click com.mibr.android.intelligentreminder refactor rename
 *   5. android:debuggable="true" to "false"
 * 	 6. Real  <uses-permission android:name="com.android.vending.CHECK_LICENSE" /> this is an amazon thing, too: take it out of amazon
 * 	 
 * 
 * 	 android vs amazon
	 	android: <uses-sdk android:minSdkVersion="5" />
	    amazon: 	<uses-sdk android:minSdkVersion="8" />
	    scan on IS_ANDROID_VERSION
	    Take out: <uses-permission android:name="com.android.vending.CHECK_LICENSE" />

 * 
 * */
	public static String CURRENT_VERSION = "5.01"; // see layout_listplus.xml, as well as the manifest file and Strings
/* When TRIAL_VS_REAL*/
	//public static String PREFERENCES_LOCATION="com.mibr.android.intelligentremindertrial_preferences";
	public static String PREFERENCES_LOCATION="com.mibr.android.intelligentreminder_preferences";
/*
 * Change version in manifest
 * End When changing version
 */
	public static INeedToo mSingleton = null;
	private static Timer mReceivingTimer = null;
	private static Timer mTransmittingTimer = null;
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mLocationManager.removeUpdates(this);
		setLatestLocation(location);
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	private LocationManager mLocationManager = null; 
	private String _bestProvider=null;
	public LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		if(_bestProvider==null) {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			_bestProvider = mLocationManager.getBestProvider(criteria, false);
			mLocationManager.requestLocationUpdates(_bestProvider, 20000, 10, this);
		}
		return mLocationManager;
	}
	public Location doLocationManager() {
		Location loc=null;
		if(_latestLocation!=null) {
			loc = _latestLocation;
		} else {
			loc = getLocationManager().getLastKnownLocation(_bestProvider);
		}
		if(loc==null) {
			startListening();
		}
		return loc;
	}
	public void startListening() {
		if(mLocationManager != null) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			try {
				getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 10, this);
				getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 10, this);
			} catch (Exception e44) {}
		}
	}
	
	public void setLatestLocation(Location loc) {
		_latestLocation=loc;
	}
	public Location getLatestLocationBB() {
		if (_latestLocation==null) {
			_latestLocation=doLocationManager();
		}
		return _latestLocation;
	}
	public Boolean isVoiceActivationActive() {
		return true;
	}
	public boolean isSpecialPhone() {
		return
		INeedToo.mSingleton.getPhoneId()!=null && (
			INeedToo.mSingleton.getPhoneId().toLowerCase().equals("22a100000d9f25c5ZZ") ||
			INeedToo.mSingleton.getPhoneId().toLowerCase().equals("a100000d9f25c5ZZ") ||
			INeedToo.mSingleton.getPhoneId().toLowerCase().equals("20013fc135cd6097xx") || //moi
			INeedToo.mSingleton.getPhoneId().toLowerCase().equals("unknown") 
		);
	}
	public boolean isTestVersion () {
		boolean b=false;
		if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
// bbhbb 2011-03-26		if(INeedToo.PREFERENCES_LOCATION.equals("com.mibr.android.intelligentremindertrial_preferences")) {
			if(
			 	 isSpecialPhone() ||
				 this.isPhoneRegistered()

			) {
				b= false;
			} else {
				b= true;
			}
		} else {
			b= false;
		}
		return b;
	}

	private Timer getMyReceivingTimer() {
		if (mReceivingTimer == null) {
			mReceivingTimer = new Timer("WebProcessingReceivingActivities");
		}
		return mReceivingTimer;
	}

	private Timer getMyTransmittingTimer() {
		if (mTransmittingTimer == null) {
			mTransmittingTimer = new Timer(
			"WebProcessingTransmittingActivities");
		}
		return mTransmittingTimer;
	}

	private void startMyReceivingTimer() {
		getMyReceivingTimer().schedule(new TimerTask() {
			public void run() {
				Intent intent = new Intent(INeedToo.this, INeedWebService.class)
				.setAction("Inbound");
				startService(intent);
			}
		}, 1000 * 60, 1000 * 60 * 15);
	}

	private void startMyTransmittingTimer() {
		getMyTransmittingTimer().schedule(new TimerTask() {
			public void run() {
				Intent intent = new Intent(INeedToo.this, INeedWebService.class)
				.setAction("Outbound");
				startService(intent);
			}
		}, 1000 * 60 * 2, 1000 * 60 * 15);
	}

	private void stopMyTransmittingTimer() {
		if (mTransmittingTimer != null) {
			try {
				mTransmittingTimer.cancel();
				mTransmittingTimer.purge();
				mTransmittingTimer = null;
			} catch (Exception e) {
			}
		}
	}

	private void stopMyReceivingTimer() {
		if (mReceivingTimer != null) {
			try {
				mReceivingTimer.cancel();
				mReceivingTimer.purge();
				mReceivingTimer = null;
			} catch (Exception e) {
			}
		}
	}

	public void enableReceiving() {
		if (mReceivingTimer == null) {
			startMyReceivingTimer();
		}
	}

	public void communicateNewValueToServices(String name,Object newValue ) {
		if(name=="LoggingLevel") {
			int newValueInt=Integer.valueOf((String) newValue);
			_logFilter=newValueInt;
			mLogger=null;
			
			Intent intent = new Intent(this,INeedLocationService.class)
				.setAction("NewLogFilter")
				.putExtra("NewLogFilter",newValueInt);
			getApplicationContext().startService(intent);
			
	    	Intent jdItent2=new Intent(this, INeedTimerServices.class)
				.setAction("NewLogFilter")
				.putExtra("NewLogFilter",newValueInt);
	    	getApplicationContext().startService(jdItent2);
		}
	}
	
	public void disableReceiving() {
		if (mReceivingTimer != null) {
			stopMyReceivingTimer();
		}
	}

	public void enableTransmitting() {
		if (mTransmittingTimer == null) {
			startMyTransmittingTimer();
		}
	}

	public void disableTransmitting() {
		if (mTransmittingTimer != null) {
			stopMyTransmittingTimer();
		}
	}
	private INeedDbAdapter mDbAdapter = null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	
	public void callbackFromVoice(Boolean succeeded, Boolean isContact) {
		Intent i2 = new Intent(this, INeedToo.class);
		i2.putExtra("initialtabindex", (long) 0);
		i2.putExtra("iscontact",false);
		startActivity(i2);
	}
	
	private Dialog dREMINDER_CONTACTS_SERVICE_FAILED_LICENSING;
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_REMINDER_CONTACTS_SERVICE_FAILED_LICENSING:
			AlertDialog.Builder buildersvc = new AlertDialog.Builder(this);
			buildersvc.setMessage("The Reminder Contact Service add-on that you've got installed is not a licensed version");
			buildersvc
			.setCancelable(false)
			.setNeutralButton(R.string.msg_cus, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) {
					String[] mailto = {"info@intelligentreminder.com",""};
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, ""
							.toString());
					sendIntent.putExtra(Intent.EXTRA_TEXT, ""
							.toString());
					sendIntent.setType("text/plain");
					startActivity(Intent.createChooser(sendIntent, "Send EMail..."));
				}
			})
			.setPositiveButton(R.string.msg_register, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) {
					

					if(INeedToo.IS_ANDROID_VERSION) {
				        String uri=getString(R.string.indeedtopayforservice);
				        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				        startActivity(ii3);
					} else {
						Uri uri = Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=com.mibr.android.remindercontacts");
						Intent intent=new Intent(Intent.ACTION_VIEW,uri);
						startActivity(intent);
					}

					
					
					
					isReminderContactsAvailable=true;
					iDidReminderContacts=false;
				}
			})
			.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			AlertDialog alert=buildersvc.create();	
			return alert;
			
			
			
			
		case DOING_SAMPLE_NEEDS_DIALOG:
			if(allItems==null) {
				return null;
			}
			dialogDoingSamples = new Dialog(this);
			dialogDoingSamples.setOwnerActivity(this);
			dialogDoingSamples.setContentView(R.layout.ineedvoiceprogress);
			dialogDoingSamples.setTitle("Working ...");
			((Button)dialogDoingSamples.findViewById(R.id.needvoiceprogress_cancel)).setEnabled(true);
			((Button)dialogDoingSamples.findViewById(R.id.needvoiceprogress_ok)).setEnabled(false);;
//			dialogDoingSamples.show();
			ArrayList stuff=new ArrayList();
			stuff.add(dialogDoingSamples);
			stuff.add(getApplicationContext());
			stuff.add(this);
			stuff.add(getLocationManager());
			stuff.add(getDbAdapter());
			stuff.add(allItems.get(whereImAtInAllItems));
			stuff.add(new Integer(1));
			new NeedByVoiceProgress().execute(stuff);
			return dialogDoingSamples;
			
		case DIALOG_SAMPLES:
			Dialog aDialog=null;
			AlertDialog.Builder builder2;
			LayoutInflater inflater2=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout2=inflater2.inflate(R.layout.ineedsamples, (ViewGroup)findViewById(R.id.hijklmnop));
			builder2 = new AlertDialog.Builder(this);
			builder2.setView(layout2);
			builder2.setTitle("Build Sample Data Set");
			builder2.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					allItems=new ArrayList<ArrayList<String>>();
					Boolean doit=false;
					StringBuilder sb=new StringBuilder();
					StringBuilder sb2=new StringBuilder();
					StringBuilder sb3=new StringBuilder();
					int cnt=0;
					Boolean dw=false;
					ArrayList<String> jdmatchesSS=null;;
					sb.append("I need Print Cartridges description HP 28 and 48 ");
					if(mWalmart!=null && mWalmart.isChecked()) {
						sb.append(" location Walmart ");
						dw=true;
					}
					if(mWalgreens!=null && mWalgreens.isChecked()) {
						sb.append(" location Walgreens ");
						dw=true;
					}
					if(dw) { // they checked something
						doit=true;						
						jdmatchesSS = new ArrayList<String>();
						jdmatchesSS.add(sb.toString());
						allItems.add(jdmatchesSS);
						cnt++;
					}
					if(mSevenEleven!=null && mSevenEleven.isChecked()) {
						sb3.append("I need Energy Drink description Red Bull location 7-Eleven ");
						ArrayList<String> jdmatches = new ArrayList<String>();
						jdmatches.add(sb3.toString());
						allItems.add(jdmatches);
						cnt++;
						doit=true;
					}
					
					if(mStarbucks!=null && mStarbucks.isChecked()) {
						sb2.append("I need Coffee description French Roast location Starbucks");
						ArrayList<String> jdmatches = new ArrayList<String>();
						jdmatches.add(sb2.toString());
						allItems.add(jdmatches);
						cnt++;
						doit=true;
					}
					if(doit) {
						whereImAtInAllItems=0;
						INeedToo.this.showDialog(DOING_SAMPLE_NEEDS_DIALOG);
					}
				
				}
			});
			mWalmart=(CheckBox)layout2.findViewById(R.id.BuildWalmart);
			mWalgreens=(CheckBox)layout2.findViewById(R.id.BuildWalgreens);
			mSevenEleven=(CheckBox)layout2.findViewById(R.id.Build7Eleven);
			mStarbucks=(CheckBox)layout2.findViewById(R.id.BuildStarbucks);

			aDialog=builder2.create();
			return aDialog;

		
		case DIALOG_SPLASH:
			/*
			jdd=new Dialog(mContext);
			jdd.setContentView(R.layout.ineed2splash);
			jdd.setTitle("W E L C O M E");
			*/
			
			AlertDialog.Builder builder;
			LayoutInflater inflater=(LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout=inflater.inflate(R.layout.ineed2splashv2, (ViewGroup)findViewById(R.id.ScrollView01lmnop2));
			builder = new AlertDialog.Builder(this);
			
			builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					if(mWantSample!=null) {
						if(mWantSample.isChecked()) {
							showDialog(DIALOG_SAMPLES);
						}
					}
				}
			});
			builder.setTitle("W E L C O M E");
			if(getDbAdapter().thereAreSomeLocationsOnSystem()) {
				LinearLayout ll=(LinearLayout)layout.findViewById(R.id.CreateDefaultValues);
				ll.setVisibility(View.INVISIBLE);
			} else {
				mWantSample=(CheckBox)layout.findViewById(R.id.BuildDefaultValues);
			}
			builder.setView(layout);
			jdd=builder.create();
			//Button butt=(Button)jdd.findViewById(R.id.Button01);
			/*butt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					jdd.cancel();
				}
			});
			*/
			break;
		default:
			return null;
		}
		return jdd;

	}
//	private static LocationManager mLocationManager = null;
	@Override
	public void onDestroy() {
		super.onDestroy();
		doUnbindService();
		try {
			mChecker.onDestroy();
		} catch (Exception eee) {}
	}
	
	private Handler mHandler;	
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // Generate 20 random bytes, and put them here.    
    private static final byte[] SALT = new byte[] {     
    	-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,     
    	-45, 77, -117, -36, -113, -11, 32, -64, 89     
    };
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhZrIaNqneMAux90tFKHBwnFvS+NXqIhcqFQ3ZrUTuQN/Uy6hZZyKUJVcnUOMVWPWtK6dtN6FzqTNNK3c8aJpAiTQH0rtFzh4lt1CI0BojSV4WfDosgLh8Tzy6iy70z7R1g8P3CiHcwbO96kO1Hut997gYtFWUO/Ot1B6SdourkxN/oUrcaS0JAjaIcBYrfQhlm8QOJw3FdGqzGjtQ6pJMVc1SI6oSBeKJfuvZy7nLU4+lwdb73McCJLfJUkhNBow+knSbg5L5YxGpPDRVxfeYvVTadJGRHiPRnVI0ndk+DZNBOifgqRQubO9lri0uwu4gx+D12CyvpC1YS2A1gn7ZwIDAQAB";
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {        
		@Override
		public void allow(int reason) {
    		if (isFinishing()) {                
    			// Don't update UI if Activity is finishing.                
    			return;            
    			}            
    		// Should allow user access.            
    		handleResultOfLicenseCheck(true);        
		}
		@Override
		public void dontAllow(int reason) {
    		if (isFinishing()) {                
    			// Don't update UI if Activity is finishing.                
    			return;            
    			}            
    		handleResultOfLicenseCheck(false);            
		}
		@Override
		public void applicationError(int errorCode) {
    		if (isFinishing()) {                
    			// Don't update UI if Activity is finishing.                
    			return;            
    			}            
    		handleResultOfLicenseCheck(false);            
		}    
    }
	
	private void handleResultOfLicenseCheck(Boolean allow) {       
		final Boolean mAllow=allow;
		mHandler.post(new Runnable() {            
			public void run() {           
				if(!mAllow) {
					TrialIsEndingWarningOrLicensingFailed("You may be dealing with an unlicensed version of Intelligent Reminder. If this is not true, please press Cancel, and re-load Intelligent Reminder.",true);
				}   
			}				
			});    
		}	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
/* this was just a test		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
*/		
//		TabHost host=this.getTabHost();
//		host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
//			
//			@Override
//			public void onTabChanged(String tabId) {
//				if(INeedToo.this._doingCreatingTabs==false) {
//					if(tabId.equals("tab1")) {
//						INeedToo.this._forceNonCompany=true;
//					}
//				}
//			}
//		});
		super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(this, this, this);		
        mLocationClient.connect();
		try {
			if (getIntent().getAction()!=null&&getIntent().getAction().equals("doPrimitiveDeletedNeed")) {
				transmitNetwork(getIntent().getStringExtra("phoneid"));
				return;
			}
		} catch (Exception eieio) {
			return;
		}
		try {
			/*
			 						putExtra("needId",needId).
						putExtra("foreignNeedId",foreignNeedId).
						putExtra("phoneid",phoneid).
						putExtra("foreignLocationId",foreignLocationId)

			 */
			if (getIntent().getAction()!=null&&getIntent().getAction().equals("transmitNetworkDeletedThisNeedByOnBehalfOf")) {
				long needId=getIntent().getLongExtra("needId",-11);
				long foreignNeedId=getIntent().getLongExtra("foreignNeedId",-11);
				String phoneId=getIntent().getStringExtra("phoneid");
				long foreignLocationId=getIntent().getLongExtra("foreignLocationId",-11);
				if(needId!=-11&&foreignNeedId!=-11&&foreignLocationId!=-11&&isNothingNot(phoneId))
					transmitNetworkDeletedThisNeedByOnBehalfOf(
						needId, 
						foreignNeedId, 
						phoneId, 
						foreignLocationId
					);
			return;
			}
		} catch (Exception ei3) {
			return;
		}
		
		
		/*bbhbb 2011-03-26*/	/*bbhbb2013_05_01 isn't this being done below?
		 * 	Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler("")); */

		mHandler = new Handler();
		
		
		doBindService();
		mSingleton = this;
		///////////mSingleton.log("onCreating_INeedToo", 1);

		if(getPackageName().toLowerCase().indexOf("trial")!=-1 && !isSpecialPhone()) {
			if(!iveCheckedAuthorizationStatusForThisPhone) {
				doViewCount=true;
				iveCheckedAuthorizationStatusForThisPhone=true;
				final Timer jdTimer = new Timer("Registering");
				jdTimer.schedule(new TimerTask() {
					public void run() {
						Thread thread=new Thread(new Runnable() {
							public void run() {
								Intent intent=new Intent(INeedToo.this,INeedWebService.class)
								.setAction("CheckStatus");
								startService(intent);
								jdTimer.cancel();
							}
						});
						thread.setPriority(Thread.MIN_PRIORITY);
						thread.run();
					}
				}, 3000, 1000 * 60 * 10);
	
			}
		} else {
			if (IS_ANDROID_VERSION) {
				if(!isSpecialPhone() || INeedToo.mSingleton.getPhoneId().toLowerCase().equals("20013fc135cd6097xx")) {
					
					final Timer jdTimer = new Timer("Licensing");
					jdTimer.schedule(new TimerTask() {
						public void run() {
							Thread thread=new Thread(new Runnable() {
								public void run() {
							        // Construct the LicenseCheckerCallback. The library calls this when done.        
									mLicenseCheckerCallback = new MyLicenseCheckerCallback();        
									// Construct the LicenseChecker with a Policy.        
									mChecker = new LicenseChecker(            
											INeedToo.this, new ServerManagedPolicy(INeedToo.this,                
													new AESObfuscator(SALT, 
															getPackageName(),
						//									"com.mibr.android.intelligentreminder",
															getDeviceId())),            
													BASE64_PUBLIC_KEY  // Your public licensing key.
													);
									mChecker.checkAccess(mLicenseCheckerCallback);
								}
							});
							thread.setPriority(Thread.MIN_PRIORITY);
							thread.run();
						}
					}, 3000, 1000 * 60 * 10);
				}
			}
		}

		try {
			_logFilter = Integer.valueOf(getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
		} catch (Exception e) {
		}
		
		if(!INeedLocationService.USING_LOCATION_SERVICES) {
			
			int logFilter=3;
			try {
				logFilter = Integer.valueOf(getSharedPreferences(
						INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
			} catch (Exception e) {
			}
			Intent jdIntent=new Intent(this, INeedTimerServices.class).
					putExtra("logFilter",logFilter);
			
			
				getApplicationContext().startService(jdIntent);
		}
		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
				""));
		if (INeedToo.mSingleton.getPhoneId().toLowerCase().equals("22a100000d9f25c5")) {
			DELAYPOSTTTS=100;
		}

// I don't need this because IHaveNeeds.onresume does it		getApplicationContext().startService(new Intent(this, INeedLocationService.class));
		if(!_doneSplashy) {
			_doneSplashy=true;
			if(!getVersionFile()) {
				stampVersion();
				showDialog(DIALOG_SPLASH);
			} else {
				//showDialog(DIALOG_SPLASH);
			}
		}
		if(allItems!=null) {
			whereImAtInAllItems++;
			if( whereImAtInAllItems<allItems.size()) {
				showDialog(DOING_SAMPLE_NEEDS_DIALOG);
			} else {
				allItems=null;
			}
		}

		setTitle(getHeading());
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			_initialTabIndex = bundle.getInt("initialtabindex", 0);
			_didContact=bundle.getBoolean("iscontact",false);
			if(_initialTabIndex==1) {
				_doingLocationCompany=bundle.getString("doingcompany");
			} else {
				_doingLocationCompany= null;
			}
		} else {
			_doingLocationCompany= null;
		}
		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(
				"Need",
				
				getResources().getDrawable(
						R.drawable.status_bar2_blackwhite)).setContent(
								new Intent(this, IHaveNeeds.class).putExtra("iscontact", this._didContact)));

		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(
				"Location",(BitmapDrawable) getResources().getDrawable(
						android.R.drawable.ic_menu_mapmode)).setContent(
								new Intent(this, IHaveLocations.class).putExtra("doingcompany", _doingLocationCompany)));
		Intent intent = new Intent(this, NeedMap.class).putExtra("doingcompany", _doingLocationCompany);
		tabHost.addTab(tabHost.newTabSpec("tab2a").setIndicator(
				"Map",(BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_dialog_map)).setContent(intent));
		/*
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(
				"History",(BitmapDrawable) getResources().getDrawable(
						R.drawable.chart)).setContent(
								new Intent(this, IHaveHistory.class)));
		*/
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(
				"System",(BitmapDrawable) getResources().getDrawable(
						R.drawable.status_bar1_blackwhite)).setContent(
								new Intent(this, ListPlus.class)));
		/*		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(
				"Need",
				scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.status_bar2_blackwhite), 36f)).setContent(
								new Intent(this, IHaveNeeds.class).putExtra("iscontact", this._didContact)));
		
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(
					"Location",
					scaleTo((BitmapDrawable) getResources().getDrawable(
							android.R.drawable.ic_menu_mapmode), 36f)).setContent(
									new Intent(this, IHaveLocations.class).putExtra("doingcompany", _doingLocationCompany)));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(
				"History",
				scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.chart), 36f)).setContent(
								new Intent(this, IHaveHistory.class)));
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(
				"System",
				scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.status_bar1_blackwhite), 31f)).setContent(
								new Intent(this, ListPlus.class)));
*/
		tabHost.setCurrentTab(_initialTabIndex);
		boolean networkingOutbound=getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
				Preferences.MODE_PRIVATE).getBoolean(
						"Networking_Outbound_Enabled", false);
		if(INeedWebService.mSingleton==null) {
		
			if (networkingOutbound) {
				enableTransmitting();
			}
			if (getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getBoolean(
							"Networking_Inbound_Enabled", false)) {
				enableReceiving();
			}
		}
	}

	public String getHeading() {
		String trialVersionBlurb="";
		if(this.isTestVersion()) {
			trialVersionBlurb="(Trial)";
		}
		Resources res=getResources();
		return String.format(res.getString(R.string.app_title),trialVersionBlurb);
	}
	public void doDatabaseCopy() {
		File file=null;
		Boolean exists=false;
		try {
			file = new File("/data/data/com.mibr.android.intelligentreminder/databases/data");
			if (file.exists()) {
				exists=true;
				Boolean canRead=file.canRead();
				
				FileInputStream isr = new FileInputStream("/data/data/com.mibr.android.intelligentremindertrial/databases/data");
				FileOutputStream pw = new FileOutputStream("/sdcard/mibr/jddatabase",false);

				byte[] cc = new byte[4096];
				try {
					int cnt = isr.read(cc, 0, 4096);
					int c = 0;
					while (cnt > 0) {
						pw.write(cc, 0, cnt);
						cnt = isr.read(cc, 0, 4096);
					}
				} finally {
					try {
						isr.close();
					} catch (Exception e) {
					}
					try {
						pw.flush();
					} catch (Exception e2) {
					}
					try {
						pw.close();
					} catch (Exception e2) {
					}
				}
			}
		} catch (Exception ee) {
			exists=false;
		}
	}
	private boolean getVersionFile() {
		boolean retValue=false;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr/version"+CURRENT_VERSION+"a.txt");
			if (file.exists()) {
				retValue=true;
			}
		} else {
			file = new File("/data/data/"+getPackageName()+"/files/version"+CURRENT_VERSION+"a.txt");
			if (file.exists()) {
				retValue=true;
			}
		}
		return retValue;
	}
	private void stampVersion() {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getVersionOutputStream();
			pw = new PrintWriter(fos);
			pw.write(CURRENT_VERSION + "\n");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception eee) {
			}
			try {
				fos.close();
			} catch (Exception eee) {
			}
		}
	}


	private FileOutputStream getVersionOutputStream() throws FileNotFoundException {
		FileOutputStream fileOutputStream_Version = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Version = new FileOutputStream("/sdcard/mibr/version"+CURRENT_VERSION+"a.txt",
					false);
		} else {
			fileOutputStream_Version = openFileOutput("version"+CURRENT_VERSION+"a.txt",
					MODE_WORLD_READABLE);
		}
		return fileOutputStream_Version;
	}
	public Boolean isReminderContactsAvailable() {
		return isReminderContactsAvailable;
	}
	public void showReminderContactsNotAvailable(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("This function requires the Intelligent Reminder add-on for Reminder Contacts.  Do you want to fetch it?")
		.setCancelable(false)
		.setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				if(IS_ANDROID_VERSION) {
			        String uri = "market://details?id=com.mibr.android.remindercontacts";
			        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			        startActivity(ii3);
				} else {
					Uri uri = Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=com.mibr.android.remindercontacts");
					Intent intent=new Intent(Intent.ACTION_VIEW,uri);
					startActivity(intent);
				}
			}
		})
		.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		AlertDialog alert=builder.create();	
		alert.setOwnerActivity(activity);
		alert.show();		
	}
	public void TrialIsEndingWarningOrLicensingFailed(String verbiage, Boolean wereDealingWithLicensingHere) {
		final Boolean wdwlh=wereDealingWithLicensingHere;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(verbiage)
		.setCancelable(false)
		.setNeutralButton(R.string.msg_cus, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				String[] mailto = {"diamondsoftware222@gmail.com",""};
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, ""
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, ""
						.toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send EMail..."));
			}
		})
		.setPositiveButton(R.string.msg_register, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				Intent i4=new Intent(INeedToo.this,INeedToPay.class);
				startActivity(i4);
			}
		})
		.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(wdwlh) {
					INeedToo.this.finish();
				}
			}
		});
		AlertDialog alert=builder.create();	
		try {
			alert.show();
		} catch (Exception ee33) {
			int bkhere=3;
			int bkhere2=bkhere;
		}
	}
	public void unRegisterPhoneWithReason(String reason) {
		File file = null;
		Boolean doAlert = false;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr/log2.txt");
			if (file.exists()) {
				file.delete();
				doAlert=true;
			}
		} else {
			file = new File("/data/data/"+getPackageName()+"/files/log2.txt");
			if (file.exists()) {
				file.delete();
				doAlert=true;
			}
		}
		if(doAlert) {
			setTitle(getHeading());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Your registration has failed.  Reason: "+reason)
			.setCancelable(false)
			.setPositiveButton(R.string.msg_register, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) {
					Intent i4=new Intent(INeedToo.this,INeedToPay.class);
					startActivity(i4);
				}
			})
			.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			AlertDialog alert=builder.create();	
			alert.show();
			
		}
	}
	public void unRegisterPhone() {
		try {
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr/log2.txt");
			if (file.exists()) {
				file.delete();
			}
		} else {
			file = new File("/data/data/"+getPackageName()+"/files/log2.txt");
			if (file.exists()) {
				file.delete();
			}
		}
		} catch (Exception eee) {
			
		} finally {
			setTitle(getHeading());
		
		}
	}
	public void registerPhone() {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		File file = null;
		try {
			if (isSdPresent()) {
				file = new File("/sdcard/mibr");
				if (!file.exists()) {
					file.mkdirs();
				}
				fos = new FileOutputStream("/sdcard/mibr/log2.txt",
						true);
			} else {
				fos = openFileOutput("log2.txt",
						MODE_WORLD_READABLE | MODE_APPEND);
			}
			fos = getLogOutputStream();	
			pw = new PrintWriter(fos);
			pw.write("");
		} catch (Exception ee3) {
			
		} finally {
			try {
				pw.close();
			} catch (Exception eee) {
			}
			try {
				fos.close();
			} catch (Exception eee) {
			}
			setTitle(getHeading());
		}

	}
	public Boolean isPhoneRegistered() {
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr/log2.txt");
			if (file.exists()) {
				return true;
			}
		} else {
			file = new File("/data/data/"+getPackageName()+"/files/log2.txt");
			if (file.exists()) {
				return true;
			}
		}
		return false;
	}
	private File getLogFile() {
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr/log.txt");
			if (file.exists()) {
				return file;
			}
		} else {
			file = new File("/data/data/"+getPackageName()+"/files/log.txt");
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	private FileInputStream getLogInputStream() throws FileNotFoundException {
		FileInputStream fileInputStream = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileInputStream = new FileInputStream("/sdcard/mibr/log.txt");
		} else {
			fileInputStream = openFileInput("log.txt");
		}
		return fileInputStream;

	}

	private FileOutputStream getLogOutputStream() throws FileNotFoundException {
		FileOutputStream fileOutputStream_Log = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Log = new FileOutputStream("/sdcard/mibr/log.txt",
					true);
		} else {
			fileOutputStream_Log = openFileOutput("log.txt",
					MODE_WORLD_READABLE | MODE_APPEND);
		}
		return fileOutputStream_Log;
	}

	public String getNickName(String phoneId) {
		String retval = phoneId;
		try {
			String nicks = getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString(
							"Networking_UserFriendlyNames", "");
			String[] sanicks = nicks.split(";");
			String phones = getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("Networking_PhoneIDs",
					"");
			String[] saphones = phones.split(";");
			for (int c = 0; c < sanicks.length; c++) {
				if (saphones[c].equalsIgnoreCase(phoneId)) {
					return sanicks[c];
				}
			}
		} catch (Exception eee) {
		}
		return retval;
	}

	public String getPhoneId() {
		String anotherID = android.provider.Settings.System.getString(
				getContentResolver(),
				android.provider.Settings.System.ANDROID_ID);
		if (INeedToo.isNothingNot(anotherID)) {
			return anotherID.trim();
		}
		try {
			String number = this.getDeviceNumber();
			if (isNothingNot(number)) {
				return number.trim();
			}
		} catch (Exception e33) {
			try {
				String id = this.getDeviceId();
				if (isNothingNot(id)) {
					return id.trim();
				}
			} catch (Exception e3d) {
			}
		}
		return "unknown";
		//return "20013fd8f4dbc118";
	}

	public static boolean isSdPresent() {
		String sdState=android.os.Environment.getExternalStorageState();
		return sdState.equals(
				android.os.Environment.MEDIA_MOUNTED) ;
	}

	public synchronized void writeLogTo(OutputStream out) 
	throws FileNotFoundException, IOException {
		FileInputStream fis = null;
		fis = getLogInputStream();
		java.io.InputStreamReader isr = new InputStreamReader(fis);
		PrintWriter pw = new PrintWriter(out);
		char[] cc = new char[4096];
		try {
			int cnt = isr.read(cc, 0, 4096);
			int c = 0;
			while (cnt > 0) {
				pw.write(cc, 0, cnt);
				cnt = isr.read(cc, 0, 4096);
			}
		} finally {
			try {
				isr.close();
			} catch (Exception e) {
			}
			try {
				pw.flush();
			} catch (Exception e2) {
			}
			try {
				pw.close();
			} catch (Exception e2) {
			}
		}
	}

	public synchronized void clearLog() {
		if (getLogFile() != null) {
			getLogFile().delete();
		}
	}

	public long getLogFileSize() {
		File file = getLogFile();
		if (file != null) {
			return file.length();
		} else {
			return 0l;
		}
	}
	
	private Timer getLocationsTimer() {
			return new Timer(
			"TransmittingNetworkActivities");
	}

	public void transmitNetworkDeletedThisNeedByOnBehalfOf(long localNeedID, long foreignNeedID, String needPhoneID, long foreignLocationId) {
		if(!needPhoneID.trim().toLowerCase().equals(this.getPhoneId().toLowerCase().trim())) { 
			TimerTaskDeleteNeedOnBehalfOf tt=new TimerTaskDeleteNeedOnBehalfOf(localNeedID, foreignNeedID, needPhoneID, foreignLocationId); 
			getLocationsTimer().schedule(tt, 1000 * 60 * 2);			
		}
	}
	private class ThreadDeleteNeedOnBehalfOf extends Thread {
		private long _localNeedID;
		private long _foreignNeedID;
		private String _needPhoneID;
		private long _foreignLocationId;
		public ThreadDeleteNeedOnBehalfOf(ThreadGroup tg, Runnable run, String name, long localNeedID, long foreignNeedID, String needPhoneID, long foreignLocationId) {
			super(tg,run, name) ;		
			_localNeedID=localNeedID;
			_foreignNeedID=foreignNeedID;
			_needPhoneID=needPhoneID;
			_foreignLocationId=foreignLocationId;
			setPriority(Thread.MIN_PRIORITY);
		}
		public void run() {
			super.run();
		}
	}
	private class TimerTaskDeleteNeedOnBehalfOf extends TimerTask {
		private long _localNeedID;
		private long _foreignNeedID;
		private String _needPhoneID;
		private long _foreignLocationId;
		public TimerTaskDeleteNeedOnBehalfOf(long localNeedID, long foreignNeedID, String needPhoneID, long foreignLocationId) {
			_localNeedID=localNeedID;
			_foreignNeedID=foreignNeedID;
			_needPhoneID=needPhoneID;
			_foreignLocationId=foreignLocationId;
		}
		public void run() {
			ThreadDeleteNeedOnBehalfOf deleteOnBehalfOfThread = new ThreadDeleteNeedOnBehalfOf(null, new RunnableDeleteNeedOnBehalfOf(_localNeedID,_foreignNeedID,_needPhoneID, _foreignLocationId), "DeletingOnBehalfOfNetwork",_localNeedID, _foreignNeedID, _needPhoneID, _foreignLocationId );
			deleteOnBehalfOfThread.setPriority(Thread.MIN_PRIORITY);
			deleteOnBehalfOfThread.start();		
		}
	}
	
	public void transmitNetwork(String thePhoneID) {
		if(thePhoneID.trim().toLowerCase().equals(this.getPhoneId().toLowerCase().trim())) {
			getLocationsTimer().schedule(new TimerTask() {
				public void run() {
			        Thread notifyingThread = new Thread(null, mTaskTransmitNetwork, "TransmittingNetwork");
			        notifyingThread.setPriority(Thread.MIN_PRIORITY);
			        notifyingThread.start();
				}
			}, 1000 * 60 * 2);
		}
	}
	
	private class RunnableDeleteNeedOnBehalfOf implements Runnable {
		private long _localNeedID;
		private long _foreignNeedID;
		private String _needPhoneID;
		private long _foreignLocationId;
		public RunnableDeleteNeedOnBehalfOf(long localNeedID, long foreignNeedID, String needPhoneID, long foreignLocationId) {
			_localNeedID=localNeedID;
			_foreignNeedID=foreignNeedID;
			_needPhoneID=needPhoneID;
			_foreignLocationId=foreignLocationId;
		}
		public void run() {
    		Intent intent=new Intent(
    				INeedToo.this,INeedWebService.class)
    					.setAction("DeleteOnBehalfOf")
    					.putExtra("localneedid", _localNeedID)
    					.putExtra("foreignneedid", _foreignNeedID)
    					.putExtra("needphoneid",_needPhoneID)
    					.putExtra("localphoneid",INeedToo.this.getPhoneId())
    					.putExtra("foreignlocationid",_foreignLocationId);
    		startService(intent);
		}
	}

	
    private Runnable mTaskTransmitNetwork = new Runnable() {
        public void run() {
    		Intent intent=new Intent(INeedToo.this,INeedWebService.class).setAction("Outbound").putExtra("doingmanually", false);
    		startService(intent);
        }
    };
	

	public void transmitLog() {
		transmitLog(false);
	}
	public void transmitLog(boolean clearWhenDone) {
		Intent intent = new Intent(this, INeedWebService.class)
		.setAction("TransmitLog").putExtra("clearWhenDone", true);
		startService(intent);
	}

	private String getDeviceNumber() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	private String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	public static String padString(boolean left, int size, String str,
			char padder) {
		if (str == null) {
			return "";
		}
		if (str.length() <= size) {
			for (int c = 0; c < (size - str.length()); c++) {
				if (left) {
					str = String.valueOf(padder).toString() + str;
				} else {
					str = str + String.valueOf(padder);
				}
			}
			return str;
		} else {
			return str;
		}

	}

	public static String leftMostCharacters(String str, int len) {
		if (str == null) {
			return "";
		} else {
			if (str.length() < len) {
				return str;
			} else {
				return str.substring(0, len - 1);
			}
		}
	}

	public static String worryAboutWatermark(String editField,
			String checkAgainst) {
		if (editField.equals(checkAgainst)) {
			return "";
		} else {
			return editField;
		}
	}

	public static boolean isNothing(Object obj) {
		if (obj == null) {
			return true;
		} else {
			if (obj.toString().trim().equals("")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNothingNot(Object obj) {
		return !isNothing(obj);
	}

	public static BitmapDrawable scaleTo(BitmapDrawable bmd, float newSize) {
		Bitmap bm = bmd.getBitmap(); 
		int width = bm.getWidth();
		float scale = (float) (newSize / (float) width);
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap bmNew = Bitmap
		.createBitmap(bm, 0, 0, width, width, matrix, true);
		return new BitmapDrawable(bmNew);
	}
	private List<Address> addressList=null;
	public List<Address> getAddressList(Location location) {
		if(addressList==null || addressList.size()==0) {
			Geocoder g=new Geocoder(getApplicationContext());
			try {
				addressList=g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			} catch (Exception eieio) {}
		}
		return addressList;
	}
	
	
/******************************* Message Stuff ****************************************************/	
    static final int MSG_GETCONTACTS =5;
    static final int MSG_HERESCONTACTS=6;
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound=false;
	private static final int DIALOG_ADDCONTACTFROMEXISTING =101;

	/** Some text view we are using to show state information. */

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case MSG_HERESCONTACTS:
				Bundle bundle=msg.getData();
				String NoLicense=bundle.getString("NoLicense");
				if(NoLicense != null && NoLicense.equals("true")) {
					showDialog(DIALOG_REMINDER_CONTACTS_SERVICE_FAILED_LICENSING);
					isReminderContactsAvailable=false;
				} else {
					isReminderContactsAvailable=true;
					String cc=bundle.getString("bbhbb");
					String[] sa1=cc.split("\\^");
					String[] ids=sa1[0].split("\\|");
					contactIds=new long[ids.length];
					contactNames=sa1[1].split("\\|");
					for(int c=0;c<ids.length;c++) {
						contactIds[c]=Long.valueOf(ids[c]);
					}
					if(msg.arg1!=JUST_CHECKING_FOR_LICENSE) {
						if(msg.arg1==REQUEST_CAME_FROM_CONTACTS_AVAILABLE) { // came from NeedView popup contracts
							bbhbb.showDialog(DIALOG_ADDCONTACTFROMEXISTING);
						} else { 
							if(msg.arg1==REQUEST_CAME_FROM_NEEDS_BY_VOICE) { 
							}
						}
		//		    	contactIds=bundle.getLongArray("ContactIds");
			//	    	contactNames=bundle.getStringArray("ContactNames");
						
						String dd=cc;
					}
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	public static Activity bbhbb=null;
	public static long[] contactIds=null;
	public static String[] contactNames=null;
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			if (!iDidReminderContacts) {
				doGetContactsFromService(JUST_CHECKING_FOR_LICENSE);
				iDidReminderContacts=true;
			}

			// We want to monitor the service for as long as we are
			// connected to it.

			// As part of the sample, tell the user what happened.
//			Toast.makeText(HelloAndroid.this, "Remote svc connected",
	//				Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			// As part of the sample, tell the user what happened.
//			Toast.makeText(HelloAndroid.this, "disconnected",
	//				Toast.LENGTH_SHORT).show();
		}
	};
	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			// Detach our existing connection.
			try {
				unbindService(mConnection);
			} catch (Exception ee) {
				int bkhere=3;
				int bkthere=bkhere;
			}
			mIsBound = false;
			//mCallbackText.setText("Unbinding.");
		}
	}
	
	
	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		try {
			// Boolean bb=bindService(new Intent(
			// HelloAndroid.this,ReminderContactsService.class),
			// mConnection, Context.BIND_AUTO_CREATE);
			//bbhbb
			if(!mIsBound) {
				Intent jdIntent=new Intent(this,ReminderContactsService.class);
				bindService(jdIntent,mConnection,Context.BIND_AUTO_CREATE);
//				startService(jdIntent);
				
				/* bbhbb 2013_05_14 
				Boolean bb = bindService(new Intent(
						"com.mibr.android.remindercontacts.app.REMOTE_SERVICE"),
						mConnection, Context.BIND_AUTO_CREATE);
				*/
				mIsBound = true;
				// indeedtopayforservice
				
				
			}
		} catch (Exception eee) {
			int bkhere = 3;
			int bkthere = bkhere;

		}
	}
	
	public void doGetContactsFromService(int msgRemember) {
		if (mService != null) {
			try {
				Message msg = Message.obtain(null, MSG_GETCONTACTS);
				msg.arg1=msgRemember;
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// There is nothing special we need to do if the service
				// has crashed.
			}
		}		
	}
	public void log(String string, int level) {
		getLogger().log(string,level);
	}
	private Logger mLogger = null;
	private int _logFilter=3;

	private Logger getLogger() {
		if (mLogger == null) {
			mLogger = new Logger(_logFilter,"INeedToo",this);
		}
		return mLogger;
	}
	@Override
	public void onConnected(Bundle arg0) {
		InitialLocation=mLocationClient.getLastLocation();
        mLocationClient.disconnect();
	}


	@Override
	public void onDisconnected() {
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}		
}
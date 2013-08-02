package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore.Audio;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class INeedLocationService extends Service {
	private static String IMAT_PROXIMITY_ALERT = "com.mibr.android.need2trial.PROXIMITY_ALERT";
	private static int MILLISECONDS_FOR_HOLDING_LOCATIONALERT = 1000000;
	public static boolean USING_LOCATION_SERVICES=false;
	private NotificationManager mNotificationManager;
	private static int dodah = 0;
	private String mPhoneId="";
	LinearLayout notificationPopup;
	final static String TAG = "bub";
	public static INeedLocationService mSingleton=null;



	/* playing window 1 */
	
	private View mView;

	private static WindowManager mWindowManager;


	private void logNotification(boolean isEntering, long id, double latitude,
			double longitude, String name) {
		Location lastKnownLocation = getmProximityManager().getLastKnownLocation();
		try {
			INeedToo.mSingleton.setLatestLocation(lastKnownLocation);
		} catch (Exception eii33) { 
			
		}
		String provider = getmProximityManager().getProvider();
		String whatsHappening = "ENTERING";
		if (!isEntering) {
			whatsHappening = "EXITING"; 
		}
		Location location = new Location(provider);
		location.setLatitude(Double.valueOf(latitude));
		location.setLongitude(Double.valueOf(longitude));
		float dx = lastKnownLocation.distanceTo(location);
		DecimalFormat df = new DecimalFormat("###.#####");
		String sformattedDx = df.format((double) dx);
		getLogger().log(whatsHappening + "|" + name + " (lat="
				+ latitude + " long=" + longitude + ")|"
				+ lastKnownLocation.getLatitude() + "|"
				+ lastKnownLocation.getLongitude() + "|" + sformattedDx, 0);
	}

	private Boolean wereDoingVoiceNotifications() {
		Boolean dodah= getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
				Preferences.MODE_PRIVATE).getBoolean(
						"VoiceNotifications", true);
		return dodah;
	}
	private Boolean wereDoingPopupNotifications() {
		Boolean dodah= getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
				Preferences.MODE_PRIVATE).getBoolean(
						"PopupNotifications", false);
		return dodah;
	}
	
	private void notifyUser(String name, boolean isEntering, int id,
			double latitude, double longitude, Boolean isContactNotification) {
		getLogger().log2("INeedLocationServices:notifyUser");	

		if (mNotificationManager == null) {
			mNotificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if (isEntering) {
			/*bbhbb1  Toast.makeText(getApplicationContext(), "1 fetchingNeedsForALocation", Toast.LENGTH_SHORT).show(); */
			
			Cursor curses = getDbAdapter().fetchNeedsForALocation(id);
			int cnt=curses.getCount();
/*bbhbb2*/			logNotification(true, (long) id, latitude, longitude, name);
			if (cnt > 0) {
				/*bbhbb1  Toast.makeText(getApplicationContext(), "2 notifyUser-gotone", Toast.LENGTH_SHORT).show();*/
				
				// Intent contentIntent = new Intent(Intent.ACTION_MAIN);
				// contentIntent.setClass(this, NeedsNotification.class);
/////////bbbhhhbbb				getLogger().log("The id="+String.valueOf(id),999);
				Intent contentIntent = new Intent(this, NeedsNotification.class)
				.putExtra("id", id).putExtra("locationname", name);
				/*bbhbb1 Toast.makeText(getApplicationContext(), "3 notifyUser-got contentIntent", Toast.LENGTH_SHORT).show();*/
				// contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				// Intent.FLAG_ACTIVITY_NEW_TASK);

				Notification nb = new Notification(com.mibr.android.intelligentreminder.R.drawable.status_bar1,
						name, System.currentTimeMillis());
				// nb.sound=Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI,
				// "6");
				if (getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getBoolean(
								"NotificationUseVibrate", true)) {
					nb.defaults |= Notification.DEFAULT_VIBRATE;
				}
				if (getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getBoolean(
								"NotificationUseSound", true)&& !(wereDoingVoiceNotifications() && !isContactNotification)) {
					nb.defaults |= Notification.DEFAULT_SOUND;
				}
				/*bbhbb1     Toast.makeText(getApplicationContext(), "4 notifyUser-got past gettingSharedPreferences", Toast.LENGTH_SHORT).show();*/
				/* NO LONGER NEEDED
				if(isContactNotification) {
					String str="Intelligent Reminder contact alert. Please open notification window for details.";
					
					Toast.makeText(INeedLocationService.this, str,
									Toast.LENGTH_LONG).show();
				}
				*/
//					nb.sound=Uri.parse("file:///sdcard/Playlists/working.m4a");
	//				nb.sound=Uri.parse("file:///sdcard/vRecord/RecordedSounds/2011Jan18 05~22~04.3gpp");
				nb.defaults|=Notification.DEFAULT_LIGHTS;
//				nb.ledARGB=0xff00ff00;
	//			nb.ledOnMS=300;
		//		nb.ledOffMS=1000;
				nb.flags|=Notification.FLAG_SHOW_LIGHTS;
	//			}
				String phoneid=mPhoneId;
				String lesNeeds="";
				String lesNeedsPopup="";
				String lesNeedsPopupDescription="";
				String stars="";
				String slashN="";
				String lesNeedsSayIt="You have the following needs at location "+name+".. ";
				String pause="";
				int nbrORows=curses.getCount();
				int nbrORowsCounter=1;
				if(nbrORows>1) {
					pause="1. ";
				}
				while (curses.moveToNext()) {
					getDbAdapter().createAlertHistory(phoneid, (long)id, Long.valueOf(curses.getString(curses.getColumnIndex("_id"))));
					lesNeeds+=stars+curses.getString(curses.getColumnIndex("Name"));
					lesNeedsPopup+=stars+curses.getString(curses.getColumnIndex("Name"));
					lesNeedsSayIt+=pause+curses.getString(curses.getColumnIndex("Name"))+". ";
					String desc=curses.getString(curses.getColumnIndex("Description"));
					if(desc != null && desc.trim().length()>0) {
						lesNeedsPopupDescription+=slashN+desc;
						slashN="\n";
					}
					stars="\n ** ";
					nbrORowsCounter++;
					
					pause=String.valueOf(nbrORowsCounter)+". ";
				}
				/*bbhbb1					        Toast.makeText(getApplicationContext(), "notifyUser-got past getting lesNeeds n desc", Toast.LENGTH_LONG).show();*/
				/*bbhbb1					        Toast.makeText(getApplicationContext(), "lesNeeds: "+(lesNeeds==null?"null":lesNeeds), Toast.LENGTH_LONG).show();*/
				PendingIntent pendingIntent = PendingIntent.getActivity(this,
					(int)System.currentTimeMillis(), contentIntent, 0);
				nb
				.setLatestEventInfo(
						getApplicationContext(),
						name + " needs:",
						lesNeeds,
						pendingIntent);
				// nb.contentIntent=pendingIntent;
				mNotificationManager.notify(id, nb);
				
				
				
				if(!isContactNotification) {
					if(wereDoingVoiceNotifications() || wereDoingPopupNotifications() ) {
						sayIt(lesNeedsSayIt,lesNeedsPopup,lesNeedsPopupDescription,name);
					}
				} else {
					/* Doing Intent
					Intent jdintent=new Intent("android.intent.action.MAIN")
					.putExtra("title", name + " Needs")
					.putExtra("message", lesNeedsPopup)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					jdintent.setClass(getApplicationContext(),NotificationDialog.class);
					getApplicationContext().startActivity(jdintent);
					*/
								
					
					/* playing window 1 
										
				    mView = new MyLoadView(this,lesNeedsPopup);

				    mParams = new WindowManager.LayoutParams(
				            WindowManager.LayoutParams.MATCH_PARENT, 150, 10, 10,
				            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				            PixelFormat.TRANSLUCENT);

				    mParams.gravity = Gravity.CENTER;
				    mParams.setTitle(name + "Needs:");

				    mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
				    mWindowManager.addView(mView, mParams);					
					
					*/
					/* doing view  BUT IT DOESN'T HAVE A BUTTON 
					LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					notificationPopup = vi.inflate(R.layout.notificationpopup, null);

					// fill in any details dynamically here
					TextView needDescription = (TextView) notificationPopup.findViewById(R.id.needDescription);
					needDescription.setText(lesNeedsPopup);
					TextView needName = (TextView) notificationPopup.findViewById(R.id.needName);
					needName.setText(name);
					Button closeMe=(Button)notificationPopup.findViewById(R.id.closeMe);
					closeMe.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							INeedLocationService.mWindowManager.removeView(INeedLocationService.this.notificationPopup);
						}
					});
				    mParams = new WindowManager.LayoutParams(
				            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

				    mParams.gravity = Gravity.CENTER;
//				    mParams.setTitle(name + "Needs:");

				    mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
				    try {
				    	mWindowManager.removeView(needDescription);
				    } catch (Exception ee) {}
				    mWindowManager.addView(needDescription, 
				    		new WindowManager.LayoutParams(
				    				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 10, 10,
				    				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,0,PixelFormat.OPAQUE));
					*/
					/* doing view */ 
					LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					notificationPopup = (LinearLayout)vi.inflate(R.layout.notificationpopup, null);
					
					TextView needDescription = (TextView) notificationPopup.findViewById(R.id.needDescription);
					needDescription.setText(lesNeedsPopupDescription);
					TextView needName = (TextView) notificationPopup.findViewById(R.id.needName);
					needName.setText(lesNeedsPopup);
					TextView needForName = (TextView) notificationPopup.findViewById(R.id.textViewJDNeedFor);
					needForName.setText(name);
					Button closeMe=(Button)notificationPopup.findViewById(R.id.closeMe);
					closeMe.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							INeedLocationService.mWindowManager.removeView(INeedLocationService.this.notificationPopup);
						}
					});
				    mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
				    try {
				    	mWindowManager.removeView(notificationPopup);
				    } catch (Exception ee) {}
				    WindowManager.LayoutParams lp=new WindowManager.LayoutParams(
		    				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 10, 10,
		    				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//		    				WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
//	    					WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,0
		    				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
		    					| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.OPAQUE);
///				    lp.token=notificationPopup.getWindowToken();
				    mWindowManager.addView(notificationPopup, lp);
								    
				    
				    /*
					final Timer jdTimer = new Timer("Registering");
					jdTimer.schedule(new TimerTask() {
						public void run() {
							INeedLocationService.mWindowManager.removeView(INeedLocationService.this.notificationPopup);
							jdTimer.cancel();
						}
					}, 5000, 1000 * 60 * 10);
					*/
				    /*
				    Handler handler = new Handler();
				    handler.postDelayed(new Runnable() {
				        @Override
				        public void run() {
							INeedLocationService.mWindowManager.removeView(INeedLocationService.this.notificationPopup);
				        }
				    }, 5000);
				    */
				}
			}
			curses.close();
		} else {
			logNotification(false, (long) id, latitude, longitude, name);
			mNotificationManager.cancel(id);
		}
	}
	
	public class ProximityManager {
		LocationManager mLocationManager = null;
		private float dxToNotify = 100f;

		public float getDxToNotify() {
			return dxToNotify;
		}

		public INeedTimerServices getmINeedTimerServices() {
			if(INeedTimerServices.mSingleton==null) {
				
				
				getLogger().log("INeedLocationsServices : starting INeedTimerServices 1");
				Intent jdIntent=new Intent(INeedLocationService.this, INeedTimerServices.class).
						putExtra("logFilter",_logFilter);
				
				
				getApplicationContext().startService(jdIntent);
			}
			return INeedTimerServices.mSingleton;
		}
		public String getProvider() {
			if(USING_LOCATION_SERVICES) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				return mLocationManager.getBestProvider(criteria, false);
			} else {
				return "gps";
			}
		}

		private Location getLastKnownLocation() {
			if(USING_LOCATION_SERVICES) {
				if (mLocationManager == null) {
					mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
				}
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String bestProvider = mLocationManager.getBestProvider(
						criteria, false);
				return mLocationManager.getLastKnownLocation(bestProvider);
			} else {
				getLogger().log("INeedLocationsServices : starting INeedTimerServices a");
				
				return getmINeedTimerServices().getLastKnownLocation();
			}
		}

		public void setDxToNotify(float value) {
			dxToNotify = value;
		}

		public void unRegisterAllProximityNotifications() {
			Iterator<Proximity> mi = mProximities.values().iterator();
			while (mi.hasNext()) {
				mi.next().manage(null, false);
			}
		}

		public void manageProximity(int id, String name, double latitude,
				double longitude, boolean isStart, int notedx) {
			if(USING_LOCATION_SERVICES) {
				if (mLocationManager == null) {
					mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
				}
			}
			Proximity existingProximity = (Proximity) mProximities.get(id);
			if (existingProximity == null) {
				existingProximity = new Proximity(id, name, latitude, longitude, notedx);
				mProximities.put(id, existingProximity);
			}
			existingProximity.manage(new Proximity(id, name, latitude,
					longitude, Integer.valueOf((int) (dxToNotify==0?200:dxToNotify))), isStart);
		}

		private Hashtable mProximities = new Hashtable();

		public class Proximity {
			private int mLocationId;
			private double mLatitude;
			private double mLongitude;
			private String mName;
			private int mNoteDx;
			private GregorianCalendar mLastSentNotification;
			private boolean mIsReceivingNotifications = false;

			private String getName() {
				return mName;
			}
			public int getmLocationId() {
				return mLocationId;
			}
			public int getmNoteDx() {
				return mNoteDx;
			}
			public double getmLatitude() {
				return mLatitude;
			}
			public double getmLongitude() {
				return mLongitude;
			}
			private String getIntentString() {
				return IMAT_PROXIMITY_ALERT + String.valueOf(mLocationId);
			}

			private PendingIntent getPendingIntent() {
				Intent intent = new Intent(getIntentString()).putExtra("name",
						mName).putExtra("id", mLocationId);
				return PendingIntent.getBroadcast(getApplicationContext(), 0,
						intent, 0);
			}

			public Proximity(int id, String name, double latitude,
					double longitude, int notedx) {
				mLastSentNotification=new GregorianCalendar();
				mLastSentNotification.add(GregorianCalendar.MINUTE, -1);
				mLocationId = id;
				mName = name;
				mLatitude = latitude;
				mLongitude = longitude;
				mNoteDx=notedx;
			}

			private void startNotifying() {
				try {
					if(USING_LOCATION_SERVICES) {
						ProximityManager.this.mLocationManager.addProximityAlert(
								mLatitude, mLongitude, (float)mNoteDx,
								MILLISECONDS_FOR_HOLDING_LOCATIONALERT,
								getPendingIntent());
						android.content.IntentFilter intentFilter = new android.content.IntentFilter(
								getIntentString());
						registerReceiver(new ProximityIntentReceiver(), intentFilter);
					} else {
						getLogger().log("INeedLocationsServices : starting INeedTimerServices b");

						ProximityManager.this.getmINeedTimerServices().registerReceiver(this,new ProximityIntentReceiver());
					}
					mIsReceivingNotifications = true;
				} catch (Exception e) {
					int i=3;
				}
			}

			private void stopNotifying() {
				try {
				if(USING_LOCATION_SERVICES) {
					ProximityManager.this.mLocationManager
					.removeProximityAlert(getPendingIntent());
				} else {
					getLogger().log("INeedLocationsServices : starting INeedTimerServices c");

					ProximityManager.this.getmINeedTimerServices().unRegisterReceiver(this);
				}
				mIsReceivingNotifications = false;
				} catch (Exception e) {
				
				}
			}

			public void manage(Proximity newProximity, boolean wantsToStart) {
				if (!wantsToStart) {
					if (!mIsReceivingNotifications) {
						return; // nothing to do
					} else {
						stopNotifying();
					}
				} else {
					if (!mIsReceivingNotifications) {
						mLatitude = newProximity.mLatitude;
						mLongitude = newProximity.mLongitude;
						mName = newProximity.mName;
						startNotifying();
					} else {
						if(mLatitude!=newProximity.mLatitude || mLongitude != newProximity.mLongitude) {
							stopNotifying();
							mLatitude = newProximity.mLatitude;
							mLongitude = newProximity.mLongitude;
							mName = newProximity.mName;
							startNotifying();
						}
					}
				}
			}

			public class ProximityIntentReceiver extends BroadcastReceiver {
				boolean _imDisabled=false;
				public void entering() {
					getLogger().log2("INeedLocationServices : entering");
					GregorianCalendar now = new GregorianCalendar();
					GregorianCalendar prior = (GregorianCalendar) Proximity.this.mLastSentNotification
					.clone();
					prior.add(GregorianCalendar.MINUTE, 1);
					if (prior.compareTo(now) < 0) {
						INeedLocationService.this.notifyUser(
								Proximity.this.mName, true,
								Proximity.this.mLocationId,
								Proximity.this.mLatitude,
								Proximity.this.mLongitude,false);
					}
					Proximity.this.mLastSentNotification = now;				
				}
				public void leaving() {
					INeedLocationService.this.notifyUser(
							Proximity.this.mName, false,
							Proximity.this.mLocationId,
							Proximity.this.mLatitude,
							Proximity.this.mLongitude,false);				
				}
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					if(!_imDisabled) {
						try {
							if (arg1.getBooleanExtra(
									LocationManager.KEY_PROXIMITY_ENTERING, false)) {
								GregorianCalendar now = new GregorianCalendar();
								GregorianCalendar prior = (GregorianCalendar) Proximity.this.mLastSentNotification
								.clone();
								prior.add(GregorianCalendar.MINUTE, 1);
								if (prior.compareTo(now) < 0) {
									INeedLocationService.this.notifyUser(
											Proximity.this.mName, true,
											Proximity.this.mLocationId,
											Proximity.this.mLatitude,
											Proximity.this.mLongitude,false);
								}
								Proximity.this.mLastSentNotification = now;
							} else {
								INeedLocationService.this.notifyUser(
										Proximity.this.mName, false,
										Proximity.this.mLocationId,
										Proximity.this.mLatitude,
										Proximity.this.mLongitude,false);
							}
						} catch (Exception ei) {
							CustomExceptionHandler.logException(ei, null);
							/*							INeedToo.mSingleton.log(ei.getMessage(),4);
							StackTraceElement[] stea=ei.getStackTrace();
							for(int c=0;c<stea.length;c++) {
								StackTraceElement ste=stea[c];
								String str=ste.toString();
								INeedToo.mSingleton.log(str,4);
							}
							INeedToo.mSingleton.transmitLog();
							 */
							_imDisabled=true;
						}
					}
				}
			}
		}
	}

	private ProximityManager getmProximityManager() {
		if(msProximityManager==null) {
			msProximityManager=new ProximityManager();
		}
		return msProximityManager;
	}
	public static int STARTING = 0;
	private ProximityManager msProximityManager = null;
	private INeedDbAdapter mDbAdapter = null;

	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean imAlive() {
		return true;
	}

	private Logger mLogger = null;
	private int _logFilter=3;

	private Logger getLogger() {
		if (mLogger == null) {
			mLogger = new Logger(_logFilter,"LocationService",this);
		}
		return mLogger;
	}
	
	public void setLogFilter(int tothis) {
		_logFilter=tothis;
	}
	
	private  Timer mVoiceHelperTimer=null;
	private  Timer getVoiceHelperTimer() {
		if (mVoiceHelperTimer == null) {
			mVoiceHelperTimer = new Timer(
			"VoiceHelperTimer");
		}
		return mVoiceHelperTimer;
	}
	
	private  Timer mVoiceHelperTimer2=null;
	private  Timer getVoiceHelperTimer2() {
		if (mVoiceHelperTimer2 == null) {
			mVoiceHelperTimer2 = new Timer(
			"VoiceHelperTimer2");
		}
		return mVoiceHelperTimer2;
	}
		
	private void sayIt(String it, String lesNeedsPopup,String lesNeedsPopupDescription,String locationName) {
		final String jdSomething=it;
			Intent jdIntent=new Intent(this, VoiceHelper.class)
			.putExtra("voicedata",it)
			.putExtra("lesNeedsPopup",lesNeedsPopup)
			.putExtra("lesNeedsPopupDescription",lesNeedsPopupDescription)
			.putExtra("laLocationName",locationName);
			jdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(jdIntent);		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mSingleton=this;

		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer());
		try {
			if (intent.getAction()!=null&&intent.getAction().equals("NewLogFilter")) {
				_logFilter=intent.getIntExtra("NewLogFilter",3);
				return;
			}

		} catch (Exception eee3) {}
		try {
			mPhoneId=intent.getStringExtra("PhoneId");
			_logFilter=intent.getIntExtra("logFilter",2);
		} catch (Exception eee3) {}
		/*bbhbb1					        Toast.makeText(getApplicationContext(), "mPhoneId: "+(mPhoneId==null?"null":mPhoneId), Toast.LENGTH_LONG).show();*/
		mLogger=null;
		getmProximityManager().setDxToNotify(Integer.valueOf(this
				.getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getString("LocationDistance",
						"500")));
		// 1=just contacts 2=justlocations 3=bothll
		Cursor curses = getDbAdapter().fetchAllLocationsPrimitive(2);
		if (curses.getCount() > 0) {
			curses.moveToFirst();
			do {
				try {
				String latitude = curses.getString(curses
						.getColumnIndex(INeedDbAdapter.KEY_LATITUDE));
				String longitude = curses.getString(curses
						.getColumnIndex(INeedDbAdapter.KEY_LONGITUDE));
				getLogger().log2("INeedLocationServices: managing:" + longitude);
				int key = curses.getInt(curses
						.getColumnIndex(INeedDbAdapter.KEY_ROWID));
				if (INeedToo.isNothingNot(latitude)
						&& INeedToo.isNothingNot(longitude)) {
					String name = curses.getString(curses
							.getColumnIndex(INeedDbAdapter.KEY_NAME));
					Double dlatitude = Double.parseDouble(latitude);
					Double dlongitude = Double.parseDouble(longitude);
					int notedx=200;
					try {
						notedx=curses.getInt(curses.getColumnIndex("notificationdx"));
					} catch (Exception eie) {}
					getmProximityManager().manageProximity(key, name, dlatitude,
							dlongitude, true, notedx);
				} else {
					getmProximityManager().manageProximity(key, null, -1d, -1d,
							false,200); // maybe the guy blanked out the GPS
					// coordinates
				}
				//Log.d(TAG, "INeedLocationServices:Managing:" + latitude);	

				} catch (Exception eieib) {
					try {
						getLogger().log(eieib.getMessage(), 3);
					} catch (Exception e333) {}
				}
			} while (curses.moveToNext());
		}
		curses.close();
		
		getLogger().log("INeedLocationsServices : starting INeedTimerServices 2");
		
		Intent jdIntent2=new Intent(this, INeedTimerServices.class).
				putExtra("logFilter",_logFilter);		
		getApplicationContext().startService(jdIntent2);
		
		doBindService();
	}

	private void unRegisterAllNotificationRequests() {
		getmProximityManager().unRegisterAllProximityNotifications();
	}

	@Override
	public void onDestroy() {
		unRegisterAllNotificationRequests();
		doUnbindService();
		if (mDbAdapter != null) {
			mDbAdapter.close();
		}
		mLogger=null;
				
		super.onDestroy();
		
		/* playing window 1
	    ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
	    mView = null;		
	    */
	}
	
	
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_INCOMING_CALL = 3;
	static final int MSG_OUTGOING_CALL = 4;
    static final int MSG_GETCONTACTS =5;
    static final int MSG_HERESCONTACTS=6;

	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound=false;

	/** Some text view we are using to show state information. */

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INCOMING_CALL:
				/*bbhbb1  Toast.makeText(getApplicationContext(), "0 About to doNotificationForContactId MSG_INCOMING_CAL", Toast.LENGTH_SHORT).show();*/
				
			doNotificationForContactId(msg.arg1);
/*				
				Bundle bundle=msg.getData();
				Uri uri=bundle.getParcelable("uri");
				String str="Incoming call: " + bundle.getString("bb") + String.valueOf(msg.arg1) + " uri: " + uri.toString();
				
				Toast.makeText(INeedLocationService.this, str,
								Toast.LENGTH_SHORT).show();
*/
				break;
			case MSG_OUTGOING_CALL:
				doNotificationForContactId(msg.arg1);
				
				
/*				
				Bundle bundle2=msg.getData();
				Uri uri2=bundle2.getParcelable("uri");
				String str2="Incoming call: " + bundle2.getString("bb") + String.valueOf(msg.arg1) + " uri: " + uri2.toString();
				Toast.makeText(INeedLocationService.this, str2,
								Toast.LENGTH_SHORT).show();
*/
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

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

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

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
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
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
			}
		} catch (Exception eee) {
			int bkhere = 3;
			int bkthere = bkhere;

		}
	}
	void doNotificationForContactId(int contactId) {
		// 1=just contacts 2=justlocations 3=bothll
		Cursor curses = getDbAdapter().fetchAllLocationsPrimitive(1);
		if (curses.getCount() > 0) {
			curses.moveToFirst();
			do {
				try {
					int jdContactID=curses.getInt(curses.getColumnIndex("contactid"));
					String jn=curses.getString(curses.getColumnIndex("name"));
					if(jdContactID==contactId) {
						int key = curses.getInt(curses
								.getColumnIndex(INeedDbAdapter.KEY_ROWID));
						String name=curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_NAME));
						
						/*bbhbb1   Toast.makeText(getApplicationContext(), "0.5 About notifyUser", Toast.LENGTH_SHORT).show();*/
						
						notifyUser(name,true,key,-1d,-1d,true);
						
						
						
					}
				} catch (Exception eieib) {
					try {
						int a=1;
						
						if(a==2) {
							getLogger().log(eieib.getMessage(), 3);
						}
					} catch (Exception e333) {}
				}
			} while (curses.moveToNext());
		}
		curses.close();
		
	}
	public class MyLoadView extends View {

	    private Paint mPaint;
	    private String _contentOfWindow;

	    public MyLoadView(Context context, String contentOfWindow) {
	        super(context);
	        mPaint = new Paint();
	        mPaint.setTextSize(50);
	        mPaint.setARGB(200, 200, 200, 200);
	        _contentOfWindow=contentOfWindow;
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        canvas.drawText(_contentOfWindow, 0, 100, mPaint);
	    }

	    @Override
	    protected void onAttachedToWindow() {
	        super.onAttachedToWindow();
	    }

	    @Override
	    protected void onDetachedFromWindow() {
	        super.onDetachedFromWindow();
	    }

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    }
	}
}

package com.mibr.android.intelligentreminder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class INeedTimerServices extends Service {
	private static long MTIMEOALARMINTENSECONDINTERVALS=12;
	private static Timer mLocationsTimer = null;
	private static Timer mLocationsTimer2=null;
	public static INeedTimerServices mSingleton=null;
	private Location mLastKnownLocation=null;
	private LocationManager mLocationManager=null;
	private String mBestProvider = null; 
	private Hashtable mRegistrations=new Hashtable();
	public static Boolean USINGSYSTEMALARM=true;
	final static String TAG = "timeserv";

	
	private class PandP {
		public INeedLocationService.ProximityManager.Proximity mProximity;
		public INeedLocationService.ProximityManager.Proximity.ProximityIntentReceiver mProximityReceiver; 
		public boolean isIn=false;
		public double dxToCheck;
	}
	public synchronized void registerReceiver(INeedLocationService.ProximityManager.Proximity proximity,INeedLocationService.ProximityManager.Proximity.ProximityIntentReceiver proximityReceiver) {
		getLogger().log2("INeedTimerServices: registerReceiver:" + Double.toString(proximity.getmLongitude()));
		PandP pnp=new PandP();
		pnp.mProximity=proximity;
		pnp.mProximityReceiver=proximityReceiver;
		pnp.dxToCheck=proximity.getmNoteDx();
		int locationId=proximity.getmLocationId();
		mRegistrations.put(locationId, pnp);
	}
	public synchronized void unRegisterReceiver(INeedLocationService.ProximityManager.Proximity proximity) {
		try {
			mRegistrations.remove(proximity.getmLocationId());
		} catch (Exception e) {

		}

	}
	public void setLogFilter(int tothis) {
		_logFilter=tothis;
	}
	
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}
	private String getBestProvider() {
		if(mBestProvider==null) {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			mBestProvider = getLocationManager().getBestProvider(criteria, false);
		}
		return mBestProvider;
	}
	public Location getLastKnownLocation() {
		if(mLastKnownLocation==null) {
			mLastKnownLocation= getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);			
		}
		return mLastKnownLocation;
	}
	private Timer getLocationsTimer() {
		if (mLocationsTimer == null) {
			mLocationsTimer = new Timer(
			"WebProcessingLocationsActivities");
		}
		return mLocationsTimer;
	}
	private synchronized void manageLocationNotifications(Location newLocation) {
		//afaire:
		/*
		 * iterate through the registrations.
		 * 		if leaving, then send a leaving message to associated ProximityReceiver.
		 * 		if entering, then send an entering message to associated ProximityReceiver
		 */
		float[] results=new float[3];
		Iterator iterator=mRegistrations.values().iterator();
		while(iterator.hasNext()){
			PandP pnp=(PandP)iterator.next();
			getLogger().log2("manageLocationNotifications for longitude: "+Double.toString(pnp.mProximity.getmLongitude()));
			Location.distanceBetween(pnp.mProximity.getmLatitude(), pnp.mProximity.getmLongitude(), newLocation.getLatitude(), newLocation.getLongitude(), results);
			if(results[0]<=(float)pnp.mProximity.getmNoteDx()) {
				if(!pnp.isIn) {
					pnp.mProximityReceiver.entering();
				}
				pnp.isIn=true;
			} else {
				if(pnp.isIn) {
					pnp.mProximityReceiver.leaving();
				}
				pnp.isIn=false;
			}
		}
	}
	public int _jdFY=0;
	public static int _nbrO_30second_chuncks=1;
	public static int _nbrO_60second_chuncks=1;

	private void doS() {
		try {
			getLogger().log("INeedTimerServices : just entered doS()");

			Location jdlocation=getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
			//			manageLocationNotifications(jdlocation);
			//			mLastKnownLocation= jdlocation;
			if(_jdFY==0) {
						if(jdlocation!=null) {
							try {
								getLogger().log(
									"doS ing: " + jdlocation.toString(), 1);
								getLogger().log2("INeedTimerServices : jdoS ing:"+ jdlocation.toString());
							} catch (Exception e) {}
						}
//						INeedToo.mSingleton.log(
//								"doS ing: ", 1);
				_jdFY++;
				long jdInterval=12;
				try {
					jdInterval=getAlarmInTenSecondIntervals();
				} catch (Exception eee) {}
				getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*9*jdInterval, 200, new LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						try {
//							if(location!=null) {
//							INeedToo.mSingleton.log("Type 2b: "+
//									location.toString(), 1);
//							}
							if(location.hasAccuracy()==false || location.getAccuracy()<412) {
								manageLocationNotifications(location);
								getLocationManager().removeUpdates(this); // added 4.29.2011
//4/29/2011								LocationManager locationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
//4/29/2011								locationManager.removeUpdates(this);
								try {
									if(
											(location.hasSpeed() && location.getSpeed()>2f)
									 || (
											mLastKnownLocation != null && location.distanceTo(mLastKnownLocation)> 100f
									)) {
										modifyAlarmMinutes(false);
									} else {
										if(location.getSpeed()<1f) {
											modifyAlarmMinutes(true);
										}
									}
								} catch (Exception ee33dd3) {}
								mLastKnownLocation= location;
							}
							if(_jdFY>0) {
								_jdFY--;
							}
						} catch (Exception ee) {
							try {
								getLogger().log(ee.getMessage(), 1);
							} catch (Exception e) {}
						}
					}
					@Override
					public void onProviderDisabled(String provider) {
						try {
							getLogger().log("Provider " + provider+ " disabled.", 1);
						} catch (Exception e) {}
					}
					@Override
					public void onProviderEnabled(String provider) {
						try {
							getLogger().log("Provider " + provider+ " enabled.", 1);
						} catch (Exception e) {}
					}
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
						//INeedToo.mSingleton.log("Provider " + provider+ " status changed to "+ String.valueOf(status)+".", 1);
					}
				},Looper.getMainLooper());

			}
		} catch (Exception e) {
			try {
				getLogger().log(
						e.getMessage(), 3);
			} catch (Exception e2) {
				
			}
		}
	}
	private void startMyLocationsTimer() {
		getLogger().log("INeedTimerServices: startMyLocationsTimer");	

		getLocationsTimer().schedule(new TimerTask() {
			public void run() {
				try {
					getLogger().log("INeedTimerServices:TimerPopped");	
					doS();
				} catch (Exception ee) {
					
				}
			}
		}, 1000 * 30 * 1, 1000 * 60 *  _nbrO_60second_chuncks);
	}
	private void stopMyLocationsTimer() {
		getLogger().log("INeedTimerServices: stopMyLocationsTimer");	
		if (mLocationsTimer != null) {
			try {
				mLocationsTimer.cancel();
				mLocationsTimer.purge();
				mLocationsTimer = null;
			} catch (Exception e) {
			}
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mSingleton=this;
		getLogger().log("INeedTimerServices: here1");	
				
		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer());
mLogger=null;
		_logFilter=2;
		try {
			if (intent.getAction()!=null&&intent.getAction().equals("NewLogFilter")) {
				_logFilter=intent.getIntExtra("NewLogFilter",2);
				return;
			}
		} catch (Exception eee3) {}
		try {
			_logFilter=intent.getIntExtra("logFilter",2);
		} catch (Exception eee3) {}
		
		getLogger().log("INeedTimerServices: here2");	
		
		if(intent != null) {
			try {
				if(intent.getBooleanExtra("WAKEUP!", false)) {
					/*The question is: is this sufficient to re-engage my timers?*/
					doS();
					return;
				}
			} catch (Exception eee) {}
		} 

		if(!USINGSYSTEMALARM ) {
			if(mLocationsTimer==null) {
				getLogger().log("INeedTimerServices: here3a");	
				startMyLocationsTimer();
			} else {
				getLogger().log("INeedTimerServices: here3b");	
				stopMyLocationsTimer();
				startMyLocationsTimer();
			}
		} else {
			getLogger().log("INeedTimerServices: here4");	
			if(mLocationsTimer2==null) {
				try {
					getLogger().log("INeedTimerServices: here4a");	
					getmAlarmSender();
				} catch (Exception ee3) {
					try {
						log(ee3.getMessage());
						StackTraceElement[] stea=ee3.getStackTrace();
						for(int c=0;c<stea.length;c++) {
							StackTraceElement ste=stea[c];
							String str=ste.toString();
							INeedTimerServices.log(str);
						}
						INeedToo.mSingleton.transmitLog(true);
					} catch (Exception ee) {}
					
				}
			}
		}
	}
	@Override
	public void onDestroy() {
		try {
			getLogger().log(
				"INeedTimerServices being destroyed", 1);
		} catch (Exception eee) {}
		if(!USINGSYSTEMALARM ) {
			stopMyLocationsTimer();
		} else {
			stopMyLocationsTimer2();
		}
		mSingleton=null;
		mLogger=null;
	}
	/*
	 * m
	 * 			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			return mLocationManager.getBestProvider(criteria, false);
				return mLocationManager.getLastKnownLocation(bestProvider);
			Location location= mLocationManager.getLastKnownLocation(bestProvider);

	 */
	public static boolean isSdPresent() {
		String sdState=android.os.Environment.getExternalStorageState();
		return sdState.equals(
				android.os.Environment.MEDIA_MOUNTED) ;
	}
	
	private static FileOutputStream getLogOutputStream() throws FileNotFoundException {
		FileOutputStream fileOutputStream_Log = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/mibr");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Log = new FileOutputStream("/sdcard/mibr/log.txt",
					true);
		}
		return fileOutputStream_Log;
	}
	
	public static void log(String string) {
		Date date = new Date();
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getLogOutputStream();
			if (fos!=null) {
				pw = new PrintWriter(fos); 
				pw.write(""
						+ (date.getYear() + 1900)
						+ "-"
						+ padString(true, 2, String
								.valueOf((date.getMonth() + 1)), '0')
								+ "-"
								+ padString(true, 2, String.valueOf(date.getDate()),
								'0')
								+ " "
								+ padString(true, 2, String.valueOf(date.getHours()),
								'0')
								+ ":"
								+ padString(true, 2, String.valueOf(date.getMinutes()),
								'0')
								+ ":"
								+ padString(true, 2, String.valueOf(date.getSeconds()),
								'0') + "|" + string + "\n");
			}
		} catch (FileNotFoundException e) {
			// afaire Auto-generated catch block
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
	private static String padString(boolean left, int size, String str,
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
	private static void resetmAlarmSender() {
		stopMyLocationsTimer2();
		getmAlarmSender();
	}
	
	private static void startMyLocationsTimer2(long trigger, long interval) {
		getLocationsTimer2().schedule(new TimerTask() {
			public void run() {
				try {
					mSingleton.getLogger().log("INeedTimerServices: locationstimer2 popped");	
					mSingleton.doS();

				} catch (Exception ee) {
					
				}
			}
		}, trigger, interval);
	}
	private static void stopMyLocationsTimer2() {
		if (mLocationsTimer2 != null) {
			try {
				mLocationsTimer2.cancel();
				mLocationsTimer2.purge();
			} catch (Exception e) {
			}
			mLocationsTimer2 = null;
		}
	}

	private static Timer getLocationsTimer2() {
		if (mLocationsTimer2 == null) {
			mLocationsTimer2 = new Timer(
			"LocationsActivities2");
		}
		return mLocationsTimer2;
	}

	
	
	private static void getmAlarmSender() {
		mSingleton.getLogger().log("INeedTimerServices: getAlarmSender");	
		int modifyingValue=1;
		try {
			modifyingValue = Integer.valueOf(mSingleton.getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("GPSFrequency", "1"));
		} catch (Exception e) {}
		mSingleton.getLogger().log("INeedTimerServices: getAlarmSender 2");	
		mSingleton.doS();
		mSingleton.getLogger().log("INeedTimerServices: getAlarmSender 3");	
		startMyLocationsTimer2(500,1000*5*MTIMEOALARMINTENSECONDINTERVALS*modifyingValue);
		
		// just for testing   startMyLocationsTimer2(2500,1000);
		
		mSingleton.getLogger().log("INeedTimerServices: getAlarmSender 4");	
	}
	public static long getAlarmInTenSecondIntervals() {

		int modifyingValue=1;
		try {
			if(MTIMEOALARMINTENSECONDINTERVALS<12) { 
				try {
					modifyingValue = Integer.valueOf(INeedToo.mSingleton.getSharedPreferences(
							INeedToo.PREFERENCES_LOCATION,
							Preferences.MODE_PRIVATE).getString("GPSFrequency", "1"));
				} catch (Exception e) {
				}
			}
		} catch (Exception eee3) {}

		
		return MTIMEOALARMINTENSECONDINTERVALS*modifyingValue;
	}
	public static void modifyAlarmMinutes(Boolean increase) {
		if(increase) {
			if(MTIMEOALARMINTENSECONDINTERVALS<18) {
				MTIMEOALARMINTENSECONDINTERVALS++;
				try {
					resetmAlarmSender();
					mSingleton.getLogger().log("Increasing alarm interval to "+String.valueOf(MTIMEOALARMINTENSECONDINTERVALS), 1);
				} catch (Exception ee33) {
					
				}
			}
		}
		if(!increase) {
			if(MTIMEOALARMINTENSECONDINTERVALS>2) {
				MTIMEOALARMINTENSECONDINTERVALS--;
				try {
					resetmAlarmSender();
					mSingleton.getLogger().log("Decreasing alarm interval to "+String.valueOf(MTIMEOALARMINTENSECONDINTERVALS), 1);
				} catch (Exception ee33) {
					
				}
			}
		}
	}
	

	private Logger mLogger = null;
	private int _logFilter=3;

	private Logger getLogger() {
		if (mLogger == null) {
			mLogger = new Logger(_logFilter,"TimerService",this);
		}
		return mLogger;
	}	
}

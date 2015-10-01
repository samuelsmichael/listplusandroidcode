package com.mibr.android.intelligentreminder;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

public class INeedTimerServicesII extends Service {
	private Timer mLocationsTimer = null;

	private Timer getLocationsTimer() {
		if (mLocationsTimer == null) {
			mLocationsTimer = new Timer(
			"WebProcessingLocationsActivities");
		}
		return mLocationsTimer;
		
	}
	public abstract class JDTimerTask extends TimerTask {
		public INeedTimerServicesII _mTimerServicesII=null;
		protected int _fS=0;
		public JDTimerTask(INeedTimerServicesII tii) {
			super();
			_mTimerServicesII=tii;
		}
	}
	private void startMyLocationsTimer() {
		getLocationsTimer().schedule(new JDTimerTask(this) {
			public void run() {

				try {
					LocationManager locationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
					Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					INeedToo.mSingleton.log("Type 1: "+
							location.toString(), 4);
					//					Looper.prepare();
					if(_fS==0) {
						_fS++;
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*10*1, 200, new LocationListener() {
							@Override
							public void onLocationChanged(Location location) {
								try {
								INeedToo.mSingleton.log("Type 2: "+
										location.toString(), 4);
								LocationManager locationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
								locationManager.removeUpdates(this);
								if(_fS>0) {
									_fS--;
								}
								} catch (Exception ee) {
									INeedToo.mSingleton.log(ee.getMessage(), 4);
								}
							}
							@Override
							public void onProviderDisabled(String provider) {
								INeedToo.mSingleton.log("Provider " + provider+ " disabled.", 4);
							}
							@Override
							public void onProviderEnabled(String provider) {
								INeedToo.mSingleton.log("Provider " + provider+ " enabled.", 4);
							}
							@Override
							public void onStatusChanged(String provider, int status, Bundle extras) {
								INeedToo.mSingleton.log("Provider " + provider+ " status changed to "+ String.valueOf(status)+".", 4);
							}
						},Looper.getMainLooper());
						//					Looper.loop();
					}
				} catch (Exception e) {
					INeedToo.mSingleton.log(e.getMessage(), 4);
				}
			}
		}, 1000 * 30 * 1, 1000 * 30 *  1);

	}
	private void stopMyLocationsTimer() {
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(mLocationsTimer==null) {
			startMyLocationsTimer();
		} else {
			stopMyLocationsTimer();
			startMyLocationsTimer();
		} 
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		stopMyLocationsTimer();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

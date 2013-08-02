package com.mibr.android.intelligentreminder;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
	public BootCompleteReceiver() {
	}

	final static String TAG = "bub";
	static Context _context=null;
	static int _logFilter;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		
		Boolean autoStartOnReboot=false;
		try {
			autoStartOnReboot = context.getSharedPreferences(
					INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getBoolean("AutoStartOnBoot",false);	
		} catch (Exception e) {
		}
		if(autoStartOnReboot) {
			_context=context;
			int logFilter=0;
			try {
				logFilter = Integer.valueOf(context.getSharedPreferences(
						INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));	
			} catch (Exception e) {
			}
			_logFilter=logFilter;
	    	Intent jdItent2=new Intent(context, INeedTimerServices.class);
			jdItent2.putExtra("logFilter",logFilter);
			context.startService(jdItent2);
			//Log.d(TAG,"BootCompleteReceiver: started INeedTimerServices");
			
			
			getLocationsTimer2().schedule(new TimerTask() {
				public void run() {
					try {
						if(INeedTimerServices.mSingleton!=null) {
							Intent jdItent=new Intent(_context,INeedLocationService.class);
							jdItent.putExtra("PhoneId",getPhoneId(_context));
							jdItent.putExtra("logFilter",_logFilter);
					    	_context.startService(jdItent);
							mLocationsTimer2.cancel();
							mLocationsTimer2.purge();
							
						}
	
						
					} catch (Exception ee) {
						
					}
				}
			}, 1000, 1000);		
			
			//Log.d(TAG,"BootCompleteReceiver: started INeedLocationService");
		}
	}	
	
	private  Timer mLocationsTimer2=null;
	private  Timer getLocationsTimer2() {
		if (mLocationsTimer2 == null) {
			mLocationsTimer2 = new Timer(
			"LocationsActivities2");
		}
		return mLocationsTimer2;
	}
	
	public String getPhoneId(Context context) {
		//Log.d(TAG,"BootCompleteReceiver: getPhoneId 1");

		String anotherID = android.provider.Settings.System.getString(
				context.getContentResolver(),
				android.provider.Settings.System.ANDROID_ID);
		//Log.d(TAG,"BootCompleteReceiver: getPhoneId 2");
		if (INeedToo.isNothingNot(anotherID)) {
			return anotherID.trim();
		}
		try {
			String number = this.getDeviceNumber(context);
			if (isNothingNot(number)) {
				return number.trim();
			}
			//Log.d(TAG,"BootCompleteReceiver: getPhoneId 3");
		} catch (Exception e33) {
			try {
				String id = this.getDeviceId(context);
				if (isNothingNot(id)) {
					return id.trim();
				}
			} catch (Exception e3d) {
			}
		}
		return "unknown";
		//return "20013fd8f4dbc118";
	}
	public boolean isNothing(Object obj) {
		if (obj == null) {
			return true;
		} else {
			if (obj.toString().trim().equals("")) {
				return true;
			}
		}
		return false;
	}	public  boolean isNothingNot(Object obj) {
		return !isNothing(obj);
	}
	private String getDeviceNumber(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	private String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

}

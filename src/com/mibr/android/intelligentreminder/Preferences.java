package com.mibr.android.intelligentreminder;


import com.mibr.android.intelligentreminder.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
	        setTitle(INeedToo.mSingleton.getHeading());
	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
	        
	        CheckBoxPreference cbp = (CheckBoxPreference)findPreference("Networking_Outbound_Enabled");
	        cbp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try {
						if(((Boolean)newValue)==false) {
							INeedToo.mSingleton.disableTransmitting();
						} else {
							INeedToo.mSingleton.enableTransmitting();
						}
					} catch (Exception ee) {}
					return true;
				}
			});
	        CheckBoxPreference cbp2 = (CheckBoxPreference)findPreference("Networking_Inbound_Enabled");
	        cbp2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try {
						if(((Boolean)newValue)==false) {
							INeedToo.mSingleton.disableReceiving();
						} else {
							INeedToo.mSingleton.enableReceiving();
						}
					} catch (Exception eee) {}
					return true;
				}
			});
	        
	        ListPreference lp1=(ListPreference)findPreference("LoggingLevel"); 
	        lp1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					INeedToo.mSingleton.communicateNewValueToServices("LoggingLevel",newValue);						
					return true;
				}
	        });
	        
	        /*
	        private boolean isMyServiceRunning() {
	            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	                if (MyService.class.getName().equals(service.service.getClassName())) {
	                    return true;
	                }
	            }
	            return false;
	        }	        
	        */
	        
        } catch (Exception eee) {}
    }
}

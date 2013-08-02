package com.mibr.android.intelligentreminder;

import android.location.Location;
import android.location.Location;
import android.location.LocationManager;

public class IRThingLocation extends IRThingAbstractLocation {
	String _latitude;
	String _longitude;
	String _address;
	public IRThingLocation(long id, String name, String phoneId, String latitude, String longitude, String address) {
		super(id, name, phoneId);
		_address=address;
		_longitude=longitude;
		_latitude=latitude;
	}
	@Override
	public Boolean isCompany() {
		return false;
	}
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public String getAddress() {
		return _address;
	}
	public String getLongitude() {
		return _longitude;
	}
	public String getLatitude() {
		return _latitude;
	}
}

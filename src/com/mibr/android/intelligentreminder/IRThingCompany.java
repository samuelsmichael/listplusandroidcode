package com.mibr.android.intelligentreminder;

import java.util.ArrayList;

import android.location.Location;
import android.location.LocationManager;

public class IRThingCompany extends IRThingAbstractLocation {
	private String _closestLatitude=null;
	private String _closestLongitude=null;
	ArrayList<IRThingLocation> _locations = new ArrayList<IRThingLocation>();

	@Override
	public String getAddress() {
		return "";
	}
	@Override
	public String getName() {
		return super.getName();
	}
	@Override
	public Boolean isCompany() {
		return true;
	}

	public IRThingCompany(long id, String name, String phoneId) {
		super(id, name, phoneId);
	}
	public void addLocation(IRThingLocation location) {
		_locations.add(location);
	}
	@Override 
	public String getLatitude() {
		if(_closestLatitude==null) {
			deriveLatNLon();
		}
		return _closestLatitude;
	}
	@Override 
	public String getLongitude() {
		if(_closestLongitude==null) {
			deriveLatNLon();
		}
		return _closestLongitude;
	}
	public void deriveLatNLon() { 
		float currentLeast=99999999f;
		for(int i=0;i<_locations.size();i++) {
			IRThingLocation loc=_locations.get(i);
			float dx=loc.getDXFromLocation();
			if(dx<currentLeast) {
				currentLeast=dx;
				_closestLatitude=loc.getLatitude();
				_closestLongitude=loc.getLongitude();
			}
		}
	}

}

package com.mibr.android.intelligentreminder;

import android.location.Location;
import android.location.LocationManager;

public abstract class IRThingAbstractLocation extends IRThing implements Comparable<IRThingAbstractLocation> {
	protected Location _locationBeingComparedDXWise=null;
	public static int CurrentSort=IRThingNeed.SORT_NAME;
    public abstract String getLatitude();
    public abstract String getLongitude();
	public IRThingAbstractLocation(long id, String name, String phoneId) {
		super(id, name, phoneId);
	}
	public void setLocationBeingComparedDXWise(Location loc) {
		_locationBeingComparedDXWise=loc;
	}
	public abstract String getAddress(); 
	public abstract Boolean isCompany();
	public float getDXFromLocation() {
		try {
			Location meloc=new Location(LocationManager.GPS_PROVIDER);
			meloc.setLatitude(Double.valueOf(getLatitude()));
			meloc.setLongitude(Double.valueOf(getLongitude()));
			return meloc.distanceTo(_locationBeingComparedDXWise);
		} catch (Exception e) {
			return 0f;
		}
	}

	@Override
	public int compareTo(IRThingAbstractLocation location) { 
		if(location==null) {
			return 1;
		}
		if(CurrentSort==IRThingNeed.SORT_NAME) {
			if(location.getName()==null) {
				return 1;
			}
			if(getName()==null) {
				return -1;
			}
			return getName().toLowerCase().compareTo(location.getName().toLowerCase());
		} else { // is SORT_LOCATIONPROXIMITY
			if (location.getDXFromLocation() < getDXFromLocation()) {
				return 1;
			} else {
				if (location.getDXFromLocation() > getDXFromLocation()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
}

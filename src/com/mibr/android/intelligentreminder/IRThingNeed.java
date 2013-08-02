package com.mibr.android.intelligentreminder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import android.location.Location;

public class IRThingNeed extends IRThing  implements Comparable<IRThingNeed> {
	private IRThingItem _item;
	private String _description;
	private PriorityQueue<IRThingAbstractLocation> _locations=new PriorityQueue<IRThingAbstractLocation>();
	public static final int SORT_NAME=1;
	public static final int SORT_LOCATIONNAME=2;
	public static final int SORT_LOCATIONPROXIMITY=3;
	public static int CurrentSort=SORT_NAME;
	public IRThingNeed(long id, String name, String phoneId, IRThingItem item) {
		super(id, name, phoneId);
		_item=item;
	}
	public void setCurrentLocation(Location curLoc) {
		Iterator<IRThingAbstractLocation> it=_locations.iterator();
		while(it.hasNext()) {
			it.next().setLocationBeingComparedDXWise(curLoc);
		}
	}
	public IRThingItem getItem() {
		return _item;
	}
	public String getDescription () {
		return getName();
	}
	public PriorityQueue<IRThingAbstractLocation> getLocations() {
		return _locations;
	}
	public void addLocation(IRThingAbstractLocation loc) {
		_locations.add(loc);
	}
	protected String getFirstLocationByName() {
		String fname=_locations.peek().getName();
		return fname;
	}
	private IRThingAbstractLocation firstLocationDXWise=null;
	protected IRThingAbstractLocation getFirstLocationDXWise() {
		if(firstLocationDXWise==null) {
			IRThingAbstractLocation tii=null;
			Iterator<IRThingAbstractLocation> it =_locations.iterator();
			float currentLeast=99999999f;
			while(it.hasNext()) {
				IRThingAbstractLocation loc=it.next();
				float dx=loc.getDXFromLocation();
				if(dx<currentLeast) {
					currentLeast=dx;
					tii=loc;
				}
			}
			firstLocationDXWise=tii;
		}
		return firstLocationDXWise;
	}
	@Override
	public int compareTo(IRThingNeed need) {
		try {
			if(need==null) {
				return 1;
			}
			if(CurrentSort==SORT_NAME) {
				if(getName()==null) {
					return -1;
				}
				return getName().toLowerCase().compareTo(need.getName().toLowerCase());
			} else {
				if(_locations.size()==0) {
					return 1;
				}
				if(need.getLocations().size()==0 || need.getFirstLocationByName()==null) {
					return 1;
				}
				if(CurrentSort==SORT_LOCATIONNAME) {
					if(getFirstLocationByName()==null) {
						return -1;
					}
					String his=need.getFirstLocationByName().trim().toLowerCase();
					String mine=getFirstLocationByName().trim().toLowerCase();
					return mine.compareTo(his);
				} else { // is SORT_LOCATIONPROXIMITY
					if(need.getFirstLocationDXWise().getDXFromLocation()<getFirstLocationDXWise().getDXFromLocation()) {
						return 1;
					} else {
						if(need.getFirstLocationDXWise().getDXFromLocation()>getFirstLocationDXWise().getDXFromLocation()) {
							return -1;
						} else {
							return 0;
						}
					}
				}
			}
		} catch (Exception ed) {
			return 0;
		}
	}

}

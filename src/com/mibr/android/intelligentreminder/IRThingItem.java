package com.mibr.android.intelligentreminder;

public class IRThingItem extends IRThing implements Comparable<IRThingItem> {

	public IRThingItem(long id, String name, String phoneId) {
		super(id, name,phoneId);

	}

	@Override
	public int compareTo(IRThingItem arg0) {
		if(arg0==null) {
			return 1;
		}
		if(getName()==null) {
			return -1;
		}
		return getName().toLowerCase().compareTo(arg0.getName().toLowerCase());
	}
}

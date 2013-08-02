package com.mibr.android.intelligentreminder;

public abstract class IRThing {
	private long _id;
	private String _name;
	private String _phoneId;
	public IRThing(long id, String name, String phoneId) {
		_id=id;
		_name=name;
		_phoneId=phoneId;
	}
	public long getId() {
		return _id;
	}
	public String getName() {
		return _name;
	}
	public String getPhoneId() {
		return _phoneId;
	}
}

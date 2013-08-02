package com.mibr.android.intelligentreminder;

public class IRLocation {
	public String _name;
	public long _id;
	public long _contactId;
	public String _phoneId;
	public IRLocation (String name, long id, String phoneId, long contactId) {
		_name=name;
		_id=id;
		_phoneId=phoneId;
		_contactId=contactId;
	}
}

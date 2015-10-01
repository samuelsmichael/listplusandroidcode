package com.mibr.android.intelligentreminder;

import java.util.Enumeration;
import java.util.Hashtable;

import android.content.Context;

public class LocationBuilder {
	private String _name="";
	private int _state=0;
	public java.util.ArrayList<String> jdaddresses;
	public java.util.ArrayList<String> jdlatitudes;
	public java.util.ArrayList<String> jdlongitudes;
	private NeedBuilder _nb;
	/*
	 * 0=Needing Name
	 * 1=Searching Internet
	 * 2=NF Internet
	 * 3=Found Internet
	 * 
	 */
	private Boolean _isCompany=false;
	private Boolean _isContact=false;
	Context _ctx;
	private INeedDbAdapter mDbAdapter = null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(_ctx);
		}
		return mDbAdapter;
	}
	public boolean isContact() {
		return _isContact;
	}
	public void writeCompanyLocations() {
		if (jdaddresses != null) {
			for(int which=0;which<jdaddresses.size();which++) {
				long value=getDbAdapter().createLocation(getName(), 
					jdaddresses.get(which), jdlatitudes.get(which),
					jdlongitudes.get(which), getName(), 500, INeedToo.mSingleton.getPhoneId(),null);
			}
		}		
	}
	public Boolean getIsCompany() {
		return _isCompany;
	}
	public void setState(int state) {
		_state=state;
	}
	public LocationBuilder(Context ctx, NeedBuilder nb) {
		_ctx=ctx;
		_nb=nb;
	}
	public void heresAWord(String word) {
		switch(_state) {
		case 0:
			addToName(word);
			break;
		default:
			break;
		}
	}
	public String getName() {
		return _name;
	}
	private String deriveAppendage() {
		String retName="";
		switch (_state) {
		case 1:
			retName=retName + " ..searching internet";
			break;
		case 2:
			retName=retName + "..not found";
			break;
		case 3:
			retName=retName + " ..found";
			break;
		default:
			break;
		}
		return retName;
	}
	public String getNameForView() {
		return _name + deriveAppendage();
	}
	private void addToName(String word) {
		word=word.trim();
		if(_name==null) {
			_name=word;
		} else {
			if(_name.equals("")) {
				_name=word;
			} else {
				_name=_name + " " + word;
			}
		}
	}
	public Boolean haveYouGotLocation() {
		Enumeration e;
		Boolean retValue=false;
		if(_nb.getCompanyNames()!=null) {
			e=_nb.getCompanyNames().keys();
			while(e.hasMoreElements()) {
				if(((String)e.nextElement()).toLowerCase().trim().equals(_name.toLowerCase().trim())) {
					_isCompany=true;
					retValue=true;
					break;
				}
			}
		}
		if(retValue==false) {
			if(_nb.getLocationNames() != null) {
				e=_nb.getLocationNames().keys();
				while(e.hasMoreElements()) {
					String str=(String)e.nextElement();
					IRLocation irloc=_nb.getLocationNames().get(str);
					str=irloc._name.toLowerCase().trim();
					if(str.equals(_name.toLowerCase().trim())) {
						retValue= true;
						if(irloc._contactId!=-1) {
							_isContact=true;
						}
						break;
					}
				}
			}
		}
		if(retValue==false) {
			if(_isContact) { // we've already built it
				retValue=true;
			} else {
				if(_nb.getContactIds()!=null) {
					for(int c=0;c<_nb.getContactIds().length;c++) {
						if(_nb.getContactNames()[c].toLowerCase().trim().equals(_name.toLowerCase().trim())) {
							retValue=true;
							_isContact=true;
							long locationId=getDbAdapter().createLocation(
									_nb.getContactNames()[c], "", null, null, "", 500, INeedToo.mSingleton.getPhoneId(), String.valueOf(_nb.getContactIds()[c]));
							IRLocation irloc=new IRLocation(_nb.getContactNames()[c], locationId, _nb.getContactNames()[c], _nb.getContactIds()[c]);
							
							_nb.addToLocationNames(_nb.getContactNames()[c], irloc);
							break;
						}
					}
				}
			}
		}
		return retValue;
	}
}


package com.mibr.android.intelligentreminder;
/*
 * ToDo phone ids need to be the real ones.
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;

public class NeedBuilder {
	private String _needName;
	private String _needDescription;
	private Hashtable<String,IRLocation> _dbLocationNames=null;
	private Hashtable<String,Long> _dbCompanyNames=new Hashtable<String,Long>();
	private Hashtable<String,IRItem> _dbItemNames=new Hashtable<String,IRItem>();
	private String[] _contactNames;
	private long[] _contactIds;
	private Activity _activity;
	public Hashtable<String,IRLocation> getLocationNames() {
		return _dbLocationNames;
	}
	public String[] getContactNames() {
		if(_contactNames==null) {
			buildContactData();
		}
		return _contactNames;
	}
	public long[] getContactIds() {
		if(_contactIds==null) {
			buildContactData();
		}
		return _contactIds;
	}
	private void buildContactData() {
		
		_contactNames=INeedToo.contactNames;
		_contactIds=INeedToo.contactIds;
		
/*bbhbb		Cursor curses=this.getDbAdapter().getContacts(_activity);
		_contactNames=new String[curses.getCount()];
		_contactIds=new long[curses.getCount()];
		if(curses.getCount()>0) {
			int c=0;
			while(curses.moveToNext()) {
				_contactNames[c]=curses.getString(curses.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				_contactIds[c]=curses.getLong(curses.getColumnIndex(ContactsContract.Contacts._ID));
				c++;
			}
		}
		curses.close();
*/	
	
	}
	public Hashtable<String,Long> getCompanyNames() {
		return _dbCompanyNames;
	}
	public Hashtable<String,IRItem> getItemNames() {
		return _dbItemNames;
	}
	
	private ArrayList<LocationBuilder> _locations=new ArrayList<LocationBuilder>();
	private int _state;
	/*
	 * state:   0 waiting on "need"
	 * 			1 building need
	 * 			2 building description
	 * 			3 building location
	 */	
	public NeedBuilder(	Activity activity,
			Hashtable<String,IRLocation> locations,
			Hashtable<String,Long> companyies, 
			Hashtable<String,IRItem> items) {
		_activity=activity;
		_dbLocationNames=locations;
		_dbCompanyNames=companyies;
		_dbItemNames=items;		
	}
	public void addToLocationNames(String locName,IRLocation irLoc) {
		this._dbLocationNames.put(locName, irLoc);
	}
	public void addToCompanyNames(String companyName,Long value) {
		getCompanyNames().put(companyName, value);
	}
	private INeedDbAdapter mDbAdapter = null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(_activity.getApplicationContext());
		}
		return mDbAdapter;
	}
	private NeedBuilder() throws Exception {
		throw new Exception("Empty constructor not valid");
	}
	public void clearNeedName() {
		_needName=null;
	}
	public void clearNeedDescription() {
		_needDescription=null;
	}
	public void prepareToReceiveLocations() {
		_state=3;
	}
	public boolean isContact() {
		boolean ret=false;
		for(int i=0;i<_locations.size();i++) {
			if(_locations.get(i).isContact()) {
				ret= true;
			}
		}
		return ret;
	}
	public void heresAWord(String word) {
		switch (_state) {
		case 14:
			addToNeedDescription(word);
			break;
		case 13:
			if(word.toLowerCase().equals("description")  || word.toLowerCase().equals("descriptions")) {
				_state=14;
			} else {
				addToNeedName(word);
			}
			break;
		case 12:
			if(word.toLowerCase().equals("about")) {
				_state=13;
			} else {
				_locations.get(_locations.size()-1).heresAWord(word);
			}
			break;
		case 11:
			if(word.toLowerCase().equals("to")) {
				_state=12;
				_locations.add(new LocationBuilder(_activity.getApplicationContext(),this));
			}
			break;
		case 0:
			if(word.toLowerCase().equals("need")) {
				_state=1;
			} else {
				if(word.toLowerCase().equals("talk")) {
					_state=11;
				}
			}
			break;
		case 1:
			if(word.toLowerCase().equals("description")  || word.toLowerCase().equals("descriptions")) {
				_state=2;
			} else {
				if(word.toLowerCase().equals("location") || word.toLowerCase().equals("locations")
						|| word.toLowerCase().equals("location:")
						|| word.toLowerCase().equals("locations:")
						) {
					_state=3;
					_locations.add(new LocationBuilder(_activity.getApplicationContext(),this));
				} else {
					addToNeedName(word);
				}
			}
			break;
		case 2:
			if(word.toLowerCase().equals("location")||
					word.toLowerCase().equals("locations")
					|| word.toLowerCase().equals("location:")
					|| word.toLowerCase().equals("locations:")
					) {
				_state=3;
				_locations.add(new LocationBuilder(_activity.getApplicationContext(),this));
			} else {
				addToNeedDescription(word);
			}
			break;
		case 3:
			if(word.toLowerCase().equals("location") || word.toLowerCase().equals("locations") 
					|| word.toLowerCase().equals("location:") 
					|| word.toLowerCase().equals("locations:") 
					|| word.toLowerCase().equals("or")  || 
					word.toLowerCase().equals("4")) {
				_locations.add(new LocationBuilder(_activity.getApplicationContext(),this));
			} else {
				if(word.equals("711")) {
					word="7-Eleven";
				}
				if(_locations.size()==0) { // handles prompting
					_locations.add(new LocationBuilder(_activity.getApplicationContext(),this));
				}
				_locations.get(_locations.size()-1).heresAWord(word);
			}
		}
	}
	public void addToNeedName(String name) {
		if (_needName == null) {
			_needName=name;
		} else {
			_needName=_needName+" " + name;
		}
	}
	public String getNeedName() {
		return _needName==null?"":_needName;
	}
	public void addToNeedDescription(String description) {
		if (_needDescription==null) {
			_needDescription=description;
		} else {
			_needDescription=_needDescription + " " + description;
		}
	}
	public String getNeedDescription() {
		return _needDescription==null?"":_needDescription;
	}
	private Boolean allLocationsAreOkay() {
		Boolean ret=true;
		for(int i=0;i<_locations.size();i++) {
			if(_locations.get(i).haveYouGotLocation()==false) {
				ret= false;
			}
		}
		return ret;
	}
	public Boolean atLeastOneLocationIsOkay() {
		Boolean ret=false;
		for(int i=0;i<_locations.size();i++) {
			if(_locations.get(i).haveYouGotLocation()) {
				ret= true;
			}
		}
		return ret;
	}
	public String getNeedLocationNamesForView() {
		String names="";
		String separator="";
		for(int i=0;i<_locations.size();i++) {
			String lName=_locations.get(i).getNameForView();
			names=names + separator + (lName==null?"":lName);
			separator="\n";
		}
		return names;
	}
	public Boolean canIUpdate() {
		if(
				(_needName != null) &&
				atLeastOneLocationIsOkay()
			) {
				return true;
			} else {
				return false;
			}
	}
	public Boolean imOkay() {
		if(
			(_needName != null) &&
			allLocationsAreOkay()
		) {
			return true;
		} else {
			return false;
		}
	}
	public void searchInternet(Context ctx, LocationManager locationManager, NeedByVoiceProgress nbvp) {
		try {
		BusinessFinder bf=null;
		List<Address> addresses=null;
		for (int n=0;n<getLocations().size();n++) {
			LocationBuilder lb=getLocations().get(n);
//			try {
//						textViewAfterLocationNames.setText(nb.getNeedLocationNamesForView());
//				} catch (Exception ee2) {}
			if(!lb.haveYouGotLocation()) {
				lb.setState(1);
				if(nbvp!=null) {
					nbvp.showStatus();
				}
				if(bf==null) {
					bf=new BusinessFinder(ctx,locationManager);
					addresses=bf.getCurrentAddress();
				}
				String jdCity=null;
				String jdState=null;
				String jdZip=null;
				String jdLat=null;
				String jdLon=null;
				
				if(addresses!=null && addresses.size()>0) {
					jdCity=addresses.get(0).getLocality();
					jdState=addresses.get(0).getAdminArea();
					jdZip=addresses.get(0).getPostalCode();
				} else {
					Location loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(loc==null) {
						loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
					if(loc!=null) {
						jdLat=String.valueOf(loc.getLatitude());
						jdLon=String.valueOf(loc.getLongitude());
					}
				}
				ArrayList<ArrayList<String>> businesses= bf.businessLocation(lb.getName(), jdCity, jdState, jdZip,jdLat,jdLon);
				java.util.ArrayList<String> jdaddresses=new java.util.ArrayList<String>();
				java.util.ArrayList<String> jdlatitudes=new java.util.ArrayList<String>();
				java.util.ArrayList<String> jdlongitudes=new java.util.ArrayList<String>();
				if(businesses.size()>0) {
					lb.setState(3);
					if(nbvp!=null) {
						nbvp.showStatus();
					}
//						try {textViewAfterLocationNames.setText(nb.getNeedLocationNamesForView());} catch (Exception ed3){}
					Boolean gotone=false;
					Iterator it=businesses.iterator();
					while (it.hasNext()) {
						ArrayList<String> al=(ArrayList<String>)it.next();
						jdaddresses.add(al.get(0) + " " + al.get(1)+ " " + al.get(2));
						jdlatitudes.add(al.get(3));
						jdlongitudes.add(al.get(4));
					}
					lb.jdaddresses=jdaddresses;
					lb.jdlatitudes=jdlatitudes;
					lb.jdlongitudes=jdlongitudes;
//						for(int which=0;which<jdaddresses.size();which++) {
//									long value=mDbAdapter.createLocation(lb.getName(), 
//									jdaddresses.get(which), jdlatitudes.get(which),
//								jdlongitudes.get(which), lb.getName(), 500, INeedToo.mSingleton.getPhoneId());
//					}
					addToCompanyNames(lb.getName(), new Long(999999));
				} else {
					lb.setState(2);
					if(nbvp!=null) {
						nbvp.showStatus();
					}
//						try {textViewAfterLocationNames.setText(nb.getNeedLocationNamesForView());} catch (Exception ed4){}
				}
			}
		}
		} catch (Exception eieio) {
			int i=3;
			int j=i;
		}
	}
	public boolean buildData(Boolean buildEvenIfNotOkay) throws Exception {
		boolean oneOfTheLocationsIsContact=false;
		boolean isContact=false;
		if(buildEvenIfNotOkay || imOkay()) {
			long zItemId=-1;
			String zPhoneId="";
			Enumeration eu=getItemNames().keys();
			while(eu.hasMoreElements()) {
				String key=(String)eu.nextElement();
				if(_needName.toLowerCase().equals(key.toLowerCase())) {
					zItemId=(long)getItemNames().get(key)._id;
					zPhoneId=getItemNames().get(key)._phoneId;
					break;
				}
			}
			if(zItemId==-1) {
				zItemId=this.getDbAdapter().createItem(_needName, INeedToo.mSingleton.getPhoneId());
				zPhoneId=INeedToo.mSingleton.getPhoneId();
			}
			long zNeedId=-1;
			zNeedId=getDbAdapter().createNeed((int)zItemId, this._needDescription, INeedToo.mSingleton.getPhoneId());
			for(int n=0;n<_locations.size();n++) {
				if(_locations.get(n).haveYouGotLocation()) {
					_locations.get(n).writeCompanyLocations();
					String locName=null;//_locations.get(n).getName().trim();
					if(_locations.get(n).getIsCompany()) {
						Enumeration e =getCompanyNames().keys();
						while (e.hasMoreElements()) {
							String ln=(String)e.nextElement();
							if(_locations.get(n).getName().toLowerCase().trim().equals(ln.trim().toLowerCase())) {
								locName=ln;
								break;
							}
						}
						
						getDbAdapter().addNeedsLocation(getCompanyNames().get(locName), locName, zNeedId, INeedToo.mSingleton.getPhoneId());
					} else {
						IRLocation loctii=null;
						Hashtable<String,IRLocation> locnames=getLocationNames();
						Iterator<IRLocation> itf=locnames.values().iterator();
						while(itf.hasNext()) {
							IRLocation loc=itf.next();
							if(loc._name.toLowerCase().trim().equals(_locations.get(n).getName().trim().toLowerCase())) {
								loctii=loc;
								if(loc._contactId!=-1) {
									isContact=true;
								}
								break;
							}
						}
						getDbAdapter().addNeedsLocation(loctii._id, null, zNeedId, loctii._phoneId);
					}
				}
			}
		} else {
			throw new Exception("Unable to build");
		}
		return isContact;
	}
	public ArrayList<LocationBuilder> getLocations() {
		return _locations;
	}
 }

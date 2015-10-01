package com.mibr.android.intelligentreminder;	

import com.mibr.android.intelligentreminder.INeedLocationService.ProximityManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.PriorityQueue;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class INeedDbAdapter {
	public static String doubleApostophize(String str) 
	{
		if(str==null) {
			return null;
		}
		return str.replaceAll("'", "''");
	}
	public static final DateFormat mDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss.S");
	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_COMPANY = "company";
	public static final String KEY_DATEDELETED = "datedeleted";
	public static final String KEY_DATECREATED = "datecreated";
	public static final String KEY_LOCATIONIDFULFILLED = "locationidfulfilled";
	public static final String KEY_PHONEIDFULFILLED = "phoneidfulfilled";
	public static final String KEY_DATECLEARED = "datecleared";
	public static final String KEY_ITEMID = "itemid";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_LOCATIONID = "locationid";
	public static final String KEY_WASADDEDASPARTOFCOMPANY = "wasaddedaspartofcompany";
	public static final String KEY_NEEDID = "needid";
	public static final String KEY_PHONEID = "phoneid";
	public static final String KEY_FOREIGNID = "_fid";

	private static final String TAG = "INeedDbAdapter";
	private static boolean IveCreatedTestData=false;

	/**
	 * Database creation sql statement 
	 */
	private static final String CREATE_TABLE_PHONEIDCOMMONNAME = "create table phoneidcommonname (_id integer primary key autoincrement, "
		+ "phoneid text not null,commonname text not null );";
	private static final String CREATE_TABLE_LOCATION = "create table location (_id integer primary key autoincrement, "
		+ "phoneid text not null, _fid integer null, name text not null,address text null, " +
		  "latitude text null, longitude text null, company string null, datedeleted datetime null, " +
		  "notificationdx int not null, contactid int null  );";
	private static final String CREATE_TABLE_ITEM = "create table item (_id integer primary key autoincrement, "
		+ "phoneid text not null, _fid integer null, name text not null );";
	private static final String CREATE_TABLE_NEED = "create table need (_id integer primary key autoincrement, "
		+ "phoneid text not null, _fid integer null, itemid int null,description text null, " +
		  "datecreated datetime null, datecleared datetime null, locationidfulfilled int null, phoneidfulfilled text null, ispublic bit null );";
	private static final String CREATE_TABLE_LOCATIONNEEDASSOCIATION = "create table locationneedassociation (_id integer primary key autoincrement, "
		+ "phoneid text not null, needid int not null,locationid int not null, fneedid int null, " +
		  "flocationid int null, wasaddedaspartofcompany bit not null  );";
	private static final String CREATE_TABLE_ALERTHISTORY = "create table alerthistory (_id integer primary key autoincrement, "
		+ "phoneid text not null, needid integer not null, locationid not null, datecreated datetime not null );";
	private static final String CREATE_TABLE_REMOTESDELETED = "create table remotesdeleted (_id integer primary key autoincrement, "
		+ "phoneid text not null, _fid integer not null );";
	
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE_LOCATION = "location";
	private static final String DATABASE_TABLE_NEED = "need";
	private static final String DATABASE_TABLE_ITEM = "item";
	private static final String DATABASE_TABLE_LOCATIONNEEDASSOCIATION = "locationneedassociation";
	private static final String DATABASE_TABLE_PHONEIDCOMMONNAME = "phoneidcommonname";
	private static final String DATABASE_TABLE_ALERTHISTORY = "alerthistory";
	private static final String DATABASE_TABLE_REMOTESDELETED = "remotesdeleted";
	private static final int DATABASE_VERSION =52;  // sb 35

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
			db.execSQL(CREATE_TABLE_LOCATION);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_ITEM);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_NEED);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_LOCATIONNEEDASSOCIATION);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_PHONEIDCOMMONNAME);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_ALERTHISTORY);
			} catch (Exception eieio33) {}
			try {
			db.execSQL(CREATE_TABLE_REMOTESDELETED);
			} catch (Exception eieio33) {}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ".");
			if(oldVersion <= 0) {
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCATION);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NEED);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ITEM);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCATIONNEEDASSOCIATION);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PHONEIDCOMMONNAME);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ALERTHISTORY);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_REMOTESDELETED);
			} else {
				if(newVersion>=37 && newVersion<47) {
					try {
						db.execSQL("ALTER TABLE " + DATABASE_TABLE_NEED + " ADD COLUMN 'phoneidfulfilled' TEXT NULL; ");
					} catch (Exception eieio33) {}
				} else {
					if(newVersion>=47 ) {
						try {
							db.execSQL("ALTER TABLE " + DATABASE_TABLE_LOCATION + " ADD COLUMN 'contactid' int NULL; ");
						} catch (Exception eieio33) {
							int bkhere=33;
						}
						try {
							db.execSQL("ALTER TABLE " + DATABASE_TABLE_LOCATIONNEEDASSOCIATION + " ADD COLUMN 'ispublic' bit NULL; ");
						} catch (Exception eieio33) {
							int bkthere=44;
						}
						try {
							db.execSQL("UPDATE " +DATABASE_TABLE_LOCATIONNEEDASSOCIATION + " SET ispublic = 0" );							
						} catch (Exception eee389) {
						}
					} else {
						if(newVersion>=51) {
							try {
								db.execSQL("ALTER TABLE " + DATABASE_TABLE_LOCATIONNEEDASSOCIATION + " DROP COLUMN ispublic; ");
							} catch (Exception e31) {
								e31=e31;
							}
						}
						try {
							db.execSQL("ALTER TABLE " + DATABASE_TABLE_NEED+ " ADD COLUMN 'ispublic' bit NULL; ");
						} catch (Exception eieio33) {
							eieio33=eieio33;
						}
						try {
							db.execSQL("UPDATE " +DATABASE_TABLE_NEED+ " SET ispublic = 0" );							
						} catch (Exception eee389) {
							eee389=eee389;
						}
					}
				}
			}
			onCreate(db);
		}
	}

	private final Context mCtx;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public INeedDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	private static Boolean didCopyDb=false;
	private SQLiteDatabase getSqlDb() {
		if (mDb == null) {
			if (mDbHelper == null) {
				mDbHelper = new DatabaseHelper(mCtx);
			}
			mDb = mDbHelper.getWritableDatabase();
			if (!didCopyDb) {
				didCopyDb=true;
				try {
					if (!INeedToo.mSingleton.isTestVersion() ) {
						if (INeedToo.mSingleton.isSdPresent()) {
							File file = new File("/sdcard/mibr/jddatabase");
							if (file.exists()) {
								FileInputStream isr = null;
								FileOutputStream pw = null;
								try {
									isr = new FileInputStream("/sdcard/mibr/jddatabase");
									// TRIAL_VS_REAL DO NOT CHANGE THESE TO .intelligentremindertrial
									pw = new FileOutputStream("/data/data/com.mibr.android.intelligentreminder/databases/data",false);
									byte[] cc = new byte[4096];
									int cnt = isr.read(cc, 0, 4096);
									int c = 0;
									while (cnt > 0) {
										pw.write(cc, 0, cnt);
										cnt = isr.read(cc, 0, 4096);
									}
								} catch (Exception ee3) {}
								finally {
									try {
										isr.close();
									} catch (Exception e) {
									}
									try {
										pw.flush();
									} catch (Exception e2) {
									}
									try {
										pw.close();
									} catch (Exception e2) {
									}
								}
								file.delete();
							}
						}
					}
				} catch (Exception ee3) {}
			}
		}
		return mDb;
	}

	public void close() {
		mDbHelper.close();
		mDbHelper = null;
		mDb = null;
	}

	public int deleteNeedsLocation(long locationId,String name, long needId, String phoneid) {
		if(locationId!=999999) { // it's not a company
			int retValue= getSqlDb().delete(DATABASE_TABLE_LOCATIONNEEDASSOCIATION, "locationid="+locationId + " AND needid="+needId, null);
			INeedToo.mSingleton.transmitNetwork(phoneid);
			return retValue;
		} else {
			String sql="DELETE FROM locationneedassociation WHERE _id IN ( SELECT lna._id FROM locationneedassociation lna " +
			" INNER JOIN location l ON l._id=lna.locationid " +
			" WHERE needId= "+needId + " AND l.company='"+doubleApostophize(name)+ "')";
			getSqlDb().execSQL( // it's a company
					sql);
			INeedToo.mSingleton.transmitNetwork(phoneid);
			return 1;
		}
	}
	public Cursor getNeedFromId(long needId) {
		return getSqlDb().rawQuery("SELECT * FROM need where _id="+needId,null);
	}
	public Cursor getAllLocationsForCompany(String company) {
		return getSqlDb().rawQuery(
				" SELECT _id FROM location WHERE company='"+doubleApostophize(company)+"' AND contactid is null", null);
	}
	public Cursor getAllLocationsForLocationName(String name) {
		return getSqlDb().rawQuery(
				" SELECT _id FROM location WHERE name='"+doubleApostophize(name)+"'", null);
	}

	public Cursor fetchActiveLocations() {
		String sql=" SELECT DISTINCT ifnull(l.company,'') as company, ln.locationid, l.name as nameLocation, ifnull(l.longitude,'') as longitude, ifnull(l.latitude,'') as latitude"+
				   " FROM locationneedassociation ln " +
				   "	INNER JOIN Location l ON l._id=ln.locationid" +
				   "	INNER JOIN Need n ON n._id=ln.needid " +
				   "	INNER JOIN Item i ON i._id=n.ItemId" +
				   " WHERE" +
				   "	datecleared is null  AND contactid is null and " +
				   "	datedeleted > '" + mDateFormat.format(new Date()) + "'" +
				   " ORDER BY company";
		return getSqlDb().rawQuery(sql, null);	
	}
	public Cursor getOutboundData() {
		String sql=" SELECT ln.needid, ln.locationid, n._id as _idNeed, ifnull(n.description,'') as description, " +
				   "		l.phoneid as phoneid,l._id as _idLocation," +
				   " 		l.name as nameLocation, ifnull(l.address,'') as address, ifnull(l.latitude,'') as latitude, " +
				   "		ifnull(l.longitude,'') as longitude, " +
				   "		l.notificationdx as notificationdx, i._id as _idItem, i.Name as nameItem, l.company" +
				   " FROM locationneedassociation ln " +
				   "	INNER JOIN Location l ON l._id=ln.locationid" +
				   "	INNER JOIN Need n ON n._id=ln.needid " +
				   "	INNER JOIN Item i ON i._id=n.ItemId" +
				   " WHERE" +
				   "	datecleared is null  AND contactid is null AND n.ispublic is not null and  n.ispublic = 1 and " +
				   "	datedeleted > '" + mDateFormat.format(new Date()) + "' AND" +
				   "	n.phoneid='"+INeedToo.mSingleton.getPhoneId()+"'";
		return getSqlDb().rawQuery(sql, null);
	}
	public boolean addNeedsLocation(long l, String string, long mNeedId, String phoneid) {
		boolean retValue=true;
		if(INeedToo.isNothingNot(string) || (l!=999999 && l!=555555)) {
			if(l==999999) {
				Cursor cu=getAllLocationsForCompany(string);
				while(cu.moveToNext()) {
					createLocationNeedAssociation(mNeedId,cu.getLong(cu.getColumnIndex("_id")),true, phoneid,false);
				}
				cu.close();
				INeedToo.mSingleton.transmitNetwork(phoneid);

			} else {
				if(l==555555) {
					Cursor cu=getAllLocationsForCompany(string);
					if(cu.getCount()>0) {
						while(cu.moveToNext()) {
							createLocationNeedAssociation(mNeedId,cu.getLong(cu.getColumnIndex("_id")),true,phoneid,false);
						}
						cu.close();
						INeedToo.mSingleton.transmitNetwork(phoneid);

					} else {
						cu.close();
						Cursor cu2=getAllLocationsForLocationName(string);
						if(cu2.getCount()>0) {
							cu2.moveToFirst();
							l=cu2.getLong(cu2.getColumnIndex("_id"));
							createLocationNeedAssociation(mNeedId, l, false, phoneid,false);
						} else {
							retValue=false;
						}
						cu2.close();
					}
				} else {
					createLocationNeedAssociation(mNeedId, l, false, phoneid,true);
				}
			}
		}
		return retValue;
	}
	public void deleteUnsavedNeeds() {
		getSqlDb().execSQL("DELETE from need WHERE itemid=999999");
	}
	public Cursor getHistory(Date fromDate, Date throughDate) {
		throughDate.setHours(23);
		throughDate.setMinutes(59);
		throughDate.setSeconds(59);
		String sql="SELECT _id as _id,DateCleared as DateCleared, ItemName as ItemName, LocationName as LocationName, Description as description, phoneid as phoneid, phoneidfulfilled as phoneidfulfilled FROM "; 
		String sql1="(SELECT n._id as _id,n.DateCleared as DateCleared, i.Name as ItemName, ifnull(l.Name,'') as LocationName, " +
		" n.Description as Description, n.phoneid as phoneid, n.phoneidfulfilled as phoneidfulfilled " +
		"FROM need n INNER JOIN item i ON i._id=n.ItemId " +
		"LEFT OUTER JOIN location l ON l._id=n.LocationIdFulfilled " +
		"WHERE n.locationidfulfilled is not null AND DateCleared >= '"+mDateFormat.format(fromDate)+"' AND DateCleared <= '"+mDateFormat.format(throughDate)+"'";
		String sql2 = " UNION " +
			" SELECT needid as _id, al.DateCreated as DateCleared, i.Name as ItemName, ifnull(l.Name,'') as LocationName, n.Description as Description, al.phoneid as phoneid, "+
			" 'ALERT' as phoneidfulfilled " +
			" FROM alerthistory al INNER JOIN need n ON al.needid=n._id " +
			" INNER JOIN item i ON i._id=n.ItemId " +
			" LEFT OUTER JOIN location l ON l._id=al.locationid " +
			" WHERE al.DateCreated >= '"+mDateFormat.format(fromDate)+"' AND al.DateCreated <= '"+mDateFormat.format(throughDate)+"' ) AS a " +
			" ORDER BY DateCleared";
		sql=sql + sql1 + sql2;
		Cursor curses=
			getSqlDb().rawQuery(
					sql
					, null);
		return curses;
	}
	public void needWasDeletedOnBelalfOf(long needId, String otherGuysPhoneId, long locationId ) { 
		deleteNeed(needId,INeedToo.mSingleton.getPhoneId(),false);
		ContentValues args = new ContentValues();
		args.put(KEY_DATECLEARED, mDateFormat.format(new GregorianCalendar()
		.getTime()));
		args.put(KEY_LOCATIONIDFULFILLED,locationId);
		args.put(KEY_PHONEIDFULFILLED, otherGuysPhoneId);
		boolean retValue= getSqlDb().update(DATABASE_TABLE_NEED, args,
				KEY_ROWID + "=" + needId , null) > 0;

		//todo: record this fact in history
	}
	public long getForeignLocationFor(long needId, String phoneid) {
		long locationid=-1;
		String sql=" SELECT la.locationid as locationid FROM locationneedassociation la WHERE needid=" + needId + " AND phoneid = '" + phoneid + "'";
		Cursor curses = getSqlDb().rawQuery(sql, null);
		if (curses.moveToFirst()) {
			locationid=curses.getLong(curses.getColumnIndex("locationid"));
		}
		curses.close();
		return locationid;
	}
	public boolean deleteNeed(long rowId, String phoneid, boolean deleteOnBehalfOfToo) { 
		long foreignNeedId=-1;
		long locationId=-1;
		if(deleteOnBehalfOfToo) {
			locationId=getForeignLocationFor(rowId, phoneid);
		}
		primitiveDeleteNeed(-1,rowId, phoneid);
		if(deleteOnBehalfOfToo || !phoneid.equals(INeedToo.mSingleton.getPhoneId())) {
			Cursor cu=this.getNeedFromId(rowId);
			if(cu.moveToFirst()) {
				foreignNeedId=cu.getLong(cu.getColumnIndex("_fid"));
				if(deleteOnBehalfOfToo) {
					deleteOnBehalfOf(rowId,phoneid, locationId, foreignNeedId);
				}
			}
			cu.close();
		}
		ContentValues args = new ContentValues();
		args.put(KEY_DATECLEARED, mDateFormat.format(new GregorianCalendar()
		.getTime()));
		if(!phoneid.equals(INeedToo.mSingleton.getPhoneId())) {
			createRemotesDeleted(phoneid, foreignNeedId);
		}
		return getSqlDb().update(DATABASE_TABLE_NEED, args,
				KEY_ROWID + "=" + rowId , null) > 0;
	}
	public void deleteCompany(String name) {
		Cursor cur2=getAllLocationsForCompany(name);
		if(cur2.getCount()>0) {
			while(cur2.moveToNext()) {
				long id2=cur2.getLong(cur2.getColumnIndex("_id"));
				deleteLocation(id2);
			}
		} 
		cur2.close();
		
	}
	public boolean deleteLocation(long rowId) {
		ContentValues args = new ContentValues();

		args.put(KEY_DATEDELETED, mDateFormat.format(new GregorianCalendar()
		.getTime()));
		return getSqlDb().update(DATABASE_TABLE_LOCATION, args,
				KEY_ROWID + "=" + rowId, null) > 0;
	}

	public void createAlertHistory(String phoneid, long locationid, long needid) {
		Date date = new GregorianCalendar().getTime();
		ContentValues initialValues = new ContentValues();
		initialValues.put("phoneid", phoneid);
		initialValues.put("needid", needid);
		initialValues.put("locationid", locationid);
		initialValues.put("datecreated", mDateFormat.format(date));
		getSqlDb().insert(DATABASE_TABLE_ALERTHISTORY, null, initialValues);
	}

	public void createRemotesDeleted(String phoneid, long foreignNeedId) {
		ContentValues initialValues = new ContentValues();
		initialValues.put("phoneid", phoneid);
		initialValues.put("_fid", foreignNeedId);
		getSqlDb().insert(DATABASE_TABLE_REMOTESDELETED, null, initialValues);
	}
	public Cursor getRemotesDeleted(String phoneid) {
		Cursor curses=getSqlDb().query(
				DATABASE_TABLE_REMOTESDELETED,
				new String[] { KEY_PHONEID, KEY_FOREIGNID},
						"phoneid = '" + phoneid + "'", null,
						null, null, null);
		return curses;
	}
	public Cursor getRemotesDeleted(String phoneid, long foreignNeedId) {
		Cursor curses=getSqlDb().query(
				DATABASE_TABLE_REMOTESDELETED,
				new String[] { KEY_PHONEID, KEY_FOREIGNID},
						"phoneid = '" + phoneid + "' AND _fid =" + foreignNeedId, null,
						null, null, null);
		return curses;
	}
	public long createLocation(String name, String address, String latitude,
			String longitude, String company, int notificationdx, String phoneid, String contactid) {
		int contactidInt=-1;
		try {
			contactidInt=Integer.valueOf(contactid);
		} catch (Exception e) {}
		Date date = new GregorianCalendar(2099, 11, 30).getTime();
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_ADDRESS, address);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUDE, longitude);
		initialValues.put(KEY_COMPANY, company);
		initialValues.put(KEY_DATEDELETED, mDateFormat.format(date));
		initialValues.put("notificationdx", notificationdx);
		initialValues.put("phoneid", phoneid);
		if(contactidInt!=-1) {
			initialValues.put("contactid", contactidInt);
		}

		return getSqlDb().insert(DATABASE_TABLE_LOCATION, null, initialValues);
	}
	public long createForeignLocation(int fid, String name, String address, String latitude,
			String longitude, String company, int notificationdx, String phoneid) {
		Date date = new GregorianCalendar(2099, 11, 30).getTime();
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_ADDRESS, address);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUDE, longitude);
		initialValues.put(KEY_COMPANY, company);
		initialValues.put(KEY_DATEDELETED, mDateFormat.format(date));
		initialValues.put("notificationdx", notificationdx);
		initialValues.put("phoneid", phoneid);
		initialValues.put("_fid", fid);

		return getSqlDb().insert(DATABASE_TABLE_LOCATION, null, initialValues);
	}
	
	public long updateOrCreateForeignLocation(int fid, String name, String address, String latitude, String longitude,
			int notificationdx, String phoneid, String company) {
		Cursor curses=getSqlDb().rawQuery("SELECT _id FROM location WHERE _fid="+fid+" and phoneid='"+doubleApostophize(phoneid)+"'", null);
		long locationId;
		if(curses.getCount()==0) {
			locationId=createForeignLocation(fid,name,address,latitude,longitude,company,notificationdx,phoneid);
		} else {
			curses.moveToFirst();
			locationId=curses.getLong(curses.getColumnIndex("_id"));
			updateLocation(locationId, name,address,latitude, longitude,company, notificationdx, phoneid);
		}
		curses.close();
		return locationId;
	}
	
	public void deleteAllTheseGuysBUTNOTME(String fPhoneid) {
		String sql=" DELETE FROM " + DATABASE_TABLE_LOCATIONNEEDASSOCIATION + " WHERE phoneid = '" +
			doubleApostophize(fPhoneid) + "' AND phoneid != '" + doubleApostophize(INeedToo.mSingleton.getPhoneId())+ "'" +
			" AND NOT EXISTS(SELECT _id FROM need n WHERE _id = needid AND phoneid = '"+INeedToo.mSingleton.getPhoneId()+"')";
		getSqlDb().execSQL(sql);	
//		Date date = new GregorianCalendar().getTime();
//		String date=mDateFormat.format(new GregorianCalendar()
//		.getTime());
//		sql = " UPDATE " + DATABASE_TABLE_NEED + "  SET " + KEY_DATECLEARED + " = '" +date+ "'  WHERE phoneid = '" +  
//		doubleApostophize(fPhoneid) + "' AND phoneid != '" + doubleApostophize(INeedToo.mSingleton.getPhoneId())+ "' ";
		
		sql=" DELETE FROM  " + DATABASE_TABLE_NEED + "  WHERE phoneid = '" +  
		doubleApostophize(fPhoneid) + "' AND phoneid != '" + doubleApostophize(INeedToo.mSingleton.getPhoneId())+ "' "; 
//		+
//		" AND datecleared is null";
		getSqlDb().execSQL(sql);	
	}

	public long updateOrCreateForeignNeed(int fid,long itemid, String description, String phoneid) {
		Cursor curses=getSqlDb().rawQuery("SELECT _id FROM need WHERE _fid="+fid+" and phoneid='"+doubleApostophize(phoneid)+"'", null);
		long needId;
		if(curses.getCount()==0) {
			needId=createForeignNeed(fid,itemid,description,phoneid);
		} else {
			curses.moveToFirst();
			needId=curses.getLong(curses.getColumnIndex("_id"));
			updateNeed(needId, itemid, description, phoneid,false );
		}
		curses.close();
		return needId;

	}
	public long createForeignNeed(int fid, long itemid, String description, String phoneid) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put("phoneid", phoneid);
		initialValues.put("_fid", fid);
		initialValues.put("itemid", itemid);
		return getSqlDb().insert(DATABASE_TABLE_NEED, null, initialValues);
	}
	
	public long updateOrCreateForeignItem(int fid, String phoneid, String name) {
		Cursor curses=getSqlDb().rawQuery("SELECT _id FROM item WHERE _fid="+fid+" and phoneid='"+doubleApostophize(phoneid)+"'", null);
		long itemId;
		if(curses.getCount()==0) {
			itemId=this.createForeignItem(name,phoneid,fid);
		} else {
			curses.moveToFirst();
			itemId=curses.getLong(curses.getColumnIndex("_id"));
			updateItem((long)itemId,name);
		}
		curses.close();
		return itemId;
	}
	public void updateItem(long id, String name) {
		ContentValues args=new ContentValues();
		args.put("name", name);
		getSqlDb().update(DATABASE_TABLE_ITEM, args, "_id="+id, null);
	}
	
	public long createForeignItem(String name, String phoneid, int fid) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put("phoneid", phoneid);
		initialValues.put("_fid", fid);
		return getSqlDb().insert(DATABASE_TABLE_ITEM, null, initialValues);
	}
	
	public long createItem(String name, String phoneid) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put("phoneid", phoneid);
		return getSqlDb().insert(DATABASE_TABLE_ITEM, null, initialValues);
	}

	public long  createNeed(int itemId, String description, String phoneid) {
		Date date = new GregorianCalendar().getTime();
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ITEMID, itemId);
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put(KEY_DATECREATED, mDateFormat.format(date));
		initialValues.put("phoneid", phoneid);
		return getSqlDb().insert(DATABASE_TABLE_NEED, null, initialValues);
	}

	public long createIfItsNotAlreadyThereLocationNeedAssociation(long needId, long locationId, boolean wasaddedaspartofcompany, String phoneid) {
		Cursor curses = getSqlDb().rawQuery(
				"SELECT * FROM "+DATABASE_TABLE_LOCATIONNEEDASSOCIATION+" where needId="+needId + " AND " +
						"locationID=" + locationId + " AND phoneid=''"+phoneid+"'",null);
		boolean doit=true;
		if(curses.getCount()>0) {
			doit=false;
		}
		curses.close();
		if(doit) {
			return createLocationNeedAssociation(needId, locationId, wasaddedaspartofcompany, phoneid,true);
		} else {
			return -1;
		}
	}
	public long createLocationNeedAssociation(long needId, long locationId, 
			boolean wasaddedaspartofcompany, String phoneid, boolean doTransmitNetwork) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NEEDID, needId);
		initialValues.put(KEY_LOCATIONID, locationId);
		initialValues.put(KEY_WASADDEDASPARTOFCOMPANY, wasaddedaspartofcompany);
		initialValues.put("phoneid", phoneid);
		long retValue = getSqlDb().insert(DATABASE_TABLE_LOCATIONNEEDASSOCIATION, null,
				initialValues);
		if (doTransmitNetwork) {
			INeedToo.mSingleton.transmitNetwork(phoneid);
		}
		return retValue;
	}
	private boolean exists(Cursor alreadyHave, long id, String name, String phoneid, boolean isContact) {
		try {
			if(alreadyHave.getCount()==0) {
				return false;
			}
			boolean wereNotDone=alreadyHave.moveToFirst();
			while(wereNotDone) {
				long myid=alreadyHave.getLong(alreadyHave.getColumnIndex("_id"));
				String myName=alreadyHave.getString(alreadyHave.getColumnIndex("Name"));
				String myPhoneid=alreadyHave.getString(alreadyHave.getColumnIndex("phoneid"));
				if(!isContact) {
						if(id!=999999 && id==myid) {
							return true;
						}
						if(id==999999 && name.equalsIgnoreCase(myName) && phoneid.equals(myPhoneid)) {
							return true;
						}
				} else {
					if(myName.equals(name)) {
						return true;
					}
				}
				wereNotDone=alreadyHave.moveToNext();
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public long findLocationWhoseContactIdIs(long contactid) {
		long id=-1;
		String sql=" SELECT _id FROM location WHERE contactid = " + contactid;
		Cursor curses=getSqlDb().rawQuery(sql,null);
		if(curses.getCount()>0) {
			curses.moveToFirst();
			id=curses.getLong(curses.getColumnIndex("_id"));
		}
		curses.close();
		return id;
	}
	
	// 1=just contacts 2=justlocations
	public AlertDialogHelper fetchCompaniesAndLocationsForNeedAddingLocation(long needId, int contacts, Activity activity ) {
		String moreString="";
		Cursor all=null;
		String[] strings=null;
		String[] phoneids=null;
		long[] ids=null;
		boolean[] isAlreadyChecked=null;
		if(contacts==2) {
			moreString=" contactid is null and ";
			String sql=
				"SELECT _id, Name, SortSequence, Picked, phoneid FROM " +
				"(SELECT DISTINCT 999999 as _id, company AS Name, 1 as SortSequence, 0 as Picked, phoneid FROM location l " +
				" WHERE "+moreString+" company is not null AND company != '' AND datedeleted > '" + mDateFormat.format(new Date()) + "' "  +
				" UNION " +
				"SELECT _id, Name, 2 as SortSequence, 0 as Picked, phoneid FROM location l " +
				" WHERE "+moreString+" (company is null OR company = '') AND datedeleted > '" + mDateFormat.format(new Date()) + "' ) AS a " +
				" ORDER BY SortSequence, upper(Name) ";
			all=getSqlDb().rawQuery(sql,null);
			int cnt=0;
			cnt=all.getCount();
			all.moveToFirst();
			strings=new String[cnt];
			phoneids=new String[cnt];
			ids=new long[cnt];
			isAlreadyChecked=new boolean[cnt];
			Cursor alreadyHave=null;
			if(needId!=-1) {
				alreadyHave=fetchCompaniesAndLocationsForNeed(needId);
			}
			int c=0;
			String thisPhonesId=INeedToo.mSingleton.getPhoneId();
			boolean gotMore=true;
			if(cnt==0) {
				gotMore=false;
			}
			while(gotMore) {
				String name=all.getString(all.getColumnIndex("Name"));
				if(INeedToo.isNothingNot(name)) {
					strings[c]=name;
					String phoneid=all.getString(all.getColumnIndex("phoneid"));
					phoneids[c]=phoneid;
					isAlreadyChecked[c]=false;
					ids[c]=all.getLong(all.getColumnIndex("_id"));
					if(needId!=-1) {
						if(exists(alreadyHave,all.getLong(all.getColumnIndex("_id")),name, phoneid,false)) {
							isAlreadyChecked[c]=true;
						}
					}
					c++;
				}
				gotMore=all.moveToNext();
			}
			all.close();
			if(alreadyHave !=null) {
				alreadyHave.close();
			}
		} else {
			if(contacts==1) {
				int cnt=0;
				if(INeedToo.mSingleton.contactIds != null) {
					cnt=INeedToo.mSingleton.contactIds.length;
				}
				strings=new String[cnt];
				phoneids=new String[cnt];
				ids=new long[cnt];
				isAlreadyChecked=new boolean[cnt];
				Cursor alreadyHave=null;
				if(needId!=-1) {
					alreadyHave=fetchCompaniesAndLocationsForNeed(needId);
				}
				String thisPhonesId=INeedToo.mSingleton.getPhoneId();
				boolean gotMore=true;
				if(cnt==0) {
					gotMore=false;
				}
				for(int c=0;c<cnt;c++) {
					String name=INeedToo.mSingleton.contactNames[c];
					
					if(INeedToo.isNothingNot(name) && name.length()>0) {
						strings[c]=INeedToo.mSingleton.contactNames[c];
						phoneids[c]=thisPhonesId;
						isAlreadyChecked[c]=false;
						ids[c]=INeedToo.mSingleton.contactIds[c];
						if(needId!=-1) {
							if(exists(alreadyHave,ids[c],strings[c], thisPhonesId,true)) {
								isAlreadyChecked[c]=true;
							}
						}
					}
				}
				if(alreadyHave !=null) {
					alreadyHave.close();
				}
				
				
	//Idon'tthinkso			all=getContacts(activity);
				
				
			}
		}
		AlertDialogHelper adh=new AlertDialogHelper();
		adh.showStrings=strings;
		adh.isAlreadySelected=isAlreadyChecked;
		adh.ids=ids;
		adh.phoneids=phoneids;
		return adh;
	}
	public Cursor fetchNeedsForLocation(long locationId) {
		String str= "SELECT n._id as _id, n.Description as Description, i.Name as Name FROM locationneedassociation la INNER JOIN " +
						" need n ON n._id=la.needid INNER JOIN " +
						" item i ON n.itemid=i._id " +
					"WHERE la.locationid="+locationId+" AND datecleared is null " +
					"ORDER BY lower(i.Name)";
		return getSqlDb().rawQuery(str,null);
	}
	public Cursor fetchCompaniesAndLocationsForNeed(long needId) {
		String str="SELECT _id, Name, SortSequence, phoneid FROM " +
		"(SELECT DISTINCT 999999 as _id, l.company AS Name, 1 as SortSequence, l.phoneid as phoneid" +
		"	FROM locationneedassociation lna " +
		"		INNER JOIN location l ON l._id=lna.locationid" +
		"	WHERE lna.needid="+needId + " AND lna.wasaddedaspartofcompany=1 AND datedeleted > '" + mDateFormat.format(new Date()) + "' "  +
		"UNION " +
		"SELECT l._id, l.name as Name, 2 as SortSequence, l.phoneid as phoneid" +
		"	FROM locationneedassociation lna " +
		"		INNER JOIN location l ON l._id=lna.locationid" +
		"	WHERE lna.needid="+needId + " AND lna.wasaddedaspartofcompany=0 AND datedeleted > '" + mDateFormat.format(new Date()) + "' ) AS a " +
		"ORDER BY SortSequence, upper(Name)";
		return getSqlDb().rawQuery(
				str, null);
	}

	/*
	public Cursor getContactsDoINeedThisAnymore(Activity activity)
    {
        // Run query
    	
    	
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
                (false ? "0" : "1") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    }
	*/
	
	public Cursor fetchLocationDescriptionsForNeed(long needId) {
		return
		getSqlDb().rawQuery(
				"SELECT DISTINCT CASE WHEN lna.wasaddedaspartofcompany = 1 THEN l.company ELSE l.name END as LocationName, l.phoneid as phoneid " +
				" FROM locationneedassociation lna " +
				"	INNER JOIN location l ON l._id=lna.locationid " +
				"   WHERE lna.needid="+needId+" AND datedeleted > '" + mDateFormat.format(new Date()) + "'",null
		);
	}
	public Cursor fetchAllNeeds() {
		Cursor curses =
			getSqlDb().rawQuery("SELECT n._id, n.Description, i.Name, n.DateCreated, '' as locations, n.phoneid as phoneid, '' as address "+
					" FROM need n INNER JOIN item i ON i._id=n.itemid " +
					" WHERE datecleared is null "+
					" ORDER BY lower(i.Name)",null);
		return curses;

	}
	private ArrayList<IRThingNeed> fetchAllNeedsAlphabeticByLocation(Location curLoc) {
		String sql="SELECT n._id as nid, n.Description as needdescription, i.Name as itemname, n.DateCreated, " +
		" l.name as lname, l.company as lcompany, n.phoneid as needphoneid, l.address as address, l._id as lid, " +
		" i._id as iid, i.phoneid as itemphoneid, l.phoneid as locphoneid, l.latitude, l.longitude"+
		" FROM need n INNER JOIN item i ON i._id=n.itemid " +
		"	 INNER JOIN locationneedassociation la ON la.needid=n._id" +
		" 	 INNER JOIN location l ON la.locationid=l._id" +
		" WHERE n.datecleared is null "+
		" ORDER BY n._id, l.company, l._id";
		Cursor curses =
			getSqlDb().rawQuery(sql,null);
		ArrayList<IRThingNeed> needs=new ArrayList<IRThingNeed>();
		long remNId=-1;
		String remCo="";
		IRThingNeed need=null;
		IRThingCompany abCo=null;
		while(curses.moveToNext()) {
			long nId=curses.getLong(curses.getColumnIndex("nid"));
			long lId=curses.getLong(curses.getColumnIndex("lid"));
			String co=curses.getString(curses.getColumnIndex("lcompany")).trim();
			if(nId!=remNId) {
				if(need!=null) {
					needs.add(need);
				}
				need=new IRThingNeed(
						nId, 
						curses.getString(curses.getColumnIndex("needdescription")), 
						curses.getString(curses.getColumnIndex("needphoneid")), 
						new IRThingItem(
								curses.getLong(curses.getColumnIndex("lid")),
								curses.getString(curses.getColumnIndex("itemname")),
								curses.getString(curses.getColumnIndex("itemphoneid"))
						));
				remNId=nId;
				remCo="";
			}
			if(!co.equals("")) {
				if(!remCo.equals(co)) {
					remCo=co;
					abCo=new IRThingCompany(lId,co,curses.getString(curses.getColumnIndex("locphoneid")));
					abCo.setLocationBeingComparedDXWise(curLoc);
					need.addLocation(abCo);
				}
				IRThingLocation irl=new IRThingLocation(
						lId,curses.getString(curses.getColumnIndex("lname")),curses.getString(curses.getColumnIndex("locphoneid")),
						curses.getString(curses.getColumnIndex("latitude")),
						curses.getString(curses.getColumnIndex("longitude")),
						curses.getString(curses.getColumnIndex("address"))
					);
				irl.setLocationBeingComparedDXWise(curLoc);
				abCo.addLocation(irl);
			} else {
				IRThingLocation irl2=new IRThingLocation(
						lId,curses.getString(curses.getColumnIndex("lname")),curses.getString(curses.getColumnIndex("locphoneid")),
						curses.getString(curses.getColumnIndex("latitude")),
						curses.getString(curses.getColumnIndex("longitude")),
						curses.getString(curses.getColumnIndex("address"))
					);
				irl2.setLocationBeingComparedDXWise(curLoc);
				need.addLocation(irl2);
			}
		}
		needs.add(need);
		curses.close();
		return needs;
	}
	public Cursor fetchAllNeedsByLocationProximity(Location curLoc) {
		getSqlDb().execSQL("DROP TABLE IF EXISTS tmpneedviewing");
		getSqlDb().execSQL("create table tmpneedviewing (" +
				"_id integer primary key autoincrement," +
				"needid integer not null, " +
				"phoneid text null," +
				"name text null, " +
				"description text null, " +
				"locations text null," +
				"address text null);");
		ArrayList<IRThingNeed> needs=fetchAllNeedsAlphabeticByLocation(curLoc);
		PriorityQueue<IRThingNeed> needsForResorting=new PriorityQueue<IRThingNeed>();
		Iterator<IRThingNeed> it=needs.iterator(); 
		while(it.hasNext()) {
			needsForResorting.add(it.next());
		}
		while(needsForResorting.size()>0) {
			IRThingNeed need=needsForResorting.poll();
			String locname=need.getLocations().peek().getName();
			ContentValues initialValues = new ContentValues();
			initialValues.put("needid", need.getId());
			initialValues.put("phoneid", need.getPhoneId());
			initialValues.put("name", need.getItem().getName());
			initialValues.put("description", need.getDescription());
			initialValues.put("locations", "");
			initialValues.put("address", "");
			getSqlDb().insert("tmpneedviewing", null, initialValues);

/*			String sql="INSERT INTO tmpneedviewing (_id,phoneid,name,description,locations,address) Values(" +
			need.getId() +","+
			"'"+need.getPhoneId()+"',"+
			"'"+need.getItem().getName()+"',"+
			"'"+need.getDescription()+"'," +
			"''," +
			"''"+
			");";
			getSqlDb().execSQL(sql,null);
*/			
		}
		String qre2=" SELECT needid, Description, Name, locations, phoneid, address,_id from tmpneedviewing ORDER by _id";
		Cursor curses = getSqlDb().rawQuery(qre2,null);
		return curses;

	}
	private void setupTestValues() {
/*		
		if(!IveCreatedTestData) {
			IveCreatedTestData=true;
			long location1 = createLocation("Brian's Home",
					"1945 Julian St Denver CO 80212", null, null, null,50,INeedToo.mSingleton.getPhoneId());
			long location2 = createLocation("Mike's Home",
					"121 S Eaton St Lakewood CO 80226", null, null, null, 200,INeedToo.mSingleton.getPhoneId());
			long item1 = createItem("Eggs",INeedToo.mSingleton.getPhoneId());
			long item2 = createItem("Kitty Litter",INeedToo.mSingleton.getPhoneId());
			long item3 = createItem("Playing Cards",INeedToo.mSingleton.getPhoneId());
			long need1 = createNeed((int) item1,
			"I need 2 dozen eggs.  Get large, brown.",INeedToo.mSingleton.getPhoneId());
			long need2 = createNeed((int) item2, "Get Feline Pine. It's the best.",INeedToo.mSingleton.getPhoneId());
			long need3 = createNeed((int) item3, "",INeedToo.mSingleton.getPhoneId());
			createLocationNeedAssociation((int) need1, (int) location1, false,INeedToo.mSingleton.getPhoneId());
			createLocationNeedAssociation((int) need2, (int) location1, false,INeedToo.mSingleton.getPhoneId());
			createLocationNeedAssociation((int) need3, (int) location2, false,INeedToo.mSingleton.getPhoneId());
			createLocationNeedAssociation((int) need3, (int) location1, false,INeedToo.mSingleton.getPhoneId());
		}
*/		
	}
	public boolean thisLocationAlreadyExists(String name, String phoneid) {
		Cursor curses =getSqlDb().rawQuery(
				"SELECT * FROM location WHERE Name = '"+doubleApostophize(name)+"' and phoneid='"+doubleApostophize(phoneid)+"'"
				, null);
		boolean yesum=false;
		if(curses.getCount()>0) {
			yesum=true;
		}
		curses.close();
		return yesum;
	}
	public int countNeedsForThisLocation(int id) {
		int i=0;
		Cursor curses=getSqlDb().rawQuery(
				"SELECT n._id FROM locationneedassociation a " +
				"INNER JOIN need n ON n._id=a.NeedID " +
				"INNER JOIN location l ON l._id=a.LocationID " +
				"WHERE l._id = "+id+" AND n.DateCleared IS NULL AND l.datedeleted > '" + mDateFormat.format(new Date()) + "'"
				, null);
		i=curses.getCount();
		curses.close();
		return i;
	}
	// 1=just contacts 2=justlocations 3=bothll

	public Cursor fetchAllLocationsIHaveLocationsOld1(int doingContacts) {
		String xtra="";
		if(doingContacts==1) {
			xtra=" contactid is not null and ";
		} else {
			if(doingContacts==2) {
				xtra=" contactid is null and ";
			}
		}
		String sql=
			"SELECT phoneid as phoneid, Name as name, Address as address, _id as _id,  SortSequence as SortSequence, contactid FROM " +
			"(SELECT DISTINCT '' as phoneid, company AS Name, '' as Address, 999999 as _id, 1 as SortSequence, ifnull(contactid,-1) as contactid FROM location l " +
			" WHERE "+xtra+" company is not null AND company != '' AND datedeleted > '" + mDateFormat.format(new Date()) + "' "  +
			" UNION " +
			"SELECT phoneid, Name, address as Address, _id,  2 as SortSequence, ifnull(contactid,-1) as contactid FROM location l " +
			" WHERE "+xtra+" (company is null OR company = '') AND datedeleted > '" + mDateFormat.format(new Date()) + "' ) AS a " +
			" ORDER BY SortSequence, upper(Name) ";
		Cursor curses=getSqlDb().rawQuery(sql,null);
		return curses;
	}
	private Cursor fetchAllLocationsIHaveLocationsNewNew1() {
		String sql=
			"SELECT phoneid, name, address, _id, ifnull(company,'') as company, latitude, longitude FROM location " +
			" WHERE datedeleted > '" + mDateFormat.format(new Date()) + "' AND contactid is null "  +
			" ORDER BY Company, Name ";
		Cursor curses=getSqlDb().rawQuery(sql,null);
		return curses;

	}

	public Boolean thereAreSomeLocationsOnSystem() {
		String sql=
			"SELECT * from Location ";
		Cursor curses=getSqlDb().rawQuery(sql,null);
		Boolean retValue=true;
		if(curses.getCount()==0) {
			retValue=false;
		}
		curses.close();
		return retValue;
	}
	private Cursor fetchAllLocationsIHaveLocationsNewNew2(String company) {
		String sql=
			"SELECT phoneid, name, address, _id, ifnull(company,'') as company, latitude, longitude FROM location " +
			" WHERE datedeleted > '" + mDateFormat.format(new Date()) + "' AND company = '"+this.doubleApostophize(company)+"' AND contactid is null " +
			" ORDER BY Company, Name ";
		Cursor curses=getSqlDb().rawQuery(sql,null);
		return curses;

	}
	
	private Cursor thefinality(Cursor curses, Location curLoc, Boolean forceAsLocations) {
		ArrayList<IRThingAbstractLocation> locations=new ArrayList<IRThingAbstractLocation>();
		String remCompany="";
		IRThingCompany irco=null;
		while (curses.moveToNext()) {
			String co=curses.getString(curses.getColumnIndex("company")).trim();
			if(!co.equals("") && !forceAsLocations ) {
				if(!remCompany.equals(co)) {
					irco=new IRThingCompany(
						curses.getLong(curses.getColumnIndex("_id")),
						curses.getString(curses.getColumnIndex("company")),
						curses.getString(curses.getColumnIndex("phoneid"))
					);
					irco.setLocationBeingComparedDXWise(curLoc);
					locations.add(irco);
					remCompany=co;
				}
				IRThingLocation irl=new IRThingLocation(
						curses.getLong(curses.getColumnIndex("_id")),
						curses.getString(curses.getColumnIndex("name")),
						curses.getString(curses.getColumnIndex("phoneid")),
						curses.getString(curses.getColumnIndex("latitude")),
						curses.getString(curses.getColumnIndex("longitude")),
						curses.getString(curses.getColumnIndex("address"))
					);
				irl.setLocationBeingComparedDXWise(curLoc);
				irco.addLocation(irl);
			} else {
				IRThingLocation irl2=new IRThingLocation(
						curses.getLong(curses.getColumnIndex("_id")),
						curses.getString(curses.getColumnIndex("name")),
						curses.getString(curses.getColumnIndex("phoneid")),
						curses.getString(curses.getColumnIndex("latitude")),
						curses.getString(curses.getColumnIndex("longitude")),
						curses.getString(curses.getColumnIndex("address"))
					);
				irl2.setLocationBeingComparedDXWise(curLoc);
				locations.add(irl2);
			}
		}
		curses.close();
		PriorityQueue<IRThingAbstractLocation> locationsForResorting=new PriorityQueue<IRThingAbstractLocation>();
		Iterator<IRThingAbstractLocation> it=locations.iterator(); 
		while(it.hasNext()) {
			locationsForResorting.add(it.next());
		}
		getSqlDb().execSQL("DROP TABLE IF EXISTS tmplocviewing");
		getSqlDb().execSQL("create table tmplocviewing (" +
				"_id integer primary key autoincrement," +
				"name text null, " +
				"locid integer not null, " +
				"phoneid text null," +
				"SortSequence int null, " +
				"address text null" +
				");");
		while(locationsForResorting.size()>0) {
			int ss=1;
			IRThingAbstractLocation irloc=locationsForResorting.poll();
			if(!irloc.isCompany()) {
				ss=2;
			}
			String phoneid=irloc.getPhoneId();
			if(ss==1) {
				phoneid="";
			}
			ContentValues initialValues = new ContentValues();
			initialValues.put("locid", irloc.getId());
			initialValues.put("phoneid", phoneid);
			initialValues.put("name", irloc.getName());
			initialValues.put("address", irloc.getAddress());
			initialValues.put("SortSequence", ss);
			getSqlDb().insert("tmplocviewing", null, initialValues);

/*			String sql="INSERT INTO tmpneedviewing (_id,phoneid,name,description,locations,address) Values(" +
			need.getId() +","+
			"'"+need.getPhoneId()+"',"+
			"'"+need.getItem().getName()+"',"+
			"'"+need.getDescription()+"'," +
			"''," +
			"''"+
			");";
			getSqlDb().execSQL(sql,null);
*/			
		}
		String qre2=" SELECT locid, name,  phoneid, address,SortSequence,_id from tmplocviewing ORDER by _id";
		Cursor curses2 = getSqlDb().rawQuery(qre2,null);
		return curses2;

		
	}

	public Cursor fetchAllLocationsIHaveLocationsNew2(String company, Location curLoc) {
		Cursor curses=fetchAllLocationsIHaveLocationsNewNew2(company);
		return thefinality(curses,curLoc,true);
		
	}
	public Cursor fetchAllLocationsIHaveLocationsNew1(Location curLoc) {
		Cursor curses=fetchAllLocationsIHaveLocationsNewNew1();
		return thefinality(curses,curLoc,false);
	}

	
	public Cursor fetchAllLocationsIHaveLocationsOld2(String company) {

		Cursor curses = getSqlDb().query(
				DATABASE_TABLE_LOCATION,
				new String[] { KEY_ROWID, KEY_NAME, KEY_ADDRESS, KEY_LATITUDE,
						KEY_LONGITUDE, KEY_COMPANY, KEY_DATEDELETED, "notificationdx", "phoneid" },
						"datedeleted > '" + mDateFormat.format(new Date()) + "' and contactid is null AND company = '"+this.doubleApostophize(company)+"'", null,
						null, null, "upper(Name)");
		return curses;
	}
	// 1=just contacts 2=justlocations 3=bothll
	public Cursor fetchAllLocationsPrimitive(int contacts) {
		String moreString="";
		if(contacts==1) {
			moreString=" and contactid is not null ";
		} else {
			if(contacts==2) {
				moreString=" and contactid is null";
			} 
		}
		Cursor curses = getSqlDb().query(
				DATABASE_TABLE_LOCATION,
				new String[] { KEY_ROWID, KEY_NAME, KEY_ADDRESS, KEY_LATITUDE,
						KEY_LONGITUDE, KEY_COMPANY, KEY_DATEDELETED, "contactid","notificationdx", "phoneid" },
						"datedeleted > '" + mDateFormat.format(new Date()) + "'" + moreString, null,
						null, null, "upper(Name)");
/*		if (curses.getCount() == 0) { // load database for testing purposes
			setupTestValues();
			curses.close();
			curses = getSqlDb().query(
					DATABASE_TABLE_LOCATION,
					new String[] { KEY_ROWID, KEY_NAME, KEY_ADDRESS, KEY_LATITUDE,
							KEY_LONGITUDE, KEY_COMPANY, KEY_DATEDELETED, "notificationdx","phoneid" },
							"datedeleted > '" + mDateFormat.format(new Date()) + "'", null,
							null, null, "upper(Name)");
		} else {
			IveCreatedTestData=true;
		}
*/		
		return curses;
	}

	public String[] fetchAllLocationsLite() {
		Cursor curses = getSqlDb().query(
				true,
				DATABASE_TABLE_LOCATION,
				new String[] { KEY_NAME, KEY_ROWID, "phoneid" },
				"datedeleted > '" + mDateFormat.format(new Date()) + "'", null, null,
				null, "upper(name)", null);
		String[] sa = new String[curses.getCount()];
		this.correspondingIds=new long[curses.getCount()];
		correspondingphoneids=new String[curses.getCount()];
		int c = 0;
		while (curses.moveToNext()) {
			sa[c] = curses.getString(0);
			correspondingIds[c]=curses.getLong(1);
			correspondingphoneids[c]=curses.getString(2);
			c++;
		}
		curses.close();
		return sa;
	}
	public long[]correspondingIds=null;
	public String[]correspondingphoneids=null;
	// 1=just contacts 2=justlocations
	public String[] fetchAllLocationsLiteButNotTheOnesfor(long needId, int contacts, Activity activity, int dummy) {
		correspondingIds=null;
		AlertDialogHelper adh = fetchCompaniesAndLocationsForNeedAddingLocation(needId,contacts,activity);
		int c=0;
		for(int i=0;i<adh.ids.length;i++) {
			if(!adh.isAlreadySelected[i]) {
				c++;
			}
		}
		String[] sa = new String[c];
		correspondingIds=new long[c];
		correspondingphoneids=new String[c];
		c=0;
		for(int i=0;i<adh.ids.length;i++) {
			if(!adh.isAlreadySelected[i]) {
				sa[c]=adh.showStrings[i] + "|" + adh.ids[i];
				correspondingIds[c]=adh.ids[i];
				correspondingphoneids[c]=adh.phoneids[i];
				c++;
			}			
		}
		return sa;
	}
	public Cursor allItems() {
		return getSqlDb().query(
				true,
				DATABASE_TABLE_ITEM,
				new String[] { KEY_NAME,"phoneid",KEY_ROWID },
				"name is not null AND name != ''", null, null,
				null, "upper(name)", null);
	}
	public String[] fetchAllItems() {
		Cursor curses = getSqlDb().query(
				true,
				DATABASE_TABLE_ITEM,
				new String[] { KEY_NAME,"phoneid" },
				"name is not null AND name != ''", null, null,
				null, "upper(name)", null);
		String[] sa = new String[curses.getCount()];
		correspondingphoneids=new String[curses.getCount()];
		int c = 0;
		while (curses.moveToNext()) {
			sa[c] = curses.getString(0);
			correspondingphoneids[c++]=curses.getString(1);
		}
		curses.close();
		return sa;
	}
	public String[] fetchAllCompanies() {
		Cursor curses = getSqlDb().query(
				true,
				DATABASE_TABLE_LOCATION,
				new String[] { KEY_COMPANY,"phoneid" },
				"company is not null AND company != '' AND datedeleted > '"
				+ mDateFormat.format(new Date()) + "'", null, null,
				null, "upper(company)", null);
		String[] sa = new String[curses.getCount()];
		int c = 0;
		while (curses.moveToNext()) {
			sa[c++] = curses.getString(0);
		}
		curses.close();
		return sa;
	}

	public Cursor fetchNeedsForALocation(long locationId) {
		String str = "SELECT n._id as _id, i.Name as Name, n.Description as Description,n.phoneid as phoneid  "
			+ " FROM locationneedassociation lna "
			+ " 	INNER JOIN need n ON n._id=lna.needid "
			+ "		INNER JOIN item i ON i._id=n.itemid "
			+ "		INNER JOIN location l ON l._id=lna.locationID "
			+ " WHERE l._id=" + locationId + " AND n.DateCleared is  null ORDER BY upper(i.Name)";
		Cursor curses = getSqlDb().rawQuery(str, null);
		return curses;
	}

	public Cursor fetchNeed(long needId) throws SQLException {
		Cursor mCursor = getSqlDb().rawQuery(
				"SELECT n._id, ifnull(i.name,'') as ItemName, n.description, n.datecreated, n.datecleared, l.name as LocationName, n.phoneid as phoneid, n.ispublic as ispublic " +
				"FROM need n " +
				"	LEFT OUTER JOIN item i ON i._id=n.itemid " +
				"	LEFT OUTER JOIN location l ON l._id=n.locationidfulfilled " +
				"WHERE n._id= "+needId, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	public Cursor fetchLocation(long rowId) throws SQLException {

		Cursor mCursor =

			getSqlDb().query(
					true,
					DATABASE_TABLE_LOCATION,
					new String[] { KEY_ROWID, KEY_NAME, KEY_ADDRESS, KEY_LATITUDE,
							KEY_LONGITUDE, KEY_COMPANY, KEY_DATEDELETED,"notificationdx","phoneid", "_fid", "contactid" },
							KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public boolean updateNeed(long rowId, String itemName, String description, String phoneid, Boolean isPublic) {
		Cursor curses=getSqlDb().rawQuery("SELECT _id FROM item WHERE name='"+doubleApostophize(itemName)+"'", null);
		long itemId;
		if(curses.getCount()==0) {
			itemId=this.createItem(itemName,phoneid);
		} else {
			curses.moveToFirst();
			itemId=curses.getLong(curses.getColumnIndex("_id"));
		}
		curses.close();
// just for testing		deleteOnBehalfOf(rowId,phoneid);
		return updateNeed(rowId, itemId,description,phoneid,isPublic);
	}

	public boolean updateNeed(long rowId, long itemId, String description, String phoneid,Boolean isPublic ) {
		ContentValues args = new ContentValues();
		args.put(KEY_ITEMID, itemId);
		args.put(KEY_DESCRIPTION, description);
		args.put("phoneid", phoneid);
		args.put("ispublic", isPublic);
		return getSqlDb().update(DATABASE_TABLE_NEED, args,
				KEY_ROWID + "=" + rowId, null) > 0;
	}
	public boolean updateLocation(long rowId, String name, String address,
			String latitude, String longitude, String company, int notificationdx, String phoneid) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_ADDRESS, address);
		args.put(KEY_LATITUDE, latitude);
		args.put(KEY_LONGITUDE, longitude);
		args.put(KEY_COMPANY, company);
		args.put("notificationdx", notificationdx);
		args.put("phoneid",phoneid);

		return getSqlDb().update(DATABASE_TABLE_LOCATION, args,
				KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	
	private void primitiveDeleteNeed(long locationId, long needId, String phoneid) {
		getSqlDb().execSQL("DELETE FROM "+DATABASE_TABLE_LOCATIONNEEDASSOCIATION+" WHERE needid="+needId);
//justfortesting		if(locationId==-1) {
//			
//			if(!phoneid.equals(INeedToo.mSingleton.getPhoneId())) {
//				deleteOnBehalfOf(needId,phoneid, locationId);
//			}
//		}
		if(INeedToo.mSingleton==null) {
			Intent jdIntent=new Intent(mCtx, INeedToo.class).
					putExtra("phoneid",phoneid).setAction("doPrimitiveDeletedNeed");
			mCtx.startActivity(jdIntent);
		} else {
			INeedToo.mSingleton.transmitNetwork(phoneid);
		}
	}
	private void deleteOnBehalfOf(long needId, String phoneid, long locationId, long foreignNeedId) {
		long foreignLocationId=-1;
		Cursor cu=null;
		if(foreignNeedId == -1) {
			cu= getNeedFromId(needId);
			while(cu.moveToNext()) {
				foreignNeedId=cu.getLong(cu.getColumnIndex("_fid"));
			}
			cu.close();
		}
		if(locationId!=-1) {
			cu=fetchLocation(locationId);
			foreignLocationId=cu.getLong(cu.getColumnIndex("_fid"));
			cu.close();
			if (INeedToo.mSingleton==null) {
				Intent jdIntent=new Intent(mCtx, INeedToo.class).
						putExtra("needId",needId).
						putExtra("foreignNeedId",foreignNeedId).
						putExtra("phoneid",phoneid).
						putExtra("foreignLocationId",foreignLocationId)
						.setAction("transmitNetworkDeletedThisNeedByOnBehalfOf");
				mCtx.startActivity(jdIntent);
			} else {
				INeedToo.mSingleton.transmitNetworkDeletedThisNeedByOnBehalfOf(needId, foreignNeedId, phoneid, foreignLocationId);
			}
		}
	}
	
	public static boolean isNothing(Object obj) {
		if (obj == null) {
			return true;
		} else {
			if (obj.toString().trim().equals("")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNothingNot(Object obj) {
		return !isNothing(obj);
	}

	private String getDeviceNumber() {
		try {
			TelephonyManager tm = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getLine1Number();
		} catch (Exception ee) {
			return "";
		}
	}

	private String getDeviceId() {
		try {
			TelephonyManager tm = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getLine1Number();
		} catch (Exception ee) {
			return "";
		}
	}

	
	public String getPhoneId() {
		String anotherID = android.provider.Settings.System.getString(
				mCtx.getContentResolver(),
				android.provider.Settings.System.ANDROID_ID);
		if (isNothingNot(anotherID)) {
			return anotherID.trim();
		}
		try {
			String number = this.getDeviceNumber();
			if (isNothingNot(number)) {
				return number.trim();
			}
		} catch (Exception e33) {
			try {
				String id = this.getDeviceId();
				if (isNothingNot(id)) {
					return id.trim();
				}
			} catch (Exception e3d) {
			}
		}
		return "unknown";
		//return "20013fd8f4dbc118";
	}
	
	public boolean imOnIt(long locationId, long needId, String phoneid) {
		primitiveDeleteNeed(locationId, needId, phoneid);
		if(!phoneid.equals(getPhoneId())) {
			deleteOnBehalfOf(needId,phoneid, locationId,-1);
		}
		ContentValues args = new ContentValues();
		args.put(KEY_DATECLEARED, mDateFormat.format(new GregorianCalendar()
		.getTime()));
		args.put(KEY_LOCATIONIDFULFILLED,locationId);
		args.put(KEY_PHONEIDFULFILLED, getPhoneId());
		boolean retValue= getSqlDb().update(DATABASE_TABLE_NEED, args,
				KEY_ROWID + "=" + needId , null) > 0;
		return retValue;
	}
	public boolean needsNotificationClearNeed(long locationId, long needId, String phoneid) {
		primitiveDeleteNeed(locationId, needId, phoneid);
		if(!phoneid.equals(INeedToo.mSingleton.getPhoneId())) {
			deleteOnBehalfOf(needId,phoneid, locationId,-1);
		}
		ContentValues args = new ContentValues();
		args.put(KEY_DATECLEARED, mDateFormat.format(new GregorianCalendar()
		.getTime()));
		return getSqlDb().update(DATABASE_TABLE_NEED, args,
				KEY_ROWID + "=" + needId , null) > 0;
	}

	public long thereExistsChezMoiThisLocation(String latitude, String longitude) {
		long locationId=-1;
		Cursor curses=null;
		try {
			curses=fetchAllLocationsPrimitive(2);
			Boolean foundIt=false;
			while(curses.moveToNext() && foundIt==false) {
				if(curses.getString(curses.getColumnIndex("phoneid")).equals(INeedToo.mSingleton.getPhoneId())) {
					Location location = new Location("gps");
					location.setLatitude(Double.valueOf(latitude));
					location.setLongitude(Double.valueOf(longitude));
					Location location2=new Location("gps");
					location2.setLatitude(Double.valueOf(curses.getString(curses.getColumnIndex("latitude"))));
					location2.setLongitude(Double.valueOf(curses.getString(curses.getColumnIndex("longitude"))));
					float dx=location.distanceTo(location2);
					if (dx<=200) {
						locationId=curses.getLong(curses.getColumnIndex("_id"));
						foundIt=true;
					}
				}
			}
		} catch (Exception ee) {			
		} finally {
			try {
				curses.close();
			} catch (Exception eee) {}
		}
		return locationId;
	}
}

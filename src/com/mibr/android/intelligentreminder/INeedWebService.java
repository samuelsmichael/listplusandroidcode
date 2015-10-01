package com.mibr.android.intelligentreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import android.widget.Toast;

public class INeedWebService extends Service {
	private INeedDbAdapter mDbAdapter = null;

	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
//	public static final String BASE_URL = "http://www.cellularassistant.com/intelligentreminder";
	public static final String BASE_URL = "http://listplus.no-ip.org/mibr/intelligentreminder";
	private static final int CONNECTION_TIMEOUT = 2000;
	private static final int CONNECTION_TIMEOUT_CHECKSTATUS = 1000;
	//private static final String BASE_URL = "http://204.12.46.18";
	//private static final String BASE_URL = "http://192.168.1.103/mibr";
	private static final String FIELD_DELIMITER="^~^";
	private static final String SMALL_FIELD_DELIMITER="^~^";
	private String _jdString="";
	public static INeedWebService mSingleton=null;
	private int _lastInboundLength=0;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	private void writeString(String str, OutputStream out) {
		_jdString += str.trim();
		PrintWriter pw2=new PrintWriter(out);
		pw2.write(str.trim());
		pw2.flush();
	}

	private boolean jdClearWhenDone(Intent intent) {
		boolean jd=false; 
		try {
			jd=intent.getBooleanExtra("clearWhenDone", false);
		} catch(Exception e) {}
		return jd;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if (intent != null) {
			mSingleton=this;
			final boolean clearWhenDone=jdClearWhenDone(intent);
			if(intent.getAction().equalsIgnoreCase("TransmitLog"))
				new Thread(new Runnable(){
					public void run() {
						transmitLog(clearWhenDone);
					}
				}).start();
			if(intent.getAction().equals("Outbound")) {
				final boolean doingmanually=intent.getBooleanExtra("doingmanually", false);
				new Thread(new Runnable(){
					public void run() {
						outbound(doingmanually);
					}
				}).start();
			}
			
			if(intent.getAction().equals("adpress")) {
				final int adnbr=intent.getIntExtra("adnbr", -1);
				if(adnbr>0) {
					new Thread(new Runnable(){
						public void run() {
							adpress(adnbr);
						}
					}).start();
				}
			}
	
			if(intent.getAction().equals("adview")) {
				final int adnbr=intent.getIntExtra("adnbr", -1);
				final String phoneid=intent.getStringExtra("phoneid");
				if(adnbr>0) {
					new Thread(new Runnable(){
						public void run() {
							adview(adnbr,phoneid);
						}
					}).start();
				}
			}
			
			
			if(intent.getAction().equals("Inbound")) {
				final boolean doingmanually=intent.getBooleanExtra("doingmanually", false);
				new Thread(new Runnable(){
					public void run() {
						inbound(doingmanually);
					}
				}).start();
			}
			if(intent.getAction().equals("CheckStatus")) {
				new Thread(new Runnable(){
					public void run() {
						checkStatus();
					}
				}).start();
			}
			if(intent.getAction().equals("Register")) {
				final String ccNbr=intent.getStringExtra("ccNbr");
				final String expMonth=intent.getStringExtra("expMonth");
				final String expYear=intent.getStringExtra("expYear");
				final String ccId=intent.getStringExtra("ccId");
				final String nameOnCard=intent.getStringExtra("nameOnCard");
				final String ccCardType=intent.getStringExtra("ccCardType");
				final String city=intent.getStringExtra("city");
				final String state=intent.getStringExtra("state");
				final String postalCode=intent.getStringExtra("postalCode");
				final String country=intent.getStringExtra("country");
				final String address=intent.getStringExtra("address");
				new Thread(new Runnable(){
					public void run() {
						register(ccNbr,  expMonth,  expYear,  ccId,  nameOnCard, ccCardType,
								address,city,state,postalCode,country);
					}
				}).start();
			}
			if(intent.getAction().equals("DeleteOnBehalfOf")) {
				final long foreignNeedId=intent.getLongExtra("foreignneedid",-1);
				final String onBehalfOfPhoneId=intent.getStringExtra("needphoneid");
				final String localPhoneId=intent.getStringExtra("localphoneid");
				final long foreignLocationId=intent.getLongExtra("foreignlocationid",-1);
				new Thread(new Runnable(){
					public void run() {
						deleteOnBehalfOf(foreignNeedId, onBehalfOfPhoneId, localPhoneId, foreignLocationId );
					}
				}).start();
			}
			if(intent.getAction().equals("BusinessLocation")) {
				final String jdBusiness=intent.getStringExtra("Business");
				final String jdZip=intent.getStringExtra("Zip");
				final String jdState=intent.getStringExtra("State");
				final String jdCity=intent.getStringExtra("City");
				new Thread(new Runnable(){
					public void run() {
						businessLocation(jdBusiness, jdCity,jdState,jdZip);
					}
				}).start();
			}
		}
	}

	private String needMoreFromReader(InputStreamReader sr) throws IOException {
		int cnt = sr.read(ca,0,4096);
		if(cnt>0) {
			char[] ca1=new char[cnt];
			for(int c=0;c<cnt;c++) {
				ca1[c]=ca[c];
			}
			return new String(ca1);
		} else {
			_weredone=true;
		}
		return "";
	}
	private String getMeMyNextField(InputStreamReader sr)  throws IOException { 
		if(_str.length()==0) {
			_str=needMoreFromReader(sr);
		}
		if(_str.length()==0) {
			_weredone=true;
			return null;
		} else {
			int index3=_str.indexOf(FIELD_DELIMITER);
			if(index3==-1) {
				_swk=needMoreFromReader(sr);
				if(_swk.length()==0) {
					_swk=_str;
					_str="";
					return _swk;
				} else {
					_str=_str+_swk;
				}
				index3=_str.indexOf(FIELD_DELIMITER);
			}
			_swk=_str.substring(0,index3);
			_str=_str.substring(index3+3);
			return _swk;
		}
	}


	private char[] ca = new char[4096];
	private boolean _weredone=false;
	private String _str="";
	private String _swk="";
	
	
	
	private void businessLocation(String business, String city, String state, String zip) {
		try {
			ArrayList<ArrayList<String>> jdLocations=new BusinessFinder(this,null).businessLocation(business,city,state,zip,null,null);
			if(jdLocations!=null) {
				Intent intent=new Intent(getApplicationContext(),LocationFindBusinesses.class)
				.putExtra("City", city)
				.putExtra("State", state)
				.putExtra("Zip", zip)
				.putExtra("Business", business)
				.putExtra("Google", jdLocations)
				.putExtra("gotLocationManager", true)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
			} else {
				try {
					if(LocationFindBusinesses.progressDialog!=null) {
						LocationFindBusinesses.progressDialog.dismiss();
						LocationFindBusinesses.progressDialog=null;
					}
				} catch (Exception e33) {
					
				}
				Toast
				.makeText(
						getApplicationContext(),
						"No items found. Try changing the business name.",
						Toast.LENGTH_LONG)
						.show();
				
			}
		} catch (Exception ee) {
			INeedToo.mSingleton.log("Failure finding businesses. "+ee.getStackTrace(), 1);
			try {
				if(LocationFindBusinesses.progressDialog!=null) {
					LocationFindBusinesses.progressDialog.dismiss();
					LocationFindBusinesses.progressDialog=null;
				}
			} catch (Exception e33) {
				
			}
			Toast
			.makeText(
					getApplicationContext(),
					"No items found. Try changing the business name.",
					Toast.LENGTH_LONG)
					.show();
			int bkhere=3;
			int bk=bkhere;
		}
	}
	
	private synchronized void deleteOnBehalfOf(long foreignNeedId, String onBehalfOfPhoneId, String localPhoneId, long foreignLocationId ) {
		try {
			INeedToo.mSingleton.log("Starting deleteOnBehalfOf", 1);
			URL u = new URL(BASE_URL + "/DeleteOnBehalfOfV3.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			writeString(localPhoneId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(onBehalfOfPhoneId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(String.valueOf(foreignNeedId),out);
			writeString(FIELD_DELIMITER,out);
			writeString(String.valueOf(foreignLocationId),out);
			writeString(FIELD_DELIMITER,out);
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is); 
			char[] cb=new char[8000]; 
			int nbrread=isr.read(cb);
			String str=String.valueOf(cb, 0, nbrread); 
			INeedToo.mSingleton.log("Result from deleteOnBehalfOf: "+str, 1);
			isr.close();
		} catch (Exception ee3) {
			try {
				INeedToo.mSingleton.log(ee3.getMessage(), 3);
			} catch (Exception eeee33) {}
		}
	}
	private synchronized void adview(int addnbr, String phoneid) {
		HttpURLConnection conn=null;
		try {
			URL u = new URL(BASE_URL + "/RecordView.aspx"); 
			conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT_CHECKSTATUS);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			writeString(phoneid,out);
			writeString(FIELD_DELIMITER,out);
			writeString(String.valueOf(addnbr),out);
			writeString(FIELD_DELIMITER,out);
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is);
			String okness=getMeMyNextField(isr);
			isr.close();
		} catch (Exception ee) {
			ee=ee;
		} finally {
			try {conn.disconnect();} catch (Exception eded) {}
		}
	}
	private synchronized void adpress(int addnbr) {
		HttpURLConnection conn=null;
		try {
			URL u = new URL(BASE_URL + "/RecordPress.aspx"); 
			conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT_CHECKSTATUS);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			writeString(String.valueOf(addnbr),out);
			writeString(FIELD_DELIMITER,out);
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is);
			String okness=getMeMyNextField(isr);
			isr.close();
		} catch (Exception ee) {} finally {try {conn.disconnect();} catch (Exception ded){}}
	}
	private synchronized void checkStatus() {
		try {
			URL u = new URL(BASE_URL + "/RegistrationStatus.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT_CHECKSTATUS);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			String deviceId="unknown";
			try {
				deviceId=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee3) {}
			writeString(deviceId,out);
			writeString(FIELD_DELIMITER,out);
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is);
			String okness=getMeMyNextField(isr);
			if(okness.equals("Ok")) {
				INeedToo.mSingleton.registerPhone();
			} else {
				if(okness.toLowerCase().equals("trialperiodending")) {
					INeedToo.mSingleton.TrialIsEndingWarningOrLicensingFailed(getMeMyNextField(isr),false);
				} else {
					String notOknessReason=getMeMyNextField(isr);
					if(notOknessReason.equals("No registration")) {
						INeedToo.mSingleton.unRegisterPhone();
					} else {
						INeedToo.mSingleton.unRegisterPhoneWithReason(notOknessReason);
					}
				}
				String yesAds=getMeMyNextField(isr);
				if(yesAds.equals("YesAds")) {
					String ad=getMeMyNextField(isr);
					String[] adData=ad.split("\\|");
					INeedToo.mSingleton.Ad1Text=adData[0];
					INeedToo.mSingleton.Ad1URI=adData[1];
					ad=getMeMyNextField(isr);
					adData=ad.split("\\|");
					INeedToo.mSingleton.Ad2Text=adData[0];
					INeedToo.mSingleton.Ad2URI=adData[1];
					String doViewCount=getMeMyNextField(isr);
					if(doViewCount.toLowerCase().equals("true")) {
						INeedToo.mSingleton.doViewCount=true;
					} else {
						INeedToo.mSingleton.doViewCount=false;
					}
					
				}
			}
			isr.close();
		} catch (Exception ee) {}
	}
	private synchronized void inbound(boolean doingmanually) {
		try {
			INeedToo.mSingleton.log("Starting inbound", 1);

			_weredone=false;
			URL u = new URL(BASE_URL + "/InboundTraffic.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			String deviceId="unknown";
			try {
				deviceId=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee3) {}
			writeString(deviceId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("Networking_PhoneIDs", ""),out);
			writeString(FIELD_DELIMITER,out);
			PrintWriter pw=new PrintWriter(out);
			pw.close();

			InputStream is = conn.getInputStream(); 
			
			InputStreamReader isr=new InputStreamReader(is);
			boolean isFirstTime=true;
			boolean _weredone2=false;
			String priorPhoneid=null;
			while(_weredone==false) {
				String fPhoneid=getMeMyNextField(isr);
				if(fPhoneid != null) {
	 				if(fPhoneid.equals("**##@@")) {
						/* This means the next fields are "deleteonbehalfof"*/
						while (_weredone2==false) {
							String myNeedId=getMeMyNextField(isr);
							if(myNeedId==null) {
								_weredone2=true;
							} else {
								String theOtherGuysPhoneID=getMeMyNextField(isr);
								String myLocationId=getMeMyNextField(isr);
								getDbAdapter().needWasDeletedOnBelalfOf(Integer.valueOf(myNeedId), theOtherGuysPhoneID, Integer.valueOf(myLocationId));
							}
						}
						_weredone=true;
					} else {
						String nameItem=getMeMyNextField(isr);						
						if(nameItem != null && nameItem.equals("**##@@")) {
							/* This means the next fields are "deleteonbehalfof"*/
							while (_weredone2==false) {
								String myNeedId=getMeMyNextField(isr);
								if(myNeedId==null) {
									_weredone2=true;
								} else {
									String theOtherGuysPhoneID=getMeMyNextField(isr);
									String myLocationId=getMeMyNextField(isr);
									getDbAdapter().needWasDeletedOnBelalfOf(Integer.valueOf(myNeedId), theOtherGuysPhoneID, Integer.valueOf(myLocationId));
								}
							}
							_weredone=true;
						} else {
							if(fPhoneid!=null && !fPhoneid.equals(priorPhoneid)) {
								getDbAdapter().deleteAllTheseGuysBUTNOTME(fPhoneid);
								priorPhoneid=fPhoneid;
							}
							isFirstTime=false;
							if(nameItem==null) {
								_weredone = true;
							} else {
								String description=getMeMyNextField(isr);
								String nameLocation=getMeMyNextField(isr);
								String address=getMeMyNextField(isr);
								String latitude=getMeMyNextField(isr);
								String longitude=getMeMyNextField(isr);
								String notificationdx=getMeMyNextField(isr);
								int intnotificationdx=200;
								try {
									intnotificationdx=Integer.valueOf(notificationdx);
								} catch (Exception e2) {}
								String fidneed=getMeMyNextField(isr);
								String fiditem=getMeMyNextField(isr);
								String fidlocation=getMeMyNextField(isr);
								String company=getMeMyNextField(isr);
								Cursor curses=getDbAdapter().getRemotesDeleted(fPhoneid, Long.valueOf(fidneed));
								if(!curses.moveToFirst()) {
									long itemID=getDbAdapter().updateOrCreateForeignItem(
											Integer.valueOf(fiditem), fPhoneid, nameItem);
									long needID=getDbAdapter().updateOrCreateForeignNeed(Integer.valueOf(fidneed),itemID, description,fPhoneid);
									long chezmoiLocation=getDbAdapter().thereExistsChezMoiThisLocation(latitude, longitude);
									long locationID=-1;
									if(chezmoiLocation!=-1) {
										locationID=chezmoiLocation;
									} else {
									locationID=getDbAdapter().updateOrCreateForeignLocation(Integer.valueOf(fidlocation), nameLocation,
											address,latitude,longitude,
											intnotificationdx, fPhoneid,company);
									}
									getDbAdapter().createLocationNeedAssociation(needID, locationID, false, fPhoneid,false);
								}
								curses.close();
							}
						}
					}
				} else {
					_weredone=true;
				}
			}
			if(doingmanually){
				Toast
				.makeText(
						getApplicationContext(),
						"Receive is complete",
						Toast.LENGTH_LONG)
						.show();
			}
			isr.close();
		} catch (Exception ee3) { 
			try {
				INeedToo.mSingleton.log(ee3.getMessage(), 3);
			} catch (Exception eeee33) {}
		}
	}

	private synchronized void register(String ccNbr, String expMonth, String expYear, String ccId, 
			String nameOnCard, String ccCardType, String address, String city, String state, String postalCode, String country) {
		try {
			INeedToo.mSingleton.log("Starting register", 1);
			URL u = new URL(BASE_URL + "/RegisterTrialV2.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			String deviceId="unknown";
			try {
				deviceId=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee3) {}
			writeString(deviceId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(jdEncode(ccNbr),out);
			writeString(FIELD_DELIMITER,out);
			writeString(expMonth,out);
			writeString(FIELD_DELIMITER,out);
			writeString(expYear,out);
			writeString(FIELD_DELIMITER,out);
			writeString(ccId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(nameOnCard,out);
			writeString(FIELD_DELIMITER,out);
			writeString(ccCardType,out);
			writeString(FIELD_DELIMITER,out);
			
			writeString(address,out);
			writeString(FIELD_DELIMITER,out);
			writeString(city,out);
			writeString(FIELD_DELIMITER,out);
			writeString(state,out);
			writeString(FIELD_DELIMITER,out);
			writeString(postalCode,out);
			writeString(FIELD_DELIMITER,out);
			writeString(country,out);
			writeString(FIELD_DELIMITER,out);

			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is); 
			String okness=getMeMyNextField(isr);
			okness=okness;
			if(okness.equals("Ok")) {
				try {
					if(INeedToPay.progressDialog!=null) {
						INeedToPay.progressDialog.dismiss();
						INeedToPay.progressDialog=null;
					}
				} catch (Exception e33) {}
				Intent intent=new Intent(getApplicationContext(),INeedToPay.class)
				.putExtra("okness", "ok")
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				;
				this.startActivity(intent);

				Toast
				.makeText(
						getApplicationContext(),
						"Thank you for registering!",
						Toast.LENGTH_LONG)
						.show();
			} else {
				try {
					if(INeedToPay.progressDialog!=null) {
						INeedToPay.progressDialog.dismiss();
						INeedToPay.progressDialog=null;
					}
				} catch (Exception e33) {
					
				}
				Toast
				.makeText(
						getApplicationContext(),
						"Failed Registering. Please try again later.",
						Toast.LENGTH_LONG)
						.show();
			}
			isr.close();
		} catch (Exception ee3) { 
			
			try {
				if(INeedToPay.progressDialog!=null) {
					INeedToPay.progressDialog.dismiss();
					INeedToPay.progressDialog=null;
				}
			} catch (Exception e33) {
				
			}
			Toast
			.makeText(
					getApplicationContext(),
					"Failed Registering. Please try again later.",
					Toast.LENGTH_LONG)
					.show();
			int bkhere=3;
			int bk=bkhere;
			
			
			try {
				INeedToo.mSingleton.log(ee3.getMessage(), 3);
			} catch (Exception eeee33) {}
		}
	}
	private String[] code = {
			"abcdefghijkl",
			"mnopqrstuvwx",
			"yzabcdefghij",
			"klmnopqrstuv",
			"wxyz12345678",
			"90abcdefghijk",
			"lmnopqrstuvwx",
			"yz0123456789",
			"01234567890abc",
			"defghijklmnopq",
			"rstuvwxyzabcde",
			"abcdefghijkl",
			"mnopqrstuvwx",
			"yzabcdefghij",
			"klmnopqrstuv",
			"wxyz12345678",
			"90abcdefghijk",
			"lmnopqrstuvwx",
			"yz0123456789",
			"01234567890abc",
			"defghijklmnopq",
			"rstuvwxyzabcde"
	};
	private String jdEncode(String ccnbr) {
		String jdcoded="";
		for(int i=0;i<ccnbr.length();i++) {
			String jdstr=code[i];
			String nbr=ccnbr.substring(i, i+1);
			int ccdigit=Integer.valueOf(nbr);
			jdcoded+=jdstr.substring(ccdigit, ccdigit+1);
		}
		return jdcoded;
	}
	private synchronized void outbound(boolean doingmanually) {
		if (doingmanually /*todo: why does it take so long? */|| !doingmanually ) {
		try {
			Cursor curses = getDbAdapter().getOutboundData();
			INeedToo.mSingleton.log("Starting outbound", 1);
			URL u = new URL(BASE_URL + "/OutboundTraffic.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			String deviceId="unknown";
			try {
				deviceId=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee3) {}
			writeString(deviceId,out);
			writeString(FIELD_DELIMITER,out);
			writeString(getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
					Preferences.MODE_PRIVATE).getString("Networking_PhoneIDs", ""),out);
			writeString(FIELD_DELIMITER,out);
			String sfd="";
			int count=curses.getCount();
			if(curses.getCount()>0) {
				while(curses.moveToNext()) {
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("needid"))),out);
					sfd=SMALL_FIELD_DELIMITER;
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("locationid"))),out);
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("_idNeed"))),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("description")),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("phoneid")),out);
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("_idLocation"))),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("nameLocation")),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("address")),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("latitude")),out);
					writeString(sfd+curses.getString(curses.getColumnIndex("longitude")),out);
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("notificationdx"))),out);
					writeString(sfd+String.valueOf(curses.getInt(curses.getColumnIndex("_idItem"))),out);
					String nameItem=curses.getString(curses.getColumnIndex("nameItem"));
					writeString(sfd+nameItem,out);
					String company=curses.getString(curses.getColumnIndex("company"));
					writeString(sfd+company,out);
				}
			}
			_jdString=_jdString+"";
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is); 
			char[] cb=new char[8000]; 
			int nbrread=isr.read(cb);
			String str=String.valueOf(cb, 0, nbrread); 
			String srr2=str;
			INeedToo.mSingleton.log("Result from outbound: "+str, 1);
			curses.close();
			if(doingmanually){
				Toast
				.makeText(
						getApplicationContext(),
						"Publish is complete",
						Toast.LENGTH_LONG)
						.show();
			}
			isr.close();
			/*			
	String sql=" SELECT ln.needid, ln.locationid, n._id as _idNeed, n.description, " +
   "		l.phoneid as phoneid,l._id as _idLocation," +
   " 		l.name as nameLocation, l.address as address, l.latitude as latitude, l.longitude as longitude, " +
   "		l.notificationdx as notificationdx, i._id as _idItem, i.Name as nameItem" +
   " FROM locationneedassociation ln " +
   "	INNER JOIN Location l ON l._id=ln.locationid" +
   "	INNER JOIN Need n ON n._id=ln.needid +" +
   "	INNER JOIN Item i ON i._id=n.ItemId" +
   " WHERE" +
   "	datecleared is null AND" +
   "	datedeleted > '" + mDateFormat.format(new Date()) + "' AND" +
   "	phoneid='"+INeedToo.mSingleton.getPhoneId()+"'";
			 */			
		} catch (Exception ee3) { 
			try {
				INeedToo.mSingleton.log(ee3.getMessage(), 3);
			} catch (Exception eeee33) {}
		}
		}
	}

	private synchronized void transmitLog(boolean clearWhenDone) {
		try {
			INeedToo.mSingleton.log("Transmitting log", 3);
			URL u = new URL(BASE_URL + "/Log.aspx"); 
			HttpURLConnection conn=(HttpURLConnection)u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(CONNECTION_TIMEOUT);
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			String deviceId="unknown";
			try {
				deviceId=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee3) {}
			writeString(deviceId,out);
			writeString(FIELD_DELIMITER,out);
			INeedToo.mSingleton.writeLogTo(out);
			InputStream is = conn.getInputStream(); 
			InputStreamReader isr=new InputStreamReader(is); 
			char[] cb=new char[8000]; 
			int nbrread=isr.read(cb);
			String str=String.valueOf(cb, 0, nbrread); 
			String srr2=str;
			if(clearWhenDone) {
				INeedToo.mSingleton.clearLog();
			}
			INeedToo.mSingleton.log("Result from transmission of Log: "+str, 1);
			isr.close();
		} catch (Exception ee3) { 
			try {
				INeedToo.mSingleton.log(ee3.getMessage(), 3);
			} catch (Exception eeee33) {}
		}
	}
}


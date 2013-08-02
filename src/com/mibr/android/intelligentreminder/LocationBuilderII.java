package com.mibr.android.intelligentreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationBuilderII {
	private Hashtable<String,IRLocation> _dbExistingLocationNames=null;
	private Hashtable<String,Long> _dbExistingCompanyNames=null;
	private String _latitude;
	private String _longitude;
	private String _name;
	private String _address="";
	private Context _ctx;
	private String _company="";
	public java.util.ArrayList<String> _jdaddresses;
	public java.util.ArrayList<String> _jdlatitudes;
	public java.util.ArrayList<String> _jdlongitudes;
	public java.util.ArrayList<String> _jdzips;
	public static String getTextFromElement(Element elem,String name) {
		try {
			NodeList nl=elem.getElementsByTagName(name);
			Element resultaddress=(Element) nl.item(0);
			resultaddress.normalize();
			NodeList array=resultaddress.getChildNodes();
			return array.item(0).getNodeValue();
		} catch (Exception ee33d) {
			return "";
		}
	}

	public static Address deriveAddressFromElement(Element elem) {
		Address address=new Address(Locale.getDefault());
		String lat=getTextFromElement(elem,"Latitude");
		address.setLatitude(Double.valueOf(lat));
		address.setLongitude(Double.valueOf(getTextFromElement(elem,"Longitude")));
		address.setAddressLine(0, getTextFromElement(elem,"Address"));
		address.setAdminArea(getTextFromElement(elem,"State"));
		address.setLocality(getTextFromElement(elem,"City"));
		address.setCountryCode(getTextFromElement(elem,"Country"));
		return address;
	}
	public static List<Address> getFromLocationName(String address, Context ctx) throws Exception {
		String addressTextText="410 Williams St Denver CO 80209";
		List<Address> addressList=null;
		try {
			addressTextText=address;
			addressTextText=addressTextText.replace("\n"," ");
			int x=4;
			if(x==3) throw new Exception("Try other method");
			Geocoder g=new Geocoder(ctx);
			addressList = g.getFromLocationName(addressTextText,5);
			if(addressList==null || addressList.size()==0) {
				throw new Exception("Try other method");
			}
			return addressList;
		} catch (Exception e) {
	    	addressList=new ArrayList<Address>();
	    	address=URLEncoder.encode(address,"UTF-8");
	        String url="http://maps.googleapis.com/maps/api/geocode/json?address="+address+"&sensor=true";
	        URL u = new URL(url);
	        HttpURLConnection conn=(HttpURLConnection)u.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.connect();
	        OutputStream out=conn.getOutputStream();
	        PrintWriter pw=new PrintWriter(out);
	        pw.close();
	        InputStream is = conn.getInputStream();
	        InputStreamReader is2=new InputStreamReader(is);
	        BufferedReader reader = new BufferedReader(is2);
	        StringBuilder sb = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	        is.close();
	        String json = sb.toString();
	        JSONObject jObj=new JSONObject(json);
	        JSONArray results=jObj.getJSONArray("results");
	        for(int i = 0; i < results.length(); i++){
	        	JSONObject addressObject=results.getJSONObject(i); 
	        	JSONArray addressComponents=addressObject.getJSONArray("address_components");
	       		String formattedAddress=addressObject.getString("formatted_address");
	       		JSONObject geometry=addressObject.getJSONObject("geometry");
	        	JSONObject location=geometry.getJSONObject("location");
	        	String lat=location.getString("lat");
	        	String lng=location.getString("lng");
	        	/* All I need is a string representation of the address, along with its longitude and latitude, so
	        	 * I'm not going to bother breaking into city, state, zip components. 
	        	*/
	            Address address2=new Address(Locale.getDefault());
	            address2.setLatitude(Double.valueOf(lat));
	            address2.setLongitude(Double.valueOf(lng));
	            address2.setAddressLine(0, formattedAddress);
	            addressList.add(address2);
	        }
	        try {is2.close();} catch (Exception eieiee) {}
	        return addressList;

		}
	}
	public Boolean setAddress(String address) {
		if(address.toLowerCase().trim().equals("fred")) {
			address="121 S Eaton St Denver CO 80226";
		}
		Boolean retValue=false;
//		Geocoder g=new Geocoder(_ctx);
		List<Address> addressList=null;
		try {
			String addressTextText=address;
			addressTextText=addressTextText.replace("\n"," ");
//			addressList = g.getFromLocationName(addressTextText,5);
			addressList=getFromLocationName(addressTextText, _ctx);
			if(addressList.size()>0) {
				setLongitude(String.valueOf(addressList.get(0).getLongitude()));
				setLatitude(String.valueOf(addressList.get(0).getLatitude()));
				_address=address;
				retValue=true;
			}
		} catch (Exception eeee) {}
		return retValue;
	}
	public void setLatitude(String lat) {
		_latitude=lat;
	}
	public void setLongitude(String lon) {
		_longitude=lon;
	}
	public void setName(String name) {
		_name=name.trim();
	}
	public Boolean doesLocationAlreadyExist() {
		Boolean foundIt=false;
		Enumeration<String> e=_dbExistingLocationNames.keys();
		while(e.hasMoreElements()) {
			if(e.nextElement().toLowerCase().trim().equals(_name.toLowerCase().trim())) {
				foundIt=true;
				break;
			}
		}
		if(!foundIt) {
			Enumeration<String> e2=_dbExistingCompanyNames.keys();
			while(e2.hasMoreElements()) {
				if(e2.nextElement().toLowerCase().trim().equals(_name.toLowerCase().trim())) {
					foundIt=true;
					break;
				}
			}
		}
		return foundIt;
	}
	public LocationBuilderII(String name2, Context ctx) {
		_ctx=ctx;
		_name=name2.trim();
		_dbExistingLocationNames=new Hashtable<String,IRLocation>();
		_dbExistingCompanyNames=new Hashtable<String,Long>();
		Cursor curses=null;
		curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld1(2);
		while(curses.moveToNext()) {
			long sseq=curses.getInt(curses.getColumnIndex("SortSequence"));
			String name=curses.getString(curses.getColumnIndex("name"));
			long id=curses.getInt(curses.getColumnIndex("_id"));
			String phoneId=curses.getString(curses.getColumnIndex("phoneid"));
			long contactid=curses.getLong(curses.getColumnIndex("contactid"));
			if(sseq!=1) {
				_dbExistingLocationNames.put(name, new IRLocation(name, id, phoneId,contactid));
			} else {
				_dbExistingCompanyNames.put(name, new Long(id));
			}
		}
		curses.close();
	}
	private INeedDbAdapter mDbAdapter=null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(_ctx);
		}
		return mDbAdapter;
	}
	public void flush() {
		int mnotdx=500;
		int ctr=1;
		if(_jdaddresses==null) {
			getDbAdapter().createLocation(_name, _address,_latitude,_longitude,_company,mnotdx,INeedToo.mSingleton.getPhoneId(),null);
		} else {
			for(int i=0;i<_jdaddresses.size();i++) {
				getDbAdapter().createLocation(_name/*per bill+" ("+String.valueOf(ctr)+")"*/, _jdaddresses.get(i),_jdlatitudes.get(i),_jdlongitudes.get(i),_name,mnotdx,INeedToo.mSingleton.getPhoneId(),null);
				ctr++;
			}
		}
	}
	public Boolean searchInternet(Context ctx, LocationManager locationManager) {
		Boolean retValue=false;
		try {
			List<Address> addresses=null;
			BusinessFinder bf=new BusinessFinder(ctx,locationManager);
			String jdCity=null;
			String jdState=null;
			String jdZip=null;
			String jdLatitude=null;
			String jdLongitude=null;
			Location loc=bf.getLastKnownLocation();
			if(loc==null) {
				addresses=bf.getCurrentAddress();
				if(addresses!=null && addresses.size()>0) {
					jdCity=addresses.get(0).getLocality();
					jdState=addresses.get(0).getAdminArea();
					jdZip=addresses.get(0).getPostalCode();
				}
			} else { 
				jdLatitude=String.valueOf(loc.getLatitude());
				jdLongitude=String.valueOf(loc.getLongitude());
			}
			ArrayList<ArrayList<String>> businesses= bf.businessLocation(_name, jdCity, jdState, jdZip, jdLatitude,jdLongitude);
			java.util.ArrayList<String> jdaddresses=new java.util.ArrayList<String>();
			java.util.ArrayList<String> jdlatitudes=new java.util.ArrayList<String>();
			java.util.ArrayList<String> jdlongitudes=new java.util.ArrayList<String>();
			java.util.ArrayList<String> jdzips=new java.util.ArrayList<String>();
			if(businesses.size()>0) {
				retValue=true;
				Boolean gotone=false;
				Iterator it=businesses.iterator();
				while (it.hasNext()) {
					ArrayList<String> al=(ArrayList<String>)it.next();
					jdaddresses.add(al.get(0) + " " + al.get(1)+ " " + al.get(2));
					jdlatitudes.add(al.get(3));
					jdlongitudes.add(al.get(4));
				}
				_jdaddresses=jdaddresses;
				_jdlatitudes=jdlatitudes;
				_jdlongitudes=jdlongitudes;
				_jdzips=jdzips;
			} else {
			}
		} catch (Exception eieio) {
			int i=3;
			int j=i;
		}
		return retValue;
	}
}

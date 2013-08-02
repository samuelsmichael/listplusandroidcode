package com.mibr.android.intelligentreminder;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

public class BusinessFinder {
	private static final String BASE_YAHOO="http://local.yahooapis.com/LocalSearchService/V3/localSearch?appid=yDSMLAbV34EUyy1AJrHKqbb1gL4A4xvchBWqr4MaNharntRqZTcCfm5Qs.ugfgTyrdoe4eoGxpM-&results=15&radius=50&sort=distance&query=";
	private static final String BASE_BING="http://api.search.live.net/xml.aspx?Appid=331AA259005BADEDD6B97257A503081075F8FC03&sources=phonebook&radius=50&count=10&sortby=distance&query=";
	private Context _ctx;
	private LocationManager mLocationManager;
	public Location getLastKnownLocation() {
		Location retValue=null;
		retValue=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(retValue==null) {
			retValue=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		return retValue;
	}
	public BusinessFinder(Context ctx, LocationManager locationManager) {
		_ctx=ctx;
		mLocationManager=locationManager;
	}
	private String doBingCityStateZip(String city, String state, String zip) {
		String tii="";
		if(INeedToo.isNothingNot(city)) {
			tii+="+"+buildNearStuff(city);
		}
		if(INeedToo.isNothingNot(state)) {
			tii+="+"+state;
		}
		if(INeedToo.isNothingNot(zip)) {
			tii+="+"+zip;
		}
		return tii;
	}
	
	
	public ArrayList<ArrayList<String>> businessLocation(String business, String city, 
			String state, String zip, String lat, String lon) {
		HttpURLConnection conn=null;
		try {
			Hashtable<String,String> alreadyDone=new Hashtable<String,String>();
			ArrayList<ArrayList<String>> jdLocations=new ArrayList<ArrayList<String>>();
			String yahooURL=BASE_YAHOO+business+"&zip="+buildNearStuff(zip)+"&city="+buildNearStuff(city)+
				"&state="+buildNearStuff(state); 
			if(lat!=null && lat!="") {
				yahooURL+="&latitude="+buildNearStuff(lat);
			}
			if(lon!=null && lon!="") {
				yahooURL+="&longitude="+buildNearStuff(lon);
			}
			yahooURL = yahooURL.replace(" ", "%20");
			URL u = new URL(yahooURL); 
			conn=(HttpURLConnection)u.openConnection();
			conn.setRequestMethod("POST"); 
			conn.setDoOutput(true);
			conn.setDoInput(true); 
			conn.connect(); 
			OutputStream out=conn.getOutputStream();
			PrintWriter pw=new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream(); 
			
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc=db.parse(is);
			doc.getDocumentElement().normalize();
			Element rootElement=doc.getDocumentElement();
			String stotalResultsReturned=rootElement.getAttribute("totalResultsReturned");
			int totalResultsReturned=Integer.parseInt(stotalResultsReturned);
			NodeList nodeList=rootElement.getChildNodes();
			if(totalResultsReturned>0) {
				for(int i=1;i<=totalResultsReturned;i++) {
					Element elem=(Element) nodeList.item(i);
					formatateValues(jdLocations, elem, alreadyDone);
				}
				try {is.close();} catch (Exception eieiee) {}
				return jdLocations;
			} else {
				try {is.close();} catch (Exception eieiee) {}
				return null;
			}
		} catch (Exception ee) {
			return null;
		} finally {
			try {conn.disconnect();} catch (Exception ee) {}
		}
	}
	private void formatateValues(ArrayList<ArrayList<String>> jdLocations, Element elem, Hashtable<String,String> alreadyDone) {
		try {
			String id=elem.getAttribute("id");
			ArrayList<String> al=new ArrayList<String>();
			String sAddress=getTextFromElement(elem,"Address");
			if(alreadyDone.get(sAddress)==null) {
				alreadyDone.put(sAddress, "");
				al.add(sAddress);
				al.add(getTextFromElement(elem,"City"));
				al.add(getTextFromElement(elem,"State"));
				al.add(getTextFromElement(elem,"Latitude"));
				al.add(getTextFromElement(elem,"Longitude"));
				jdLocations.add(al);
			} 
		} catch (Exception eiei) {
			int x=3;
		}
	}
	private String getTextFromElement(Element elem,String name) {
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
	private String buildNearStuff(String item) {
		if(item==null) {
			item="";
		}
		String weirdStuff="";
		String buildThis="";
		if(item.trim().length()>0) {
			buildThis+=weirdStuff+item.trim().replaceAll(" ", "%20");
			weirdStuff="%20";
		}
		return buildThis;
	}
	
	public List<Address> getCurrentAddress() throws Exception {
		if(INeedToo.mSingleton.getLatestLocationBB()!=null) {
			Location location=INeedToo.mSingleton.getLatestLocationBB();			
			return INeedToo.mSingleton.getAddressList(location);
		} else {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			String bestProvider = mLocationManager.getBestProvider(criteria, false);
			if(mLocationManager.isProviderEnabled(bestProvider)) {
				Location location=mLocationManager.getLastKnownLocation(bestProvider);
				if(location==null) { // try agin
					location=mLocationManager.getLastKnownLocation(bestProvider);
				}
				if(location!=null) {
					List<Address> addressList=null;
					Geocoder g=new Geocoder(_ctx);
					addressList=g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
					if(addressList==null || addressList.size()==0) { // try agin again
						addressList=g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);						
					}
					return addressList;
				} else {
/* TODO what to do when can't find location					ArrayList<Address> addressList=new ArrayList<Address>();
					android.location.Address address = new android.location.Address(null);
					address.setPostalCode("63348");
					address.setLocality("Foristell");
					address.setAdminArea("MO");
					addressList.add(address);
					return addressList;
					//Toast.makeText(getApplicationContext(), getString(R.string.error_unabletogetlocation), Toast.LENGTH_LONG).show();
	*/			}
			} else {
				//Toast.makeText(getApplicationContext(), getString(R.string.error_locationprovidernotactive), Toast.LENGTH_LONG).show();
			}
		}
		return null;
	}
}

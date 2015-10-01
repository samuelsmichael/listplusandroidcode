package com.mibr.android.intelligentreminder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
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
	public static final String GOOGLE_API_KEY = "AIzaSyCiLgS6F41lPD-aHj7yMycVDv38gb1vd2o";
	public static final int LIMIT_NBR_ACCESSES = 10;
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
	
	public void getBusinessesNamedNear(String business,String lat,String lon ,
			ArrayList<Address> runningList, String nextPageToken,
			int nbrOfAccessesLeft, int nthAccessStartingAt1) throws Exception {
		business=business.trim();
		String rememberLastGoodNextPageToken=null;
		int countRetries=0;
		int priorRunningListCount=0;
		while (true) {
			nextPageToken=getBusinessesNamedNearPrivate(business,lat,lon,runningList,nextPageToken,nbrOfAccessesLeft,nthAccessStartingAt1);
			nbrOfAccessesLeft--;
			nthAccessStartingAt1++;
			/* for some reason, Google returns nothing if called to quickly with "nextpage"*/
			if(priorRunningListCount==runningList.size() &&
					runningList.size()>0 && rememberLastGoodNextPageToken!=null && countRetries<2) {
				countRetries++;
				nextPageToken=rememberLastGoodNextPageToken;
				Thread.currentThread().sleep(2500);
				

			} else {
				countRetries=0;
				if(nextPageToken!=null) {
					rememberLastGoodNextPageToken=nextPageToken;
				}
				priorRunningListCount=runningList.size();
				if(nextPageToken==null || nbrOfAccessesLeft<=0) {
					break;
				}
			}
		}
	}
	
	private String getBusinessesNamedNearPrivate(String business,String lat, String lon,
			ArrayList<Address> runningList, String nextPageToken,
			int nbrOfAccessesLeft, int nthAccessStartingAt1) throws Exception { 
	
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
				+ lat
				+ ","
				+ lon
				+ "&pagetoken="
				+ (nextPageToken == null ? "" : nextPageToken)
				+ "&radius=38000&keyword="+URLEncoder.encode(business)+"&sensor=true&key="
				+ GOOGLE_API_KEY;
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		PrintWriter pw = new PrintWriter(out);
		pw.close();
		InputStream is = conn.getInputStream();
		InputStreamReader is2 = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(is2);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		conn.disconnect();
		String json = sb.toString();
		JSONObject jObj = new JSONObject(json);
		nextPageToken = null;
		try {
			String status = jObj.getString("status");
			if (status == "OVER_QUERY_LIMIT") {
				Thread.currentThread().sleep(1000);
				nbrOfAccessesLeft--;
				nthAccessStartingAt1++;
				return nextPageToken;
			}
		} catch (Exception e) {
		}
		try {
			nextPageToken = jObj.getString("next_page_token");
		} catch (Exception e) {
		}
		JSONArray results = jObj.getJSONArray("results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject geometry = ((JSONObject) results.get(i))
					.getJSONObject("geometry");
			JSONObject location2 = geometry.getJSONObject("location");
			String lat2 = location2.getString("lat");
			String lng = location2.getString("lng");
			String name = ((JSONObject) results.get(i)).getString("name");
			String vicinity = ((JSONObject)results.get(i)).getString("vicinity");
			Address address = new Address(Locale.getDefault());
			address.setLatitude(Double.valueOf(lat2));
			address.setLongitude(Double.valueOf(lng));
			address.setAddressLine(0, name+"-"+vicinity);
			runningList.add(address);
		}
		if (nextPageToken != null && nbrOfAccessesLeft > 1) {
	//		Thread.currentThread().sleep(2500);
			return nextPageToken;
		} else {
			return null;
		} 
	}
	
	public ArrayList<ArrayList<String>> businessLocation(String business, String city, 
			String state, String zip, String lat, String lon) {
		
		try {
			if (lat==null||lon==null)  {
				List<Address> addressList = null;
				Geocoder g = new Geocoder(_ctx);
				addressList = g.getFromLocationName(
						(""+(city==null?"":city)+" "+(state==null?"":state)+" "+(zip==null?"":zip)).trim()
							, 4);
				lat=String.valueOf(addressList.get(0).getLatitude());
				lon=String.valueOf(addressList.get(0).getLongitude());
			}
			ArrayList<Address> trainStationAddresses = new ArrayList<Address>();
			getBusinessesNamedNear(business, lat, lon, trainStationAddresses,
					null, LIMIT_NBR_ACCESSES,1);
			ArrayList<ArrayList<String>> jdAlAlStr=new ArrayList<ArrayList<String>>();
			for(int i=0;i<trainStationAddresses.size();i++) {
				Address a=trainStationAddresses.get(i);
				ArrayList<String> alStr=new ArrayList<String>();
				alStr.add(a.getAddressLine(0));
				alStr.add("");
				alStr.add("");
				alStr.add(String.valueOf(a.getLatitude()));
				alStr.add(String.valueOf(a.getLongitude()));
				jdAlAlStr.add(alStr);
			}
			return jdAlAlStr;
		} catch (Exception e) {
			return null;
		}
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

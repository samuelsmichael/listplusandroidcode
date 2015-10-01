package com.mibr.android.intelligentreminder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mibr.android.intelligentreminder.R;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Config;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NeedMap extends Activity implements GoogleMap.InfoWindowAdapter {

	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1002;
	private GoogleMap mMap = null;
	private MapFragment mMapFragment;
	private INeedDbAdapter mDbAdapter=null;

	
	public class LocationViewOnMap {
		private long mLocationId;
		private ArrayList<NeedViewOnMap> mNeeds=new ArrayList<NeedViewOnMap>();
		private String mName;
		private LatLng mLatLng;
		public LocationViewOnMap (long locationId,String name, double latitude, double longitude) {
			mLocationId=locationId;
			mLatLng=new LatLng(latitude, longitude);
			mName=name;
		}
		public void addNeed(NeedViewOnMap nvop) {
			mNeeds.add(nvop);
		}
		public LatLng getPosition() {
			return mLatLng;
		}
		public NeedViewOnMap getNeedAtPosition(int position) {
			return mNeeds.get(position);
		}
		
	    public String getName() { return mName; }
	}
	public class NeedViewOnMap  {
		private int[] mBmps;
		public int getIntAt(int i) {
			return mBmps[i];
		}
		public NeedViewOnMap(int[] ia) {
			mBmps=ia;
		}
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_need_map);
		buildDesriptors();
	}

    @Override
	protected void onResume() {
		super.onResume();

		if (checkPlayServices()) {
			onResumeSetupMapIfNeeded();
		}
	}
	
    private int[][] buildDesriptors() {
	    int[][] bmda=new int[10][5];
    	bmda[0][0] = R.drawable.mm_20_redm50;
    	bmda[0][1] = R.drawable.mm_20_redm25;
    	bmda[0][2] = R.drawable.mm_20_red;
    	bmda[0][3] = R.drawable.mm_20_redp25;
    	bmda[0][4] = R.drawable.mm_20_redp50;
    	bmda[1][0] = R.drawable.mm_20_yellowm50;
    	bmda[1][1] = R.drawable.mm_20_yellowm25;
    	bmda[1][2] = R.drawable.mm_20_yellow;
    	bmda[1][3] = R.drawable.mm_20_yellowp25;
    	bmda[1][4] = R.drawable.mm_20_yellowp50;
    	bmda[2][0] = R.drawable.mm_20_greenm50;
    	bmda[2][1] = R.drawable.mm_20_greenm25;
    	bmda[2][2] = R.drawable.mm_20_green;
    	bmda[2][3] = R.drawable.mm_20_greenp25;
    	bmda[2][4] = R.drawable.mm_20_greenp50;
    	bmda[3][0] = R.drawable.mm_20_bluem50;
    	bmda[3][1] = R.drawable.mm_20_bluem25;
    	bmda[3][2] = R.drawable.mm_20_blue;
    	bmda[3][3] = R.drawable.mm_20_bluep25;
    	bmda[3][4] = R.drawable.mm_20_bluep50;
    	bmda[4][0] = R.drawable.mm_20_blackm50;
    	bmda[4][1] = R.drawable.mm_20_blackm25;
    	bmda[4][2] = R.drawable.mm_20_black;
    	bmda[4][3] = R.drawable.mm_20_blackp25;
    	bmda[4][4] = R.drawable.mm_20_blackp50;
    	bmda[5][0] = R.drawable.mm_20_whitem50;
    	bmda[5][1] = R.drawable.mm_20_whitem25;
    	bmda[5][2] = R.drawable.mm_20_white;
    	bmda[5][3] = R.drawable.mm_20_whitep25;
    	bmda[5][4] = R.drawable.mm_20_whitep50;
    	bmda[6][0] = R.drawable.mm_20_brownm50;
    	bmda[6][1] = R.drawable.mm_20_brownm25;
    	bmda[6][2] = R.drawable.mm_20_brown;
    	bmda[6][3] = R.drawable.mm_20_brownp25;
    	bmda[6][4] = R.drawable.mm_20_brownp50;
    	bmda[7][0] = R.drawable.mm_20_graym50;
    	bmda[7][1] = R.drawable.mm_20_graym25;
    	bmda[7][2] = R.drawable.mm_20_gray;
    	bmda[7][3] = R.drawable.mm_20_grayp25;
    	bmda[7][4] = R.drawable.mm_20_grayp50;
    	bmda[8][0] = R.drawable.mm_20_purplem50;
    	bmda[8][1] = R.drawable.mm_20_purplem25;
    	bmda[8][2] = R.drawable.mm_20_purple;
    	bmda[8][3] = R.drawable.mm_20_purplep25;
    	bmda[8][4] = R.drawable.mm_20_purplep50;
    	bmda[9][0] = R.drawable.mm_20_orangem50;
    	bmda[9][1] = R.drawable.mm_20_orangem25;
    	bmda[9][2] = R.drawable.mm_20_orange;
    	bmda[9][3] = R.drawable.mm_20_orangep25;
    	bmda[9][4] = R.drawable.mm_20_orangep50;   
    	return bmda;
    }
	
	
	
	private void animateCamera(double latitude, double longitude) {
		
	   LatLng latLng = new LatLng(latitude, longitude);
	   new ShowTheMap(this, latLng, mMap,buildDesriptors()).execute();  

	}
	
	/*
	 * Do a check to see if the map object (mMap) has already been created. If
	 * not, then we have to prepare for displaying it, and that involves also
	 * "finding initial location" -- which is our location -- and fetching all
	 * of the rail stations in the vicinity. The reason I do this onResume is
	 * that onResume gets called even after a popped up dialog box is present
	 * and then is closed ... which would be the case, say, if the user didn't
	 * have Play Services installed, and was presented with the dialog to
	 * install it.
	 */
	private void onResumeSetupMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
					R.id.map2);
			mMap = mMapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
				// mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
				mMap.setMyLocationEnabled(true);
			}
		} else {
			// mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
			mMap.setMyLocationEnabled(true);
		}
		if(mMap!=null) {
			if(INeedToo.InitialLocation!=null) {
				animateCamera(INeedToo.InitialLocation.getLatitude(),INeedToo.InitialLocation.getLongitude());
			}
			mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
				public void onMapLongClick(LatLng point) {
				}
			});
		}
	}
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter==null) {
			mDbAdapter=new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_RECOVER_PLAY_SERVICES:
			if (resultCode == RESULT_CANCELED) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this,
						AlertDialog.THEME_TRADITIONAL);
				builder.setTitle("Application Alert")
						.setMessage(
								"This application won't run without Google Play Services installed")
						.setPositiveButton("Okay",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	/*
	 * The Play Services API has to be installed on the user's machine in order
	 * for the map to show up. I check for it here, and if it isn't present,
	 * then a dialog is presented to the user allowing him to fetch it.
	 */
	private boolean checkPlayServices() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				showPlaystoreAPIErrorDialog(status);
			}
			return false;
		}
		return true;
	}

	public void showPlaystoreAPIErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}	

	
	public class ShowTheMap extends AsyncTask<Void, Void, Void> {

	    private final Activity activity;
	    private final LatLng local;
	    private final GoogleMap map;
	    private int[][] bmda=new int[10][5];


	    public ShowTheMap(Activity activity, LatLng local,
	            GoogleMap map, int[][] b) {
	                this.activity = activity;
	                this.local = local;
	                this.map = map;
	                bmda=b;
	    }

	    protected Void doInBackground(Void... params) {
	        return null;
	    }
	    
	    private int findBitmapDescriptor(
	    		long needId,Hashtable<Long,Integer> indexByNeedId) {
	    	Integer index=null;
	    	try {
	    		index=indexByNeedId.get(Long.valueOf(needId));
	    	} catch (Exception ee3) {}
	    	if(index==null) {
	    		index=locationsIndex;
	    		indexByNeedId.put(Long.valueOf(needId), locationsIndex);
	    		locationsIndex++;
	    		if(locationsIndex>9) {
	    			locationsIndex=0;
	    		}
	    	}
	    	return index;
	    }	    
	    
	    private int locationsIndex=0;
	    protected void onPostExecute(Void result) {
	    	CameraPosition position = new CameraPosition.Builder()
	        .target(local)
	        .bearing(0)
	        .tilt(0)
	        .zoom(11)
	        .build();
	    	
	    	ArrayList<LocationViewOnMap> viewsOnMap=new ArrayList<LocationViewOnMap>();
	    	locationsIndex=0;
	    	Hashtable<Long,Integer> indexByNeedId=new Hashtable<Long,Integer>();
	    	CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
	        map.animateCamera(update);
	        Cursor cu = getDbAdapter().fetchActiveLocations();
	        String saveCompanyName=null;
	        int cnt1=cu.getCount();
	        if (cnt1>0) {
	        	while (cu.moveToNext()) {
					double latitude=Double.valueOf(cu.getString(cu.getColumnIndex("latitude")));
					double longitude=Double.valueOf(cu.getString(cu.getColumnIndex("longitude")));
					long locationId=Long.valueOf(cu.getLong(cu.getColumnIndex("locationid")));
					String nameLocation=cu.getString(cu.getColumnIndex("nameLocation"));
					String companyName=cu.getString(cu.getColumnIndex("company"));
					if(companyName == null) {
						companyName=nameLocation;
					}
					
					saveCompanyName=companyName;
					LocationViewOnMap lvom=new LocationViewOnMap(
							locationId,nameLocation, latitude, longitude);
					viewsOnMap.add(lvom);
					Cursor curses = getDbAdapter().fetchNeedsForALocation(locationId);
					int cnt2=curses.getCount();
					LatLng latlng = new LatLng(latitude, longitude);
			        int startingIndex=2-((cnt2-1)/2);
			        if(startingIndex<0) {
			        	startingIndex=2;
			        }
					if (cnt2 > 0) {		
						StringBuilder sb=new StringBuilder();
						sb.append("Needs:\n");
						String nl="";
						long needId;
						BitmapDescriptor bmd=null;
						while (curses.moveToNext()) {
							int tiltIndex=1;
							needId=curses.getLong(curses.getColumnIndex("_id"));
							sb.append(nl+curses.getString(curses.getColumnIndex("Name")));
							nl="\n";
							int jdLocationIndex=findBitmapDescriptor(needId,indexByNeedId);
							int[] ia=new int[5];
							for (int m=0;m<5;m++) {
								ia[m]=bmda[jdLocationIndex][startingIndex];
								startingIndex++;
								if(startingIndex>4) {
									startingIndex=0;
								}
							}
							NeedViewOnMap nvom=new NeedViewOnMap(ia);
							
							lvom.addNeed(nvom);
							/*
							mMap.addMarker(new MarkerOptions().position(latlng)
									.title(nameLocation)
									.snippet(String.valueOf(locationId))
									.icon(bmd));
							*/
						}
					}
					curses.close();
				}
	        	for(int i=0;i<viewsOnMap.size();i++) {
	        		Bitmap bm=null;
	        		int nth=0;
	        		for (int j=0;j<viewsOnMap.get(i).mNeeds.size();j++) {
	        			bm=buildBitmap(viewsOnMap.get(i).getNeedAtPosition(j),bm,nth);
	        			mMap.addMarker(new MarkerOptions()
	        				.position(viewsOnMap.get(i).getPosition())
	        				.title(viewsOnMap.get(i).getName())
	        				.snippet(String.valueOf(viewsOnMap.get(i).mLocationId))
	        				.icon(BitmapDescriptorFactory.fromBitmap(bm))
	        			);
	        			nth++;
	        			if(nth>4) {
	        				nth=0;
	        			}
	        		}
	        	}
	        	cu.close();
	        	map.setInfoWindowAdapter((GoogleMap.InfoWindowAdapter)activity);
//	        	clusterer.updateMarkers();
			}
	    }
		private Bitmap buildBitmap(NeedViewOnMap nvom, Bitmap bm, int nth) {
			bm=blendBitmaps(bm,BitmapFactory.decodeResource(getResources(), nvom.getIntAt(nth)));
			return bm;
		}
		private Bitmap blendBitmaps(Bitmap bm1, Bitmap bm2) {
			if(bm1==null && bm2 != null) {
				return bm2;
			} else {
				if(bm2==null && bm1!=null) {
					return bm1;
				} else {
					if(bm2==null && bm1==null) {
						return null;
					} else {
						Bitmap.Config config = bm1.getConfig();
						if(config == null){
						config = Bitmap.Config.ARGB_8888;
						}
						   
						Bitmap newBitmap = Bitmap.createBitmap(bm1.getWidth(), bm1.getHeight(), config);
						Canvas newCanvas = new Canvas(newBitmap);

						newCanvas.drawBitmap(bm1, 0, 0, null);
						Paint paint = new Paint();
						paint.setAlpha(128);
						newCanvas.drawBitmap(bm2, 0, 0, paint);
						return newBitmap;
					}
				}
			}
		}
	}

	@Override
	public View getInfoWindow (Marker arg0) {
		return null;
	}

	
	@Override
	public View getInfoContents (Marker arg0) {
		final View markerView=getLayoutInflater().inflate(R.layout.markerview, null);
		long locationId=Long.valueOf(arg0.getSnippet());
		Cursor curLoc=getDbAdapter().fetchLocation(locationId);
		ListView listView=null;
		if(curLoc.getCount()>0) {
			curLoc.moveToFirst();
			String company=curLoc.getString(curLoc.getColumnIndex(INeedDbAdapter.KEY_COMPANY));
			String name=curLoc.getString(curLoc.getColumnIndex(INeedDbAdapter.KEY_NAME));
			String address=curLoc.getString(curLoc.getColumnIndex(INeedDbAdapter.KEY_ADDRESS));
			curLoc.close();
			if(arg0.getTitle()!=null) {
				((TextView)markerView.findViewById(R.id.tvCompanyName)).setText(arg0.getTitle().trim());
			} else {
				((TextView)markerView.findViewById(R.id.tvCompanyName)).setText("");				
			}
			if(address!=null) {
				((TextView)markerView.findViewById(R.id.tvLocationNameOrAddress)).setText(address.trim());
			} else {
				((TextView)markerView.findViewById(R.id.tvLocationNameOrAddress)).setText("");
			}
			listView=(ListView)markerView.findViewById(R.id.listView1);
			Cursor curses=getDbAdapter().fetchNeedsForALocation(locationId);
			ArrayList<MapViewDetailData> mpdd=new ArrayList<MapViewDetailData>(); 
			if(curses.getCount()>0) {
				while(curses.moveToNext()) {
					mpdd.add(new MapViewDetailData(
							curses.getString(curses.getColumnIndex("Name")),
							curses.getString(curses.getColumnIndex("Description")),
							curses.getLong(curses.getColumnIndex("_id"))
							));
				}
			}
			curses.close();
			listView.setAdapter(new MapViewAdapter(mpdd,this));
		}
		return markerView;
	}	
	class MapViewDetailData {
		private String _name;
		private String _description;
		private long _id;
		public MapViewDetailData(String name, String description, long id) {
			_name=name;
			_description=description;
			_id=id;
		}
		public long getId() {
			return _id;
		}
		public String getName() {
			return _name;
		}
		public String getDescription() {
			return _description;
		}
	}
	class MapViewAdapter extends BaseAdapter {
		private ArrayList<MapViewDetailData> _list;
		private Activity mActivity;
		public MapViewAdapter(ArrayList<MapViewDetailData> list, Activity activity) {
			_list=list;
			mActivity=activity;
		}
		@Override
		public int getCount() {
			return _list.size();
		}

		@Override
		public Object getItem(int position) {
			return _list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return _list.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = mActivity.getLayoutInflater().inflate (R.layout.markerviewdetail, parent,false);
			if(_list.get(position).getName()!=null) {
				((TextView)view.findViewById(R.id.tvMarkerName)).setText(_list.get(position).getName().trim());
			} else {
				((TextView)view.findViewById(R.id.tvMarkerName)).setText("");
			}
			if(_list.get(position).getDescription()!=null) {
				((TextView)view.findViewById(R.id.tvMarkerDescription)).setText(_list.get(position).getDescription().trim());
			} else {
				((TextView)view.findViewById(R.id.tvMarkerDescription)).setText("");
			}
			return view;
		}
	
	}
}

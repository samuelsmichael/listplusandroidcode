package com.mibr.android.intelligentreminder;

import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mibr.android.intelligentreminder.R;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LocationsViaMap extends Activity {

	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1002;
	private GoogleMap mMap = null;
	private MapFragment mMapFragment;
	private double _longitude;
	private double _latitude;
	private INeedDbAdapter mDbAdapter=null;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locations_via_map);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			_latitude = extras.getDouble("lat",40);
			_longitude = extras.getDouble("long",-103);
		}
		final Button cancel = (Button) findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(LocationsViaMap.this, INeedToo.class);
				intent.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
				startActivity(intent);
				LocationsViaMap.this.finish();
			}
		});				
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (checkPlayServices()) {
			onResumeSetupMapIfNeeded();
		}
	}
	
	private void doLocationUpdate(int notdx,Address address, String locationName) {
			getDbAdapter().createLocation(
					locationName,
					address.getAddressLine(0), 
					String.valueOf(address.getLatitude()), 
					String.valueOf(address.getLongitude()), 
					"",
					notdx,
					INeedToo.mSingleton.getPhoneId(),null);
			Toast.makeText(getApplicationContext(), getString(R.string.msg_locationcreated), Toast.LENGTH_LONG).show();
		}
	
	
	private void animateCamera(double latitude, double longitude) {
		
	   LatLng latLng = new LatLng(latitude, longitude);
	   new ShowTheMap(this, latLng, mMap).execute();  
	   
/*	   
       CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
       mMap.animateCamera(cameraUpdate);		
		
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(new LatLng(latitude,
						longitude), 11f,0f,0f)));*/
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
			animateCamera(_latitude,_longitude);
			mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
				public void onMapLongClick(LatLng point) {
					Address useThisAddress=new Address(Locale.getDefault());
					useThisAddress.setLatitude(point.latitude);
					useThisAddress.setLongitude(point.longitude);
					List<Address> addresses =null;
					try {
					Geocoder g = new Geocoder(LocationsViaMap.this);
					addresses = g.getFromLocation(
							(double) point.latitude,
							(double) point.longitude, 2);
					} catch (Exception e) {}
					if (addresses != null && addresses.size() > 0) {
						useThisAddress = addresses.get(0);
					}
					new NickNameDialog(LocationsViaMap.this,useThisAddress,null).show();
				}
			});
		}
	}
	private boolean thisLocationNameAlreadyExists(String name, String phoneid) {
		return this.getDbAdapter().thisLocationAlreadyExists(name,phoneid);
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

	    public ShowTheMap(Activity activity, LatLng local,
	            GoogleMap map) {
	                this.activity = activity;
	                this.local = local;
	                this.map = map;

	    }

	    protected Void doInBackground(Void... params) {
	        return null;
	    }

	    protected void onPostExecute(Void result) {
	    	CameraPosition position = new CameraPosition.Builder()
	        .target(local)
	        .bearing(0)
	        .tilt(0)
	        .zoom(11)
	        .build();
	        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
	        map.animateCamera(update);
	    }	
	}
	
	public static class NickNameDialog {
		private Activity mActivity;
		private Address mAddress;
		private String mPriorName;

		private NickNameDialog() {
			super();
		}

		public NickNameDialog(Activity activity, Address address, String priorName) {
			mActivity = activity;
			mAddress = address;
			mPriorName=priorName;
		}

		public void show() {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			LayoutInflater inflater = mActivity.getLayoutInflater();

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			final View view=inflater.inflate(R.layout.nickname, null);
			final EditText nickName = (EditText) view.findViewById(R.id.nickname);
			if(mPriorName!=null) {
				nickName.setText(mPriorName);
			} else {
				nickName.setText(mAddress.getAddressLine(0));
			}
			
			builder.setView(view);
			builder.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String locationName=mAddress.getAddressLine(0);
				final String nameText=nickName.getText().toString();
							if( ((LocationsViaMap)mActivity).thisLocationNameAlreadyExists(nameText,INeedToo.mSingleton.getPhoneId())) {
								AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
								builder.setMessage(R.string.error_locationnamealreadyexists)
								.setCancelable(false)
								.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int id) {
										new NickNameDialog(mActivity,mAddress,nameText).show();
									}
								});
								AlertDialog alert=builder.create();	
								alert.show();
								return;
							}
							
							if(!nickName.getText().toString().trim().equals("")) {
								locationName=nickName.getText().toString();
							}
							
							int notdx=200;
							try {
								notdx=Integer.valueOf(mActivity.getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
										Preferences.MODE_PRIVATE).getString("LocationDistance", "200"));
								((LocationsViaMap)mActivity).doLocationUpdate(notdx,mAddress,locationName);
							} catch (Exception e) {		
							
							}
							
							Intent intent = new Intent(mActivity, INeedToo.class);
							intent.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
							mActivity.startActivity(intent);
							mActivity.finish();
						}
					}).setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent intent = new Intent(mActivity, INeedToo.class);
							intent.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
							mActivity.startActivity(intent);
							mActivity.finish();
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
}

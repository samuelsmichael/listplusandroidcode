package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.CustomExceptionHandler;
import com.mibr.android.intelligentreminder.INeedDbAdapter;
import com.mibr.android.intelligentreminder.INeedToo;
import com.mibr.android.intelligentreminder.INeedWebService;
import com.mibr.android.intelligentreminder.LocationView;
import com.mibr.android.intelligentreminder.NeedView;
import com.mibr.android.intelligentreminder.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


 

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LocationFindBusinesses extends Activity {
	
    public static ProgressDialog progressDialog;  

	public static final int DIALOG_ADDLOCATIONS=312;
	private static final int DIALOG_ISTRIALVERSION = 3;
	private INeedDbAdapter mDbAdapter=null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter==null) {
			mDbAdapter=new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
	private ArrayList<ArrayList<String>> mBackFromGoogle=null;
	private Button location_button_getbusinesses;
	private AutoCompleteTextView actv;
	private Button location_button_getbusinesses_cancel;
	private EditText jdCity;
	private EditText jdState;
	private EditText jdZip;
	private LocationManager mLocationManager=null;
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}
	private void doOnCreate(Bundle savedInstanceState) {
		try {
			if(progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		} catch (Exception ee) {
			
		}
		setContentView(R.layout.locations_from_business_search);
		setTitle(INeedToo.mSingleton.getHeading());
		location_button_getbusinesses=(Button)findViewById(R.id.location_button_getbusinesses);
		location_button_getbusinesses_cancel=(Button)findViewById(R.id.location_button_getbusinesses_cancel);
		actv=(AutoCompleteTextView)findViewById(R.id.location_business);
		jdCity=(EditText)findViewById(R.id.location_near_city);
		jdState=(EditText)findViewById(R.id.location_near_state);
		jdZip=(EditText)findViewById(R.id.location_near_zip);
		ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,getDbAdapter().fetchAllCompanies());
		actv.setAdapter(ad);
		jdCity.setText("");
		jdState.setText("");
		jdZip.setText("");
		mBackFromGoogle = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("Google");
		if(mBackFromGoogle != null) {
			actv.setText(getIntent().getStringExtra("Business"));
			jdCity.setText(getIntent().getStringExtra("City"));
			jdState.setText(getIntent().getStringExtra("State"));
			jdZip.setText(getIntent().getStringExtra("Zip"));
			showDialog(DIALOG_ADDLOCATIONS);
		} else {
			try {
				List<Address> addressList = new BusinessFinder(LocationFindBusinesses.this,getLocationManager()).getCurrentAddress();
				if(addressList.size()>0) {
					jdCity.setText(addressList.get(0).getLocality());
					jdState.setText(addressList.get(0).getAdminArea());
					jdZip.setText(addressList.get(0).getPostalCode());
				}
			} catch (Exception eieio) {
				int bkhere=3;
				int bkthere=bkhere;
			}
		}

		location_button_getbusinesses_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i2=new Intent(LocationFindBusinesses.this, INeedToo.class);
				i2.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
				startActivity(i2);    
				LocationFindBusinesses.this.finish();				
			}
		});

		location_button_getbusinesses.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if(actv.getText().toString().trim().length()==0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(LocationFindBusinesses.this);
					builder.setMessage("Business name must not be blank for this function.")
					.setCancelable(false)
					.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							actv.requestFocus();
						}
					});
					AlertDialog alert=builder.create();	
					alert.show();
					return;
				} 

				if(jdZip.getText().toString().trim().length()==0) {
					if(jdState.getText().toString().trim().length()==0 || jdCity.getText().toString().trim().length()==0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(LocationFindBusinesses.this);
						builder.setMessage("Please supply a zip or the city and state.")
						.setCancelable(false)
						.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								jdCity.requestFocus();
							}
						});
						AlertDialog alert=builder.create();	
						alert.show();
						return;
					}
				} 
				new Thread(new Runnable() {
					public void run() {
				        LocationFindBusinesses.this.progressDialog = ProgressDialog.show(  
				                LocationFindBusinesses.this,  
				                "Please wait...finding businesses", // title  
				                "Finding instances of "+actv.getText().toString()+ " in the vicinity requested", // message  
				                true // indeterminate  
				        );  
						
					}
				}).run();
//		        LocationFindBusinesses.this.progressDialog = ProgressDialog.show(  
	//	                LocationFindBusinesses.this,  
		//                "Please wait...finding businesses", // title  
		  //              "Finding instances of "+actv.getText().toString()+ " in the vicinity requested", // message  
		    //            true // indeterminate  
		      //  );  

	//			Toast.makeText(getApplicationContext(), "Finding " + actv.getText().toString()+"'s. Please standby.", Toast.LENGTH_LONG).show();

				final Timer jdTimer = new Timer("GettingBusiness");
				jdTimer.schedule(new TimerTask() {
					public void run() {
						Intent intent=new Intent(LocationFindBusinesses.this,INeedWebService.class)
						.setAction("BusinessLocation")
						.putExtra("Business", actv.getText().toString())
						.putExtra("City", jdCity.getText().toString())
						.putExtra("State", jdState.getText().toString())
						.putExtra("Zip",jdZip.getText().toString());
						startService(intent);
						jdTimer.cancel();
					}
				}, 500, 1000 * 60 * 10);
			}
		});

	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			boolean gotLocationManager=getIntent().getBooleanExtra("gotLocationManager", false);
			if(!gotLocationManager) {
				getLocationManager();
				Intent intent=new Intent(this,LocationFindBusinesses.class)
				.putExtra("gotLocationManager", true);
				startActivity(intent);
				this.finish();
			}
			doOnCreate(savedInstanceState);
		} catch (Exception ee) {
			CustomExceptionHandler.logException(ee,this);
		}
	}
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog dialog=null;
		switch (dialogId) {
		case DIALOG_ADDLOCATIONS:
			java.util.List<String> addresses=new java.util.ArrayList<String>();
			java.util.List<String> latitudes=new java.util.ArrayList<String>();
			java.util.List<String> longitudes=new java.util.ArrayList<String>();
			ArrayList<ArrayList<String>> data=mBackFromGoogle; // I'm too lazy to change data's to mBackFromGoogle's
			if(data.size()>0) {
				Boolean gotone=false;
				Iterator it=data.iterator();
				while (it.hasNext()) {
					ArrayList<String> al=(ArrayList<String>)it.next();
					addresses.add(al.get(0) + " " + al.get(1)+ " " + al.get(2));
					latitudes.add(al.get(3));
					longitudes.add(al.get(4));
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose one or more " + actv.getText().toString()+"'s");
				final String[] jdbusinesses=new String[addresses.size()];
				final String[] jdlatitudes=new String[latitudes.size()];
				final String[] jdlongitudes=new String[longitudes.size()];
				final boolean[] didit=new boolean[addresses.size()];
				final long[] ids=new long[addresses.size()];
				boolean[] jd=new boolean[addresses.size()];
				for(int c=0;c<addresses.size();c++) {
					jdbusinesses[c]=addresses.get(c).replaceAll("\"", "");
					jd[c]=false;
					didit[c]=false;
				}
				for(int c=0;c<latitudes.size();c++) {
					jdlatitudes[c]=latitudes.get(c);
				}
				for(int c=0;c<longitudes.size();c++) {
					jdlongitudes[c]=longitudes.get(c);
				}

				builder.setMultiChoiceItems(jdbusinesses, jd,
						new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						if (isChecked) {
							if(!didit[which]) {
								didit[which]=true;
								String [] addr=jdbusinesses[which].split(" ");
								String nameo=actv.getText().toString();
								if(true || !INeedToo.mSingleton.isTestVersion()) {
									ids[which]=LocationFindBusinesses.this.getDbAdapter().createLocation(nameo, 
											jdbusinesses[which], jdlatitudes[which],
											jdlongitudes[which], actv.getText().toString(), 200, INeedToo.mSingleton.getPhoneId(),null);
								}
							}
						} else {
							if(true || !INeedToo.mSingleton.isTestVersion()) {
								LocationFindBusinesses.this.getDbAdapter().deleteLocation(ids[which]);
							}
							didit[which]=false;
						}
					}
				});
				builder.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						LocationFindBusinesses.this.dismissDialog(DIALOG_ADDLOCATIONS);
						if(false && INeedToo.mSingleton.isTestVersion()) {
							showDialog(DIALOG_ISTRIALVERSION);
						} else {
							Intent i2 = new Intent(LocationFindBusinesses.this, INeedToo.class);
							i2.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
							startActivity(i2);
							LocationFindBusinesses.this.finish();
						}
					}
				});
				dialog = builder.create();
				dialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
					}
				});
			}
			break;
		case DIALOG_ISTRIALVERSION:
			AlertDialog.Builder builder3 = new AlertDialog.Builder(
					LocationFindBusinesses.this);
			builder3.setMessage(R.string.trial_version_alert2);
			builder3.setCancelable(false);
			builder3.setPositiveButton(R.string.msg_register, new DialogInterface.OnClickListener() {
				public void onClick(
						DialogInterface dialog,
						int id) {
					Intent i4=new Intent(LocationFindBusinesses.this,INeedToPay.class);
					startActivity(i4);
					LocationFindBusinesses.this.finish();
				}
			});
			builder3.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i2 = new Intent(LocationFindBusinesses.this, INeedToo.class);
					i2.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
					startActivity(i2);
					LocationFindBusinesses.this.finish();
				}
			});

			dialog = builder3.create();
			break;
		default:
			return null;
		}
		return dialog;
	}
}


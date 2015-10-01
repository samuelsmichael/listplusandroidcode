package com.mibr.android.intelligentreminder;



import com.mibr.android.intelligentreminder.R;

import java.io.IOException;
import java.util.List;



import android.net.*;
import android.provider.Contacts.People;
import android.provider.Contacts;
import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LocationView extends ListActivity  implements LocationListener {
	private EditText nameText;
	private TextView latitudeText;
	private TextView longitudeText;
	private EditText addressText;
	private AutoCompleteTextView actv;
	private EditText locationAddress;
	private Button deriveFromAddress;
	private Button deriveFromContacts;
	private int mnotdx;
	private INeedDbAdapter mDbAdapter=null;
	private long mRowId=-1l;
	private boolean mIsANewNeed;
	private long mButComeBackToNeedViewForId;
	private static final int DIALOG_NOTIFICATIONDX = 1;
	private static final int DIALOG_CONTACTLIST = 2;
	private static final int DIALOG_ISTRIALVERSION = 3;
	private String mPhoneID;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private static LocationManager mLocationManager=null;

	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter==null) {
			mDbAdapter=new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
	@Override 
	public void onDestroy() {
		if(mDbAdapter!=null) {
			mDbAdapter.close();
		}
		super.onDestroy();
	}
	private static Boolean geocoderIsInitialized=false;
	private void initializeLocationStuff() {
		if(!geocoderIsInitialized) {
			geocoderIsInitialized=true;
//			Geocoder g=new Geocoder(LocationView.this);
		
	//		try {
		//		g.getFromLocationName("121 S Eaton St; Lakewood CO 80226",5);
			//} catch (IOException e1) {
//				int x=3;
	//			int y=x;
		//	} finally {
			//}
			try {
				LocationManager locationManager = getLocationManager();
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String bestProvider = locationManager.getBestProvider(criteria, false);
				if(locationManager.isProviderEnabled(bestProvider)) {
					locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
					Location loc=locationManager.getLastKnownLocation(bestProvider);
					int x=4;
					int y=x;
				}
			} catch (Exception ee3) {
				int x=3;
				int y=x;
			}
		}
	}
	private void doOnCreate(Bundle savedInstanceState) {
		if(!geocoderIsInitialized) {
//			Thread initializeStuff = new Thread(new Runnable() {
//				public void run() {
//					initializeLocationStuff();
//				}
//			});
//			initializeStuff.setPriority(Thread.MIN_PRIORITY);
//			initializeStuff.start();
			initializeLocationStuff();
		}
		
		mButComeBackToNeedViewForId=getIntent().getLongExtra("ButComeBackToNeedViewForId", -1);
		setContentView(R.layout.location_view3);
		setTitle("Location Identification");
		nameText=(EditText)findViewById(R.id.location_title_id);
		latitudeText = (TextView)findViewById(R.id.location_view_latitude);
		longitudeText = (TextView)findViewById(R.id.location_view_longitude);
		addressText=(EditText)findViewById(R.id.location_address_id);
		actv=(AutoCompleteTextView)findViewById(R.id.location_company);
		locationAddress=(EditText)findViewById(R.id.location_address_id);
		deriveFromAddress=(Button)findViewById(R.id.ButtonFillFromAddress);
		deriveFromContacts=(Button)findViewById(R.id.ButtonContacts);
		nameText.requestFocus();

		Button deriveFromCurrentLocation = (Button) findViewById(R.id.buttonfileefromcurrentlocation);
		Button cancel = (Button) findViewById(R.id.location_button_cancel);
		Button confirm = (Button)findViewById(R.id.location_button_confirm);
		try {
			mPhoneID=INeedToo.mSingleton.getPhoneId();
		} catch (Exception ee) {
			mPhoneID="unknown";
		}


		Bundle extras = getIntent().getExtras();
		mIsANewNeed = true;
		if (extras != null) {
			mRowId = extras.getLong(INeedDbAdapter.KEY_ROWID,-1l);
			Cursor curses=getDbAdapter().fetchLocation(mRowId);
			if(curses.getCount()>0) {
				mIsANewNeed=false;
				nameText.setText(curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_NAME)));
				try {
					actv.setText(curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_COMPANY)));
					actv.setTextColor(Color.BLACK);
				} catch (Exception e) {
					actv.setText(R.string.location_optional);
				}
				latitudeText.setText(curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_LATITUDE)));
				longitudeText.setText(curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_LONGITUDE)));
				addressText.setText(curses.getString(curses.getColumnIndex(INeedDbAdapter.KEY_ADDRESS)));
				addressText.setTextColor(Color.BLACK);
				mPhoneID=curses.getString(curses.getColumnIndex("phoneid"));
				Cursor curses2 = getDbAdapter().fetchNeedsForLocation(mRowId);
				startManagingCursor(curses2);
				String[] from = new String[] { "Name", "Description" };
				int[] to = new int[] { R.id.location_view_list_needs_need, R.id.location_view_list_needs_item };
				setListAdapter(mSimpleCursorAdapter = new SimpleCursorAdapter(this,
						R.layout.location_view_list_needs, curses2, from, to));

			} else {
				actv.setText(R.string.location_optional);
				locationAddress.setText(R.string.location_address_hint);
			}
			curses.close();
			//TODO We gotta clean up our cursors.  Curses!
		} else {
			actv.setText(R.string.location_optional);
			locationAddress.setText(R.string.location_address_hint);
		}

		actv.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View view, boolean b) {
				if(b) {
					String str=((AutoCompleteTextView)view).getText().toString().trim();
					if(str.equals(getString(R.string.location_optional))) {
						((AutoCompleteTextView)view).setText("");
						((AutoCompleteTextView)view).setTextColor(Color.WHITE);
					}
				}
			}
		}
		);

		locationAddress.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View view, boolean b) {
				if(b) {
					String str=((EditText)view).getText().toString().trim();
					if(str.equals(getString(R.string.location_address_hint))) {
						((EditText)view).setText("");
						((EditText)view).setTextColor(Color.WHITE);
					}
				}
			}
		}
		);

		ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,getDbAdapter().fetchAllCompanies());
		actv.setAdapter(ad);
		/*				new ArrayAdapter<String>(this,something,);
				new SimpleCursorAdapter(
						getApplicationContext(),
						R.layout.location_view2,getDbAdapter().fetchAllCompanies(),				
						new String[] {INeedDbAdapter.KEY_NAME,INeedDbAdapter.KEY_ADDRESS,INeedDbAdapter.KEY_COMPANY},
						new int[] {R.id.location_title_id,R.id.location_address_id,R.id.location_company}
						);
						)
		 */
		/*
        ContentResolver content = getContentResolver();
        Cursor cursor = content.query(Contacts.People.CONTENT_URI,
                PEOPLE_PROJECTION, null, null, Contacts.People.DEFAULT_SORT_ORDER);
        AutoComplete4.ContactListAdapter adapter =
                new AutoComplete4.ContactListAdapter(this, cursor);

        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.edit);
        textView.setAdapter(adapter);		
		 */

		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i2;
				if(LocationView.this.mButComeBackToNeedViewForId!=-1) {
					i2 = new Intent(LocationView.this, NeedView.class);
					i2.putExtra("needid", LocationView.this.mButComeBackToNeedViewForId);
					i2.putExtra("CominFromLocationView", true);
					i2.putExtra("locationid", LocationView.this.mRowId);
				} else {
					i2 = new Intent(LocationView.this, INeedToo.class);
					i2.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
				}
				startActivity(i2);    

				LocationView.this.finish();
			}

		});

		deriveFromContacts.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				if(INeedToo.mSingleton.isTestVersion()) {
	//				showDialog(DIALOG_ISTRIALVERSION);
		//		} else {
					showDialog(DIALOG_CONTACTLIST);
			//	}
			}

		});

		deriveFromAddress.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				Geocoder g=new Geocoder(LocationView.this);
				List<Address> addressList=null;
				try {
					String addressTextText=addressText.getText().toString();
					addressTextText=addressTextText.replace("\n"," ");
	//				addressList = g.getFromLocationName(addressTextText,5);
					addressList=LocationBuilderII.getFromLocationName(addressTextText, getApplicationContext());
					if(addressList.size()>0) {
						if(addressList.size()>1) {
							final String[] addresses=new String[addressList.size()];
							final double [] latitudes=new double[addressList.size()];
							final double [] longitudes=new double[addressList.size()];
							for(int i=0;i<addressList.size();i++) {

								int maxIndex=addressList.get(i).getMaxAddressLineIndex();
								StringBuilder sb=new StringBuilder();
								String nl="";
								for(int c=0;c<maxIndex;c++) {
									sb.append(nl+addressList.get(i).getAddressLine(c));
									nl="\n";
								}
								addresses[i]=sb.toString();
								latitudes[i]=addressList.get(i).getLatitude();
								longitudes[i]=addressList.get(i).getLongitude();
							}
							AlertDialog.Builder builder=new AlertDialog.Builder(LocationView.this);
							builder.setTitle("More than one location found. Pick one.");
							builder.setItems(addresses, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String latitude=String.valueOf(latitudes[which]);
									String longitude = String.valueOf(longitudes[which]);
									latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
									longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));									
								}
							});
							AlertDialog alert=builder.create();	
							alert.show();
						} else {
							Address a=addressList.get(0);
							String latitude=String.valueOf(a.getLatitude());
							String longitude = String.valueOf(a.getLongitude());
							latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
							longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));
						}
					} else {
						
						
						Toast.makeText(getApplicationContext(), "No locations found for this address. Sometimes this occurs when the system isn't through initializing. Try just pushing the search button again, and if this doesn't work, try refining your address.", Toast.LENGTH_LONG).show();											
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Problem tyring to get address. Msg:"+e.getMessage() + ". This problem often goes away by pushing the button again.", Toast.LENGTH_LONG).show();					
				}
			}
		});
		confirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if(INeedToo.isNothing(nameText.getText().toString().trim())) {
					AlertDialog.Builder builder = new AlertDialog.Builder(LocationView.this);
					builder.setMessage(R.string.error_namemustnotbeempty)
					.setCancelable(false)
					.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							nameText.requestFocus();
						}
					});
					AlertDialog alert=builder.create();	
					alert.show();
				} else {
					if(LocationView.this.mIsANewNeed && thisLocationNameAlreadyExists(nameText.getText().toString(),mPhoneID)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(LocationView.this);
						builder.setMessage(R.string.error_locationnamealreadyexists)
						.setCancelable(false)
						.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								nameText.requestFocus();
							}
						});
						AlertDialog alert=builder.create();	
						alert.show();
					} else {
						mnotdx=200;
						try {
							mnotdx=Integer.valueOf(getSharedPreferences(INeedToo.PREFERENCES_LOCATION,
									Preferences.MODE_PRIVATE).getString("LocationDistance", "200"));
						} catch (Exception e) {

						}
						if(INeedToo.isNothing(latitudeText.getText().toString()) ||
								INeedToo.isNothing(longitudeText.getText().toString())) {
							AlertDialog.Builder builder = new AlertDialog.Builder(LocationView.this);
							builder.setMessage(R.string.warning_nogpsinfo)
							.setCancelable(false)
							.setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									doLocationUpdate(mnotdx);
								}
							})
							.setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							AlertDialog alert=builder.create();	
							alert.show();
						}  else {
							doLocationUpdate(mnotdx);
						}
					}
				}
			}

		});

		deriveFromCurrentLocation.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				LocationManager locationManager = getLocationManager();
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String bestProvider = locationManager.getBestProvider(criteria, false);
				if(locationManager.isProviderEnabled(bestProvider)) {
					Location location=locationManager.getLastKnownLocation(bestProvider);
					if(location!=null) {
						String latitude=String.valueOf(location.getLatitude());
						String longitude = String.valueOf(location.getLongitude());
						latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
						longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.error_unabletogetlocation), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.error_locationprovidernotactive), Toast.LENGTH_LONG).show();
				}
			}

		});
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if(mLocationManager==null) {
				getLocationManager();

				long id=getIntent().getLongExtra(INeedDbAdapter.KEY_ROWID, 998877);
				long needid=getIntent().getLongExtra("ButComeBackToNeedViewForId",998877);
				Intent intent=new Intent(this,LocationView.class);
				if(id!=998877) {
					intent.putExtra(INeedDbAdapter.KEY_ROWID, id);
				}
				if(needid!=998877) {
					intent.putExtra("ButComeBackToNeedViewForId", needid);
				}

				startActivity(intent);
				this.finish();
			}
			doOnCreate(savedInstanceState);
			/*bbhbb 2011-03-26*/		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));
		} catch (Exception ee) {
			CustomExceptionHandler.logException(ee,this);
		}
	}
	private void doLocationUpdate(int notdx) {
		if(LocationView.this.mRowId!=-1) {
			getDbAdapter().updateLocation(LocationView.this.mRowId,nameText.getText().toString().trim(), INeedToo.worryAboutWatermark(addressText.getText().toString().trim(),getString(R.string.location_address_hint)), 
					latitudeText.getText().toString().trim(), longitudeText.getText().toString().trim(), INeedToo.worryAboutWatermark(actv.getText().toString().trim(),getString(R.string.location_optional)),
					notdx,mPhoneID);
			Toast.makeText(getApplicationContext(), getString(R.string.msg_locationupdated), Toast.LENGTH_LONG).show();
		} else {
			LocationView.this.mRowId=getDbAdapter().createLocation(
					nameText.getText().toString().trim(), 
					INeedToo.worryAboutWatermark(addressText.getText().toString().trim(),getString(R.string.location_address_hint)), 
					latitudeText.getText().toString().trim(), 
					longitudeText.getText().toString().trim(), 
					INeedToo.worryAboutWatermark(actv.getText().toString().trim(),
					getString(R.string.location_optional)),
					notdx,
					mPhoneID,null);
			Toast.makeText(getApplicationContext(), getString(R.string.msg_locationcreated), Toast.LENGTH_LONG).show();
		}
		Intent i2;
		if(LocationView.this.mButComeBackToNeedViewForId!=-1) {
			i2 = new Intent(LocationView.this, NeedView.class);
			i2.putExtra("needid", LocationView.this.mButComeBackToNeedViewForId);
			i2.putExtra("CominFromLocationView", true);
			i2.putExtra("locationid", LocationView.this.mRowId);
		} else {
			i2 = new Intent(LocationView.this, INeedToo.class);
			i2.putExtra("initialtabindex", (int)1).putExtra("doingcompany", (String)null);
		}
		startActivity(i2);    
		LocationView.this.finish();

	}
	private boolean thisLocationNameAlreadyExists(String name, String phoneid) {
		return this.getDbAdapter().thisLocationAlreadyExists(name,phoneid);
	}
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog dialog=null;
		switch (dialogId) {
		case DIALOG_ISTRIALVERSION:
			AlertDialog.Builder builder3 = new AlertDialog.Builder(
					LocationView.this);
			builder3.setMessage(R.string.trial_version_alert2);
			builder3.setCancelable(false);
			builder3.setPositiveButton(R.string.msg_register, new DialogInterface.OnClickListener() {
				public void onClick(
						DialogInterface dialog,
						int id) {
					Intent i4=new Intent(LocationView.this,INeedToPay.class);
					startActivity(i4);
				}
			});
			builder3.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			dialog = builder3.create();
			break;
		case DIALOG_CONTACTLIST:
			String[] projection= new String[] {
					People._ID,
					People.NAME,
			};
			Uri contacts=People.CONTENT_URI;
			Cursor managedCursor=managedQuery(contacts,
					projection,
					null,
					null,
					People.NAME+ " ASC");
			if(managedCursor.getCount()>0) {
				String contactNames[]=new String[managedCursor.getCount()];
				final String ids[]=new String[managedCursor.getCount()];
				int c=0;
				int j=0;
				while(managedCursor.moveToNext()) {
					String fTSExclamationPoint=managedCursor.getString(managedCursor.getColumnIndex(People.NAME));
					if(fTSExclamationPoint!=null) {
						contactNames[j]=fTSExclamationPoint;
						ids[j]=managedCursor.getString(managedCursor.getColumnIndex(People._ID));
						INeedToo.mSingleton.log("ContactName["+String.valueOf(j)+"]="+contactNames[j],1);
						INeedToo.mSingleton.log("ids["+String.valueOf(j)+"]="+ids[j],1);
						j++;
					}
					c++;
				}
				final String[] contactNames2=new String[j];
				final String[] ids2=new String[j];
				for(int k=0;k<j;k++) {
					contactNames2[k]=contactNames[k].trim();
					ids2[k]=ids[k].trim();
				}
				AlertDialog.Builder builder2=new AlertDialog.Builder(this);
				builder2.setTitle("Pick a Contact.");
				builder2.setItems(contactNames2, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						nameText.setText(contactNames2[which]);
						String id=String.valueOf(ids2[which]);

						String selection=Contacts.ContactMethods.PERSON_ID+"=? AND " +Contacts.ContactMethods.KIND + "=?"; 
						INeedToo.mSingleton.log("Getting contact for id:"+id,1);
						String args[]=new String[] {id,Integer.toString(Contacts.KIND_POSTAL)};
						Uri contacts=Contacts.ContactMethods.CONTENT_URI;

						Cursor managedCursor2=managedQuery(contacts,
								null,
								selection,
								args,
								null);
						String[] sa=managedCursor2.getColumnNames();
						if(managedCursor2.getCount()>0) {
							managedCursor2.moveToNext();
							for(int m=0;m<20;m++) {
								String bubba="nunce";
								String bubba2="nunce";
								try {
									bubba=managedCursor2.getString(m);
								} catch (Exception ee33) {}
								try {
									bubba2=sa[m];
								} catch (Exception ee33) {}
								INeedToo.mSingleton.log("Field:"+String.valueOf(m)+bubba,1);
								INeedToo.mSingleton.log("Colname:"+String.valueOf(m)+bubba2,1);
							}
							String address=managedCursor2.getString(managedCursor2.getColumnIndex("data"));
//							nameText.setText(managedCursor2.getString(4));
							if(INeedToo.isNothingNot(address)) {
								try {
									addressText.setText(address);
//									Geocoder g=new Geocoder(LocationView.this);
									List<Address> addressList=null;
//									addressList = g.getFromLocationName(address,5);
									address=address.replace("\n"," ");
									addressList=LocationBuilderII.getFromLocationName(address, getApplicationContext());
									if(addressList.size()>1) {
										final String[] addresses=new String[addressList.size()];
										final double [] latitudes=new double[addressList.size()];
										final double [] longitudes=new double[addressList.size()];
										for(int i=0;i<addressList.size();i++) {

											int maxIndex=addressList.get(i).getMaxAddressLineIndex();
											StringBuilder sb=new StringBuilder();
											String nl="";
											for(int c=0;c<maxIndex;c++) {
												sb.append(nl+addressList.get(i).getAddressLine(c));
												nl="\n";
											}
											addresses[i]=sb.toString();
											latitudes[i]=addressList.get(i).getLatitude();
											longitudes[i]=addressList.get(i).getLongitude();
										}
										AlertDialog.Builder builder=new AlertDialog.Builder(LocationView.this);
										builder.setTitle("More than one location found. Pick one.");
										builder.setItems(addresses, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												String latitude=String.valueOf(latitudes[which]);
												String longitude = String.valueOf(longitudes[which]);
												latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
												longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));									
											}
										});
										AlertDialog alert=builder.create();	
										alert.show();
									} else {
										if(addressList.size()==1) {
											Address a=addressList.get(0);
											String latitude=String.valueOf(a.getLatitude());
											String longitude = String.valueOf(a.getLongitude());
											latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
											longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));
										} else {
											Toast.makeText(getApplicationContext(), "No GPS locations found for address associated with this contact.", Toast.LENGTH_LONG).show();											
										}
									}
								} catch (Exception ee33) {
									Toast.makeText(getApplicationContext(), "Failure trying to get address. Msg: "+ee33.getMessage(), Toast.LENGTH_LONG).show();											
								}
							} else {
								Toast.makeText(getApplicationContext(), "This contact doesn't have an address.", Toast.LENGTH_LONG).show();											
							}
						} else {
							Toast.makeText(getApplicationContext(), "This contact doesn't have an address.", Toast.LENGTH_LONG).show();											
						}
						//String latitude=String.valueOf(latitudes[which]);
						//String longitude = String.valueOf(longitudes[which]);
						//latitudeText.setText(INeedToo.leftMostCharacters(latitude,10));
						//longitudeText.setText(INeedToo.leftMostCharacters(longitude,10));									
					} 
				});
				dialog=builder2.create();	
			}
			break;

		default:
			return null;
		}
		return dialog;
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLocationChanged(Location location) {
		getLocationManager().removeUpdates(this);
	}
	@Override
	public void onProviderEnabled(String str) {
		
	}
}

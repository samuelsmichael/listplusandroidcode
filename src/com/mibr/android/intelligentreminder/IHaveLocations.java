package com.mibr.android.intelligentreminder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.mibr.android.intelligentreminder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class IHaveLocations extends ListActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener{
	private INeedDbAdapter mDbAdapter = null;
	private SimpleCursorAdapter mSimpleCursorAdapter = null;
	private String _doingLocationCompany = null;
	private TextView _titleOfCompany = null;
	private Button _addLocationManually=null;
	private Button _addLocationViaVoice=null;
	private Button _addLocationViaMap=null;
	private Button buttonAd;
	private TextToSpeech mTts;
	private static final int MY_DATA_CHECK_CODE = 32229000;
	private static final int SAYLOCATIONNAME = 32220001;
	private static final int SAYOKAYORRETRYLOCATIONNAME = 322200002;
	private static final int SAYCOORDINATESMETHOD = 32200003;
	private static final int RETRYGETLOCATIONFROMHERE = 32200004;
	private static final int SAYLOCATIONADDRESS = 32200005;
	private static final int SAYOKAYORRETRYLOCATIONADDRESS = 32200006;
	private static final int SAYOKAYORRETRYLOCATIONBUSINESSNAME = 32200008;
	private Spinner sortOrder=null;
	private Location _remLoc;
	
    private String remPrompt;
    private LocationBuilderII mLocBuilder=null;
    private String savLocationName;
    private String savLocationAddress;
    private String savLocationBusinessName;
	private int cntRetries=0;
    private LocationClient mLocationClient;
	private static int jdCurrentSortOrder=0;
	private LocationManager getLocationManager() {
		return INeedToo.mSingleton.getLocationManager();
	}
	private void doPrompting(String youSaid, String prompt, int sendResults) {
		 remPrompt=prompt;
		if(youSaid!=null) {
			 
			mTts.speak(youSaid, TextToSpeech.QUEUE_ADD, null);
			 
		} 
		if (prompt != null) {
			HashMap<String,String> hm = new HashMap<String,String>();
			hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(sendResults));
			mTts.speak(prompt, TextToSpeech.QUEUE_ADD, hm);
		//	now....1. Initialize LocationManager in IHaveNeeds a la IHaveLocations
		//	2.  Trace the writing of need.  Where is it exceptioning?
							
		}
	}
	
	
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	@Override
	public void onDestroy() {
		if (mDbAdapter != null) {
			try {
			mDbAdapter.close();
			} catch (Exception eee) {
				int bkhere=3;
				int bkthere=bkhere;
			}
		}
		if(mTts!=null) {
			try {
				mTts.stop();
				mTts.shutdown();
				mTts=null;
			} catch (Exception eeee) {}
		}
		super.onDestroy();
	}

	private int _klugeCntCursorItems;

	private void doOnCreate(Bundle savedInstanceState) {
        mLocationClient = new LocationClient(this, this, this);		
        mLocationClient.connect();

		if(INeedToo.mSingleton.doViewCount) {
			final Timer jdTimer = new Timer("ViewCountingLocations");
			jdTimer.schedule(new TimerTask() {
				public void run() {
					Thread thread=new Thread(new Runnable() {
						public void run() {
							try {
							Intent intent2 = new Intent(IHaveLocations.this, INeedWebService.class)
							.setAction("adview").putExtra("phoneid", INeedToo.mSingleton.getPhoneId()).putExtra("adnbr",2);
							startService(intent2);
							} catch (Exception ee3) {
								
							}
							finally {
								jdTimer.cancel();
							}
						}
					});
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.run();
				}
			}, 2000, 1000 * 60 * 10);
		}
		_rc=0;
		_klugeCntCursorItems=0;
		INeedToo.mSingleton.startListening();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			_doingLocationCompany = bundle.getString("doingcompany");
		}
		// if(INeedToo.mSingleton._forceNonCompany==true) {
		// _doingLocationCompany=null;
		// }
		INeedToo.mSingleton._forceNonCompany = false;
		Cursor curses = null;
		IRThingNeed.CurrentSort=IRThingNeed.SORT_NAME;
		Boolean didit=false;
		if(jdCurrentSortOrder==1) {
			Location loc=getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc==null) {
				loc=getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			if(loc!=null) {
				_remLoc=loc;
				didit=true;
				IRThingAbstractLocation.CurrentSort=IRThingNeed.SORT_LOCATIONPROXIMITY;
				if (_doingLocationCompany == null) {
					if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
						setContentView(R.layout.locations_list_v3trial);
					} else {
						setContentView(R.layout.locations_list_v3);
					}
					sortOrder=(Spinner)findViewById(R.id.sort_byl);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsNew1(loc);
		
				} else {
					if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
						setContentView(R.layout.locations_list_v3_companytrial);
					} else {
						setContentView(R.layout.locations_list_v3_company);
					}
					sortOrder=(Spinner)findViewById(R.id.sort_byl);
					_titleOfCompany = (TextView) findViewById(R.id.TextViewjdCompany);
					_titleOfCompany.setText("Locations for company "
							+ _doingLocationCompany);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsNew2(
							_doingLocationCompany,loc);
				}
				
			} else {
				didit=false;
			}
		}
		if(jdCurrentSortOrder==0 || !didit) {
			Boolean dodah2=false;
			IRThingAbstractLocation.CurrentSort=IRThingNeed.SORT_NAME;
			if(jdCurrentSortOrder!=0) {
				dodah2=true;
				jdCurrentSortOrder=0;
				Toast.makeText(getApplicationContext(),
						"Failed getting current location...reverting back to sort-by-name",
						Toast.LENGTH_SHORT).show();

			}
			if (_doingLocationCompany == null) {
				if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
					setContentView(R.layout.locations_list_v3trial);
				} else {
					setContentView(R.layout.locations_list_v3);
				}
				sortOrder=(Spinner)findViewById(R.id.sort_byl);
				curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld1(2);
	
			} else {
				if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
					setContentView(R.layout.locations_list_v3_companytrial);
				} else {
					setContentView(R.layout.locations_list_v3_company);
				}
				sortOrder=(Spinner)findViewById(R.id.sort_byl);
				_titleOfCompany = (TextView) findViewById(R.id.TextViewjdCompany);
				_titleOfCompany.setText("Locations for company "
						+ _doingLocationCompany);
				curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld2(
						_doingLocationCompany);
			}
			if(dodah2) {
				sortOrder.setSelection(0);
			}
		}
		if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
			buttonAd=(Button)findViewById(R.id.ButtonAd);
			try {
				buttonAd.setText(INeedToo.mSingleton.Ad2Text);
			} catch (Exception e) {
				buttonAd.setText("Press here to purchase the non-trial version of Intelligent Reminder ... only US$ 0.99");
			}
			buttonAd.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					buttonAd.setBackgroundColor(0xaafed148);
					return false;
				}
			});
			buttonAd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					buttonAd.setBackgroundDrawable(getResources().getDrawable(R.drawable.plan_48));
			        String uri="market://details?id=com.mibr.android.intelligentreminder";
					try {
						uri=INeedToo.mSingleton.Ad2URI;
					} catch (Exception ee33) {}
					Intent intent2 = new Intent(IHaveLocations.this, INeedWebService.class)
					.setAction("adpress").putExtra("adnbr", 2);
					startService(intent2);			        
			        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			        startActivity(ii3);
				}
			});
			buttonAd.requestFocus();
		}
		
/*		if(jdCurrentSortOrder==0) {
			
			if (_doingLocationCompany == null) {
				setContentView(R.layout.locations_list_v3);
				sortOrder=(Spinner)findViewById(R.id.sort_byl);
				curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld1();
	
			} else {
				setContentView(R.layout.locations_list_v3_company);
				sortOrder=(Spinner)findViewById(R.id.sort_byl);
				_titleOfCompany = (TextView) findViewById(R.id.TextViewjdCompany);
				_titleOfCompany.setText("Locations for company "
						+ _doingLocationCompany);
				curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld2(
						_doingLocationCompany);
			}
		} else {
			Location loc=getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc==null) {
				loc=getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			if(loc!=null) {
				IRThingNeed.CurrentSort=IRThingNeed.SORT_LOCATIONPROXIMITY;
				if (_doingLocationCompany == null) {
					setContentView(R.layout.locations_list_v3);
					sortOrder=(Spinner)findViewById(R.id.sort_byl);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsNew1(loc);
		
				} else {
					setContentView(R.layout.locations_list_v3_company);
					sortOrder=(Spinner)findViewById(R.id.sort_byl);
					_titleOfCompany = (TextView) findViewById(R.id.TextViewjdCompany);
					_titleOfCompany.setText("Locations for company "
							+ _doingLocationCompany);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsNew2(
							_doingLocationCompany,loc);
				}
				
			} else {
				if (_doingLocationCompany == null) {
					setContentView(R.layout.locations_list_v3);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld1();
		
				} else {
					setContentView(R.layout.locations_list_v3_company);
					_titleOfCompany = (TextView) findViewById(R.id.TextViewjdCompany);
					_titleOfCompany.setText("Locations for company "
							+ _doingLocationCompany);
					curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld2(
							_doingLocationCompany);
				}
			}
		}
*/		
		startManagingCursor(curses);
		if (curses.getCount() > 0) {
			final ListView lv = getListView();
			String[] from = new String[] { "phoneid", "name", "address" };
			int[] to = new int[] { R.id.locationlist_phoneid_name,
					R.id.locationlist_name, R.id.locationlist_address };
			if (_doingLocationCompany != null) {				
				setListAdapter(mSimpleCursorAdapter = new SimpleCursorAdapter(
						getApplicationContext(), R.layout.locations_list_list,
						curses, from, to) {
					@Override
					public void setViewText(TextView v, String text) {
						super.setViewText(v, convText(v, text));
					}
				});
			} else {
				mSimpleCursorAdapter = new MySimpleCursorAdapter(
						getApplicationContext(), R.layout.locations_list_list_company,
				curses, from, to,this);
				setListAdapter(mSimpleCursorAdapter);
				_klugeCntCursorItems=curses.getCount();
//				setListAdapter(mSimpleCursorAdapter = new SimpleCursorAdapter(
//						getApplicationContext(), R.layout.locations_list_list_company,
//						curses, from, to) {
//					@Override
//					public void setViewText(TextView v, String text) {
//						super.setViewText(v, convText(v, text));
//					}
//				});
			}
			lv.setAdapter(mSimpleCursorAdapter);
			registerForContextMenu(lv);
			/*
			 * If I can get the lv.post to work, this might work ... but it
			 * doesn't work lv.post(new Runnable() { public void run() { float y
			 * = 10/0; lv.setSelection(10); } }); /* /*works but doesn't solve
			 * my problem new Thread() { public void run() {
			 * lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			 * lv.setSelection(10); lv.postInvalidate(); } }.start();
			 */
			// setListAdapter(mSimpleCursorAdapter);
		}
		_addLocationManually = (Button) findViewById(R.id.ineed_createlocation);
		_addLocationViaVoice = (Button) findViewById(R.id.ineed_createlocationviavoice);
		_addLocationViaMap = (Button) findViewById(R.id.ineed_createlocationviamap);
		
		if(_addLocationManually != null) {
			_addLocationManually.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						Intent i = new Intent(IHaveLocations.this, LocationView.class);
						startActivity(i);
						IHaveLocations.this.finish();
					} catch (Exception e) {}
				}
			});
		}
		if(_addLocationViaVoice != null) {
			_addLocationViaVoice.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						doPrompting(null,"Say Location name",SAYLOCATIONNAME);
					} catch (Exception e) {}
				}
			});
		}
		if(_addLocationViaMap != null) {
			_addLocationViaMap.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						IHaveLocations.this.finish();
						Intent intent=new Intent(IHaveLocations.this,LocationsViaMap.class);
						intent.putExtra("lat",_remLoc.getLatitude()).
								putExtra("long",_remLoc.getLongitude());
						startActivity(intent);
						IHaveLocations.this.finish();
					} catch (Exception e) {}
				}
			});
		}		sortOrder.setSelection(jdCurrentSortOrder);
		sortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if(jdCurrentSortOrder!=position) {
					jdCurrentSortOrder=position;
		            Intent i2 = new Intent(IHaveLocations.this, INeedToo.class);
		            i2
		            	.putExtra("initialtabindex", (int)1)
		            	.putExtra(
							"doingcompany",
							IHaveLocations.this._doingLocationCompany);
		            startActivity(i2);    		
					IHaveLocations.this.finish();
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				int bff=4;
				
			}
		});
		
		
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent,MY_DATA_CHECK_CODE);
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		_remLoc=mLocationClient.getLastLocation();
        mLocationClient.disconnect();
	}


	@Override
	public void onDisconnected() {
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}		
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
					// success, create the TTS instance
					mTts = new TextToSpeech(this.getApplicationContext(), new OnInitListener(){
						@Override
						public void onInit(int arg0) {
							if(arg0==TextToSpeech.SUCCESS) {
								mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
									@Override
									public void onUtteranceCompleted(String utteranceId) {
										final String mud=utteranceId;
										final Timer jdTimer = new Timer("WaitingForHearingAids");
										jdTimer.schedule(new TimerTask() {
											public void run() {
											 	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
												intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
												intent.putExtra(RecognizerIntent.EXTRA_PROMPT, remPrompt + " ...");
												startActivityForResult(intent, Integer.valueOf(mud));	
												this.cancel();
											}
										}, INeedToo.DELAYPOSTTTS, 1000 * 60 * 10);
									}
								});
								}							
							}						
					});
					int x=2;
					int y=x;
				} else {
					// missing data, install it
					Intent installIntent = new Intent();
					installIntent.setAction(
					TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installIntent);
				}
		}		
		if (requestCode == SAYLOCATIONNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("cancel")) {
						break;
					} else {
						savLocationName=matches.get(i);
						if(savLocationName.equals("711")) {
							savLocationName="7-Eleven";
						}
						doPrompting("You said " + savLocationName,"Say Okay or Retry",SAYOKAYORRETRYLOCATIONNAME);
						break;
					}
				}
			}
		}		
		if (requestCode == SAYOKAYORRETRYLOCATIONNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("okay")) {
						mLocBuilder=new LocationBuilderII(savLocationName,getApplicationContext());
						if(mLocBuilder.doesLocationAlreadyExist()) {
							doPrompting("This location already exists, please try again.","Say Location name",SAYLOCATIONNAME);
						} else {
							doPrompting(null,"Choose location type.  Say. Here, Address, or Business",SAYCOORDINATESMETHOD);
						}
					} else {
						doPrompting(null,"Say Location name",SAYLOCATIONNAME);
					}
					break;
				}
			}							
		}
		if (requestCode == SAYCOORDINATESMETHOD) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("here")) {
						Location location=INeedToo.mSingleton.doLocationManager();
						if(location!=null) {
							mLocBuilder.setLatitude(String.valueOf(location.getLatitude()));
							mLocBuilder.setLongitude(String.valueOf(location.getLongitude()));
							mLocBuilder.flush();
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);
							refreshList();
						} else {
							doPrompting("Failed getting current location.  Do you want to try again?  Say Yes or No.",
									"Say Yes or No",RETRYGETLOCATIONFROMHERE);
						}
					} else {
						if(matches.get(i).toLowerCase().trim().equals("business")) {
							Boolean didit=false;
							doPrompting("Searching the internet",null,-1);
							mLocBuilder.searchInternet(this,getLocationManager());
						} else {
							if(matches.get(i).toLowerCase().trim().equals("address")) {
								doPrompting(null,"Say Location address",SAYLOCATIONADDRESS);
							} else {
								if(matches.get(i).toLowerCase().trim().equals("contacts")) {
									mTts.speak("This function is not yet implemented", TextToSpeech.QUEUE_ADD, null);
								} else {
									doPrompting("Did not recognize location type. Choose location type.  Say Here, Address, or Business","Say Here, Address, or Business",SAYCOORDINATESMETHOD);
								}
							}
						}
					}
					break;
				}
			}							
		}
		if (requestCode == SAYOKAYORRETRYLOCATIONBUSINESSNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("yes")) {
						doPrompting("Searching internet",null,-1);
						mLocBuilder.searchInternet(this,getLocationManager());
					} else {
					}
					break;
				}
			}
		}
		if (requestCode == SAYLOCATIONADDRESS) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					savLocationAddress=matches.get(i);
					if(!matches.get(i).toString().toLowerCase().equals("cancel")) { 
						cntRetries=0;
						doPrompting("You said " + matches.get(i),"Say Okay or Retry",SAYOKAYORRETRYLOCATIONADDRESS);
						break;
					} else {
						break;
					}
				}
			}
		}		
		if (requestCode == SAYOKAYORRETRYLOCATIONADDRESS) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("okay")) {
						if(mLocBuilder.setAddress(savLocationAddress)) {
							mLocBuilder.flush();
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);
							refreshList();
						} else {
							Boolean didit=false;
							while (cntRetries < 4 && !didit) {
								if(mLocBuilder.setAddress(savLocationAddress)) {
									mLocBuilder.flush();
									mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);
									didit=true;
									break;
								}
								cntRetries++;
							}
							if(didit) {
								refreshList();
								return;
							}
							doPrompting("Failed trying to get address's location.  Please try again.","Say Location address; or say, Cancel",SAYLOCATIONADDRESS);
						}
					} else {
						doPrompting(null,"Say Location address; or say, cancel",SAYLOCATIONADDRESS);
					}
					break;
				}
			}							
		}
		if (requestCode == RETRYGETLOCATIONFROMHERE) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("no")) {
						break;
					} else {
						Location location=INeedToo.mSingleton.doLocationManager();
						if(location!=null) {
							mLocBuilder.setLatitude(String.valueOf(location.getLatitude()));
							mLocBuilder.setLongitude(String.valueOf(location.getLongitude()));
							mLocBuilder.flush();
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);
							refreshList();
						} else {
							doPrompting("Failed getting current location.  Do you want to try again?  Say Yes or No.",
									"Say Yes or No",RETRYGETLOCATIONFROMHERE);
						}
					}
					break;
				}
			}
		}		
	}
	public void searchingTheInternetCallback(boolean okay) {
		if(okay) {
			mLocBuilder.flush();
			doPrompting("Success!",null,-1);
			refreshList();	
		} else {
			doPrompting("Failed trying to get business location.  Do you want to try again?.","Say, yes, or no",SAYOKAYORRETRYLOCATIONBUSINESSNAME);			
		}
	}
	private void refreshList() {
		Intent i2 = new Intent(IHaveLocations.this, INeedToo.class);
	    i2.putExtra("initialtabindex", (int)1);
	    startActivity(i2);    		
	    IHaveLocations.this.finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			this.doOnCreate(savedInstanceState);
			/*bbhbb 2011-03-26*/		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));

		} catch (Exception e) {
			CustomExceptionHandler.logException(e, this);
		}

	}

	private String suffix = "";

	private int _rc=0;
	public String convText(TextView v, String text) {
//		if(_klugeCntCursorItems!=0 && _rc>=_klugeCntCursorItems) {
//			return text;
//		}
		try {
			switch (v.getId()) {
			case R.id.locationlist_phoneid_name:
				_rc++;
				if (!text.equals(INeedToo.mSingleton.getPhoneId())
						&& !text.equals("")) {
					suffix = " (" + INeedToo.mSingleton.getNickName(text) + ")";
				} else {
					suffix = "";
				}
				if(!text.trim().equals("")) { // its not a company
					View vroot=(View)v.getParent();
					View v3=vroot.findViewById(R.id.locationlist_moreicon);
					if(v3!=null ) {
						v3.setVisibility(View.INVISIBLE);
					}
				} else {
					View vroot=(View)v.getParent();
					View v3=vroot.findViewById(R.id.locationlist_moreicon);
					if(v3!=null ) {
						v3.setVisibility(View.VISIBLE);
					}
				}
				break;
			case R.id.locationlist_name:
				String st = new String(text + suffix);
				return st;
			case R.id.locationlist_address:
				return text.replace('\n', ' ');
			}
		} catch (Exception e) {
		}
		return text;
	}
	private static String _previousS=null;

	@Override
	public void onResume() {
		try {
			Object sortOrderSelection=sortOrder.getSelectedItem();
			int x=jdCurrentSortOrder;
			String s=_doingLocationCompany;
			if(x==1 && s==null && _previousS!=null) { // this means I came from location drilldown and arrowed back
				_previousS=s;
				Intent i2 = new Intent(IHaveLocations.this, INeedToo.class);
				i2.putExtra("initialtabindex", (int) 1);
				startActivity(i2);

				/*
				mSimpleCursorAdapter = new MySimpleCursorAdapter(
						getApplicationContext(), R.layout.locations_list_list_company,
				curses, from, to,this);
				setListAdapter(mSimpleCursorAdapter);
				_klugeCntCursorItems=curses.getCount();

				
				mSimpleCursorAdapter.s
				getDbAdapter().fetchAllLocationsIHaveLocationsNew1(_remLoc);
				*/
			}
			_previousS=s;
			
			int logFilter=3;
			try {
				logFilter = Integer.valueOf(getSharedPreferences(
						INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
			} catch (Exception e) {
			}
			Intent jdItent3=new Intent(this, INeedLocationService.class);
			jdItent3.putExtra("PhoneId",INeedToo.mSingleton.getPhoneId());
			jdItent3.putExtra("logFilter",logFilter);
			getApplicationContext().startService(jdItent3);
			Intent jdIntent=new Intent(this, INeedTimerServices.class).
					putExtra("logFilter",logFilter);
			getApplicationContext().startService(jdIntent);
			// getApplicationContext().startService(new Intent(this,
			// INeedTimerServicesII.class));
			mSimpleCursorAdapter.notifyDataSetChanged();
		} catch (Exception eee) {
		}
		INeedToo.mSingleton.startListening();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if(INeedToo.mSingleton.isTestVersion()) {
			inflater.inflate(R.menu.ihavelocationstrial, menu);
		} else {
			inflater.inflate(R.menu.ihavelocations, menu);
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addlocation:
			String[] sa = this.getDbAdapter().fetchAllLocationsLite();
			if (false && sa.length >= 4 && INeedToo.mSingleton.isTestVersion()) {
				AlertDialog.Builder builder2 = new AlertDialog.Builder(
						IHaveLocations.this);
				builder2.setMessage(R.string.trial_version_alert);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.msg_register,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent i4=new Intent(IHaveLocations.this,INeedToPay.class);
								startActivity(i4);
							}
						});
				builder2.setNegativeButton(R.string.msg_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				AlertDialog alert = builder2.create();
				alert.show();
				return true;
			} else {
				Intent i = new Intent(this, LocationView.class);
				startActivity(i);
//2011_03_27				this.finish();
				return true;
			}
		case R.id.businessSearch:
			Intent i7 = new Intent(this, LocationFindBusinesses.class);
			startActivity(i7);
			return true;
		case R.id.searchlocations:
			Intent i2 = new Intent(this, FnSearchLocation.class).putExtra(
					"IsFromIHaveLocations", true);
			startActivity(i2);
			return true;
		case R.id.preferences_menu:
			Intent i23 = new Intent(this, Preferences.class);
			startActivity(i23);
			return true;
		case R.id.support_menu:
			Intent i3 = new Intent(this, INeedSupport.class);
			startActivity(i3);
			return true;
		case R.id.register_menu:
			Intent i4=new Intent(this,INeedToPay.class);
			startActivity(i4);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mSimpleCursorAdapter.getCursor().moveToPosition(position);
		long locid=id;
		try {
			locid=mSimpleCursorAdapter.getCursor().getLong(mSimpleCursorAdapter.getCursor().getColumnIndex("locid"));
		} catch (Exception ee3) {}
		String companyName = mSimpleCursorAdapter.getCursor().getString(
				mSimpleCursorAdapter.getCursor().getColumnIndex("name"));
		int ss=2;
		try {
			ss=mSimpleCursorAdapter.getCursor().getInt(mSimpleCursorAdapter.getCursor().getColumnIndex("SortSequence"));
		} catch (Exception ee3) {}
		if (ss == 1) {
			Intent i2 = new Intent(IHaveLocations.this, INeedToo.class);
			i2.putExtra("initialtabindex", (int) 1).putExtra("doingcompany",
					companyName);
			startActivity(i2);
		} else {
			Intent i = new Intent(this, LocationView.class);
			i.putExtra(INeedDbAdapter.KEY_ROWID, locid);
			startActivity(i);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ihavelocations_context, menu);
	}

	private long willtherealidpleasestandup(long id, SQLiteCursor slc) {
		try {
			return slc.getLong(slc.getColumnIndex("locid"));
		} catch (Exception e) {
			return id;
		}
	}
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		SQLiteCursor slc=(SQLiteCursor)mSimpleCursorAdapter.getItem(info.position);
		slc.moveToPosition(info.position);
		final String jdName=slc.getString(slc.getColumnIndex("name"));
		final long locid=willtherealidpleasestandup(info.id,slc);
		String title=item.getTitle().toString();
		switch (item.getItemId()) {
		case R.id.menu_map_location:

			if (locid == 999999) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.msg_cannotmapcompany),
						Toast.LENGTH_LONG).show();
				return true;
			}
			
			
			Cursor curses3=getDbAdapter().fetchLocation(locid);
			String company=curses3.getString(curses3.getColumnIndex("company"));
				String latitude=curses3.getString(curses3.getColumnIndex("latitude"));
				String longitude=curses3.getString(curses3.getColumnIndex("longitude"));
				
	//            String uri = "google.streetview:cbll=" + latitude + "," + longitude + "&cbp=1,180,,0,2.0";            
	  //          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
	            
	            
	            curses3.close();
	//            String uri = "geo:" + latitude + "," + longitude+"?z=17?q="+latitude+","+longitude;
	            String uri = "geo:" + latitude + "," + longitude+"?q="+latitude+","+longitude;
	            Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
	            startActivity(ii3);
			
			return true;
		case R.id.menu_edit_location:
			if (locid == 999999) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.msg_cannoteditdeletecompany),
						Toast.LENGTH_LONG).show();
				return true;
			}
			Intent i = new Intent(this, LocationView.class);
			i.putExtra(INeedDbAdapter.KEY_ROWID, locid);
			startActivity(i);
			return true;
		case R.id.menu_delete_location:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String areyousure="Are you sure you want to delete " + jdName + "?";
			builder.setMessage(areyousure)
					.setCancelable(false).setPositiveButton(R.string.msg_yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if (thereBeNeedsAssociatedWithThisLocation(locid, jdName)) {
										AlertDialog.Builder builder2 = new AlertDialog.Builder(
												IHaveLocations.this);
										builder2
												.setMessage(
														R.string.alert_thereareneedsassociatedwiththislocation)
												.setCancelable(false)
												.setPositiveButton(
														R.string.msg_yes,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																if(locid==999999) {
																	IHaveLocations.this.mDbAdapter
																	.deleteCompany(jdName);
																} else {
																	IHaveLocations.this.mDbAdapter
																			.deleteLocation(locid);
																}
																Intent i2 = new Intent(
																		IHaveLocations.this,
																		INeedToo.class);
																i2
																		.putExtra(
																				"initialtabindex",
																				(int) 1)
																		.putExtra(
																				"doingcompany",
																				IHaveLocations.this._doingLocationCompany);
																startActivity(i2);
																IHaveLocations.this
																		.finish();
																Toast
																		.makeText(
																				getApplicationContext(),
																				getString(R.string.msg_locationdeleted),
																				Toast.LENGTH_LONG)
																		.show();
															}
														})
												.setNegativeButton(
														R.string.msg_no,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
															}
														});
										AlertDialog alert = builder2.create();
										alert.show();
									} else {
										
										if(locid==999999) {
											IHaveLocations.this.mDbAdapter
											.deleteCompany(jdName);
										} else {
											IHaveLocations.this.mDbAdapter
													.deleteLocation(locid);
										}
										mSimpleCursorAdapter
												.notifyDataSetChanged();
										Intent i2 = new Intent(
												IHaveLocations.this,
												INeedToo.class);
										i2
												.putExtra("initialtabindex",
														(int) 1)
												.putExtra(
														"doingcompany",
														IHaveLocations.this._doingLocationCompany);
										startActivity(i2);
										IHaveLocations.this.finish();
										Toast
												.makeText(
														getApplicationContext(),
														getString(R.string.msg_locationdeleted),
														Toast.LENGTH_LONG)
												.show();
									}
								}
							}).setNegativeButton(R.string.msg_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private boolean thereBeNeedsAssociatedWithThisLocation(long id, String name) {
		Boolean retness=false;
		if(id==999999) {
			Cursor cur2=getDbAdapter().getAllLocationsForCompany(name);
			if(cur2.getCount()>0) {
				while(cur2.moveToNext()) {
					long id2=cur2.getLong(cur2.getColumnIndex("_id"));
					Cursor curses = this.getDbAdapter().fetchNeedsForALocation(id2);
					if (curses.getCount() > 0) {
						retness = true;
					}
					curses.close();
				}
			} else {
				retness=false;
			}
			cur2.close();
		} else {
			Cursor curses = this.getDbAdapter().fetchNeedsForALocation(id);
			retness = false;
			if (curses.getCount() > 0) {
				retness = true;
			}
			curses.close();
		}
		return retness;
	}
}

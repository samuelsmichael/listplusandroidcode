package com.mibr.android.intelligentreminder;


import com.mibr.android.intelligentreminder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class IHaveNeeds extends ListActivity implements RespondsToNeedByVoiceProgress {
	private int mHeight;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private boolean amDoing2ndDialog;
	private long amDoing2ndDialogId;
	private Dialog _voiceHelpDialog;
	private Dialog _voiceProgressDialog;
	private Button createNeed;
	private Button createNeedUsingVoice;
	private Button createneedusingvoicejprompts;
	private Button buttonAd;
	private String holdLocationName;
	private String holdNeedName;
	private static final int DIALOG_VOICE_HELP = 441;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 22221234;
	private static final int SAYNEEDNAME = 22220001;
	private static final int SAYOKAYORRETRYNEEDNAME = 222200002;
	private static final int SAYOKAYORRETRYLOCATION = 222200003;
	private static final int SAYDESCRIPTIONNAME = 22220004;
	private static final int SAYOKAYORRETRYDESCRIPTION = 222200005;
	private static final int SAYLOCATIONNAME = 22220006;
	private static final int DOYOUWANTTOSEARCHTHEINTERNET = 222200007;
	private static final int DOYOUWANTTOTRYTOSEARCHTHEINTERNETAGAIN = 222200008;
	private static final int SAYYESORNOFETCHADDON = 222200009;
	private static final int MY_DATA_CHECK_CODE = 22229000;
	private NeedBuilder nbPrompt;
	private LocationManager mLocationManager=null;
	private TextToSpeech mTts;
	private String saveDescription;
	private static int jdCurrentSortOrder=0;
	
	private LocationManager getLocationManager() {
		return INeedToo.mSingleton.getLocationManager();
//		if(mLocationManager==null) {
//			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		}
//		return mLocationManager; 
	}

	private INeedDbAdapter mDbAdapter = null;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
	 String remPrompt="";
	private void doPrompting(String youSaid, String prompt, int sendResults) {
		 remPrompt=prompt;
		 try {
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
		 } catch (Exception e) {}
	}
	
	
	private void doOnCreate(Bundle savedInstanceState) {
		if(INeedToo.mSingleton.doViewCount) {
			final Timer jdTimer = new Timer("ViewCountingNeeds");
			jdTimer.schedule(new TimerTask() {
				public void run() {
					Thread thread=new Thread(new Runnable() {
						public void run() {
							try {
							Intent intent2 = new Intent(IHaveNeeds.this, INeedWebService.class)
							.setAction("adview").putExtra("phoneid", INeedToo.mSingleton.getPhoneId()).putExtra("adnbr", 1);
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
		
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("iscontact")) {
			if(!INeedToo.mSingleton.isReminderContactsAvailable()) {
				INeedToo.mSingleton.showReminderContactsNotAvailable(this);
			}
		}
		if(getPackageName().toLowerCase().indexOf("trial")!=-1) {
			setContentView(R.layout.needs_listv3trial);
			buttonAd=(Button)findViewById(R.id.ButtonAd);
			try {
				buttonAd.setText(INeedToo.mSingleton.Ad1Text);
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
						uri=INeedToo.mSingleton.Ad1URI;
					} catch (Exception ee33) {}
					Intent intent2 = new Intent(IHaveNeeds.this, INeedWebService.class)
					.setAction("adpress").putExtra("adnbr", 1);
					startService(intent2);			        
			        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			        startActivity(ii3);
				}
			});
			buttonAd.requestFocus();
		} else {
			setContentView(R.layout.needs_listv3);
		}
		INeedToo.mSingleton.startListening();
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent,MY_DATA_CHECK_CODE);
		getDbAdapter().deleteUnsavedNeeds();
		Cursor curses=null;
		String[] from=null;
		if(jdCurrentSortOrder==1) {
			from=new String[] {"phoneid", "needid","name","description","locations","address"};
			IRThingNeed.CurrentSort=IRThingNeed.SORT_LOCATIONNAME;
			curses = getDbAdapter().fetchAllNeedsByLocationProximity(null);
		} else {
			if(jdCurrentSortOrder==2) {
				from=new String[] {"phoneid", "needid","name","description","locations","address"};
				Location loc=getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (loc==null) {
					loc=getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				if(loc!=null) {
					IRThingNeed.CurrentSort=IRThingNeed.SORT_LOCATIONPROXIMITY;
					curses=getDbAdapter().fetchAllNeedsByLocationProximity(loc);
				} else {
					Toast.makeText(getApplicationContext(),
							"Failed getting current location...reverting back to sort-by-name",
							Toast.LENGTH_SHORT).show();
					jdCurrentSortOrder=0;
					curses = getDbAdapter().fetchAllNeeds();
				}
			}  else {
				IRThingNeed.CurrentSort=IRThingNeed.SORT_NAME;
				curses = getDbAdapter().fetchAllNeeds();
				from=new String[] {"phoneid", "_id","name","description","locations","address"};
			}
		}
		startManagingCursor(curses);
		
		int[] to=new int[] {R.id.needs_list_list_phoneid,R.id.needs_list_list_needid,R.id.needs_list_list_itemname, R.id.needs_list_list_description, R.id.needs_list_list_locations, R.id.needs_list_list_address};
		setListAdapter(mSimpleCursorAdapter=new SimpleCursorAdapter(this,R.layout.needs_list_list,curses,from,to) {
			@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v,convText(v,text));
			}
		});
		registerForContextMenu(getListView());
		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		createNeed=(Button)findViewById(R.id.ineed_createneed);
		createNeedUsingVoice=(Button)findViewById(R.id.ineed_createneedusingvoice);
		createneedusingvoicejprompts=(Button)findViewById(R.id.ineed_createneedusingvoicejprompts);
		if (activities.size() != 0) {
		} else {
			//createNeedUsingVoice.setEnabled(false);
			//createneedusingvoicejprompts.setEnabled(false);
			//createNeedUsingVoice.setText("Recognizer not present");
		}
		createneedusingvoicejprompts.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				INeedToo.mSingleton.doGetContactsFromService(INeedToo.REQUEST_CAME_FROM_NEEDS_BY_VOICE);
				buildLocationArrays();
				nbPrompt=new NeedBuilder(IHaveNeeds.this,_dbLocationNames,_dbCompanyNames,_dbItemNames);
				doPrompting(null,"Say Need name",SAYNEEDNAME);
			}
		});
		createNeed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(getApplicationContext(), NeedView.class).putExtra("needid", -1).putExtra("ComingInFresh", true);
					startActivity(i);
//bbhbb				    IHaveNeeds.this.finish();
				} catch (Exception e) {}
			}
		});
		createNeedUsingVoice.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				INeedToo.mSingleton.doGetContactsFromService(INeedToo.REQUEST_CAME_FROM_NEEDS_BY_VOICE);
				showDialog(DIALOG_VOICE_HELP);
			}
		});
		Spinner sortOrder=(Spinner)findViewById(R.id.sort_by);
		sortOrder.setSelection(jdCurrentSortOrder);
		sortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if(jdCurrentSortOrder!=position) {
					jdCurrentSortOrder=position;
		            Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
		            i2.putExtra("initialtabindex", (int)0);
		            startActivity(i2);    		
					IHaveNeeds.this.finish();
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				int bff=4;
				
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			removeDialog(DIALOG_VOICE_HELP);
		}
		if (requestCode == SAYNEEDNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("cancel")) {
						break;
					} else {
						nbPrompt.addToNeedName(matches.get(i).trim());
						holdNeedName=matches.get(i).trim();
						doPrompting("You said " + matches.get(i),"Say Okay or Retry",SAYOKAYORRETRYNEEDNAME);
						break;
					}
				}
			}
		}		
		if (requestCode == SAYOKAYORRETRYNEEDNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("okay")) {
						doPrompting(null,"Give a Description, or say, None",SAYDESCRIPTIONNAME);
					} else {
						if(matches.get(i).toLowerCase().trim().equals("retry")) {
							nbPrompt.clearNeedName();
							doPrompting(null,"Say Need name",SAYNEEDNAME);
						} else {
							doPrompting("You said " + holdNeedName,"Say Okay or Retry",SAYOKAYORRETRYNEEDNAME);
						}
					}
					break;
				}
			}							
		}
		if (requestCode == SAYDESCRIPTIONNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("none")) {
						doPrompting(null,"Say Location or Contact Name",SAYLOCATIONNAME);
						break;
					} else {
						saveDescription=matches.get(i).trim();
						doPrompting("You said " + matches.get(i).trim(),"Say Okay or Retry",SAYOKAYORRETRYDESCRIPTION);
						break;
					}
				}
			}							
		}
		if (requestCode == SAYOKAYORRETRYDESCRIPTION) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("okay")) {
						nbPrompt.addToNeedDescription(saveDescription);
						doPrompting(null,"Say Location (or Contact) Name",SAYLOCATIONNAME);
					} else {
						nbPrompt.clearNeedDescription();
						doPrompting(null,"Give a Description, or say, None",SAYDESCRIPTIONNAME);
					}
					break;
				}
			}							
		}
		if(requestCode==SAYLOCATIONNAME) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					holdLocationName=matches.get(i);
					doPrompting("You said " + matches.get(i),"Say Okay or Retry",SAYOKAYORRETRYLOCATION);
					break;
				}				
			} 
		}
		if (requestCode == SAYOKAYORRETRYLOCATION) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().equals("okay")) {
						nbPrompt.prepareToReceiveLocations();
						ArrayList<String> words=new ArrayList<String>();
						String separator="";
						String[] strWords=holdLocationName.split(" ");
						for(int n=0;n<strWords.length;n++) {
							String word=strWords[n].trim();
							words.add(word);
							separator=" ";
						}
						for(int i2=0;i2<words.size();i2++) {
							String str=words.get(i2);
							nbPrompt.heresAWord(str);
						}
						if(nbPrompt.imOkay()) {
							try {
								boolean isContact=nbPrompt.buildData(false);
								if(isContact) {
									if(!INeedToo.mSingleton.isReminderContactsAvailable()) {
										doPrompting(null,"You have created a Contact Need which will alert you whenever a telephone conversation is established with that Contact. However, this feature requires our Contact Reminder add-on, which you do not have on this phone.  Do you want to go fetch it?  Say, yes, or no.",SAYYESORNOFETCHADDON);
										break;
									}
								}
							} catch (Exception eee333) {
								int bkhere=3;
								int bkthere=bkhere;
							}
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);
							refreshList();
						} else {
							doPrompting(null,"Location not found, do you want to search the internet?  Say Yes or No",DOYOUWANTTOSEARCHTHEINTERNET);
						}
					} else {
						if(matches.get(i).toLowerCase().equals("retry")) {
							doPrompting(null,"Say Location (or Contact) Name",SAYLOCATIONNAME);
							break;
						}
					}
					break;
				}
			}							
		}
		if (requestCode == SAYYESORNOFETCHADDON) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("yes")) {

						if(INeedToo.IS_ANDROID_VERSION) {
					        String uri = "market://details?id=com.mibr.android.remindercontacts";
					        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					        startActivity(ii3);
						} else {
							Uri uri = Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=com.mibr.android.remindercontacts");
							Intent intent=new Intent(Intent.ACTION_VIEW,uri);
							startActivity(intent);
						}

				        
					
					}
				}
			}
		}

		if (requestCode == DOYOUWANTTOSEARCHTHEINTERNET) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("yes")) {
						doPrompting("Searching internet",null,-1);
						nbPrompt.searchInternet(getApplicationContext(),getLocationManager(),null);
						try {
							nbPrompt.buildData(false);
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);		
							refreshList();
						} catch (Exception eee) {	
							doPrompting(null,"Failed connection to internet.  Do you want to try again? Say Yes or No.", DOYOUWANTTOTRYTOSEARCHTHEINTERNETAGAIN);		
						}
					} else {
						try {
							nbPrompt.buildData(true);
							mTts.speak("Success!, Need built without Location", TextToSpeech.QUEUE_ADD, null);		
							refreshList();
						} catch (Exception eee333) {
							
						}
					}
					break;
				}
			}							
		}
		if (requestCode == DOYOUWANTTOTRYTOSEARCHTHEINTERNETAGAIN) {
			if(resultCode == RESULT_OK) {
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for(int i=0;i<matches.size();i++) {
					if(matches.get(i).toLowerCase().trim().equals("yes")) {
						doPrompting("Searching internet",null,-1);
						nbPrompt.searchInternet(getApplicationContext(),getLocationManager(),null);
						try {
							nbPrompt.buildData(false);
							mTts.speak("Success!", TextToSpeech.QUEUE_ADD, null);		
							refreshList();
						} catch (Exception eee) {	
							doPrompting(null,"Failed connectiong to internet.  Do you want to try again? Say Yes or No.", DOYOUWANTTOTRYTOSEARCHTHEINTERNETAGAIN);		
						}
					} else {
						if(matches.get(i).toLowerCase().trim().equals("no")) {
							try {
								nbPrompt.buildData(true);
								mTts.speak("Success! ... Need built without Location", TextToSpeech.QUEUE_ADD, null);		
								refreshList();
							} catch (Exception eee333) {
								
							}
						}
					}
					break;
				}
			}							
		}
		if (requestCode==MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
					// success, create the TTS instance
					mTts = new TextToSpeech(this.getApplicationContext(), new OnInitListener(){
						@Override
						public void onInit(int arg0) {
							if(arg0==TextToSpeech.SUCCESS) {
								try {
									mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
										@Override
										public void onUtteranceCompleted(String utteranceId) {
											final String mud=utteranceId;
											final Timer jdTimer = new Timer("WaitingForHearingAids");
											jdTimer.schedule(new TimerTask() {
												public void run() {
													try {
													 	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
														intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
														if (remPrompt!=null) {
															intent.putExtra(RecognizerIntent.EXTRA_PROMPT, remPrompt + " ...");
														} else {
															intent.putExtra(RecognizerIntent.EXTRA_PROMPT, " ...");
														}
														startActivityForResult(intent, Integer.valueOf(mud));
													} catch (Exception eieio) {}
													this.cancel();
												}
											}, INeedToo.DELAYPOSTTTS, 1000 * 60 * 10);
										}
									});
								} catch (Exception ee) {}
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
		
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			_voiceProgressDialog = new Dialog(this);
			_voiceProgressDialog.setOwnerActivity(this);
			_voiceProgressDialog.setContentView(R.layout.ineedvoiceprogress);
			_voiceProgressDialog.setTitle("Working ...");
			((Button)_voiceProgressDialog.findViewById(R.id.needvoiceprogress_cancel)).setEnabled(true);
			((Button)_voiceProgressDialog.findViewById(R.id.needvoiceprogress_ok)).setEnabled(false);;

			
			_voiceProgressDialog.show();
			
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			ArrayList stuff=new ArrayList();
			stuff.add(_voiceProgressDialog);
			stuff.add(getApplicationContext());
			stuff.add(this);
			stuff.add(this.getLocationManager());
			stuff.add(getDbAdapter());
			stuff.add(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
			stuff.add(new Integer(5));			
			new NeedByVoiceProgress().execute(stuff);
		}
			
/*			
		// Fill the list view with the strings the recognizer thought it could have heard
			String rawData="";
			String separator="";
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			ArrayList<String> words=new ArrayList<String>();
			for(int i=0;i<matches.size();i++) {
				String match=matches.get(i);
				String[] strWords=match.split(" ");
				for(int n=0;n<strWords.length;n++) {
					String word=strWords[n].trim();
					rawData=rawData+separator+word;
					words.add(word);
					separator=" ";
				}
			}
			TextView rawDataText=(TextView)_voiceProgressDialog.findViewById(R.id.ineedvoicehelp_rawdata);
			rawDataText.setText(rawData);
			nb=new NeedBuilder(this);
			ArrayList stuff=new ArrayList();
			stuff.add(words);
			stuff.add(_voiceProgressDialog);
			stuff.add(getApplicationContext());
			stuff.add(nb);
			stuff.add(this);
			stuff.add(rawData);
			stuff.add(this.getLocationManager());
			stuff.add(getDbAdapter());
			new NeedByVoiceProgress().execute(stuff);
*/
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private ArrayList _stuff;
	public void callbackFromVoice(Boolean succeeded, Boolean isContact) {
		if(succeeded) {
			Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
			i2.putExtra("initialtabindex", (long) 0);
			i2.putExtra("iscontact",isContact);
			startActivity(i2);
			IHaveNeeds.this.finish();
		}
	}
	
	private void refreshList() {
		Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
	    i2.putExtra("initialtabindex", (int)0);
	    startActivity(i2);    		
	    IHaveNeeds.this.finish();
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_VOICE_HELP:
			_voiceHelpDialog=new Dialog(this);
			_voiceHelpDialog.setContentView(R.layout.ineedvoicehelp);
			_voiceHelpDialog.setTitle("Examples");
			break;
		default:
			return null;
		}
		return _voiceHelpDialog;

	}
	@Override
	public void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {	
		case DIALOG_VOICE_HELP:
			dialog.getWindow().setFlags(
					android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
					android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
					0xffffffff);
			
			LayoutParams lp=dialog.getWindow().getAttributes();
			lp.y=mHeight-575;
			if(lp.y<0) {
				lp.y=0;
			}
			dialog.getWindow().setAttributes(lp);
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say...I NEED something LOCATION aLocation");
//			intent.putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", "2000");
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			break;
		default:
			break;
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
		this.doOnCreate(savedInstanceState);
		/*bbhbb 2011-03-26*/		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));
		} catch (Exception ee) {
			CustomExceptionHandler.logException(ee,this);
		}
	}
	private String getNickname(String phoneid) {
		return INeedToo.mSingleton.getNickName(phoneid);
	}
	private int needId;
	private String suffix ="";
	private String jdaddress="";
	private String convText(TextView v, String text) {
		try {
		switch (v.getId()) {
		case R.id.needs_list_list_phoneid:
			jdaddress="";
			if(!text.equals(INeedToo.mSingleton.getPhoneId())) {
				suffix=" ("+getNickname(text)+")";
			}
			break;
		case R.id.needs_list_list_needid:
			needId=Integer.parseInt(text);
			return "";
		case R.id.needs_list_list_address:
			jdaddress=text;
			return "";
		case R.id.needs_list_list_locations:
			if(!text.trim().equals("")) {
				return text + " " + jdaddress;
			} else {
				Cursor curses2=getDbAdapter().fetchLocationDescriptionsForNeed(needId);
				String blank="";
				StringBuilder sb=new StringBuilder();
				while(curses2.moveToNext()) {
					sb.append(blank+curses2.getString(curses2.getColumnIndex("LocationName")));
					String bubba=sb.toString();
					blank=" * ";
				}
				curses2.close();
				return sb.toString();
			}
		case R.id.needs_list_list_itemname:
			String tt=new String(text+suffix);
			suffix="";
			return tt;
// for date created			return text.substring(0,16);
//			return text.substring(5, 6)+"/" + text.substring(8,9) + text.substring(0,3);
		}
		} catch (Exception e) {}
		return text;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if(INeedToo.mSingleton.isTestVersion() ) {
			inflater.inflate(R.menu.ihaveneedstrial, menu);
		} else {
			inflater.inflate(R.menu.ihaveneeds, menu);
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addneed_menu:
			Intent i = new Intent(this, NeedView.class).putExtra("needid", -1).putExtra("ComingInFresh", true);
			startActivity(i);
			return true;
		case R.id.preferences_menu:
			Intent i2=new Intent(this,Preferences.class);
			startActivity(i2);
			return true;
		case R.id.support_menu:
			Intent i3=new Intent(this,INeedSupport.class);
			startActivity(i3);
			return true;
		case R.id.register_menu:
			Intent i4=new Intent(this,INeedToPay.class);
			startActivity(i4);
			return true;
		case R.id.addon_menu:
			Intent i5=new Intent(this,IHaveAddons.class);
			startActivity(i5);
			return true;
		case R.id.history_menu:
			Intent i6=new Intent(this, IHaveHistory.class);		
			startActivity(i6);
			return true;
		}
			
		return super.onMenuItemSelected(featureId, item);
	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mSimpleCursorAdapter.getCursor().moveToPosition(position);
        long needid=id;
        try {
        	if(jdCurrentSortOrder!=0) {
        		needid=mSimpleCursorAdapter.getCursor().getLong(mSimpleCursorAdapter.getCursor().getColumnIndex("needid"));
        	}
        } catch (Exception eieie) {}
        Intent i = new Intent(this, NeedView.class).putExtra("needid", needid).putExtra("ComingInFresh", true);
        startActivity(i);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater=getMenuInflater();
    	inflater.inflate(R.menu.ihaveneeds_context_menu,menu);
    }
    private String thePhoneID="";
	private long willtherealidpleasestandup(long id) {
		long jdid=id;
        try {
        	if(jdCurrentSortOrder!=0) {
        		jdid=mSimpleCursorAdapter.getCursor().getLong(mSimpleCursorAdapter.getCursor().getColumnIndex("needid"));
        	}
        } catch (Exception eieie) {}
    	return jdid;
	}

    public boolean onContextItemSelected(MenuItem item) {
    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        mSimpleCursorAdapter.getCursor().moveToPosition(info.position);
    	
        final long needid=willtherealidpleasestandup(info.id);
    	
    	Cursor cu= this.mDbAdapter.getNeedFromId(needid);
    	while(cu.moveToNext()) {
			thePhoneID=cu.getString(cu.getColumnIndex("phoneid"));
		}
    	cu.close();
    	switch(item.getItemId()) {
    	case R.id.menu_edit_need:
            Intent i = new Intent(this, NeedView.class).putExtra("ComingInFresh", true);;
            i.putExtra("needid", needid);
            startActivity(i);    		
    		return true;
    	case R.id.menu_delete_need:
    		amDoing2ndDialog=false;
    		amDoing2ndDialogId=needid;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.alert_areyousuredeleteneed)
				.setCancelable(false)
				.setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						String jeThisPhoneID=IHaveNeeds.this.thePhoneID;
						String myPhoneID=INeedToo.mSingleton.getPhoneId();
						if(!IHaveNeeds.this.thePhoneID.equals(INeedToo.mSingleton.getPhoneId())) {
								amDoing2ndDialog=true;
								AlertDialog.Builder builder22 = new AlertDialog.Builder(IHaveNeeds.this);
								builder22.setMessage(R.string.alert_foreignneeddeletetoo)
										.setCancelable(false)
										.setPositiveButton(R.string.msg_yes,
												new DialogInterface.OnClickListener() {
													public void onClick(DialogInterface dialog,
															int id) {
														IHaveNeeds.this.mDbAdapter.deleteNeed(amDoing2ndDialogId,IHaveNeeds.this.thePhoneID,true);
											            Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
											            i2.putExtra("initialtabindex", (int)0);
											            startActivity(i2);    		
											            IHaveNeeds.this.finish();
											            Toast.makeText(getApplicationContext(), getString(R.string.msg_needdeleted), Toast.LENGTH_LONG).show();
													}
												}
											)
										.setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,int id) {					
												IHaveNeeds.this.mDbAdapter.deleteNeed(amDoing2ndDialogId,IHaveNeeds.this.thePhoneID,false);
									            Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
									            i2.putExtra("initialtabindex", (int)0);
									            startActivity(i2);    		
									            IHaveNeeds.this.finish();
									            Toast.makeText(getApplicationContext(), getString(R.string.msg_needdeleted), Toast.LENGTH_LONG).show();
											}
										}
								);
								AlertDialog alert=builder22.create();	
								alert.show();
							}
							if(!amDoing2ndDialog) {
								IHaveNeeds.this.mDbAdapter.deleteNeed(needid,IHaveNeeds.this.thePhoneID,false);
					            Intent i2 = new Intent(IHaveNeeds.this, INeedToo.class);
					            i2.putExtra("initialtabindex", (int)0);
					            startActivity(i2);    		
					            IHaveNeeds.this.finish();
					            Toast.makeText(getApplicationContext(), getString(R.string.msg_needdeleted), Toast.LENGTH_LONG).show();
							}
						}
				})
			.setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {					
				}
			});
			AlertDialog alert=builder.create();	
			alert.show();
			return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }
	@Override
	public void onResume() {
		try {
			
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			mHeight=display.getHeight();
			
			
			int logFilter=3;
			try {
				logFilter = Integer.valueOf(getSharedPreferences(
						INeedToo.PREFERENCES_LOCATION,
						Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
			} catch (Exception e) {
			}
	    	Intent jdItent2=new Intent(this, INeedTimerServices.class);
			jdItent2.putExtra("logFilter",logFilter);
			getApplicationContext().startService(jdItent2);
//			getApplicationContext().startService(new Intent(this, INeedTimerServicesII.class));

			getLocationsTimer2().schedule(new TimerTask() {
				public void run() {
					try {
						if(INeedTimerServices.mSingleton!=null) {
							int logFilter2=3;
							try {
								logFilter2 = Integer.valueOf(getSharedPreferences(
										INeedToo.PREFERENCES_LOCATION,
										Preferences.MODE_PRIVATE).getString("LoggingLevel", "3"));
							} catch (Exception e) {
							}							Intent jdItent=new Intent(IHaveNeeds.this,INeedLocationService.class);
							jdItent.putExtra("PhoneId",INeedToo.mSingleton.getPhoneId());
							jdItent.putExtra("logFilter",logFilter2);
							getApplicationContext().startService(jdItent);
							mLocationsTimer2.cancel();
							mLocationsTimer2.purge();
						}
					} catch (Exception ee) {
						
					}
				}
			}, 1000, 1000);		
			
	    	INeedToo.mSingleton.startListening();
		} catch (Exception eee) {}
		super.onResume();
	}

	private  Timer mLocationsTimer2=null;
	private  Timer getLocationsTimer2() {
		if (mLocationsTimer2 == null) {
			mLocationsTimer2 = new Timer(
			"LocationsActivities2");
		}
		return mLocationsTimer2;
	}
		
	
	@Override
	public void onDestroy() {
		if(null!=mDbAdapter) {
			mDbAdapter.close();
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
	private Hashtable<String,IRLocation> _dbLocationNames=null;
	private Hashtable<String,Long> _dbCompanyNames=new Hashtable<String,Long>();
	private Hashtable<String,IRItem> _dbItemNames=new Hashtable<String,IRItem>();
	private void buildLocationArrays() {
		try {
			_dbLocationNames=new Hashtable<String,IRLocation>();
			_dbCompanyNames=new Hashtable<String,Long>();
			_dbItemNames=new Hashtable<String,IRItem>();
			Cursor curses=null;
			curses = getDbAdapter().fetchAllLocationsIHaveLocationsOld1(3);
			while(curses.moveToNext()) {
				long sseq=curses.getInt(curses.getColumnIndex("SortSequence"));
				String name=curses.getString(curses.getColumnIndex("name"));
				long id=curses.getInt(curses.getColumnIndex("_id"));
				String phoneId=curses.getString(curses.getColumnIndex("phoneid"));
				long contactid=curses.getLong(curses.getColumnIndex("contactid"));
				if(sseq!=1) {
					_dbLocationNames.put(name, new IRLocation(name, id, phoneId,contactid));
				} else {
					_dbCompanyNames.put(name, new Long(id));
				}
			}
			curses.close();
			curses=getDbAdapter().allItems();
			while(curses.moveToNext()) {
				String name=curses.getString(curses.getColumnIndex("name"));
				long id=curses.getInt(curses.getColumnIndex("_id"));
				String phoneId=curses.getString(curses.getColumnIndex("phoneid"));
				_dbItemNames.put(name, new IRItem(name,id,phoneId));
			}
			curses.close();
		} catch (Exception e) {
			e=e;
		}		
	}

}

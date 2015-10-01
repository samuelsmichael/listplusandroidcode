package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NeedByVoiceProgress extends AsyncTask<ArrayList, Hashtable, Boolean> {

	Button voiceProgressCancel;
	Button voiceProgressOkay;
	ProgressBar pb=null;
	int nbrSeconds=0;
	Dialog dialog;
	Timer mWaitingTimer=null;
	NeedBuilder nb=null;
	INeedDbAdapter mDbAdapter = null;
	RespondsToNeedByVoiceProgress caller=null;
	TextView textViewAfterNeedName;
	TextView textViewAfterNeedDescription;
	TextView textViewAfterLocationNames;
	TextView textViewLocationName;
	LocationManager locationManager;
	private static int SECONDS_DELAY =5;
	private static int _jdSecondsDelay=5;
//	String nName;
//	String desc;
//	String lNames;
	Boolean jdokay=false;
	String rawData;
	Hashtable progress=new Hashtable();
	String dialogTitle="Working ...";
	Boolean okayEnabled=false;
	private Timer getMyWaitingTimer() {
		if (mWaitingTimer == null) {
			mWaitingTimer = new Timer("WaitingForUserInput");
		}
		return mWaitingTimer;
	}
	private void startMyWaitingTimer() {
		nbrSeconds=0;
		getMyWaitingTimer().schedule(new TimerTask() {
			public void run() {
				nbrSeconds++;
//				if(nbrSeconds>1) {
	//				progress.put("showStatus", null);
	//				textViewAfterNeedName.setText(nName);
		//			textViewAfterNeedDescription.setText(desc);
			//		textViewAfterLocationNames.setText(lNames);
		//		}
				progress.put("WaitingForUserOkayCountdown", new Integer((int)(((float)nbrSeconds/(float)_jdSecondsDelay)*100f)));
				publishProgress(progress);
				if(nbrSeconds==_jdSecondsDelay) {
					mWaitingTimer.cancel();
					dialog.dismiss();
					try {
						nb.buildData(false);
						jdokay=true;
					} catch (Exception e) {
					}
					caller.callbackFromVoice(jdokay,nb.isContact());
				}			
			}
		}, 0, 1000 * 1);
	}
	public void showStatus() {
		ArrayList<Object> al=new ArrayList<Object>();
		al.add(nb.getNeedName());
		al.add(nb.getNeedDescription());
		al.add(nb.getNeedLocationNamesForView());
		al.add(dialogTitle);
		al.add(okayEnabled);
		progress.put("showStatus", al);
		publishProgress(progress);		
	}

	
	@Override
	protected Boolean doInBackground(ArrayList... arg0) {		
		ArrayList stuff=arg0[0];
		dialog=(Dialog)stuff.get(0);
		Context ctx=(Context)stuff.get(1);
		caller=(RespondsToNeedByVoiceProgress)stuff.get(2);
		locationManager=(LocationManager)stuff.get(3);
		mDbAdapter=(INeedDbAdapter)stuff.get(4);
		ArrayList<String> matches = (ArrayList<String>)stuff.get(5);
		_jdSecondsDelay=(Integer)stuff.get(6);

		voiceProgressCancel=(Button)dialog.findViewById(R.id.needvoiceprogress_cancel);
		voiceProgressOkay=(Button)dialog.findViewById(R.id.needvoiceprogress_ok);
		voiceProgressCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mWaitingTimer!=null) {
					try {mWaitingTimer.cancel();} catch(Exception e44){}
				}
				try {dialog.dismiss();} catch(Exception e44){}
//				getMyWaitingTimer().cancel();
	//			dialog.dismiss();
			}
		});
		voiceProgressOkay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mWaitingTimer!=null) {
					try {mWaitingTimer.cancel();} catch(Exception e44){}
				}
				try {dialog.dismiss();} catch(Exception e44){}
//				getMyWaitingTimer().cancel();
//				dialog.dismiss();
				try {
					nb.buildData(false);
					caller.callbackFromVoice(true,nb.isContact());
				} catch (Exception e) {
					caller.callbackFromVoice(false,nb.isContact());
				}
			}
		});
		textViewAfterNeedName=(TextView)dialog.findViewById(R.id.ineedvoicehelp_needname);
		textViewAfterNeedDescription=(TextView)dialog.findViewById(R.id.ineedvoicehelp_needdescription);
		textViewAfterLocationNames=(TextView)dialog.findViewById(R.id.ineedvoicehelp_locationnames);
		textViewLocationName=(TextView)dialog.findViewById(R.id.ineedvoicehelp_contactorlocationname);
		pb=(ProgressBar)dialog.findViewById(R.id.voiceneedprogressbar);
		
		NeedBuilder[] nba=new NeedBuilder[matches.size()];
		nb=null;
		buildLocationArrays();
		for(int i=0;i<matches.size();i++) {
			nba[i]=new NeedBuilder((Activity)caller,_dbLocationNames,_dbCompanyNames,_dbItemNames);
			String rawData="";
			String separator="";
			ArrayList<String> words=new ArrayList<String>();
			String match=matches.get(i);
			String[] strWords=match.split(" ");
			for(int n=0;n<strWords.length;n++) {
				String word=strWords[n].trim();
				if(!word.equals("")) {
					rawData=rawData+separator+word;
					words.add(word);
					separator=" ";
				}
			}
			for(int i2=0;i2<words.size();i2++) {
				String str=words.get(i2);
				nba[i].heresAWord(str);
			}
			if(nba[i].imOkay()) {
				nb=nba[i];
				break;
			}
		}
		if(nb==null) {
			if(nba[0]!=null) {
				nb=nba[0];
			} else {
				dialog.dismiss();
				return false;
			}
		}
		showStatus();
		
		

//		rawData=(String)stuff.get(5);
//		nb=(NeedBuilder)stuff.get(2);
//		ArrayList<String> words=(ArrayList<String>)stuff.get(0);
		try {
//			for(int i=0;i<words.size();i++) {
//				String str=words.get(i);
//				nb.heresAWord(str);
//			}
//			try {
//				nName=nb.getNeedName();
//				desc=nb.getNeedDescription();
//				lNames=nb.getNeedLocationNamesForView();
//				textViewAfterNeedName.setText(nName);
//				textViewAfterNeedDescription.setText(desc);
//				textViewAfterLocationNames.setText(lNames);
//			} catch (Exception ee3) {
//				ee3=ee3;
//				 
//			}
			
//			try {
			if(nb.getNeedName().equals("")) {
				dialogTitle="Please supply Need name";
				showStatus();
//				dialog.setTitle("Failed");
			
			} else {
				if(nb.imOkay()) {
//					dialog.setTitle("Okay");
	//				voiceProgressOkay.setEnabled(true);
					dialogTitle="Okay";
					okayEnabled=true;
					showStatus();
				} else {
					if(nb.isContact()) {
						dialogTitle="Contact not found";
						showStatus();
					} else {
						dialogTitle="Searching internet...";
	//					try {dialog.setTitle("Searcing internet...");} catch (Exception ed1){}
						nb.searchInternet(ctx,locationManager,this);
						if(nb.atLeastOneLocationIsOkay()) {
							dialogTitle="Okay";
							okayEnabled=true;
							showStatus();
	//						try {dialog.setTitle("Okay");} catch (Exception e) {}
	//						try {voiceProgressOkay.setEnabled(true);} catch (Exception e) {}
						} else {
							dialogTitle="No found locations";
							showStatus();
	//						dialog.setTitle("Failed");
							//return false;
						}
					}
				}
			}
		} catch (Exception eee333) {
			dialog.dismiss();

//				dialog.setTitle(eee333.getMessage());
			return false;
		}
		startMyWaitingTimer();
		return true;
	}
	@Override
	protected void onProgressUpdate(Hashtable... arg0) {
		Hashtable lprog=(Hashtable)arg0[0];
		try {			
			if(lprog.get("WaitingForUserOkayCountdown")!=null) {
				int flt=(Integer)lprog.get("WaitingForUserOkayCountdown");
				pb.setProgress(flt);
			}
		} catch (Exception e) {}
		try {
			if(lprog.get("showStatus")!=null) {
				ArrayList<Object> status=(ArrayList<Object>)lprog.get("showStatus");
				textViewAfterNeedName.setText((String)status.get(0));
				textViewAfterNeedDescription.setText((String)status.get(1));
				textViewAfterLocationNames.setText((String)status.get(2));
				dialog.setTitle((String)status.get(3));
				voiceProgressOkay.setEnabled((Boolean)status.get(4));
				if(nb.isContact()) {
					textViewLocationName.setText("Contact");
				} else {
					textViewLocationName.setText("Location");
				}
			}
		} catch (Exception e2) {}
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
			curses = mDbAdapter.fetchAllLocationsIHaveLocationsOld1(3);
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
			curses=mDbAdapter.allItems();
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

	@Override
	protected void onPostExecute(Boolean result) {
		if(result==true) {
			
		} else {
			
		}
	}
}

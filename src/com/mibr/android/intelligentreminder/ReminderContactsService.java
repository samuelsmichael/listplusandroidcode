package com.mibr.android.intelligentreminder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;


public class ReminderContactsService extends Service {

	private static Boolean IS_ANDROID_VERSION=true;
	
	private Handler mHandler;	
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // Generate 20 random bytes, and put them here.    
    private static final byte[] SALT = new byte[] {     
    	-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,     
    	-45, 77, -117, -36, -113, -11, 32, -64, 89     
    };
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhZrIaNqneMAux90tFKHBwnFvS+NXqIhcqFQ3ZrUTuQN/Uy6hZZyKUJVcnUOMVWPWtK6dtN6FzqTNNK3c8aJpAiTQH0rtFzh4lt1CI0BojSV4WfDosgLh8Tzy6iy70z7R1g8P3CiHcwbO96kO1Hut997gYtFWUO/Ot1B6SdourkxN/oUrcaS0JAjaIcBYrfQhlm8QOJw3FdGqzGjtQ6pJMVc1SI6oSBeKJfuvZy7nLU4+lwdb73McCJLfJUkhNBow+knSbg5L5YxGpPDRVxfeYvVTadJGRHiPRnVI0ndk+DZNBOifgqRQubO9lri0uwu4gx+D12CyvpC1YS2A1gn7ZwIDAQAB";
    /* bbhbb 2013_05_10 no longer handles licensing
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {        
    	public void allow() {
   			handleResultOfLicenseCheck(true);
    	}        
    	public void dontAllow() {
    		handleResultOfLicenseCheck(false);            
    	}
		@Override
		public void applicationError(ApplicationErrorCode errorCode) {
			// TODO Auto-generated method stub
			int bkhere=3;
			int bkthere=bkhere;
		}    
    }
	
	private void handleResultOfLicenseCheck(Boolean allow) {       
		iveFailedLicensing=!allow;
	}	
	*/
	
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();    
    /** Holds last value set by a client. */    
    int mValue = 0;	
    boolean iveFailedLicensing=false;
	
    /**
    * Command to the service to register a client, receiving callbacks
    * from the service. The Message's replyTo field must be a Messenger of
    * the client where callbacks should be sent.
    */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
    * Command to the service to unregister a client, ot stop receiving callbacks
    * from the service. The Message's replyTo field must be a Messenger of
    * the client as previously given with MSG_REGISTER_CLIENT.
    */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
    * Command to service to set a new value. This can be sent to the
    * service to supply a new value, and will be sent by the service to
    * any registered clients with the new value.
    */
    static final int MSG_INCOMING_CALL = 3;
    static final int MSG_OUTGOING_CALL = 4;
    static final int MSG_GETCONTACTS =5;
    static final int MSG_HERESCONTACTS=6;


    /**
    * Handler of incoming messages from clients.
    */
    class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
		    switch (msg.what) {
			    case MSG_REGISTER_CLIENT:
			    	mClients.add(msg.replyTo);
			    	break;
			    case MSG_UNREGISTER_CLIENT:
			    	mClients.remove(msg.replyTo);
			    	break;
			    case  MSG_GETCONTACTS:
			    	Bundle bundle=new Bundle();
			    	if(iveFailedLicensing) {
			    		bundle.putString("NoLicense","true");
			    	} else {
			    		getContacts();
				    	long[] la=new long[2];
				    	la[0]=10;
				    	la[1]=11;
				    	String[] sa=new String[3];
				    	sa[0]="hh";
				    	sa[1]="ii";
				    	sa[2]="jj";
				    	String del="";
				    	StringBuffer sb=new StringBuffer();
				    	for(int c=0;c<_contactIds.length;c++) {
				    		sb.append(del+String.valueOf(_contactIds[c]));
				    		del="|";
				    	}
				    	sb.append("^");
				    	del="";
				    	for(int n=0;n<_contactNames.length;n++) {
				    		sb.append(del+_contactNames[n]);
				    		del="|";
				    	}
	//			    	bundle.putLongArray("ContactIds", la/* _contactIds*/);
		//		    	bundle.putStringArray("ContactNames", sa/*_contactNames*/);
				    	bundle.putString("bbhbb",sb.toString());
			    	}
			    	Message msgOut=Message.obtain(null,MSG_HERESCONTACTS);
			    	msgOut.arg1=msg.arg1;
			    	msgOut.setData(bundle);
				try {
				//	Toast.makeText(getApplicationContext(), "doing bbhbbxx", Toast.LENGTH_LONG).show();
					msg.replyTo.send(msgOut);
				} catch (RemoteException e) {
		//			Toast.makeText(getApplicationContext(), "failed bbhbb "+e.getMessage(), Toast.LENGTH_LONG).show();

				}
			    	break;
			    default:
			    	super.handleMessage(msg);
		    }
	    }    
    }

    /**     
     * Target we publish for clients to send messages to IncomingHandler.     
     * */    
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	TelephonyManager tm=null;
	@Override
	public void onCreate() {
		if(tm==null) {
			jdRegisterStuff();
		}
/*		bbhbb3 No longer do licesne check
		final Timer jdTimer = new Timer("Licensing");
		if( IS_ANDROID_VERSION ) {
			jdTimer.schedule(new TimerTask() {
				public void run() {
					Thread thread=new Thread(new Runnable() {
						public void run() {
					        // Construct the LicenseCheckerCallback. The library calls this when done.        
							mLicenseCheckerCallback = new MyLicenseCheckerCallback();        
							// Construct the LicenseChecker with a Policy.        
							mChecker = new LicenseChecker(            
									ReminderContactsService.this, new ServerManagedPolicy(ReminderContactsService.this,                
											new AESObfuscator(SALT, 
													getPackageName(),
													android.provider.Settings.Secure.ANDROID_ID)),            
											BASE64_PUBLIC_KEY  // Your public licensing key.
											);
							mChecker.checkAccess(mLicenseCheckerCallback);
						}
					});
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.run();
				}
			}, 3000, 1000 * 60 * 10);
		}
*/
		super.onCreate();
	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}	
	private int getIdForPhone(String phone) {
		int jdid=-1;
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phone));
		try {
			Cursor curses = getContentResolver().query(
					uri,
					new String[] { PhoneLookup.DISPLAY_NAME,
							PhoneLookup._ID }, null, null, null);
			while (curses.moveToNext()) {
//				Object obj = curses.getString(curses
	//					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
				jdid=curses.getInt(curses
						.getColumnIndex(PhoneLookup._ID));
		//		Object obj2 = obj;

			}
			curses.close();
		} catch (Exception ee) {}
		return jdid;
	}
	private void jdRegisterStuff() {
		tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if(tm!=null) {
		tm.listen(
				new PhoneStateListener() {
					@Override 
					public void onCallStateChanged(int state, String incomingNumber) {
						if(state==1) {
	//				        Toast.makeText(getApplicationContext(), "PhoneStateListener " + incomingNumber + " state: " + String.valueOf(state), Toast.LENGTH_LONG).show();
					        if(mClients!=null) {
							    for (int i=mClients.size()-1; i>=0; i--) {
								    try {
								    	Message msg=Message.obtain(null,
									    		MSG_INCOMING_CALL, getIdForPhone(incomingNumber),state);
									    mClients.get(i).send(msg);
								    } catch (RemoteException e) {
//								        Toast.makeText(getApplicationContext(), "PhoneException " + e.getMessage(), Toast.LENGTH_LONG).show();
									    // The client is dead. Remove it from the list;
									    // we are going through the list from back to front
									    // so this is safe to do inside the loop.
									    mClients.remove(i);
								    }
							    }
					        }
						}
					}
				}
				, PhoneStateListener.LISTEN_CALL_STATE);
		}	
	
		IntentFilter iff=new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
		iff.setPriority(1);
        //Toast.makeText(getApplicationContext(), "Registering iff ", Toast.LENGTH_SHORT).show();
		
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
//               Toast.makeText(arg0, "Service BopGetReadyAAAAAAaaa! ", Toast.LENGTH_LONG).show();
			   if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) { 
	//                Toast.makeText(arg0, "Service BopGetReady! "+getResultData(), Toast.LENGTH_LONG).show();
				   if (getResultData()!=null) {      
		               // Toast.makeText(arg0, "Service Bop2233 "+getResultData(), Toast.LENGTH_SHORT).show();
					    for (int i=mClients.size()-1; i>=0; i--) {
						    try {
						    	
/*						    	Uri uri = ContactsContract.Contacts.CONTENT_URI;
						    	
						    	Bundle bundle=new Bundle();
						    	bundle.putString("bb", "boboOutgoing123");
						    	bundle.putParcelable("uri", uri);
*/	
						    	Message msg=Message.obtain(null,
							    		MSG_OUTGOING_CALL, getIdForPhone(getResultData()),0);
//						    	msg.setData(bundle);
	/*bbhbb1					        Toast.makeText(getApplicationContext(), "1  About to send(msg)", Toast.LENGTH_SHORT).show();*/
							    mClients.get(i).send(msg);
						    } catch (RemoteException e) {
	/*bbhbb1					        Toast.makeText(getApplicationContext(), "PhoneException " + e.getMessage(), Toast.LENGTH_LONG).show();*/
							    // The client is dead. Remove it from the list;
							    // we are going through the list from back to front
							    // so this is safe to do inside the loop.
							    mClients.remove(i);
						    }
					    }
					   }     
			   }   
			}
		}, iff);
        //Toast.makeText(getApplicationContext(), "Registered iff: " , Toast.LENGTH_SHORT).show();

	}
	@Override 
	public void onDestroy() {
        //Toast.makeText(getApplicationContext(), "OnDestroy: " , Toast.LENGTH_LONG).show();
		super.onDestroy();
		if(tm!=null) {
		}
	}

//bb1	private ArrayList<? extends Parcelable> _contactNames=null;
//bb1	private ArrayList<? extends Parcelable> _contactIds=null;
	private String[] _contactNames;
	private long[] _contactIds;
    public void getContacts()
    {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
                (false ? "0" : "1") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor curses= getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        _contactNames=new String[curses.getCount()];
        _contactIds=new long[curses.getCount()];
        int c=0;
        while (curses.moveToNext()) {
			_contactNames[c]=curses.getString(curses.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			_contactIds[c]=Long.valueOf(curses.getLong(curses.getColumnIndex(ContactsContract.Contacts._ID)));
			c++;
        }
        curses.close();
    }
	
	
}

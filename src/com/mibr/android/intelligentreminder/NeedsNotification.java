package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NeedsNotification extends ListActivity {
	private INeedDbAdapter mDbAdapter=null;
	private int mLocationId;
	private String mLocationName;
	private long mNeedId;
	private static final int DIALOG_DISPOSE_NEED=333;
	private TextView noteLocAddr;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private NotificationManager mNotificationManager;
	boolean doCancelNotification=false;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
	
	private Logger mLogger = null;
	private int _logFilter=3;

	private Logger getLogger() {
		if (mLogger == null) {
			mLogger = new Logger(_logFilter,"NeedsNotification",this);
		}
		return mLogger;
	}
	private void doOnCreate(Bundle savedInstanceState) {
		setContentView(R.layout.statusnotificationv2);
		TextView tv=(TextView)findViewById(R.id.notification_name);
		TextView noteLocAddr=(TextView)findViewById(R.id.notelocaddr);
		TextView locationSlashContactHeading=(TextView)findViewById(R.id.LocationSlashContactHeading);
		Button mappezMoi=(Button)findViewById(R.id.statnote_mapit);
		mLocationName=getIntent().getStringExtra("locationname");
		tv.setText(mLocationName);
		mLocationId=getIntent().getIntExtra("id", -1);
//bbbhhhbbb		getLogger().log("The mLocationId="+String.valueOf(mLocationId),999);

		Cursor curses0=getDbAdapter().fetchLocation(mLocationId);
		String address="";
		long contactid=-1;
		try {
			address=curses0.getString(curses0.getColumnIndex("address"));
			contactid=curses0.getLong(curses0.getColumnIndex("contactid"));
		} catch (Exception ee) {}
		curses0.close();
		if(contactid!=0 && contactid!=-1) {
			setTitle("You have needs for this Contact");
			locationSlashContactHeading.setText("Contact:");
			LinearLayout la=(LinearLayout)findViewById(R.id.LinearLayout01address);
			la.setVisibility(View.INVISIBLE);
			LinearLayout la2=(LinearLayout)findViewById(R.id.bottom_control_bar);
			la2.setVisibility(View.INVISIBLE);
		} else {
			locationSlashContactHeading.setText("Location:");
			setTitle("You have needs at this Location");
			noteLocAddr.setText(address);
		}
		Cursor curses = getDbAdapter().fetchNeedsForALocation(mLocationId);
		startManagingCursor(curses);
		if (curses.getCount() > 0) {
			//		setContentView(R.layout.locations_list);
			mSimpleCursorAdapter = new SimpleCursorAdapter(
					getApplicationContext(), 
					R.layout.statusnotification_list, 
					curses,
					new String[] {"phoneid","Name","Description"}, 
					new int[] {R.id.notification_phoneid_name,R.id.notification_item_name, R.id.notification_item_desc}
			){
				@Override
				public void setViewText(TextView v, String text) {
					super.setViewText(v,convText(v,text));
				}

			};
			setListAdapter(mSimpleCursorAdapter);
			registerForContextMenu(this.getListView());
			if(getIntent().getBooleanExtra("ClearNotification", false)) {
				if(mNotificationManager==null) {
					mNotificationManager=(android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				}		
				mNotificationManager.cancel(mLocationId);
			}
		} else {
			if(mNotificationManager==null) {
				mNotificationManager=(android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			}		
			mNotificationManager.cancel(mLocationId);
		}
		mappezMoi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Cursor curses3=getDbAdapter().fetchLocation(mLocationId);
					String company=curses3.getString(curses3.getColumnIndex("company"));
						String latitude=curses3.getString(curses3.getColumnIndex("latitude"));
						String longitude=curses3.getString(curses3.getColumnIndex("longitude"));
							            
			            curses3.close();
			            String uri = "geo:" + latitude + "," + longitude+"?q="+latitude+","+longitude;
			            Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			            startActivity(ii3);

				} catch (Exception e) {}
			}
		});

	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			this.doOnCreate(savedInstanceState);
		} catch (Exception e) {
			CustomExceptionHandler.logException(e,this);
		}

	}
	
	private String suffix ="";
	private String convText(TextView v, String text) {
		try {
		switch (v.getId()) {
		case R.id.notification_phoneid_name:
			try {
				if(!text.equals(INeedToo.mSingleton.getPhoneId())) {
					suffix=" ("+INeedToo.mSingleton.getNickName(text)+")";
				}
			} catch (Exception e3) {}
			break;
		case R.id.notification_item_name:
			String st=new String(text+ suffix);
			suffix="";
			return st;
		}
		} catch (Exception ee) {}
		return text;
	}

	@Override
	public void onDestroy() {
		if(null!=mDbAdapter) {
			mDbAdapter.close();
		}
		super.onDestroy();
	}

	@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	MenuInflater inflater=getMenuInflater();
	inflater.inflate(R.menu.needs_notification_imonit,menu);
}
private String thePhoneID="";
public boolean onContextItemSelected(MenuItem item) {
	boolean retCode=true;
	doCancelNotification=false;
	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	Cursor cu= this.mDbAdapter.getNeedFromId(info.id);
	while(cu.moveToNext()) {
		thePhoneID=cu.getString(cu.getColumnIndex("phoneid"));
	}
	cu.close();

	switch(item.getItemId()) {
	case R.id.needs_notification_im_on_it:
		this.getDbAdapter().imOnIt((long)mLocationId, info.id, thePhoneID/*which is the needId*/);
		break;
	case R.id.needs_notification_clear_need:
		this.getDbAdapter().needsNotificationClearNeed((long)mLocationId, info.id,thePhoneID/*which is the needId*/);
		break;
	case R.id.needs_notification_leave_need:
		doCancelNotification=true;
		break;
	default:
		retCode= super.onContextItemSelected(item);
	}
	Intent intent=new Intent(this,NeedsNotification.class).putExtra("id", mLocationId).putExtra("locationname", mLocationName);
	if (doCancelNotification) {
		intent.putExtra("ClearNotification", true);
	}
	mSimpleCursorAdapter.notifyDataSetChanged();
	finish();
	startActivity(intent);
	return retCode;
}
@Override
protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    mSimpleCursorAdapter.getCursor().moveToPosition(position);
    mNeedId=id;
	showDialog(DIALOG_DISPOSE_NEED);

//    Intent i = new Intent(this, NeedView.class).putExtra("needid", needid).putExtra("ComingInFresh", true);
  //  startActivity(i);
}
@Override
public Dialog onCreateDialog(int id) {
	Dialog jddialog=null;
	switch (id) {
	case DIALOG_DISPOSE_NEED:
		doCancelNotification=false;
		Cursor cu= this.mDbAdapter.getNeedFromId(mNeedId);
		while(cu.moveToNext()) {
			thePhoneID=cu.getString(cu.getColumnIndex("phoneid"));
		}
		cu.close();

		final CharSequence[] items = {"Clear - I'm on it!","Clear - I don't need it anymore.","Leave need, but clear notification."};
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Pick disposition");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0) {
					NeedsNotification.this.getDbAdapter().imOnIt((long)mLocationId, mNeedId, thePhoneID/*which is the needId*/);
				} else {
					if(which==1) {
						NeedsNotification.this.getDbAdapter().needsNotificationClearNeed((long)mLocationId, mNeedId,thePhoneID/*which is the needId*/);						
					} else {
						doCancelNotification=true;
					}
				}
				Intent intent=new Intent(NeedsNotification.this,NeedsNotification.class).putExtra("id", mLocationId).putExtra("locationname", mLocationName);
				if (doCancelNotification) {
					intent.putExtra("ClearNotification", true);
				}
				mSimpleCursorAdapter.notifyDataSetChanged();
				finish();
				startActivity(intent);				
			}
		});
		jddialog= builder.create();

		break;
	default:
	}
	return jddialog;
}

}

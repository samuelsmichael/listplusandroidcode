package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ListPlus extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));
		setContentView(R.layout.layout_listplus);
		TextView ListPlus_PhoneID=(TextView)findViewById(R.id.ListPlus_PhoneID);
		TextView ListPlus_LogfileSize=(TextView)findViewById(R.id.ListPlus_LogFileSize);
		Button clearLog=(Button)findViewById(R.id.ListPlus_ButtonClearLog);
		Button transmitLog=(Button)findViewById(R.id.ListPlus_ButtonTransmitLog);
		Button outboundManually=(Button)findViewById(R.id.ListPlus_ManualOutbound);
		Button inboundManually=(Button)findViewById(R.id.ListPlus_ManualInbound);
		setTitle("List Plus System Activities");
		try {
			String jdstring=INeedToo.mSingleton.getPhoneId();
			ListPlus_PhoneID.setText(jdstring);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ListPlus_LogfileSize.setText(
			new DecimalFormat("###,###,###,##0").format(INeedToo.mSingleton.getLogFileSize())+" bytes");
		transmitLog.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				INeedToo.mSingleton.transmitLog();
				Toast.makeText(getApplicationContext(), getString(R.string.msg_logbeingtransmitted), Toast.LENGTH_LONG).show();
			}
		});
		outboundManually.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(ListPlus.this,INeedWebService.class).setAction("Outbound").putExtra("doingmanually", true);
				startService(intent);
			}
		});
		inboundManually.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(ListPlus.this,INeedWebService.class).setAction("Inbound").putExtra("doingmanually", true);
				startService(intent);
			}
		});
		clearLog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ListPlus.this);
				builder.setMessage(R.string.alert_areyousureclearlog)
					.setCancelable(false)
					.setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							INeedToo.mSingleton.clearLog();	
							Toast.makeText(getApplicationContext(), getString(R.string.msg_logcleared), Toast.LENGTH_LONG).show();
							Intent intent=new Intent(ListPlus.this,INeedToo.class).putExtra("initialtabindex", 3);
							ListPlus.this.startActivity(intent);
							ListPlus.this.finish();
						}
					})
				.setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {					
					}
				});
				AlertDialog alert=builder.create();	
				alert.show();
			}

		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if(INeedToo.mSingleton.isTestVersion()) {
			inflater.inflate(R.menu.listplus_systemtrial_menu, menu);
		} else {
			inflater.inflate(R.menu.listplus_system_menu, menu);
		}
		return true;
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
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
			this.finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}

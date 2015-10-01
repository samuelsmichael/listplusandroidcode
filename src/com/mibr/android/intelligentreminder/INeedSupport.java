package com.mibr.android.intelligentreminder;


import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

public class INeedSupport extends Activity {
	private static final int DIALOG_VOICE_HELP2 = 4412;
	private static Dialog voiceHelpDialog=null;
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_VOICE_HELP2:
			voiceHelpDialog=new Dialog(this);
			voiceHelpDialog.setContentView(R.layout.ineedvoicehelp2);
			voiceHelpDialog.setTitle("Voice command examples");
			Button ineedvoicehelp2_ok=(Button)voiceHelpDialog.findViewById(R.id.ineedvoicehelp2_ok);
			ineedvoicehelp2_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					INeedSupport.this.voiceHelpDialog.dismiss();
				}
			});
			return voiceHelpDialog;
		default:
			return null;
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Intelligent Reminder Support");
		setContentView(R.layout.need_support);
		Button webSite=	(Button)findViewById(R.id.SupportWeb);
		Button doc=	(Button)findViewById(R.id.SupportDocumentation);
		Button quickStart=	(Button)findViewById(R.id.SupportQuickStart);
		Button email=(Button)findViewById(R.id.SupportEmailUs);
		Button voiceCommandExamples=(Button)findViewById(R.id.VoiceCommandExamples);
		
		voiceCommandExamples.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_VOICE_HELP2);
			}
		});
		doc.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(INeedSupport.this);
				builder.setMessage("The documentation will be downloaded, after which you will receive a notification.  Open the notification to read the documentation.")
					.setCancelable(false)
					.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							Uri uri = Uri.parse(INeedWebService.BASE_URL+"/documentation/usersguidelite.doc");
							Intent intent=new Intent(Intent.ACTION_VIEW,uri);
							startActivity(intent);
							
						}
					});
				AlertDialog alert=builder.create();	
				alert.show();
			}
		});
		quickStart.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(INeedSupport.this);
				builder.setMessage("The Quick Start guide will be downloaded, after which you will receive a notification.  Open the notification to read the document.")
					.setCancelable(false)
					.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							Uri uri = Uri.parse(INeedWebService.BASE_URL+"/documentation/quickstart.pdf");
							Intent intent=new Intent(Intent.ACTION_VIEW,uri);
							startActivity(intent);
							
						}
					});
				AlertDialog alert=builder.create();	
				alert.show();
			}
		});
		webSite.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(INeedWebService.BASE_URL+"/default.aspx");
				Intent intent=new Intent(Intent.ACTION_VIEW,uri);
				startActivity(intent);
			}
		});
		email.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String[] mailto = {"samuelsmichael222@gmail.com",""};
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, ""
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, ""
						.toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send EMail..."));
			}
		});
/*
		Thread
				.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
						""));
		setTitle("Intelligent Reminder Support");
		final TabHost tabHost = getTabHost();
/ *		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(
				"Contact Us",(BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_send)).setContent(
				new Intent(this, INeedContact.class).putExtra("isreturn", getIntent().getBooleanExtra("isreturn", false))));
* /
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(
				"User's Guide",(BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_info_details)).setContent(
				new Intent(this, IHaveTrouble.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(
				"Our Web Site",(BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_home)).setContent(
				new Intent(this, MiBrWebSite.class)));
/ *		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(
				"System",(BitmapDrawable) getResources().getDrawable(
						R.drawable.status_bar1_blackwhite)).setContent(
				new Intent(this, ListPlus.class)));
* /
/ *
 * 		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(
				"Trouble Shooting",
				INeedToo.scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_info_details), 31f)).setContent(
				new Intent(this, IHaveTrouble.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(
				"MiBr Web Site",
				INeedToo.scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_home), 36f)).setContent(
				new Intent(this, MiBrWebSite.class)));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(
				"Contact Us",
				INeedToo.scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.ic_menu_send), 36f)).setContent(
				new Intent(this, INeedContact.class).putExtra("isreturn", getIntent().getBooleanExtra("isreturn", false))));
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(
				"System",
				INeedToo.scaleTo((BitmapDrawable) getResources().getDrawable(
						R.drawable.status_bar1_blackwhite), 31f)).setContent(
				new Intent(this, ListPlus.class)));
		
 * /
		
		
		Bundle bundle = getIntent().getExtras();
		int initialTabIndex = 0;
		if (bundle != null) {
			initialTabIndex = bundle.getInt("initialtabindex", 0);
		}
		tabHost.setCurrentTab(initialTabIndex);
*/
	}

}

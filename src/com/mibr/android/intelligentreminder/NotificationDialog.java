package com.mibr.android.intelligentreminder;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.mibr.android.intelligentreminder.R;

public class NotificationDialog extends Activity {

	static public View mHelpView= null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mHelpView=(LinearLayout)vi.inflate(R.layout.ineedvoicehelp, null);
		
	    WindowManager.LayoutParams lp=new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 10, 10,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//				WindowManager.LayoutParams.TYPE_APPLICATION,
//				WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
//				WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,0
//				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				,PixelFormat.OPAQUE);
///				    lp.token=notificationPopup.getWindowToken();
		final WindowManager mWm=(WindowManager)getSystemService(WINDOW_SERVICE);
	    mWm.addView(mHelpView,lp);
	}

}

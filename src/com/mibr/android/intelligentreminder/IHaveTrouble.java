package com.mibr.android.intelligentreminder;





import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

public class IHaveTrouble extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
				""));
		setContentView(R.layout.ihavetrouble);
*/
/*
		setContentView(R.layout.mibrwebsite);

		WebView webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		// webview.loadUrl("http://www.mibrtech.com"); logfiles
		webview
				.loadUrl("http://www.cellularassistant.com/documentation/usersguidelite.doc");
		int bkhere=23;
*/
		Uri uri = Uri.parse("http://www.cellularassistant.com/intelligentreminder/documentation/usersguide.doc");
		Intent intent=new Intent(Intent.ACTION_VIEW,uri);
		startActivity(intent);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if(INeedToo.mSingleton.isTestVersion()) {
			inflater.inflate(R.menu.ineedsupporttrial_menu, menu);
		} else {
			inflater.inflate(R.menu.ineedsupport_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences_menu_ineedsupport:
			Intent i23 = new Intent(this, Preferences.class);
			startActivity(i23);
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

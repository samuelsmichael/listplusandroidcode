package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class INeedContact extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if(getIntent().getBooleanExtra("isreturn", false)==true) {
		// Toast.makeText(getApplicationContext(),
		// "Email being sent.  Thank you!", Toast.LENGTH_LONG).show();
		// finish();
		// }
		Thread
				.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
						""));
		setContentView(R.layout.ineedcontact);
		final EditText subject = (EditText) findViewById(R.id.ineedcontact_EditTextSubject);
		final EditText body = (EditText) findViewById(R.id.ineedcontact_EditTextBody);
		Button submit = (Button) findViewById(R.id.ineedcontact_ButtonSendEmail);
		submit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] mailto = {"info@intelligentreminder.com",""};
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject.getText()
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, body.getText()
						.toString());
				sendIntent.setType("text/plain");
				// Toast.makeText(getApplicationContext(),
				// "Email being sent.  Thank you!", Toast.LENGTH_LONG).show();
				startActivity(Intent.createChooser(sendIntent, "Send EMail..."));
				// finish();
				// Intent intent=new
				// Intent(INeedContact.this,INeedSupport.class).putExtra("initialtabindex",
				// 2).putExtra("isreturn",true);
				// startActivity(intent);
			}
		});
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

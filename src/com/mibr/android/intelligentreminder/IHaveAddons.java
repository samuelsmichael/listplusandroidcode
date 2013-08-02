package com.mibr.android.intelligentreminder;
import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class IHaveAddons extends Activity {
	ImageButton addOnGet=null;
	TextView addOnTitle=null;
	TextView addOnFooter=null;
	TextView addOnDescription=null;
	ImageView addOnImage=null;
	EditText suggestionBox=null;
	Button suggestionBoxButton=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Intelligent Reminder Add-ons");
		setContentView(R.layout.ihave_addons);
		addOnGet=(ImageButton)findViewById(R.id.AddOnGet);
		addOnImage=(ImageView)findViewById(R.id.AddOnImage);
		addOnTitle=(TextView)findViewById(R.id.AddOnTitle);
		addOnFooter=(TextView)findViewById(R.id.AddOnFooter);
		addOnDescription=(TextView)findViewById(R.id.AddOnDescription);
		suggestionBoxButton=(Button)findViewById(R.id.SuggestionBoxButton);
		suggestionBox=(EditText)findViewById(R.id.SuggestionBox);
		if(INeedToo.isReminderContactsAvailable) {
			addOnGet.setImageDrawable(getResources().getDrawable(R.drawable.android_market_purchased));
			addOnGet.setEnabled(false);
			addOnFooter.setText("This add-on is already installed");
		} else {
			addOnGet.setImageDrawable(getResources().getDrawable(R.drawable.android_market));
			addOnFooter.setText("");
		}
		addOnTitle.setText("Contact Reminder");
		addOnDescription.setText("Contact Reminder enables you to create Needs and associate them with people in your Contacts directory.  When that person calls you, or when you call that person, you receive a notification displaying the Needs.");
		addOnImage.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon_contact2));
		suggestionBoxButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] mailto = {"info@intelligentreminder.com",""};
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestions"
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, suggestionBox.getText()
						.toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send EMail..."));
				
			}
		});
		addOnGet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
		});
	}
}

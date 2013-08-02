package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FnSearchLocation extends Activity {
	ArrayAdapter<String> mLocationsAdapter=null;
	
	private INeedDbAdapter mDbAdapter = null;
	private long mNeedId;
	private String[]mSa=null;
	private long[]mId=null;
	private boolean mIsFromIHaveLocations=false;
	private boolean isLocation;
	private AutoCompleteTextView mSearchLocation;
	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}
	private void doOnCreate(Bundle savedInstanceState) {
		String itemName=getIntent().getStringExtra("itemTitle");
		isLocation=getIntent().getBooleanExtra("IsLocation", false);
		int contacts=1;
		if(isLocation) {
			contacts=2;
		}
		mIsFromIHaveLocations=getIntent().getBooleanExtra("IsFromIHaveLocations", false);
		if(contacts==1) {
			setTitle("Contacts");
		} else {
			if(INeedToo.isNothing(itemName)) {
				setTitle(getString(R.string.search_locations_title2));
			} else {
				this.setTitle(getString(R.string.search_locations_title)+" "+itemName);
			}
		}
		mNeedId=getIntent().getLongExtra("needid", -1l);
		setContentView(R.layout.need_view_search_location);
		mSa=null;
		if(mIsFromIHaveLocations) {
			mSa=getDbAdapter().fetchAllLocationsLite();
		} else {
			mSa=getDbAdapter().fetchAllLocationsLiteButNotTheOnesfor(mNeedId,contacts,this,1);
			mId=new long[mSa.length];
			for(int cc=0;cc<mSa.length;cc++) {
				String[] sa=mSa[cc].split("\\|");
				mSa[cc]=sa[0];
				mId[cc]=Long.valueOf(sa[1]);
			}		
		}
		mLocationsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, mSa);
		mSearchLocation=(AutoCompleteTextView)findViewById(R.id.need_location_item);
		mSearchLocation.setAdapter(mLocationsAdapter);
		mSearchLocation.requestFocus();
		Button cancel=(Button)findViewById(R.id.search_location_button_cancel);
		Button okay=(Button)findViewById(R.id.search_location_button_confirm);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {			
				if(mIsFromIHaveLocations) {
					finish();
				} else {
					Intent i = new Intent(getApplicationContext(), NeedView.class).putExtra("needid", mNeedId).putExtra("ComingInFresh", true);
					startActivity(i);
					finish();
				}
			}
		});
		okay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = null;
				if(mIsFromIHaveLocations) {
					long si=getSearchedId();
					i=new Intent(getApplicationContext(),LocationView.class).putExtra(INeedDbAdapter.KEY_ROWID, si);
				} else {
					if(!isLocation) {
						long mmiidd=-1;
						for(int jj=0;jj<mSa.length;jj++) {
							if(mSa[jj].equals(mSearchLocation.getText().toString())) {
								mmiidd=mId[jj];
								break;
							}
						}
						if(mmiidd==-1) {
							Toast.makeText(getApplicationContext(), "The Location must be one selected from the list.", Toast.LENGTH_LONG).show();
							return;							
						}
						long locationId=getDbAdapter().findLocationWhoseContactIdIs(mmiidd);
						if(locationId<0) {
							locationId=getDbAdapter().createLocation(mSearchLocation.getText().toString(), 
									"", "0","0", "", 5000, "", String.valueOf(mmiidd));
						}						
						getDbAdapter().createLocationNeedAssociation(mNeedId, locationId, false, INeedToo.mSingleton.getPhoneId(), false);
					} else {
						if(INeedToo.isNothingNot(mSearchLocation.getText().toString())) {
							if (!getDbAdapter().addNeedsLocation(555555, mSearchLocation.getText().toString(), mNeedId, INeedToo.mSingleton.getPhoneId())) {
								Toast.makeText(getApplicationContext(), "The Location must be one selected from the list.", Toast.LENGTH_LONG).show();
								return;							
							}
						}
					}
					i = new Intent(getApplicationContext(), NeedView.class).putExtra("needid", mNeedId).putExtra("ComingInFresh", true);
				}
				startActivity(i);
				finish();
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
	private long getSearchedId() {
		long[]la=getDbAdapter().correspondingIds;
		for(int c=0;c<mSa.length;c++) {
			if(mSa[c].equalsIgnoreCase(mSearchLocation.getText().toString())) {
				return la[c];
			}
		}
		return -1;
	}
}

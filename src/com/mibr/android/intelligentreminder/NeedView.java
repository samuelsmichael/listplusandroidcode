package com.mibr.android.intelligentreminder;




import com.mibr.android.intelligentreminder.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NeedView extends ListActivity {
	private INeedDbAdapter mDbAdapter = null;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private long mNeedId;
	private String mItemTitle;
	private boolean mComingInFresh;
	private String mPhoneID;
	private AutoCompleteTextView mItem;
	private EditText mDescription;
	private boolean mWereDealingWithANewNeedHere=false;
	private static final int DIALOG_ADDLOCATION = 1;
	private static final int DIALOG_SEARCHLOCATION = 2;
	private static final int DIALOG_ADDCONTACTFROMEXISTING =101;
	private int stupidCount = 0; 
	private boolean amDoing2ndDialog;

	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	private CheckBox mShareIt;
	private void doOnCreate(Bundle savedInstanceState) {
		setContentView(R.layout.need_view_v2);
		mNeedId = getIntent().getLongExtra("needid", -1l);
		mComingInFresh=getIntent().getBooleanExtra("ComingInFresh",false);
		setTitle("Need Identification");

		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
		InputMethodManager.HIDE_IMPLICIT_ONLY);		
		
		
		Button cancel = (Button) findViewById(R.id.location_button_cancel);
		Button confirm = (Button) findViewById(R.id.location_button_confirm);
		Button addLocation = (Button) findViewById(R.id.needview_button_location);
		Button newLocation = (Button) findViewById(R.id.needview_button_location_new);
		Button searchLocations  = (Button) findViewById(R.id.needview_button_location_search);
		Button fromExistingContact = (Button) findViewById(R.id.needview_button_contact);
		Button searchContacts  = (Button) findViewById(R.id.needview_button_contact_search);
		mItem = (AutoCompleteTextView) findViewById(R.id.need_item);
		mShareIt=(CheckBox)findViewById(R.id.ShareIt);
		
		if(mComingInFresh) {
			mItem.requestFocus();			
		}
		mDescription = (EditText) findViewById(R.id.need_view_description_id);
		if (mNeedId != -1 ) {
			if(mComingInFresh) {
				mWereDealingWithANewNeedHere = false;
			} else {
				mWereDealingWithANewNeedHere = true;
			}
			Cursor needCursor = getDbAdapter().fetchNeed(mNeedId);
			mItemTitle=needCursor.getString(needCursor
					.getColumnIndex("ItemName"));
			mItem.setText(mItemTitle);
			mDescription.setText(needCursor.getString(needCursor
					.getColumnIndex("description")));
			mPhoneID=needCursor.getString(needCursor.getColumnIndex("phoneid"));
			int ispublic=needCursor.getInt(needCursor.getColumnIndex("ispublic"));
			if(ispublic==0) {
				mShareIt.setChecked(true);
			} else {
				mShareIt.setChecked(false);
			}
			needCursor.close();
		} else {
			try {
			mPhoneID=INeedToo.mSingleton.getPhoneId();
			} catch (Exception ee) {
				mPhoneID="unknown";
			}
			mNeedId = getDbAdapter().createNeed(999999, "",mPhoneID);
			mWereDealingWithANewNeedHere = true;
		}
		Cursor curses = getDbAdapter().fetchCompaniesAndLocationsForNeed(
				mNeedId);
		startManagingCursor(curses);
		String[] from = new String[] { "Name" };
		int[] to = new int[] { R.id.need_view_list_locations_location };
		setListAdapter(mSimpleCursorAdapter = new SimpleCursorAdapter(this,
				R.layout.need_view_list_locations, curses, from, to));
		registerForContextMenu(getListView());
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, getDbAdapter()
				.fetchAllItems());
		mItem.setAdapter(ad);
		fromExistingContact.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(INeedToo.mSingleton.isReminderContactsAvailable()) {
					INeedToo.bbhbb=NeedView.this;					
					INeedToo.mSingleton.doGetContactsFromService(INeedToo.REQUEST_CAME_FROM_CONTACTS_AVAILABLE);
					getDbAdapter().updateNeed(mNeedId,
							NeedView.this.mItem.getText().toString().trim(),
							NeedView.this.mDescription.getText().toString().trim(),mPhoneID,!NeedView.this.mShareIt.isChecked());
//bbhbbbhbb	No longer necessary				showDialog(DIALOG_ADDCONTACTFROMEXISTING);
				} else {
					INeedToo.mSingleton.showReminderContactsNotAvailable(NeedView.this);
				}
			}
		});
		searchContacts.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(INeedToo.mSingleton.isReminderContactsAvailable()) {
					INeedToo.mSingleton.doGetContactsFromService(INeedToo.REQUEST_CAME_FROM_SEARCH_CONTACTS);
					getDbAdapter().updateNeed(mNeedId,
							NeedView.this.mItem.getText().toString().trim(),
							NeedView.this.mDescription.getText().toString().trim(),NeedView.this.mPhoneID,!NeedView.this.mShareIt.isChecked());
					String itemTitle=INeedToo.isNothingNot(NeedView.this.mItemTitle)?NeedView.this.mItemTitle:NeedView.this.mItem.getText().toString();
					Intent intent = new Intent(NeedView.this,FnSearchLocation.class)
						.putExtra("needid", NeedView.this.mNeedId).putExtra("itemTitle", itemTitle)
						.putExtra("IsLocation",false);
					startActivity(intent);
				} else {
					INeedToo.mSingleton.showReminderContactsNotAvailable(NeedView.this);
				}
			}
		});
		addLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDbAdapter().updateNeed(mNeedId,
						NeedView.this.mItem.getText().toString().trim(),
						NeedView.this.mDescription.getText().toString().trim(),mPhoneID,!NeedView.this.mShareIt.isChecked());
				showDialog(DIALOG_ADDLOCATION);
			}
		});
		newLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDbAdapter().updateNeed(mNeedId,
						NeedView.this.mItem.getText().toString().trim(),
						NeedView.this.mDescription.getText().toString().trim(),mPhoneID,!NeedView.this.mShareIt.isChecked());
				Intent intent = new Intent(
						NeedView.this,
						LocationView.class).putExtra(
								"ButComeBackToNeedViewForId",
								NeedView.this.mNeedId);
				startActivity(intent);
				NeedView.this.finish();
			}
		});
		searchLocations.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 1=just contacts 2=justlocations 3=bothll
				Cursor curses = NeedView.this.getDbAdapter().fetchAllLocationsPrimitive(2);
				if (curses.getCount() == 0) {
					curses.close();
					stupidCount=0;
					AlertDialog.Builder builder = new AlertDialog.Builder(
							NeedView.this);
					builder.setMessage(
							R.string.error_tryingtoaddneed_andnotlocations)
							.setCancelable(false).setPositiveButton(
									R.string.msg_yes,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {
											NeedView.this.stupidCount++;
											if(NeedView.this.stupidCount == 1) {
												Intent intent = new Intent(
														NeedView.this,
														LocationView.class).putExtra(
																"ButComeBackToNeedViewForId",
																NeedView.this.mNeedId);
												startActivity(intent);
											}
										}
									}).setNegativeButton(R.string.msg_no,
											new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {

										}
									});
					AlertDialog alert = builder.create();
					alert.show();
					return;
				}
				curses.close();
				getDbAdapter().updateNeed(mNeedId,
						NeedView.this.mItem.getText().toString().trim(),
						NeedView.this.mDescription.getText().toString().trim(),NeedView.this.mPhoneID,!NeedView.this.mShareIt.isChecked());
				String itemTitle=INeedToo.isNothingNot(NeedView.this.mItemTitle)?NeedView.this.mItemTitle:NeedView.this.mItem.getText().toString();
				Intent intent = new Intent(NeedView.this,FnSearchLocation.class)
					.putExtra("needid", NeedView.this.mNeedId).putExtra("itemTitle", itemTitle)
					.putExtra("IsLocation",true);
				startActivity(intent);
				
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mItem.getWindowToken(), 0);

				if (NeedView.this.mWereDealingWithANewNeedHere) {
					NeedView.this.getDbAdapter().deleteNeed(mNeedId,mPhoneID,false);
					// TODO But what if they added some locations in the
					// interim?  I would have to keep track of original Need's Locations, & restore them.
				}
				NeedView.this.finish();
			}

		});
		confirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mItem.getWindowToken(), 0);

				
				if (INeedToo.isNothing(mItem.getText().toString().trim())) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							NeedView.this);
					builder.setMessage(R.string.error_itemmustnotbeempty)
					.setCancelable(false).setPositiveButton(
							R.string.msg_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog, int id) {
									mItem.requestFocus();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					if(thereBeNoLocationsDefinedForMe()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								NeedView.this);
						builder.setMessage(R.string.warning_therebenolocationsdefinedforme)
						.setCancelable(false).setPositiveButton(
								R.string.msg_yes,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int id) {
										dodoUpdate();
									}
								}).setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										return;										
									}
								});
						AlertDialog alert = builder.create();
						alert.show();
					} else {
						dodoUpdate();
				}
				}
			}
		});
		if(!getIntent().getBooleanExtra("CominFromLocationView", false)) {
			// 1=just contacts 2=justlocations 3=bothll
			Cursor curses2 = this.getDbAdapter().fetchAllLocationsPrimitive(3);
			if (curses2.getCount() == 0) {
				curses2.close();
				this.stupidCount=0;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						NeedView.this);				
				 builder.setMessage(R.string.msg_therearenolocationsdefined)
						.setCancelable(false)
						.setPositiveButton(
								R.string.msg_yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										NeedView.this.stupidCount++;
										if(NeedView.this.stupidCount == 1) {
										
											Intent intent = new Intent(
													NeedView.this,
													LocationView.class).putExtra(
															"ButComeBackToNeedViewForId",
															NeedView.this.mNeedId);
											startActivity(intent);
											NeedView.this.finish();
										}
									}
								})
						.setNegativeButton(R.string.msg_no,
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
			curses2.close();
		} else {
			long locationid=getIntent().getLongExtra("locationid",-1l);
			if(locationid!=-1l) {
			getDbAdapter().addNeedsLocation(
					locationid,
					null,
					NeedView.this.mNeedId,INeedToo.mSingleton.getPhoneId());
			}
			Intent i2 = new Intent(NeedView.this, NeedView.class)
				.putExtra("ComingInFresh", true)
				.putExtra("needid", mNeedId);
			startActivity(i2);
			NeedView.this.finish();			
		}
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			this.doOnCreate(savedInstanceState);
			/*bbhbb 2011-03-26*/		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));
		} catch (Exception e) {
			CustomExceptionHandler.logException(e,this);
		}
	}
	
	private void dodoUpdate() {
		getDbAdapter().updateNeed(mNeedId,
				mItem.getText().toString().trim(),
				mDescription.getText().toString().trim(),mPhoneID,!mShareIt.isChecked());
		if (!NeedView.this.mWereDealingWithANewNeedHere) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.msg_needUpdated),
					Toast.LENGTH_LONG).show();
			INeedToo.mSingleton.log("Need "+mItem.getText()+" created.",0);
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.msg_needCreated),
					Toast.LENGTH_LONG).show();
			INeedToo.mSingleton.log("Need "+mItem.getText()+" updated.",0);
		}
		Intent i2 = new Intent(NeedView.this, INeedToo.class);
		i2.putExtra("initialtabindex", (long) 0);
		startActivity(i2);
		NeedView.this.finish();
	
	}
	private boolean wereDealingWithACompanyHere(long id) {
		return id == 999999;
	}

	private boolean thereBeNoLocationsDefinedForMe() {
		boolean retness=true;
		Cursor curses=getDbAdapter().fetchLocationDescriptionsForNeed(mNeedId);
		if(curses.getCount()>0) {
			retness=false;
		}
		curses.close();
		return retness;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		if (!wereDealingWithACompanyHere(((AdapterContextMenuInfo) menuInfo).id)) {
			inflater.inflate(R.menu.needview_locations_context_menu, menu);
		} else {
			inflater.inflate(R.menu.needview_company_context_menu, menu);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
		.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.needview_menu_delete_need:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.alert_areyousuredeleteneedviewlocation)
			.setCancelable(false).setPositiveButton(R.string.msg_yes,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					mSimpleCursorAdapter.getCursor()
					.moveToPosition(info.position);
					String name = mSimpleCursorAdapter
					.getCursor().getString(
							mSimpleCursorAdapter
							.getCursor()
							.getColumnIndex(
							"Name"));
					NeedView.this.mDbAdapter
					.deleteNeedsLocation(info.id, name,
							mNeedId,mPhoneID);
					Intent i2 = new Intent(NeedView.this,
							NeedView.class).putExtra("ComingInFresh", true).putExtra("needid", mNeedId);
					startActivity(i2);
					NeedView.this.finish();
					Toast
					.makeText(
							getApplicationContext(),
							getString(R.string.msg_locationremovedfromneed),
							Toast.LENGTH_LONG).show();
					INeedToo.mSingleton.log("LocationID "+info.id+" removed from NeedID "+ mNeedId,0);
				}
			}).setNegativeButton(R.string.msg_no,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if(mWereDealingWithANewNeedHere) {
			if(INeedToo.mSingleton.isTestVersion()) {
				inflater.inflate(R.menu.needs_view_menu_for_new_needtrial,menu);
			} else {
				inflater.inflate(R.menu.needs_view_menu_for_new_need,menu);
			}
		} else {	
			if(INeedToo.mSingleton.isTestVersion()) {
				inflater.inflate(R.menu.need_viewtrial_menu, menu);
			} else {
				inflater.inflate(R.menu.need_view_menu, menu);
			}
		}
		return true;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DIALOG_ADDLOCATION:
				break;
			case DIALOG_ADDCONTACTFROMEXISTING:
				break;
			default:
				break;
		}
	}

	private AlertDialogHelper adh;

	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog dialog;
		switch (dialogId) {
		case DIALOG_ADDCONTACTFROMEXISTING:
			
			AlertDialog.Builder builderC = new AlertDialog.Builder(this);
			builderC.setTitle(R.string.needview_addcontact_dialog_title);
			adh = getDbAdapter()
			.fetchCompaniesAndLocationsForNeedAddingLocation(mNeedId,1,this);

			builderC.setMultiChoiceItems(adh.showStrings, adh.isAlreadySelected,
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which,
						boolean isChecked) {
					if (!isChecked) {
						long locationId=getDbAdapter().findLocationWhoseContactIdIs(NeedView.this.adh.ids[which]);
						getDbAdapter().deleteNeedsLocation(
								locationId,
								NeedView.this.adh.showStrings[which],
								NeedView.this.mNeedId,
								NeedView.this.mPhoneID);
					} else {
						long locationId=getDbAdapter().findLocationWhoseContactIdIs(NeedView.this.adh.ids[which]);
						if(locationId<0) {
							locationId=getDbAdapter().createLocation(NeedView.this.adh.showStrings[which], 
									"", "0","0", "", 5000, "", String.valueOf(NeedView.this.adh.ids[which]));
						}
						getDbAdapter().addNeedsLocation(
								locationId,
								NeedView.this.adh.showStrings[which],
								NeedView.this.mNeedId,NeedView.this.adh.phoneids[which]);
					}
				}
			});
			builderC.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					NeedView.this.dismissDialog(DIALOG_ADDCONTACTFROMEXISTING);
				}
			});
			dialog = builderC.create();
			dialog
			.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Intent i = new Intent(NeedView.this, NeedView.class)
					.putExtra("needid", NeedView.this.mNeedId).putExtra("ComingInFresh", true);
					startActivity(i);
					NeedView.this.finish();

				}
			});
			break;
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
		case DIALOG_ADDLOCATION:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.needview_addlocation_dialog_title);
			adh = getDbAdapter()
			.fetchCompaniesAndLocationsForNeedAddingLocation(mNeedId,2,this);

			builder.setMultiChoiceItems(adh.showStrings, adh.isAlreadySelected,
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which,
						boolean isChecked) {
					if (!isChecked) {
						getDbAdapter().deleteNeedsLocation(
								NeedView.this.adh.ids[which],
								NeedView.this.adh.showStrings[which],
								NeedView.this.mNeedId,
								NeedView.this.mPhoneID);
					} else {
						getDbAdapter().addNeedsLocation(
								NeedView.this.adh.ids[which],
								NeedView.this.adh.showStrings[which],
								NeedView.this.mNeedId,NeedView.this.adh.phoneids[which]);
					}
				}
			});
			builder.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					NeedView.this.dismissDialog(DIALOG_ADDLOCATION);
				}
			});
			dialog = builder.create();
			dialog
			.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Intent i = new Intent(NeedView.this, NeedView.class)
					.putExtra("needid", NeedView.this.mNeedId).putExtra("ComingInFresh", true);
					startActivity(i);
					NeedView.this.finish();

				}
			});
			break;
		default:
			return null;
		}
		return dialog;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.need_addnewlocation:
			Intent intent = new Intent(
					NeedView.this,
					LocationView.class).putExtra(
							"ButComeBackToNeedViewForId",
							NeedView.this.mNeedId);
			startActivity(intent);
			NeedView.this.finish();
			return true;
		case R.id.register_menu:
			Intent i4=new Intent(this,INeedToPay.class);
			startActivity(i4);
			this.finish();
			return true;
		case R.id.need_addlocation:
		case R.id.need_menu_search_location:
			// 1=just contacts 2=justlocations 3=bothll
			Cursor curses = this.getDbAdapter().fetchAllLocationsPrimitive(2);
			if (curses.getCount() == 0) {
				curses.close();
				stupidCount=0;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						NeedView.this);
				builder.setMessage(
						R.string.error_tryingtoaddneed_andnotlocations)
						.setCancelable(false).setPositiveButton(
								R.string.msg_yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										NeedView.this.stupidCount++;
										if(NeedView.this.stupidCount == 1) {
											Intent intent = new Intent(
													NeedView.this,
													LocationView.class).putExtra(
															"ButComeBackToNeedViewForId",
															NeedView.this.mNeedId);
											startActivity(intent);
										}
									}
								}).setNegativeButton(R.string.msg_no,
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
			curses.close();
			getDbAdapter().updateNeed(mNeedId,
					NeedView.this.mItem.getText().toString().trim(),
					NeedView.this.mDescription.getText().toString().trim(),NeedView.this.mPhoneID,!NeedView.this.mShareIt.isChecked());
			if(item.getItemId()==R.id.need_addlocation) {
				showDialog(DIALOG_ADDLOCATION);
			} else {
				String itemTitle=INeedToo.isNothingNot(NeedView.this.mItemTitle)?NeedView.this.mItemTitle:NeedView.this.mItem.getText().toString();
				Intent intentxx = new Intent(this,FnSearchLocation.class).putExtra("needid", this.mNeedId).putExtra("itemTitle", itemTitle);
				startActivity(intentxx);
			}
			return true;
		case R.id.need_menu_delete_need:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			amDoing2ndDialog=false;
			builder.setMessage(R.string.alert_areyousuredeleteneed)
			.setCancelable(false).setPositiveButton(R.string.msg_yes,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					String jeThisPhoneID=mPhoneID;
					String myPhoneID=INeedToo.mSingleton.getPhoneId();
					if(!mPhoneID.equals(INeedToo.mSingleton.getPhoneId())) {
						amDoing2ndDialog=true;
						AlertDialog.Builder builder22 = new AlertDialog.Builder(NeedView.this);
						builder22.setMessage(R.string.alert_foreignneeddeletetoo)
								.setCancelable(false)
								.setPositiveButton(R.string.msg_yes,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												NeedView.this.mDbAdapter.deleteNeed(mNeedId,mPhoneID,true);
												Intent i2 = new Intent(NeedView.this,
														INeedToo.class);
												i2.putExtra("initialtabindex", (int) 0);
												startActivity(i2);
												NeedView.this.finish();
												Toast
												.makeText(
														getApplicationContext(),
														getString(R.string.msg_needdeleted),
														Toast.LENGTH_LONG).show();
											}
										}
								)
								.setNegativeButton(R.string.msg_no, 
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,int id) {					
												NeedView.this.mDbAdapter.deleteNeed(mNeedId,mPhoneID,false);
												Intent i2 = new Intent(NeedView.this,
														INeedToo.class);
												i2.putExtra("initialtabindex", (int) 0);
												startActivity(i2);
												NeedView.this.finish();
												Toast
												.makeText(
														getApplicationContext(),
														getString(R.string.msg_needdeleted),
														Toast.LENGTH_LONG).show();
											}
										}
								)
								;
						AlertDialog alert=builder22.create();	
						alert.show();
					}
					if(!amDoing2ndDialog) {
						NeedView.this.mDbAdapter.deleteNeed(mNeedId,mPhoneID,false);
						Intent i2 = new Intent(NeedView.this,
								INeedToo.class);
						i2.putExtra("initialtabindex", (int) 0);
						startActivity(i2);
						NeedView.this.finish();
						Toast
						.makeText(
								getApplicationContext(),
								getString(R.string.msg_needdeleted),
								Toast.LENGTH_LONG).show();
					}
				}
			}).setNegativeButton(R.string.msg_no,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}

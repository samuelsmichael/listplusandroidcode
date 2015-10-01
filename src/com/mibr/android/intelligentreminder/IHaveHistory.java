package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class IHaveHistory extends ListActivity {
	private Date mFromDDate;
	private Date mThroughDDate;
	private EditText mFromDate;
	private EditText mThroughDate;
	private INeedDbAdapter mDbAdapter = null;

	private INeedDbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new INeedDbAdapter(this);
		}
		return mDbAdapter;
	}

	private Date deriveDate(String sdate) {
		Date date;
		if (INeedToo.isNothing(sdate)) {
			date = new Date();
		} else {
			try {
				date = new Date(Date.parse(sdate));
			} catch (Exception e) {
				date = new Date();
			}
		}
		return date;
	}

	private String myDateFormatter(Date date) {
		Calendar cf = Calendar.getInstance();
		cf.setTime(date);
		return new Formatter(Locale.getDefault()).format("%tD", cf).toString();
	}
	private String myDateFormatterWithTime(Date date) {
		Calendar cf = Calendar.getInstance();
		cf.setTime(date);
		String t1=new Formatter(Locale.getDefault()).format("%tD", cf).toString();
		String t2=new Formatter(Locale.getDefault()).format("%tT", cf).toString();
		return t1 + "\n" + t2;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Alert History");
/*bbhbb 2011-03-26*/		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(""));
		setContentView(R.layout.history_list);
		Button go = (Button) findViewById(R.id.history_buttongo);
		final EditText fromDate = (EditText) findViewById(R.id.history_from_date_edit);
		final EditText throughDate = (EditText) findViewById(R.id.history_through_date_edit);
		if (mFromDDate == null) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.add(GregorianCalendar.DAY_OF_YEAR, -1);
			fromDate.setText(myDateFormatter(gc.getTime()));
		}
		if (mThroughDDate == null) {
			throughDate.setText(myDateFormatter(new Date()));
		}
		go.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mFromDDate = deriveDate(fromDate.getText().toString());
				mThroughDDate = deriveDate(throughDate.getText().toString());
				fromDate.setText(myDateFormatter(mFromDDate));
				throughDate.setText(myDateFormatter(mThroughDDate));
				Cursor curses = getDbAdapter().getHistory(mFromDDate,
						mThroughDDate);
				startManagingCursor(curses);
				String[] from = new String[] { "DateCleared", "ItemName",
						"LocationName", "phoneidfulfilled", "description" };
				int[] to = new int[] { R.id.history_date,
						R.id.history_itemname, R.id.history_locationname, R.id.history_by,
						R.id.history_description };
				setListAdapter(new SimpleCursorAdapter(IHaveHistory.this,
						R.layout.history_list_list, curses, from, to)
				
				  {
				  
				  @Override public void setViewText(TextView v, String text) {
				  super.setViewText(v,convText(v,text)); } });
			}
		});
	}

	private String convText(TextView v, String text) {
		switch (v.getId()) {
		case R.id.history_date:
			try {
				SimpleDateFormat sdf=new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.S");
				Date jdDate=sdf.parse(text);
			return myDateFormatterWithTime(jdDate);
			} catch (Exception e) {
				return text;
			}
		case R.id.history_by:
			if(text.equals("ALERT")) {
				return "ALERT";
			} else {
				if(!text.equals("") && !text.equals(INeedToo.mSingleton.getPhoneId())) {
					return INeedToo.mSingleton.getNickName(text);
				} else {
					return "";
				}
			}
		default:
			break;
		}
		return text;
	}
}

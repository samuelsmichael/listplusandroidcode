package com.mibr.android.intelligentreminder;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {
	private IHaveLocations _caller;
	public MySimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, IHaveLocations caller) {
		super(context, layout, c, from, to);
		_caller=caller;
	}
	@Override
	public void setViewText(TextView v, String text) {
		super.setViewText(v, _caller.convText(v, text));
	}
}

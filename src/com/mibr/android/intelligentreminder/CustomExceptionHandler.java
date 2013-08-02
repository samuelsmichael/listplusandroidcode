package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;






import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler mDefaultUEH;
	private String mUrl;

	public CustomExceptionHandler(String url) {
		mUrl = url;
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	public static void logException(Throwable e, Context context) {

		INeedToo.mSingleton.log(e.getMessage(),4);
		StackTraceElement[] stea=e.getStackTrace();
		for(int c=0;c<stea.length;c++) {
			StackTraceElement ste=stea[c];
			String str=ste.toString();
			INeedToo.mSingleton.log(str,4);
		}
		INeedToo.mSingleton.transmitLog();
/*bbhbb 2011-03-26
		if(context!=null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("An error has occurred, and a log of this error has been sent to the manufacturer.  Look for a new release with the fixed version within 24 hours!")
			.setCancelable(false)
			.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
				}
			});
			AlertDialog alert=builder.create();	
			alert.show();
		}
*/
		//		INeedToo.mSingleton.clearLog();
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		INeedToo.mSingleton.log(e.getMessage(),4);
		StackTraceElement[] stea=e.getStackTrace();
		for(int c=0;c<stea.length;c++) {
			StackTraceElement ste=stea[c];
			String str=ste.toString();
			INeedToo.mSingleton.log(str,4);
		}
		INeedToo.mSingleton.transmitLog(true);
		/*
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stackTrace = result.toString();
		printWriter.close();
		INeedToo.mSingleton.log("Logging Halt", 3);
		INeedToo.mSingleton.transmitLog();
		 */
		mDefaultUEH.uncaughtException(t, e);
	}

}

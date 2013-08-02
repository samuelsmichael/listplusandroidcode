package com.mibr.android.intelligentreminder;

import java.lang.Thread.UncaughtExceptionHandler;

public class CustomExceptionHandlerTimer implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler mDefaultUEH;
	public CustomExceptionHandlerTimer() {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		try {
			INeedTimerServices.log(e.getMessage());
			StackTraceElement[] stea=e.getStackTrace();
			for(int c=0;c<stea.length;c++) {
				StackTraceElement ste=stea[c];
				String str=ste.toString();
				INeedTimerServices.log(str);
			}
			INeedToo.mSingleton.transmitLog(true);
		} catch (Exception ee) {}
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

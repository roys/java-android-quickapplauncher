package com.roysolberg.android.util;

import android.util.Log;

/**
 * Convenience class for logging to {@link Log}.
 *
 */
public class Logger {

	protected String tag;
	protected int logLevel;
	
	protected Logger(String tag, int logLevel){
		this.tag = tag;
		this.logLevel = logLevel;
	}
	
	/**
	 * Returns logger that logs to android.util.Log.
	 * 
	 * @param tag String with tag to use for logging.
	 * @param logLevel int with what level of logging should be sent to
	 * underlying logger. See constants of {@link Log}.
	 * @return Logger.
	 */
	public static Logger getLogger(String tag, int logLevel){
		return new Logger(tag, logLevel);
	}
	
	public void verbose(String message) {
		if(logLevel <= Log.VERBOSE)
			Log.v(tag, message);
	}
	
	public void verbose(String message, Throwable throwable) {
		if(logLevel <= Log.VERBOSE)
			Log.v(tag, message, throwable);
	}
	
	public void debug(String message) {
		if(logLevel <= Log.DEBUG)
			Log.d(tag, message);
	}
	
	public void debug(String message, Throwable throwable) {
		if(logLevel <= Log.DEBUG)
			Log.d(tag, message, throwable);
	}
	
	public void info(String message) {
		if(logLevel <= Log.INFO)
			Log.i(tag, message);
	}
	
	public void info(String message, Throwable throwable) {
		if(logLevel <= Log.INFO)
			Log.i(tag, message, throwable);
	}
	
	public void warn(String message) {
		if(logLevel <= Log.WARN)
			Log.w(tag, message);
	}
	
	public void warn(String message, Throwable throwable) {
		if(logLevel <= Log.WARN)
			Log.w(tag, message, throwable);
	}
	
	public void error(String message) {
		if(logLevel <= Log.ERROR)
			Log.e(tag, message);
	}
	
	public void error(String message, Throwable throwable) {
		if(logLevel <= Log.ERROR)
			Log.e(tag, message, throwable);
	}

}

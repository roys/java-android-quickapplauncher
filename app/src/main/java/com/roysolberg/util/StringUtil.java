package com.roysolberg.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StringUtil {

	public static boolean isEmpty(String str) {
		if(str == null || "".equals(str) || str.trim().equals(""))
			return true;
		return false;
	}

	public static boolean isEmail(String email) {
		if(isEmpty(email))
			return false;
		return email.contains("@") && email.trim().length() >= 3;
	}

	public static String toLowerCase(String str) {
		return str != null ? str.toLowerCase() : null;
	}

	public static String trim(String str) {
		return str != null ? str.trim() : null;
	}

	public static String substring(String str, int start, int end) {
		if(str == null)
			return null;
		return str.substring(start, Math.min(end, str.length()));
	}
	
	public static String urlEncode(String str){
		try {
			return str == null ? null : URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Got UnsupportedEncodingException while trying to url encode.", e);
		}
	}
	
	public static String urlDecode(String str){
		try {
			return str == null ? null : URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Got UnsupportedEncodingException while trying to url decode.", e);
		}
	}

}

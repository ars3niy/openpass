package com.example.openpass;

import android.util.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 2016-07-24.
 */
class TokenEncryption {
	public static byte[] getKey(String pin) {
		MessageDigest hash = null;
		try {
			hash = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		hash.update("ub39kub60kbd3xkiut49kxxb4tkb40xb5nx945xbic4".getBytes());
		hash.update(pin.getBytes());
		return hash.digest();
	}
	
	public static String encryptCode(String code, String pin) {
		byte[] key = getKey(pin);
		if (key == null)
			return null;

		String res_code = "";
		for (int i = 0; i < code.length(); i++) {
			int c = (code.getBytes()[i] ^ key[i % key.length]) & 0xff;
			if (res_code != "")
				res_code += " ";
			res_code += String.format("%x", c);
		}
		
		return res_code;
	}
	
	public static String decryptCode(String value, String pin) {
		byte[] key = getKey(pin);
		if (key == null)
			return null;
		
		String[] values = value.split("\\s+");
		char[] result = new char[values.length];
		for (int i = 0; i < values.length; i++)
			result[i] = (char)((byte)Integer.parseInt(values[i], 16) ^ key[i % key.length]);
		
		return String.valueOf(result);
	}

}

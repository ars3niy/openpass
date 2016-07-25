package com.example.openpass;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Vector;

/**
 * Created by user on 2016-07-24.
 */
class Settings {
	private static final String PREF_TOKENCOUNT = "tokencount";
	private static final String PREF_TOKENNAME = "name";
	private static final String PREF_TOKENCODE = "code";
	private static final String PREF_TOKEN_NEXTINDEX = "index";

	public static void saveTokens(Context ctx, final Vector<TokenInfo> tokens) {
		SharedPreferences prefs = ctx.getSharedPreferences("Tokens", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		
		editor.putInt(PREF_TOKENCOUNT, tokens.size());
		for (int i = 0; i < tokens.size(); i++) {
			editor.putString(PREF_TOKENNAME + i, tokens.elementAt(i).name);
			editor.putString(PREF_TOKENCODE + i, tokens.elementAt(i).encryptedCode);
			editor.putLong(PREF_TOKEN_NEXTINDEX + i, tokens.elementAt(i).nextSequenceIndex);
		}
		
		editor.commit();
	}

	public static Vector<TokenInfo> loadTokens(Context ctx) {
		Vector<TokenInfo>tokens = new Vector<>();

		SharedPreferences prefs = ctx.getSharedPreferences("Tokens", Context.MODE_PRIVATE);
		int tokencount = prefs.getInt(PREF_TOKENCOUNT, 0);
		for (int i = 0; i < tokencount; i++) {
			String name = prefs.getString(PREF_TOKENNAME + i, null);
			String code = prefs.getString(PREF_TOKENCODE + i, null);
			long nextIndex = prefs.getLong(PREF_TOKEN_NEXTINDEX + i, 0);
			if ((name != null) && (code != null))
				tokens.add(new TokenInfo(name, code, nextIndex));
		}
		
		return tokens;
	}

}

package com.example.openpass;

/**
 * Created by user on 2016-07-24.
 */
class TokenInfo {
	String name;
	String encryptedCode;
	long nextSequenceIndex;

	TokenInfo(String _name, String _code, long _number) {
		name = _name;
		encryptedCode = _code;
		nextSequenceIndex = _number;
	}
}

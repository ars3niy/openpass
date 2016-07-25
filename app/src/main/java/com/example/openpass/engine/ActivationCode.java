package com.example.openpass.engine;
import java.lang.Exception;

public class ActivationCode {
	private byte[] payload = null;
	
	public byte[] getEntropy() {
		return this.payload;
	}
	
	public boolean isValid() {return this.payload != null;}
	
	public ActivationCode(String key){
		if (key.length() != 0)
			tryConstructEntropyFromLegacyKey(key);
	}
	
	private boolean tryConstructEntropyFromLegacyKey(String key) {
		String normalized = Base32.validateAndNormalize(key);
		if (normalized.length() == 16) {
			this.payload = Base32.decode(normalized);
			//System.out.print("decoded: ");
			//for (byte b: this.payload)
			//	System.out.printf("%x ", (int)(b & 0xff));
			//System.out.println();
			return true;
		}
		return false;
	}
	
}

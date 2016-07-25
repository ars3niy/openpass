package com.example.openpass.engine;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MobilePass {
	public static String generateToken(String activation_key, long index) {
		return generateToken(activation_key, index, "");
	}
	
	static byte[] longTo8ByteArray(long a) {
		final int len = 8;
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++) {
			res[len-1-i] = (byte)a;
			a /= 0x100;
		}
		return res;
	}
	
	private static byte[] KDF1(MessageDigest hash, byte[] secret, byte[] iv,
			int start_position, int key_length) {
		int counter_start = 0;
		int counter = counter_start;

		int digest_size = hash.getDigestLength();
		int digests_required = (int) ((key_length + digest_size - 1) / digest_size);
		int digest_counter = 0;

		byte[] key = new byte[key_length];

		while (true) {
			if (digest_counter >= digests_required) {
				//print "KDF1:", reduce(lambda s1, s2: s1+s2, map(lambda x: "%.2x" % (x), list(key)))
				return key;
			}

			//System.out.print("KDF secret: ");
			//for (byte b: secret)
			//	System.out.printf("%x ", (int)(b & 0xff));
			//System.out.println();
			hash.update(secret);

			hash.update((byte)(counter >> 24));
			hash.update((byte)(counter >> 16));
			hash.update((byte)(counter >> 8));
			hash.update((byte)counter);

			if ((iv != null) && (iv.length > 0)) {
				//System.out.println("updating iv");
				hash.update(iv);
			}

			byte[] digest = hash.digest();
			//System.out.print("KDF difest: ");
			//for (byte b: digest)
			//	System.out.printf("%x ", (int)(b & 0xff));
			//System.out.println();
			//print "KDF1 digest =", hash.hexdigest()

			if (key_length > digest_size) {
				System.arraycopy(digest, 0, key, start_position, digest_size);
				start_position += digest_size;
				key_length -= digest_size;
			} else
				System.arraycopy(digest, 0, key, start_position, key_length);

			counter += 1;
			digest_counter += 1;
		}
	}
	
	private static byte[] getKey(byte[] entropy, String policy) {
		byte[] secret = entropy;
		if (policy.length() != 0)
			secret = policy.getBytes();
		
		MessageDigest hash = null;
		try {
			hash = MessageDigest.getInstance("SHA-256");
		} catch  (NoSuchAlgorithmException e) {
			return null;
		}
		return KDF1(hash, secret, null, 0, 32);
	}
	
	private static int truncateHash(byte[] bytes) {
		int offset = bytes[bytes.length-1] & 0xf;
		return ((bytes[offset] & 0x7f) << 24) | ((bytes[offset+1] & 0xff) << 16) |
	            ((bytes[offset+2] & 0xff) << 8) | ((bytes[offset+3] & 0xff));
	}
	
	public static String generateToken(String activation_key, long index, String policy) {
		byte[] message = longTo8ByteArray(index);
		ActivationCode code = new ActivationCode(activation_key);
		
		byte[] entropy = code.getEntropy();
		byte[] key = getKey(entropy, policy);
		if (key == null)
			return null;
		
		SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
		Mac mac = null;
		try {
			mac = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		try {
			mac.init(keySpec);
		} catch (InvalidKeyException e) {
			return null;
		}
        byte[] rawHmac = mac.doFinal(message);
        int truncated = truncateHash(rawHmac);
		
        String result = String.valueOf(truncated % 1000000);
		while (result.length() < 6)
			result = "0" + result;
		return result;
	}
}

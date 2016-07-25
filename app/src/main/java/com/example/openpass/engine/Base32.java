package com.example.openpass.engine;

import java.util.Random;

public class Base32 {
	private static final int invalidValue = 100;
	private static final int characterBits = 5;

	private static final byte[] characterValues = {
		100, 100, 26, 27, 28, 29, 30, 31, 100, 100, 100, 100, 100, 100,
		100, 100, 100, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 100, 100, 100, 100,
		100, 100, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 100, 100, 100, 100, 100
	};
	
	private static int characterOrdinalValue(char character) {
		String s = "";
		s += character;
		int character_ascii_value = s.toUpperCase().charAt(0);
		
		if (character_ascii_value == 48)
			character_ascii_value = 79;

		if (character_ascii_value == 49)
			character_ascii_value = 73;
		return character_ascii_value;
	}
	
	private static byte characterValue(char character) {
		int character_ordinal_value = characterOrdinalValue(character);
		int character_value_index = character_ordinal_value - 48;
		return characterValues[character_value_index];
	}

	public static boolean isBase32Character(char character) {
		int character_ascii_value = character;
		int character_value_index = character_ascii_value - 48;
		return (character_value_index >= 0) &&
				(character_value_index < characterValues.length) &&
				(characterValues[character_value_index] != invalidValue);
	}
	
	public static byte[] decode(String s) {
		int len = 0;
		int nbyte = (s.length() * characterBits)/8;
		byte[] result = new byte[nbyte];
		for (int i = 0; i < nbyte; i++)
			result[i] = 0;
		
		char[] chars = s.toCharArray();
		for (int pos = chars.length-1; pos >= 0; pos--) {
			char character = chars[pos];
			byte character_value = characterValue(character);
			for (int i = 0; i < characterBits; i++) {
				if ((character_value & 1) != 0)
					result[nbyte-1 - len/8] |= 1 << (len % 8);
				character_value >>= 1;
				len++;
			}
		}

		//System.out.print("decoded: ");
		//for (byte b: result)
		//	System.out.printf("%x ", (int)(b & 0xff));
		//System.out.println();

		return result;
	}
	
	public static String validateAndNormalize(String encoded_string) {
		if (encoded_string.length() == 0)
			return encoded_string;

		int checksum = 0;
		String normalized_string = "";

		int index = -1;
		for (char character: encoded_string.toCharArray()) {
			if (! isBase32Character(character))
				continue;

			index++;
			byte character_value = characterValue(character);
			int ordinal_value = characterOrdinalValue(character);

			if (index % 5 == 4) {
				if (character_value != checksum % 32)
					return "";
				else
					checksum = 0;
			} else {
				normalized_string += character;
				checksum += ordinal_value * (1 + index % 5);
			}
		}

		return normalized_string;
	}
	
	public static String generate20() {
		Random rng = new Random();
		
		char[] char_ordinalvalue_by_value = new char[32];
		for (int i = 0; i < characterValues.length; i++)
			if (characterValues[i] != invalidValue)
				char_ordinalvalue_by_value[characterValues[i]] = (char)(i + 48);
		
		String result = "";
		int checksum = 0;
		for (int index = 0; index < 20; index++) {
			int ordinal_value;
			if (index % 5 == 4) {
				ordinal_value = char_ordinalvalue_by_value[checksum % 32];
				checksum = 0;
			} else {
				ordinal_value = char_ordinalvalue_by_value[rng.nextInt(32)];
				checksum += ordinal_value * (1 + index % 5); 
			}
			
			if ((index % 5 == 0) && (index != 0))
				result += " ";
			result += String.valueOf((char)ordinal_value).toUpperCase();
		}
		
		return result;
	}
}


package com.preston159.save;

import java.io.File;

/**
 * An example class which stores and retrieves data
 * @author Preston Petrie
 */
public class Example {
	
	public static void main(String[] args) {
		Save s = new Save(
				new File("test.bin"), //file to which to save
				new Data("byte", DataType.BYTE, 1), //1 byte
				new Data("8bools", DataType.BOOLS_8, 1), //1 byte, 8 booleans
				new Data("12", DataType.INT_8BIT, 1), //1 byte, 8-bit signed integer
				new Data("12345", DataType.INT_16BIT, 1), //2 bytes, 16-bit signed integer
				new Data("128", DataType.UINT_8BIT, 1), //1 byte, 8-bit unsigned integer
				new Data("Z", DataType.CHAR_ASCII, 1), //1 byte, ascii character
				new Data("ABCDEFG", DataType.CHAR_ASCII, 7), //7 bytes, 7 ascii characters
				new Data("3_umlaut_u", DataType.CHAR_UNICODE, 3) //6 bytes, 3 unicode characters
		);
		s.storeByte("byte", (byte) 0x55); //store the byte 0x55 at "byte"
		s.storeBools("8bools", new boolean[] { true, true, true, true, false, false, false, true }); //store an array of bools at "8bools"
		s.storeInt("12", 12); //store the signed integer 12 at "12"
		s.storeInt("12345", 12345); //store the signed integer 12345 at "12345"
		s.storeUint("128", 128); //store the unsigned integer 128 at "128"
		s.storeChar("Z", 'Z');	//store the character 'Z' at "Z"
		s.storeString("ABCDEFG", "ABCDEFG"); //store the string "ABCDEFG" at "ABCDEFG"
		s.storeString("3_umlaut_u", "üüf"); //store the string "üüf" at "3_umlaut_u"
		String u = s.getString("3_umlaut_u"); //retrieve the string at "3_umlaut_u"
		s.storeString("3_umlaut_u", u.replace('f', 'ü')); //replace the 'f' in the string with an 'ü'
		s.store(); //write data to file
		System.out.println(Integer.toHexString(s.getByte("byte"))); //retrieve the byte at "byte" and print in hex
		boolean[] bools = s.getBools("8bools"); //retrieve the boolean[] at "8bools" and print
		for(int i = 0;i < bools.length;i++) {
			System.out.print(bools[i] + " ");
		}
		System.out.println();
		System.out.println(s.getInt("12")); //print the signed integer at "12"
		System.out.println(s.getInt("12345")); //print the signed integer at "12345"
		System.out.println(s.getUint("128")); //print the unsigned integer at "128"
		System.out.println(s.getChar("Z")); //print the character at "Z"
		System.out.println(s.getString("ABCDEFG")); //print the string at "ABCDEFG"
		System.out.println(s.getString("3_umlaut_u")); //print the string at "3_umlaut_u"
	}
	
}

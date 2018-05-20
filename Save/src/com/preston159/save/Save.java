package com.preston159.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Preston Petrie
 */
public class Save {
	
	private File f;
	private byte[] bytes;
	
	private StorageData sd;
	
	/**
	 * Create a {@code Save} object
	 * @param f		The {@code File} in which to store the data
	 * @param data	An array specifying the data types and their names
	 */
	public Save(File f, Data... data) {
		this.f = f;
		if(!f.exists()) {
			createFile();
		}
		sd = new StorageData(data.length);
		int len = 0;
		for(int i = 0;i < data.length;i++) {
			Data d = data[i];
			len += (d.getLength() * d.getType().getLength());
			sd.setDataAt(i, d.getName(), d.getLength(), d.getType());
		}
		bytes = new byte[len];
		loadFile();
	}
	
	/**
	 * Creates the save file
	 */
	private void createFile() {
		try {
			f.createNewFile();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Loads the information from the save file
	 */
	private void loadFile() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(bytes);
			fis.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Stores the information in the save file
	 */
	public void store() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			fos.write(bytes);
			fos.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * <p>Gets a {@code byte} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.BYTE}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@code getBytes()} to get all values</p>
	 * @param name	The name of the data
	 * @return		The {@code byte} stored
	 */
	public byte getByte(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || type != DataType.BYTE) {
			throw new InvalidSearchException();
		}
		return bytes[start];
	}
	
	/**
	 * <p>Gets a {@code byte[]} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.BYTE}</p>
	 * @param name	The name of the data
	 * @return		The {@code byte[]} stored
	 */
	public byte[] getBytes(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || type != DataType.BYTE) {
			throw new InvalidSearchException();
		}
		byte[] out = new byte[sd.getLengthOf(name)];
		for(int i = 0;i < out.length;i++) {
			out[i] = bytes[start + i];
		}
		return out;
	}
	
	/**
	 * <p>Gets a {@code boolean} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.BOOL}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@code getBools()} to get all values</p>
	 * @param name	The name of the data
	 * @return		The {@code boolean} stored
	 */
	public boolean getBool(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || type != DataType.BOOL) {
			throw new InvalidSearchException();
		}
		byte b = bytes[start];
		return b == 0xff;
	}
	
	/**
	 * <p>Gets a {@code boolean[]} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.BOOL} OR {@code DataType.BOOLS_8}</p>
	 * @param name	The name of the data
	 * @return		The {@code boolean[]} stored
	 */
	public boolean[] getBools(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.BOOL && type != DataType.BOOLS_8)) {
			throw new InvalidSearchException();
		}
		int len = sd.getLengthOf(name);
		if(type == DataType.BOOL) {
			return getLongBools(start, len);
		}
		return getShortBools(start, len);
	}
	
	private boolean[] getLongBools(int start, int len) {
		boolean[] out = new boolean[len];
		for(int i = 0;i < len;i++) {
			int pos = start + i;
			out[i] = bytes[pos] == 0xff;
		}
		return out;
	}
	
	private boolean[] getShortBools(int start, int len) {
		boolean[] out = new boolean[len * 8];
		for(int i = 0;i < len;i++) {
			int pos = start + i;
			for(int j = 0;j < 8;j++) {
				byte b = bytes[pos];
				b = (byte) (b << j);
				out[(i * 8) + j] = (b & 0b10000000) == 0b10000000;
			}
		}
		return out;
	}
	
	/**
	 * <p>Gets a signed integer from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.INT_8BIT} OR {@code DataType.INT_16BIT} OR {@code DataType.INT_24BIT} OR {@code DataType.INT_32BIT}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>There currently does not exist a function to get multiple integer values</p>
	 * @param name	The name of the data
	 * @return		The signed integer stored
	 */
	public int getInt(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.INT_8BIT && type != DataType.INT_16BIT
				&& type != DataType.INT_24BIT && type != DataType.INT_32BIT)) {
			throw new InvalidSearchException();
		}
		int out = 0;
		switch(type) {
		case INT_32BIT:
			out += bytes[start + 3] << 24 & 0xff;
			out += bytes[start + 2] << 16 & 0xff;
			out += bytes[start + 1] << 8 & 0xff;
			out += bytes[start] & 0xff;
			break;
		case INT_24BIT:
			out += bytes[start + 2] << 16;
			out += bytes[start + 1] << 8 & 0xff;
			out += bytes[start] & 0xff;
			break;
		case INT_16BIT:
			out += bytes[start + 1] << 8;
			out += bytes[start] & 0xff;
			break;
		default:
			out += bytes[start];
			break;
		}
		return out;
	}
	
	/**
	 * <p>Gets an unsigned integer from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.UINT_8BIT} OR {@code DataType.UINT_16BIT} OR {@code DataType.UINT_24BIT}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>There currently does not exist a function to get multiple integer values</p>
	 * @param name	The name of the data
	 * @return		The unsigned integer stored
	 */
	public int getUint(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.UINT_8BIT && type != DataType.UINT_16BIT
				&& type != DataType.UINT_24BIT)) {
			throw new InvalidSearchException();
		}
		return getUint(start, type);
	}
	
	private int getUint(int start, DataType type) {
		int out = 0;
		switch(type) {
		case UINT_24BIT:
			out += bytes[start + 2] << 16 & 0xff;
		case UINT_16BIT:
			out += bytes[start + 1] << 8 & 0xff;
		default:
			out += bytes[start] & 0xff;
			break;
		}
		return out;
	}
	
	/**
	 * <p>Gets a {@code char} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.CHAR_ASCII} OR {@code DataType.CHAR_UNICODE}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@code getString()} to get all values</p>
	 * @param name	The name of the data
	 * @return		The {@code char} stored
	 */
	public char getChar(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.CHAR_ASCII && type != DataType.CHAR_UNICODE)) {
			throw new InvalidSearchException();
		}
		if(type == DataType.CHAR_ASCII) {
			return (char) (bytes[start] & 0b01111111);
		}
		return getUnicodeChar(start);
	}
	
	private char getUnicodeChar(int start) {
		int out = 0;
		out += bytes[start + 1] << 8 & 0xff;
		out += bytes[start] & 0xff;
		return (char) out;
	}
	
	/**
	 * <p>Gets a {@code String} from the file with the specified name</p>
	 * <p>Data must be of type {@code DataType.CHAR_ASCII} OR {@code DataType.CHAR_UNICODE}</p>
	 * @param name	The name of the data
	 * @return		The {@code String} stored
	 */
	public String getString(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.CHAR_ASCII && type != DataType.CHAR_UNICODE)) {
			throw new InvalidSearchException();
		}
		int len = sd.getLengthOf(name);
		if(type == DataType.CHAR_ASCII) {
			return getASCIIString(start, len);
		}
		return getUnicodeString(start, len);
	}
	
	private String getASCIIString(int start, int len) {
		String out = "";
		for(int i = 0;i < len;i++) {
			out += (char) bytes[start + i];
		}
		out = out.replace("\0", "");
		return out;
	}
	
	private String getUnicodeString(int start, int len) {
		String out = "";
		for(int i = 0;i < len;i += 2) {
			out += (char) (getUint(start + i, DataType.UINT_16BIT));
		}
		return out;
	}
	
	
	/**
	 * <p>Stores a {@code byte} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.BYTE}</p>
	 * @param name	The name of the data
	 * @param data	The {@code byte} to store
	 */
	public void storeByte(String name, byte data) {
		storeBytes(name, new byte[] { data });
	}
	
	/**
	 * <p>Stores a {@code byte[]} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.BYTE}</p>
	 * @param name	The name of the data
	 * @param data	The {@code byte[]} to store
	 */
	public void storeBytes(String name, byte[] data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || type != DataType.BYTE) {
			throw new InvalidSearchException();
		}
		int len = sd.getLengthOf(name);
		if(data.length > len) {
			System.out.println("WARNING: STORING DATA IN " + name + " WHICH WILL BE TRUNCATED");
		}
		for(int i = 0;i < len;i++) {
			if(data.length <= i) {
				bytes[start + i] = 0;
			} else {
				bytes[start + i] = data[i];
			}
		}
	}
	
	private void storeBytes(int start, byte[] data, int dlen) {
		if(data.length > dlen) {
			System.out.println("WARNING: STORING DATA AT BYTE INDEX " + start + " WHICH WILL BE TRUNCATED");
		}
		for(int i = 0;i < dlen;i++) {
			if(data.length <= i) {
				bytes[start + i] = 0;
			} else {
				bytes[start + i] = data[i];
			}
		}
	}
	
	/**
	 * <p>Stores a {@code boolean} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.BOOL}</p>
	 * @param name	The name of the data
	 * @param data	The {@code boolean} to store
	 */
	public void storeBool(String name, boolean data) {
		storeBools(name, new boolean[] { data });
	}
	
	/**
	 * <p>Stores a {@code boolean[]} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.BOOL} OR {@code DataType.BOOLS_8}</p>
	 * <p>If Data is of type {@code DataType.BOOLS_8}, the array's size must be a multiple of 8</p>
	 * @param name	The name of the data
	 * @param data	The {@code boolean[]} to store
	 */
	public void storeBools(String name, boolean[] data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.BOOL && type != DataType.BOOLS_8)) {
			throw new InvalidSearchException();
		}
		int len = sd.getLengthOf(name);
		if(type == DataType.BOOL) {
			storeLongBools(start, data, len);
		} else {
			storeShortBools(start, data, len);
		}
	}
	
	private void storeLongBools(int start, boolean[] data, int dlen) {
		int len = data.length;
		byte[] store = new byte[len];
		for(int i = 0;i < len;i++) {
			store[i] = data[i] ? (byte) 0xff : (byte) 0x00;
		}
		storeBytes(start, store, dlen);
	}
	
	private void storeShortBools(int start, boolean[] data, int dlen) {
		if(data.length % 8 != 0) {
			throw new IllegalArgumentException("Number of booleans for type BOOLS_8 must be a multiple of 8");
		}
		int len = data.length / 8;
		byte[] store = new byte[len];
		for(int i = 0;i < len;i++) {
			byte b = 0;
			for(int j = 0;j < 8;j++) {
				byte c = data[(i * 8) + j] ? (byte) 1 : (byte) 0;
				c = (byte) (c << (7 - j));
				b = (byte) (b | c);
			}
			store[i] = b;
		}
		storeBytes(start, store, dlen);
	}
	
	/**
	 * <p>Stores a signed integer in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.INT_8BIT} OR {@code DataType.INT_16BIT} OR {@code DataType.INT_24BIT} OR {@code DataType.INT_32BIT}</p>
	 * @param name	The name of the data
	 * @param data	The signed integer to store
	 */
	public void storeInt(String name, int data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.INT_8BIT && type != DataType.INT_16BIT
				&& type != DataType.INT_24BIT && type != DataType.INT_32BIT)) {
			throw new InvalidSearchException();
		}
		if(type == DataType.INT_8BIT) {
			storeBytes(start, new byte[] { (byte) (data & 0xff) }, 1);
			return;
		}
		if(type == DataType.INT_16BIT) {
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff) }, 2);
			return;
		}
		if(type == DataType.INT_24BIT) {
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff) }, 3);
			return;
		}
		storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff) }, 4);
	}
	
	/**
	 * <p>Stores an unsigned integer in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.UINT_8BIT} OR {@code DataType.UINT_16BIT} OR {@code DataType.UINT_24BIT}</p>
	 * @param name	The name of the data
	 * @param data	The unsigned integer to store
	 */
	public void storeUint(String name, int data) {
		if(data < 0) {
			throw new IllegalArgumentException("Can't store negative number in a UINT");
		}
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.UINT_8BIT && type != DataType.UINT_16BIT
				&& type != DataType.UINT_24BIT)) {
			throw new InvalidSearchException();
		}
		storeUint(start, type, data);
	}
	
	private void storeUint(int start, DataType type, int data) {
		if(type == DataType.UINT_8BIT) {
			storeBytes(start, new byte[] { (byte) (data & 0xff) }, 1);
			return;
		}
		if(type == DataType.UINT_16BIT) {
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff) }, 2);
			return;
		}
		storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff) }, 3);
	}
	
	/**
	 * <p>Stores a {@code char} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.CHAR_ASCII} OR {@code DataType.CHAR_UNICODE}</p>
	 * @param name	The name of the data
	 * @param data	The {@code char} to store
	 */
	public void storeChar(String name, char data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.CHAR_ASCII && type != DataType.CHAR_UNICODE)) {
			throw new InvalidSearchException();
		}
		if(type == DataType.CHAR_ASCII) {
			storeBytes(start, new byte[] { (byte) (data & 0b01111111) }, 1);
			return;
		}
		//storeBytes(start, new byte[] { (byte) (data >> 8 & 0xff), (byte) (data & 0xff) }, 2);
		storeUint(start, DataType.UINT_16BIT, (int) data);
	}
	
	/**
	 * <p>Stores a {@code String} in the file at the specified name</p>
	 * <p>Data must be of type {@code DataType.CHAR_ASCII} OR {@code DataType.CHAR_UNICODE}</p>
	 * @param name	The name of the data
	 * @param data	The {@code String} to store
	 */
	public void storeString(String name, String data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		int dlen = sd.getLengthOf(name);
		if(start == -1 || (type != DataType.CHAR_ASCII && type != DataType.CHAR_UNICODE)) {
			throw new InvalidSearchException();
		}
		//handle unicode
		int strlen = data.length();
		if(type == DataType.CHAR_ASCII) {
			byte[] store = new byte[strlen];
			for(int i = 0;i < strlen;i++) {
				store[i] = (byte) (data.charAt(i) & 0b01111111);
			}
			storeBytes(start, store, dlen);
		} else {
			if(strlen * 2 > dlen) {
				System.out.println("WARNING: STORING DATA AT BYTE INDEX " + start + " WHICH WILL BE TRUNCATED");
			}
			for(int i = 0;i < dlen;i += 2) {
				int j = i / 2;
				if(j >= strlen) {
					storeUint(start + i, DataType.UINT_16BIT, 0);
				} else {
					storeUint(start + i, DataType.UINT_16BIT, (int) data.charAt(j));
				}
			}
		}
	}

}

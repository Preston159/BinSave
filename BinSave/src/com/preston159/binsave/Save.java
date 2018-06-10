
package com.preston159.binsave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.zip.DataFormatException;

/**
 * Stores save data
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
	 * <p>Data must be of type {@link DataType#BYTE BYTE}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@link #getBytes(String) getBytes} to get all values</p>
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
	 * <p>Data must be of type {@link DataType#BYTE BYTE}</p>
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
	 * <p>Data must be of type {@link DataType#BOOL BOOL}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@link #getBools(String) getBools} to get all values</p>
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
	 * <p>Data must be of type {@link DataType#BOOL BOOL} OR {@link DataType#BOOLS_8 BOOLS_8}</p>
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
	 * <p>Data must be of type {@link DataType#INT_8BIT INT_8BIT} OR {@link DataType#INT_16BIT INT_16BIT} OR {@link DataType#INT_24BIT INT_24BIT} OR {@link DataType#INT_32BIT INT_32BIT}</p>
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
		return getInt(start, type);
	}
	
	private int getInt(int start, DataType type) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		switch(type) {
		case INT_32BIT:
			buffer.put(0, bytes[start + 3]);
			buffer.put(1, bytes[start + 2]);
			buffer.put(2, bytes[start + 1]);
			buffer.put(3, bytes[start]);
			break;
		case INT_24BIT:
			if((bytes[start + 2] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
				buffer.put(1, (byte) (bytes[start + 2]));
			} else {
				buffer.put(1, bytes[start + 2]);
			}
			buffer.put(2, bytes[start + 1]);
			buffer.put(3, bytes[start]);
			break;
		case INT_16BIT:
			if((bytes[start + 1] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
				buffer.put(1, (byte) 0xff);
				buffer.put(2, (byte) (bytes[start + 1]));
			} else {
				buffer.put(2, bytes[start + 1]);
			}
			buffer.put(3, bytes[start]);
			break;
		default:
			if((bytes[start] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
				buffer.put(1, (byte) 0xff);
				buffer.put(2, (byte) 0xff);
				buffer.put(3, (byte) (bytes[start]));
			} else {
				buffer.put(3, bytes[start]);
			}
			break;
		}
		return buffer.getInt();
	}
	
	/**
	 * <p>Gets a signed integer from the file with the specified name</p>
	 * <p>Data must be of type {@link DataType#INT_8BIT INT_8BIT} OR {@link DataType#INT_16BIT INT_16BIT} OR {@link DataType#INT_24BIT INT_24BIT} OR
	 * {@link DataType#INT_32BIT INT_32BIT} OR {@link DataType#INT_40BIT INT_40BIT} OR {@link DataType#INT_48BIT INT_48BIT} OR
	 * {@link DataType#INT_56BIT INT_56BIT} OR {@link DataType#INT_64BIT INT_64BIT}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>There currently does not exist a function to get multiple integer values</p>
	 * @param name	The name of the data
	 * @return		The signed integer stored
	 */
	public long getLongInt(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.INT_8BIT && type != DataType.INT_16BIT && type != DataType.INT_24BIT && type != DataType.INT_32BIT &&
				type != DataType.INT_40BIT && type != DataType.INT_48BIT && type != DataType.INT_56BIT && type != DataType.INT_64BIT)) {
			throw new InvalidSearchException();
		}
		ByteBuffer buffer = ByteBuffer.allocate(8);
		ByteBuffer sb = ByteBuffer.allocate(4);
		int si = getInt(start, DataType.INT_32BIT);
		sb.putInt(si);
		byte[] sa = sb.array();
		buffer.put(new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, sa[0], sa[1], sa[2], sa[3] });
		switch(type) {
		case INT_32BIT:
		case INT_24BIT:
		case INT_16BIT:
		case INT_8BIT:
			break;
		case INT_64BIT:
			buffer.put(0, bytes[start + 7]);
			buffer.put(1, bytes[start + 6]);
			buffer.put(2, bytes[start + 5]);
			buffer.put(3, bytes[start + 4]);
			break;
		case INT_56BIT:
			if((bytes[start + 6] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
			}
			buffer.put(1, bytes[start + 6]);
			buffer.put(2, bytes[start + 5]);
			buffer.put(3, bytes[start + 4]);
			break;
		case INT_48BIT:
			if((bytes[start + 5] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
				buffer.put(1, (byte) 0xff);
			}
			buffer.put(2, bytes[start + 5]);
			buffer.put(3, bytes[start + 4]);
			break;
		default:
			if((bytes[start + 4] & 0b10000000) == 0b10000000) {
				buffer.put(0, (byte) 0xff);
				buffer.put(1, (byte) 0xff);
				buffer.put(2, (byte) 0xff);
			}
			buffer.put(3, bytes[start + 4]);
			break;
		}
		return buffer.getLong(0);
	}
	
	/**
	 * <p>Gets an unsigned integer from the file with the specified name</p>
	 * <p>Data must be of type {@link DataType#UINT_8BIT UINT_8BIT} OR {@link DataType#UINT_16BIT UINT_16BIT} OR {@link DataType#UINT_24BIT UINT_24BIT}</p>
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
		ByteBuffer buffer = ByteBuffer.allocate(4);
		switch(type) {
		case UINT_24BIT:
			buffer.put(1, bytes[start + 2]);
		case UINT_16BIT:
			buffer.put(2, bytes[start + 1]);
		default:
			buffer.put(3, bytes[start]);
			break;
		}
		return buffer.getInt();
	}
	
	/**
	 * <p>Gets an unsigned integer from the file with the specified name</p>
	 * <p>Data must be of type {@link DataType#UINT_8BIT UINT_8BIT} OR {@link DataType#UINT_16BIT UINT_16BIT} OR {@link DataType#UINT_24BIT UINT_24BIT}
	 * OR {@link DataType#UINT_32BIT UINT_32BIT} OR {@link DataType#UINT_40BIT UINT_40BIT} OR {@link DataType#UINT_48BIT UINT_48BIT} OR
	 * {@link DataType#UINT_56BIT UINT_56BIT}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>There currently does not exist a function to get multiple integer values</p>
	 * @param name	The name of the data
	 * @return		The unsigned integer stored
	 */
	public long getLongUint(String name) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.UINT_8BIT && type != DataType.UINT_16BIT && type != DataType.UINT_24BIT && type != DataType.UINT_32BIT &&
				type != DataType.UINT_40BIT && type != DataType.UINT_48BIT && type != DataType.UINT_56BIT)) {
			throw new InvalidSearchException();
		}
		return getLongUint(start, type);
	}
	
	private long getLongUint(int start, DataType type) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		switch(type) {
		case UINT_56BIT:
			buffer.put(1, bytes[start + 6]);
		case UINT_48BIT:
			buffer.put(2, bytes[start + 5]);
		case UINT_40BIT:
			buffer.put(3, bytes[start + 4]);
		case UINT_32BIT:
			buffer.put(4, bytes[start + 3]);
		case UINT_24BIT:
			buffer.put(5, bytes[start + 2]);
		case UINT_16BIT:
			buffer.put(6, bytes[start + 1]);
		default:
			buffer.put(7, bytes[start]);
			break;
		}
		return buffer.getLong();
	}
	
	/**
	 * <p>Gets a {@code char} from the file with the specified name</p>
	 * <p>Data must be of type {@link DataType#CHAR_ASCII CHAR_ASCII} OR {@link DataType#CHAR_UNICODE CHAR_UNICODE}</p>
	 * <p>If the length of the data stored at the specified name is larger than 1, returns only the first value</p>
	 * <p>Use {@link #getString(String) getString} to get all values</p>
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
	 * <p>Data must be of type {@link DataType#CHAR_ASCII CHAR_ASCII} OR {@link DataType#CHAR_UNICODE CHAR_UNICODE}</p>
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
	 * <p>Data must be of type {@link DataType#BYTE BYTE}</p>
	 * @param name	The name of the data
	 * @param data	The {@code byte} to store
	 */
	public void storeByte(String name, byte data) {
		storeBytes(name, new byte[] { data });
	}
	
	/**
	 * <p>Stores a {@code byte[]} in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#BYTE BYTE}</p>
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
	 * <p>Data must be of type {@link DataType#BOOL BOOL}</p>
	 * @param name	The name of the data
	 * @param data	The {@code boolean} to store
	 */
	public void storeBool(String name, boolean data) {
		storeBools(name, new boolean[] { data });
	}
	
	/**
	 * <p>Stores a {@code boolean[]} in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#BOOL BOOL} OR {@link DataType#BOOLS_8 BOOLS_8}</p>
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
	 * <p>Data must be of type {@link DataType#INT_8BIT INT_8BIT} OR {@link DataType#INT_16BIT INT_16BIT} OR {@link DataType#INT_24BIT INT_24BIT} OR
	 * {@link DataType#INT_32BIT INT_32BIT}</p>
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
	 * <p>Stores a signed integer in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#INT_8BIT INT_8BIT} OR {@link DataType#INT_16BIT INT_16BIT} OR {@link DataType#INT_24BIT INT_24BIT} OR
	 * {@link DataType#INT_32BIT INT_32BIT} OR {@link DataType#INT_40BIT INT_40BIT} OR {@link DataType#INT_48BIT INT_48BIT} OR
	 * {@link DataType#INT_56BIT INT_56BIT} OR {@link DataType#INT_64BIT INT_64BIT}</p>
	 * @param name	The name of the data
	 * @param data	The signed integer to store
	 */
	public void storeLongInt(String name, long data) {
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1 || (type != DataType.INT_8BIT && type != DataType.INT_16BIT && type != DataType.INT_24BIT && type != DataType.INT_32BIT &&
				type != DataType.INT_40BIT && type != DataType.INT_48BIT && type != DataType.INT_56BIT && type != DataType.INT_64BIT)) {
			throw new InvalidSearchException();
		}
		switch(type) {
		case INT_8BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff) }, 1);
			return;
		case INT_16BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff) }, 2);
			return;
		case INT_24BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff) }, 3);
			return;
		case INT_32BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff) }, 4);
			return;
		case INT_40BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff) }, 5);
			return;
		case INT_48BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff), (byte) (data >> 40 & 0xff) }, 6);
			return;
		case INT_56BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff), (byte) (data >> 40 & 0xff), (byte) (data >> 48 & 0xff) }, 7);
			return;
		default:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff), (byte) (data >> 40 & 0xff), (byte) (data >> 48 & 0xff), (byte) (data >> 56 & 0xff) }, 8);
		}
	}
	
	/**
	 * <p>Stores an unsigned integer in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#UINT_8BIT UINT_8BIT} OR {@link DataType#UINT_16BIT UINT_16BIT} OR {@link DataType#UINT_24BIT UINT_24BIT}</p>
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
	 * <p>Stores an unsigned integer in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#UINT_8BIT UINT_8BIT} OR {@link DataType#UINT_16BIT UINT_16BIT} OR {@link DataType#UINT_24BIT UINT_24BIT}
	 * OR {@link DataType#UINT_32BIT UINT_32BIT} OR {@link DataType#UINT_40BIT UINT_40BIT} OR {@link DataType#UINT_48BIT UINT_48BIT} OR
	 * {@link DataType#UINT_56BIT UINT_56BIT}</p>
	 * @param name	The name of the data
	 * @param data	The unsigned integer to store
	 */
	public void storeLongUint(String name, long data) {
		if(data < 0) {
			throw new IllegalArgumentException("Can't store negative number in a UINT");
		}
		int start = sd.getStartOf(name);
		DataType type = sd.getTypeOf(name);
		if(start == -1) {
			throw new InvalidSearchException();
		}
		if(type == DataType.UINT_8BIT || type == DataType.UINT_16BIT || type == DataType.UINT_24BIT) {
			storeUint(start, type, (int) data);
		} else if(type == DataType.UINT_32BIT || type == DataType.UINT_40BIT || type == DataType.UINT_48BIT || type == DataType.UINT_56BIT) {
			storeLongUint(start, type, data);
		} else {
			throw new InvalidSearchException();
		}
	}
	
	private void storeLongUint(int start, DataType type, long data) {
		switch(type) {
		case UINT_32BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff) }, 4);
			break;
		case UINT_40BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff) }, 5);
			break;
		case UINT_48BIT:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff), (byte) (data >> 40 & 0xff) }, 6);
			break;
		default:
			storeBytes(start, new byte[] { (byte) (data & 0xff), (byte) (data >> 8 & 0xff), (byte) (data >> 16 & 0xff), (byte) (data >> 24 & 0xff),
					(byte) (data >> 32 & 0xff), (byte) (data >> 40 & 0xff), (byte) (data >> 48 & 0xff) }, 7);
			break;
		}
	}
	
	/**
	 * <p>Stores a {@code char} in the file at the specified name</p>
	 * <p>Data must be of type {@link DataType#CHAR_ASCII CHAR_ASCII} OR {@link DataType#CHAR_UNICODE CHAR_UNICODE}</p>
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
	 * <p>Data must be of type {@link DataType#CHAR_ASCII CHAR_ASCII} OR {@link DataType#CHAR_UNICODE CHAR_UNICODE}</p>
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
	
	/**
	 * Convert this {@code Save} object to a {@code Properties} object
	 * @return	The {@code Properties} object
	 */
	public Properties convertToProperties() {
		Properties p = new Properties();
		String[] names = sd.getNames();
		for(String name : names) {
			String data = "";
			switch(sd.getTypeOf(name)) {
			case BYTE:
				byte[] bytes = getBytes(name);
				for(byte b : bytes) {
					data += "0x" + Integer.toHexString(b & 0xff) + ";";
				}
				data = data.substring(0, data.length() - 1);
				break;
			case BOOL:
			case BOOLS_8:
				boolean[] bools = getBools(name);
				for(boolean b : bools) {
					data += b + ";";
				}
				data = data.substring(0, data.length() - 1);
				break;
			case INT_8BIT:
			case INT_16BIT:
			case INT_24BIT:
			case INT_32BIT:
				data = String.valueOf(getInt(name));
				break;
			case UINT_8BIT:
			case UINT_16BIT:
			case UINT_24BIT:
				data = String.valueOf(getUint(name));
				break;
			case CHAR_ASCII:
			case CHAR_UNICODE:
				data = getString(name);
			}
			p.setProperty(name, data);
		}
		return p;
	}
	
	/**
	 * Loads this object's data from a {@code Properties} object
	 * @param p	The properties object from which to load
	 * @throws DataFormatException	if data in the specified {@code Properties} cannot be correctly decoded
	 */
	public void loadFromProperties(Properties p) throws DataFormatException {
		String[] names = sd.getNames();
		for(String name : names) {
			switch(sd.getTypeOf(name)) {
			case BYTE:
				String[] bd = p.getProperty(name, "0x00").split(";");
				byte[] bytes = new byte[bd.length];
				for(int i = 0;i < bd.length;i++) {
					try {
						bytes[i] = (byte) Integer.parseInt(bd[i].replaceFirst("0x", ""), 16);
					} catch(NumberFormatException nfe) {
						throw new DataFormatException("Invalid byte data at key \"" + name + "\" index " + i);
					}
				}
				storeBytes(name, bytes);
				break;
			case BOOL:
			case BOOLS_8:
				String[] arr = p.getProperty(name, "false").split(";");
				boolean[] bools = new boolean[arr.length];
				for(int i = 0;i < bools.length;i++) {
					try {
						bools[i] = parseBoolean(arr[i]);
					} catch(DataFormatException dfe) {
						throw new DataFormatException("Invalid boolean data at key \"" + name + "\" index " + i);
					}
				}
				storeBools(name, bools);
				break;
			case INT_8BIT:
			case INT_16BIT:
			case INT_24BIT:
			case INT_32BIT:
				int i;
				try {
					i = Integer.parseInt(p.getProperty(name, "0"));
				} catch(NumberFormatException nfe) {
					throw new DataFormatException("Invalid signed integer data at key \"" + name + "\"");
				}
				storeInt(name, i);
				break;
			case UINT_8BIT:
			case UINT_16BIT:
			case UINT_24BIT:
				int u;
				try {
					u = Integer.parseInt(p.getProperty(name, "0"));
				} catch(NumberFormatException nfe) {
					throw new DataFormatException("Invalid unsigned integer data at key \"" + name + "\"");
				}
				storeUint(name, u);
				break;
			case CHAR_ASCII:
			case CHAR_UNICODE:
				storeString(name, p.getProperty(name, "\0"));
			}
		}
	}
	
	private static boolean parseBoolean(String s) throws DataFormatException {
		if(s.equalsIgnoreCase("true")) {
			return true;
		} else if(s.equalsIgnoreCase("false")) {
			return false;
		} else {
			throw new DataFormatException("Invalid boolean string");
		}
	}

}

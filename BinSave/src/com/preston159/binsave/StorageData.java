package com.preston159.binsave;

/**
 * Store storage data for a {@code Save} object
 * @author Preston Petrie
 */
public class StorageData {
	
	private int len;
	
	private String[] names;
	private int[] lens;
	private DataType[] types;
	private int[] start;
	private boolean calculated = false;
	
	/**
	 * Construct a {@code StorageData} object
	 * @param len	The number of datapoints
	 */
	public StorageData(int len) {
		this.len = len;
		names = new String[len];
		lens = new int[len];
		types = new DataType[len];
		start = new int[len];
	}
	
	/**
	 * Gives the data stored at a specied index a name, byte length, and type
	 * @param i		The index of the datapoint
	 * @param name	The name of the datapoint
	 * @param len	The byte length of the datapoint
	 * @param type	The type of the datapoint
	 */
	public void setDataAt(int i, String name, int len, DataType type) {
		names[i] = name;
		lens[i] = len * type.getLength();
		types[i] = type;
	}
	
	/**
	 * Calculate the starting byte index of each datapoint
	 */
	public void calc() {
		start[0] = 0;
		for(int i = 1;i < len;i++) {
			start[i] = start[i - 1] + lens[i - 1];
		}
		calculated = true;
	}
	
	/**
	 * Get the starting byte index of a specified datapoint
	 * @param name	The name of the datapoint
	 * @return	The starting byte index of the datapoint with the specified name
	 */
	public int getStartOf(String name) {
		if(!calculated) {
			calc();
		}
		for(int i = 0;i < len;i++) {
			if(names[i].equals(name)) {
				return start[i];
			}
		}
		return -1;
	}
	
	/**
	 * Get the byte length of the specified datapoint
	 * @param name	The name of the datapoint
	 * @return	The byte length of the datapoint with the specified name
	 */
	public int getLengthOf(String name) {
		if(!calculated) {
			calc();
		}
		for(int i = 0;i < len;i++) {
			if(names[i].equals(name)) {
				return lens[i];
			}
		}
		return -1;
	}
	
	/**
	 * Get the type of the specified datapoint
	 * @param name	The name of the datapoint
	 * @return	The byte length of the datapoint with the specified name
	 */
	public DataType getTypeOf(String name) {
		if(!calculated) {
			calc();
		}
		for(int i = 0;i < len;i++) {
			if(names[i].equals(name)) {
				return types[i];
			}
		}
		return null;
	}
	
	/**
	 * Get an array containing the names of the data stored
	 * @return	An array containing the names of the data stored
	 */
	public String[] getNames() {
		String[] out = new String[names.length];
		for(int i = 0;i < out.length;i++) {
			out[i] = names[i];
		}
		return out;
	}
	
}

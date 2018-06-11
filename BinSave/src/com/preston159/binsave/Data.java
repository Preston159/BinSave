package com.preston159.binsave;

/**
 * Stores the name, type, and number of objects at a datapoint, for use in constructing a {@code Save} object
 * @author Preston Petrie
 */
public class Data {
	
	private String name;
	private DataType type;
	private int len;
	
	/**
	 * Construct a {@code Data} object
	 * @param name	The name of the datapoint
	 * @param type	The type of the datapoint
	 * @param len	The length of the datapoint (number of type stored)
	 */
	public Data(String name, DataType type, int len) {
		this.name = name;
		this.type = type;
		this.len = len;
	}
	
	/**
	 * Construct a {@code Data} object with a length of 1
	 * @param name	The name of the datapoint
	 * @param type	The type of the datapoint
	 */
	public Data(String name, DataType type) {
		this(name, type, 1);
	}
	
	/**
	 * Get the name of the datapoint
	 * @return	The name of the datapoint
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the type of the datapoint
	 * @return	The type of the datapoint
	 */
	public DataType getType() {
		return type;
	}
	
	/**
	 * Get the length of the datapoint
	 * @return	The length of the datapoint
	 */
	public int getLength() {
		return len;
	}
	
}

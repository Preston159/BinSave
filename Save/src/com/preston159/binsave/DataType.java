package com.preston159.binsave;

/**
 * The storage type of a datapoint
 * @author Preston Petrie
 */
public enum DataType {
	
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> a byte</p>
	 */
	BYTE(1),
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> a boolean</p>
	 */
	BOOL(1),
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> 8 booleans</p>
	 */
	BOOLS_8(1),
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> an 8-bit signed integer</p>
	 */
	INT_8BIT(1),
	/**
	 * <p><strong>Length:</strong> 2 byts</p>
	 * <p><strong>Stores:</strong> a 16-bit signed integer</p>
	 */
	INT_16BIT(2),
	/**
	 * <p><strong>Length:</strong> 3 bytes</p>
	 * <p><strong>Stores:</strong> a 24-bit signed integer</p>
	 */
	INT_24BIT(3),
	/**
	 * <p><strong>Length:</strong> 4 byte</p>
	 * <p><strong>Stores:</strong> a 32-bit signed integer</p>
	 */
	INT_32BIT(4),
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> an 8-bit unsigned integer</p>
	 */
	UINT_8BIT(1),
	/**
	 * <p><strong>Length:</strong> 2 bytes</p>
	 * <p><strong>Stores:</strong> a 16-bit unsigned integer</p>
	 */
	UINT_16BIT(2),
	/**
	 * <p><strong>Length:</strong> 3 bytes</p>
	 * <p><strong>Stores:</strong> a 24-bit unsigned integer</p>
	 */
	UINT_24BIT(3),
	/**
	 * <p><strong>Length:</strong> 1 byte</p>
	 * <p><strong>Stores:</strong> an ASCII character</p>
	 */
	CHAR_ASCII(1),
	/**
	 * <p><strong>Length:</strong> 2 bytes</p>
	 * <p><strong>Stores:</strong> a Unicode character</p>
	 */
	CHAR_UNICODE(2)
	
	;
	
	private final int len;
	
	DataType(final int len) {
		this.len = len;
	}
	
	public int getLength() {
		return len;
	}
	
}
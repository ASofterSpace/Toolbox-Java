/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import com.asofterspace.toolbox.Utils;


/**
 * A simple and easy to use buffer of bytes
 *
 * @author Moya (a softer space), 2019
 */
public class ByteBuffer {

	private int len;

	private int filled;

	private byte[] buf;


	public ByteBuffer() {

		len = 1024;

		filled = 0;

		buf = new byte[len];
	}

	private void doubleLength() {

		int newLen = 2 * len;
		byte[] newBuf = new byte[newLen];

		System.arraycopy(buf, 0, newBuf, 0, len);

		buf = newBuf;
		len = newLen;
	}

	public void append(byte nextByte) {

		filled++;

		if (filled > len) {
			doubleLength();
		}

		buf[filled - 1] = nextByte;
	}

	public int getLength() {

		return filled;
	}

	public byte[] toArray() {

		byte[] result = new byte[filled];

		System.arraycopy(buf, 0, result, 0, filled);

		return result;
	}

	public String toString() {

		return new String(toArray(), Utils.BINARY_CHARSET);
	}

}

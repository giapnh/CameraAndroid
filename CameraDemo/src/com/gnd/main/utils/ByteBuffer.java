package com.gnd.main.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ByteBuffer
 * 
 * @author TBComputer
 */
public final class ByteBuffer {

	public byte[] arr;
	private int limit;
	public int position = 0;

	public ByteBuffer() {
		this(20);
	}

	public ByteBuffer(int initialCapacity) {
		this(new byte[initialCapacity]);
	}

	public ByteBuffer(byte[] buffer) {
		this.arr = buffer;
		this.limit = this.arr.length;
	}

	public ByteBuffer(InputStream input) throws IOException {
		int len;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		arr = new byte[1024];
		while ((len = input.read(arr, 0, 1024)) != -1) {
			bos.write(arr, 0, len);
		}
		arr = bos.toByteArray();
		position = 0;
		limit = arr.length;
		bos.close();
	}

	public static short d(byte[] shortBytes) {
		return (short) (shortBytes[0] << 8 | shortBytes[1] & 0xFF);
	}

	public final byte[] getArray() {
		return this.arr;
	}

	/**
	 * Returns this buffer's capacity. </putInt>
	 * 
	 * @return The capacity of this buffer
	 */
	public final int capacity() {
		return this.arr.length;
	}

	public final void ensureCapacity(int extra) {
		if ((extra = this.position + extra) > this.arr.length) {
			extra = Math.max(extra, this.arr.length * 175 / 100);
			byte[] arrayOfByte = this.arr;
			this.arr = new byte[extra];
			System.arraycopy(arrayOfByte, 0, this.arr, 0, arrayOfByte.length);
			this.limit = this.arr.length;
		}
	}

	/**
	 * Flips this buffer. The limit is set to the current position and then the
	 * position is set to zero. If the mark is defined then it is discarded.
	 * Change state to read
	 */
	public final void flip() {
		this.limit = this.position;
		this.position = 0;
	}

	/**
	 * Copy data from buffer to array
	 * 
	 * @param array
	 */
	public final void get(byte[] array) {
		get(array, 0, array.length);
	}

	/**
	 * /** get(byte[] arr,int offset,int length)<br>
	 * copy data from buffer to arr
	 * 
	 * @param array
	 * @param offset
	 * @param length
	 */
	public final void get(byte[] array, int offset, int length) {
		if (this.limit - this.position >= length) {
			System.arraycopy(this.arr, this.position, array, offset, length);
			skip(length);
			return;
		}
		throw new IllegalStateException("underflow");
	}

	public final boolean getBoolean() {
		return getByte() != 0;
	}

	/**
	 * =getByte() <br>
	 * get the next byte in buffer
	 * 
	 * @return next byte value
	 */
	public final byte getByte() {
		if (this.limit > this.position) {
			return this.arr[(this.position++)];
		}
		throw new IllegalStateException("underflow");
	}

	/**
	 * @return the int value at buffer's current position
	 */
	public final int getInt() {
		if (this.limit >= 4 + this.position) {
			return this.arr[(this.position++)] << 24
					| (this.arr[(this.position++)] & 0xFF) << 16
					| (this.arr[(this.position++)] & 0xFF) << 8
					| this.arr[(this.position++)] & 0xFF;
		}
		throw new IllegalStateException("underflow");
	}

	/**
	 * getLong()<br>
	 * 
	 * @return the long value at buffer's current position
	 */
	public final long getLong() {
		if (this.limit >= 8 + this.position) {
			long l1 = (long) getInt() << 32;
			long l2 = getInt() & 0xFFFFFFFFL;
			return l1 | l2;
		}
		throw new IllegalStateException("underflow");
	}

	public final short getShort() {
		if (this.limit >= 2 + this.position) {
			return (short) (this.arr[(this.position++)] << 8 | this.arr[(this.position++)] & 0xFF);
		}
		throw new IllegalStateException("underflow");
	}

	public final String getString() {
		int len = getShort();
		String s = new String(arr, position, len);
		position += len;
		return s;
	}

	public final String getUTF() {
		int len = getShort();
		if (len > this.remaining()) {
			throw new IllegalStateException("underflow");
		}
		if (len == 0) {
			return "";
		}
		String rs = readUTF(this.arr, this.position, len);
		this.skip(len);
		return rs;

		// try {
		// String rs= null;
		// int z= len>5000?1: 1;
		// long l1= System.currentTimeMillis();
		// while(z--> 0){
		// rs= new String(arr, position, len, "UTF-8");
		// rs = readUTF(this.arr, this.position, len);
		// // skip(len);
		// }
		// if(len> 5000)
		// System.out.println("Time to read: "+
		// (System.currentTimeMillis()-l1));
		// skip(len);
		// return rs;
		// } catch (Exception ex) {
		// throw new IllegalStateException("unable encode");
		// }
	}

	/**
	 * Returns this buffer's position. </putInt>
	 * 
	 * @return The position of this buffer
	 */
	public final int position() {
		return this.position;
	}

	/**
	 * Sets this buffer's position. If the mark is defined and larger than the
	 * new position then it is discarded. </putInt>
	 * 
	 * @param newPosition
	 *            The new position value; must be non-negative and no larger
	 *            than the current limit
	 * 
	 * @return This buffer
	 * 
	 * @throws IllegalArgumentException
	 *             If the preconditions on <tt>newPosition</tt> do not hold
	 */
	public final void position(int newpos) {
		if ((newpos >= 0) && (newpos <= this.limit)) {
			this.position = newpos;
			return;
		}
		throw new IllegalArgumentException("Illegal position");
	}

	/**
	 * wrap(byte arr,int offset,int length)<br>
	 * copy data from arr to buffer
	 * 
	 * @param arr
	 */
	public final void put(byte[] array) {
		wrap(array, 0, array.length);
	}

	public final void putBuffer(ByteBuffer buf) {
		putBuffer(buf, buf.remaining());
	}

	/**
	 * copy data from Buffer to current buffer
	 * 
	 * @param buf
	 * @param length
	 */
	public final void putBuffer(ByteBuffer buf, int length) {
		length = Math.min(length, buf.remaining());
		if ((buf instanceof ByteBuffer)) {
			wrap(((ByteBuffer) buf).arr, ((ByteBuffer) buf).position, length);
			((ByteBuffer) buf).skip(length);
			return;
		}
		byte[] tmp = new byte[length];
		buf.get(tmp);
		put(tmp);
	}

	/**
	 * wrap(byte arr,int offset,int length)<br>
	 * copy data from arr to buffer
	 * 
	 * @param array
	 * @param offset
	 * @param length
	 */
	public final void wrap(byte[] array, int offset, int length) {
		ensureCapacity(length);
		if (this.limit - this.position >= length) {
			System.arraycopy(array, offset, this.arr, this.position, length);
			skip(length);
			return;
		}
		throw new IllegalStateException("overflow");
	}

	public final void putBoolean(boolean paramBoolean) {
		put((byte) (paramBoolean ? 1 : 0));
	}

	/**
	 * Put showTextBox byte to array
	 * 
	 * @param paramByte
	 */
	public final void put(byte paramByte) {
		ensureCapacity(1);
		if (this.limit - this.position > 0) {
			this.arr[(this.position++)] = paramByte;
			return;
		}
		throw new IllegalStateException("overflow");
	}

	public final void putInt(int value, int pos) {
		if (pos < 0 || pos > limit) {
			throw new IllegalArgumentException(pos + ">" + position);
		}
		if (this.limit - this.position >= 4) {
			this.arr[pos] = (byte) (value >> 24);
			this.arr[pos + 1] = ((byte) (value >> 16));
			this.arr[pos + 2] = ((byte) (value >> 8));
			this.arr[pos + 3] = ((byte) value);
			return;
		}
		throw new IllegalStateException("overflow");
	}

	public final void putInt(int value) {
		ensureCapacity(4);
		if (this.limit - this.position >= 4) {
			this.arr[(this.position++)] = (byte) (value >> 24);
			this.arr[(this.position++)] = ((byte) (value >> 16));
			this.arr[(this.position++)] = ((byte) (value >> 8));
			this.arr[(this.position++)] = ((byte) value);
			return;
		}
		throw new IllegalStateException("overflow");
	}

	public final void putLong(long value) {
		// ensureCapacity(8);
		// if (this.limit - this.position >= 8) {
		// int i = (int) (value >> 32);
		// value = (int) value;
		// this.arr[(this.position++)] = (byte) (i >> 24);
		// this.arr[(this.position++)] = (byte) (i >> 16);
		// this.arr[(this.position++)] = (byte) (i >> 8);
		// this.arr[(this.position++)] = (byte) i;
		// this.arr[(this.position++)] = (byte) (value >> 24);
		// this.arr[(this.position++)] = (byte) (value >> 16);
		// this.arr[(this.position++)] = (byte) (value >> 8);
		// this.arr[(this.position++)] = (byte) (value);
		// return;
		// }
		// throw new IllegalStateException("overflow");
		if (position + 8 > limit) {
			ensureCapacity(position + 8);
		}
		int high = (int) (value >>> 32);
		int low = (int) value;
		arr[position] = (byte) (high >> 24);
		arr[position + 1] = (byte) (high >> 16);
		arr[position + 2] = (byte) (high >> 8);
		arr[position + 3] = (byte) high;
		arr[position + 4] = (byte) (low >> 24);
		arr[position + 5] = (byte) (low >> 16);
		arr[position + 6] = (byte) (low >> 8);
		arr[position + 7] = (byte) low;
		position += 8;
	}

	public final void putShort(short value) {
		// ensureCapacity(2);
		// if (this.limit - this.position >= 2) {
		// this.arr[(this.position++)] = ((byte) (value >> 8));
		// this.arr[(this.position++)] = ((byte) value);
		// return;
		// }
		// throw new IllegalStateException("overflow");
		if (position + 2 > limit) {
			ensureCapacity(position + 4);
		}
		arr[position++] = (byte) (value >> 8);
		arr[position++] = (byte) value;
		return;
	}

	/**
	 * Put UTF-8 String to Buffer
	 * 
	 * @param str
	 */
	public final void putUTF(String str) {
		int i;
		byte[] arrayOfByte = new byte[(i = str == null ? 0 : str.length()) << 2];
		int j = 0;
		for (int k = 0; (k < i) && (str != null); k++) {
			int m;
			if (((m = str.charAt(k)) >= 0) && (m < 128)) {
				arrayOfByte[(j++)] = ((byte) m);
			} else if ((m > 127) && (m < 2048)) {
				arrayOfByte[(j++)] = ((byte) (m >>> 6 | 0xC0));
				arrayOfByte[(j++)] = ((byte) (m & 0x3F | 0x80));
			} else if ((m > 2047) && (m < 65536)) {
				arrayOfByte[(j++)] = ((byte) (m >>> 12 | 0xE0));
				arrayOfByte[(j++)] = ((byte) (m >>> 6 & 0x3F | 0x80));
				arrayOfByte[(j++)] = ((byte) (m & 0x3F | 0x80));
			} else if ((m > 65535) && (m < 1048575)) {
				arrayOfByte[(j++)] = 63;
			}
		}
		if (j <= 65535) {
			this.putShort((short) j);
			this.wrap(arrayOfByte, 0, j);
			return;
		}
		throw new IllegalArgumentException("string too long");
	}

	/**
	 * Put asci string to buffer
	 * 
	 * @param str
	 */
	public final void putString(String str) {
		byte[] data = str.getBytes();
		putShort((short) data.length);
		put(data);
	}

	/**
	 * Returns the number of elements between the current position and the
	 * limit. </putInt>
	 * 
	 * @return The number of elements remaining in this buffer
	 */
	public final int remaining() {
		return this.limit - this.position;
	}

	public final void skip(int paramInt) {
		ensureCapacity(paramInt);
		position(this.position + paramInt);
	}

	public final byte[] toByteArray() {
		byte[] arrayOfByte = new byte[limit];
		System.arraycopy(this.arr, 0, arrayOfByte, 0, limit);
		return arrayOfByte;
	}

	/**
	 * Read UTF String
	 * 
	 * @param arrData
	 * @param offset
	 * @param length
	 * @return
	 */
	private static String readUTF(byte[] arrData, int offset, int length) {
		// long t1= System.currentTimeMillis();
		char[] charset = new char[length];
		length = offset + length;
		int count = 0;
		while (offset < length) {
			int ch;
			if (((ch = arrData[(offset++)] & 0xFF) & 0x80) == 0) {
				charset[(count++)] = ((char) ch);
			} else if ((ch & 0xE0) == 192) {
				charset[(count++)] = ((char) ((ch & 0x1F) << 6 | arrData[(offset++)] & 0x3F));
			} else if ((ch & 0xF0) == 224) {
				charset[(count++)] = ((char) ((ch & 0xF) << 12
						| (arrData[(offset++)] & 0x3F) << 6 | arrData[(offset++)] & 0x3F));
			} else {
				if ((ch & 0xF8) == 240) {
					offset += 3;
				}
				charset[(count++)] = '?';
			}
		}
		return new String(charset, 0, count);
	}

	public int limit() {
		return limit;
	}
}

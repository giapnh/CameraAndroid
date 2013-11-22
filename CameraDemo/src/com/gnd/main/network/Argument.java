package com.gnd.main.network;

import java.lang.reflect.Field;

import com.gnd.main.utils.ByteBuffer;

public class Argument {

	public static final byte LONG = 8;
	public static final byte INT = 4;
	public static final byte SHORT = 2;
	public static final byte BYTE = 1;
	// Raw data
	public static final byte STRING = 10;
	public static final byte RAW = 11;

	public byte type;
	public long numberValue;
	public String stringValue;
	public byte[] byteValue;

	public Argument() {
	}

	public short read(ByteBuffer in) throws Exception {
		short code = in.getShort();
		type = in.getByte();
		if (type < 10) {
			switch (type) {
			case LONG:
				numberValue = in.getLong();
				break;
			case BYTE:
				numberValue = in.getByte();
				break;
			case SHORT:
				numberValue = in.getShort();
				break;
			case INT:
				numberValue = in.getInt();
				break;
			}
		} else {
			int size = in.getInt();
			byte[] b = new byte[size];
			in.get(b, 0, size);
			switch (type) {
			case STRING:
				stringValue = new String(b, "utf-8");
				break;
			default:
				byteValue = b;
				break;
			}
		}
		return code;
	}

	public Argument(byte type) {
		this.type = type;
	}

	public Argument(byte type, long value) {
		this(type);
		this.numberValue = value;
	}

	public Argument(String value) {
		this(STRING);
		this.stringValue = value;
	}

	public Argument(byte[] value) {
		this(RAW);
		this.byteValue = value;
	}

	public void setNumberValue(long numberValue) {
		this.numberValue = numberValue;
	}

	public void setByteValue(byte[] byteValue) {
		this.byteValue = byteValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public String toString() {
		String s = "";
		switch (type) {
		case SHORT:
			s += "short: " + (short) numberValue;
			break;
		case INT:
			s += "int: " + (int) numberValue;
			break;
		case STRING:
			s += "String: " + stringValue;
			break;
		case RAW:
			s += "Raw: " + byteValue.length;
			break;
		case BYTE:
			s += "byte: " + (byte) numberValue;
			break;
		case LONG:
			s += "long: " + numberValue;
			break;
		}
		return s;
	}

	public void write(ByteBuffer des) {
		des.put(type);
		switch (type) {
		case SHORT:
			des.putShort((short) numberValue);
			break;
		case INT:
			des.putInt((int) numberValue);
			break;
		case STRING:
			des.putShort((short) 0);
			des.putUTF(stringValue);
			break;
		case RAW:
			des.putInt(byteValue.length);
			des.put(byteValue);
			break;
		case BYTE:
			des.put((byte) numberValue);
			break;
		case LONG:
			des.putLong(numberValue);
			break;
		}
	}

	// for debug
	public static String getArgumentAsString(short argCode) {
		Field[] fields = Argument.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				if (fields[i].getShort(null) == argCode) {
					return fields[i].getName();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "unknown";
	}

	public static final short ARG_TEXT = 1;
	public static final short ARG_IMAGE = 2;

	public static final short ARG_GAME_CONFIG = 3;
	public static final short ARG_TIME = 4;
}

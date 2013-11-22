package com.gnd.main.network;

import java.io.EOFException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;

import com.gnd.main.utils.ByteBuffer;

public class Command {
	private static byte encrypt = 0;
	public short code;
	Hashtable<Short, Argument> args = new Hashtable<Short, Argument>();
	public Command next;

	public void read(InputStream in) throws Exception {
		byte[] header = read(in, 4);
		int len = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16)
				| ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
		if (len > 1024000 || len < 0)
			throw new Exception("Data len failure: " + len);
		int encrypt = in.read();
		ByteBuffer buff = new ByteBuffer(readAndDecrypt(in, len, encrypt));
		read(buff);
		Command cursor = this;
		while (buff.remaining() > 0) {
			cursor = cursor.createNext();
			cursor.read(buff);
		}
	}

	public Command clone() {
		Command cmd = new Command(code);
		Enumeration<Short> keys = args.keys();
		while (keys.hasMoreElements()) {
			Short code = keys.nextElement();
			Argument arg = args.get(code);
			cmd.addArgument(code, arg);
		}
		return cmd;
	}

	public void trim() {
		next = null;
	}

	public byte[] read(InputStream in, int len) throws Exception {
		byte[] b = new byte[len];
		int offset = 0;
		while (len > 0) {
			int result = in.read(b, offset, len);
			if (result < 0) {
				throw new EOFException();
			}
			offset += result;
			len -= result;
		}
		return b;
	}

	private byte[] readAndDecrypt(InputStream in, int len, int encrypt)
			throws Exception {
		return descript(read(in, len), encrypt);
	}

	public byte[] descript(byte[] data, int encrypt) {
		// temp
		return data;
	}

	public byte[] toBytes() {
		ByteBuffer buff = new ByteBuffer();
		write(buff);
		buff.flip();
		byte[] data = encrypt(buff.toByteArray());
		buff = new ByteBuffer();
		buff.putInt(data.length);
		buff.put(encrypt);
		buff.put(data);
		buff.flip();
		return buff.toByteArray();
	}

	private byte[] encrypt(byte[] src) {
		// TODO
		return src;
	}

	public Command() {
	}

	public Command(short code) {
		this.code = code;
	}

	public Command createNext() {
		next = new Command();
		return next;
	}

	public Command createNext(short code) {
		next = new Command(code);
		return next;
	}

	public void setCode(short code) {
		this.code = code;
	}

	public void read(ByteBuffer in) throws Exception {
		code = in.getShort();
		int num = in.getByte();
		for (int i = 0; i < num; i++) {
			Argument arg = new Argument();
			short code = arg.read(in);
			addArgument(code, arg);
		}
	}

	public Command addArgument(short code, Argument arg) {
		args.put(code, arg);
		return this;
	}

	public Command addShort(short code, long value) {
		return addArgument(code, new Argument(Argument.SHORT, value));
	}

	public byte[] getRaw(short code) {
		Argument arg = args.get(code);
		if (arg != null)
			return arg.byteValue;
		return null;
	}

	public String getString(short code, String def) {
		Argument arg = args.get(code);
		if (arg != null)
			return arg.stringValue;
		return def;
	}

	public int getInt(short code, long def) {
		Argument arg = args.get(code);
		if (arg != null)
			return (int) arg.numberValue;
		return (int) def;
	}

	public short getShort(short code, long def) {
		Argument arg = args.get(code);
		if (arg != null)
			return (short) arg.numberValue;
		return (short) def;
	}

	public long getLong(short code, long def) {
		Argument arg = args.get(code);
		if (arg != null)
			return arg.numberValue;
		return def;
	}

	public byte getByte(short code, long def) {
		Argument arg = args.get(code);
		if (arg != null)
			return (byte) arg.numberValue;
		return (byte) def;
	}

	public Command addInt(short code, long value) {
		return addArgument(code, new Argument(Argument.INT, value));
	}

	public Command addByte(short code, long value) {
		return addArgument(code, new Argument(Argument.BYTE, value));
	}

	public Command addLong(short code, long value) {
		return addArgument(code, new Argument(Argument.LONG, value));
	}

	public Command addString(short code, String value) {
		return addArgument(code, new Argument(value));
	}

	public Command addRaw(short code, byte[] value) {
		return addArgument(code, new Argument(value));
	}

	public void write(ByteBuffer buff) {
		buff.putShort(code);
		buff.put((byte) args.size());
		Enumeration<Short> keys = args.keys();
		while (keys.hasMoreElements()) {
			Short code = keys.nextElement();
			Argument arg = args.get(code);
			buff.putShort(code);
			arg.write(buff);
		}
		if (next != null)
			next.write(buff);
	}

	@Override
	public String toString() {
		String s = "************Command: " + getCommandName(code) + "[" + code
				+ "]\n";
		Enumeration<Short> keys = args.keys();
		while (keys.hasMoreElements()) {
			Short code = keys.nextElement();
			Argument arg = args.get(code);
			s += "    " + Argument.getArgumentAsString(code) + "[" + code + "]"
					+ arg.toString() + "\n";
		}
		s += "\n";
		if (next != null)
			s += "next\n" + next.toString();
		return s;
	}

	// for debug
	public static String getCommandName(short _command) {
		Field[] fields = Command.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				if (fields[i].getShort(null) == _command) {
					return fields[i].getName();
				}
			} catch (Exception e) {
			}
		}
		return "unknown";
	}

	public static final short CMD_SEND_TEXT = 0;
	public static final short CMD_SEND_IMAGE = 1;
	public static final short CMD_SEND_GAME = 2;

}

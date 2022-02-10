package io.github.imagemsg;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

public class DataContainer {
	Random rand = new Random();
	Encryption crypt;

	File srcFile = null;
	String filename = null;
	byte[] payload;
	int size = 0;

	public DataContainer(Encryption crypt) {
		this.crypt = crypt;
	}

	public void setFile(String filename) {
		srcFile = new File(filename) ;
		this.filename = srcFile.getName();
	}

	public Biterator getBiterator() {
		return new Biterator(payload,0,payload.length ) ;
	}
	
	public void encrypt() {
		payload = crypt.encrypt(payload);
	}

	public void decryptHeader() {
		byte[] bar = crypt.decrypt(payload, 0, 16);
		int fname_length = ((int) bar[0]) & 0xff;
		int block_length = 1 + fname_length + 4;
		int pad = 16 - (block_length % 16);
		bar = crypt.decrypt(payload, 0, block_length + pad);
		//System.out.println("encrypt() " + hex(bar, 0, bar.length));

		filename=null;
		if (fname_length > 0) {
			filename = new String(bar, 1, fname_length);
			System.out.println("decrypt() filename: " + filename);
		}
		size = ti(bar, 1 + fname_length, 4);
		System.out.println("decrypt() size: " + size);

	}

	public void decryptAll() {
		payload = crypt.decrypt(payload, 0, payload.length);
	}

	public String getMsg() {
		return new String(payload, 1+getFileNameLength()+4,size) ;
	}
	
	/**
	 * fill the last 16 bytes with rand
	 */
	public void pad() {
		for (int i = payload.length - 16; i < payload.length; i++)
			payload[i] = (byte) rand.nextInt();
	}

	public void setStringMessage(String msg) {
		size = msg.length();
		alloc(size);
		System.arraycopy(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8), 0, payload, 5, size);
	}

	public int getFileNameLength() {
		int flen = 0;
		if (filename != null) {
			flen = filename.length();
		}
		return flen;
	}

	/**
	 * allocate 1xB (filename size) + N x B for file name bytes + 4 data length +
	 * data + (pad to 16bytes)
	 */
	public void alloc(int size) {
		int flen = getFileNameLength();
		int totalLength = 1 + flen + 4 + size;
		int pad = 16 - (totalLength % 16);

		payload = new byte[totalLength + pad];
		// file the last 16 bytes with rand
		pad();
		intToBytes(size, payload, 1 + flen);

		payload[0] = (byte) flen;
		for (int i = 0; i < flen; i++)
			payload[i + 1] = (byte) filename.charAt(i);

		System.out.println("alloc() " + payload.length);

	}

	public void readDataFromFile() {
		try {
			File f = srcFile ;
			size = (int) f.length();
			alloc(size);
			FileInputStream is = new FileInputStream(f);
			int num = 1 + getFileNameLength() + 4;

			int ch = 0;
			while (num < size) {
				ch = is.read();
				payload[num++] = (byte) ch;
			}
			is.close();
		} catch (Exception ex) {
			System.out.println("Error: count not open file: " + filename);
			System.exit(0);
		}
	}

	public static String hex(byte[] bar, int start, int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			String s = Integer.toString(bar[i] & 0xFF, 16);
			if (s.length() == 1)
				sb.append("0");
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	public static void intToBytes(int i, byte[] bar, int pos) {
		bar[pos] = (byte) ((i >> 24) & 0xff);
		bar[pos + 1] = (byte) ((i >> 16) & 0xff);
		bar[pos + 2] = (byte) ((i >> 8) & 0xff);
		bar[pos + 3] = (byte) ((i) & 0xff);
	}

	public static int ti(byte[] bar, int off, int len) {
		int ret = 0;
		for (int i = 0; i < len; i++) {
			ret = ret << 8;
			ret += (((int) bar[off + i]) & 0xFF);
		}
		return ret;
	}

}

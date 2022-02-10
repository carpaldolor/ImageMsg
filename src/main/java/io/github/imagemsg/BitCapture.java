package io.github.imagemsg;

import java.io.ByteArrayOutputStream;

public class BitCapture {
	ByteArrayOutputStream bos;

	int curBit = 0;
	int curByte = 0;

	int size = 0;

	public BitCapture() {
		this.bos = new ByteArrayOutputStream();
		curByte = 0;
	}

	/**
	 * Add 0 or 1
	 */
	public int addBit(int bit) {

		if (bit > 0) {
			curByte |= (1 << curBit);
		}

		if (++curBit > 7) {
			curBit = 0;
			bos.write(curByte);
			size++;
			curByte = 0;
		}

		return size;
	}

	public byte[] getBytes() {
		return bos.toByteArray();
	}

	public int size() {
		return size;
	}

}

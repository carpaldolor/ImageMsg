package io.github.imagemsg;

import java.util.Random;

public class Biterator {
	Random rand = new Random();
	
	boolean done = false ;
	int start = 0;
	int pos = 0;
	int size = 0;
	byte[] data;

	int bit = 0;

	public Biterator(byte[] data, int start, int length) {
		this.data = data;
		this.start = start;
		this.pos = start;
		this.size = length;
		this.bit = 0;
		this.done = false ;
	}

	public boolean isDone() {
		return done ;
	}
	
	/*
	 * return 1,0 or -1 for end
	 */
	public int nextBit() {
		if(done) {
			//return noise
			return rand.nextInt() & 0x01 ;
		}
		
		int ret = ((((int) data[pos]) & 0xFF) >> bit) & 0x01;
		if (++bit > 7) {
			bit = 0;
			if( ++pos >= (start+size)) {
				done=true;
			}
		}
		return ret;
	}

}

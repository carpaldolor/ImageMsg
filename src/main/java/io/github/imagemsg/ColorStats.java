package io.github.imagemsg;


public class ColorStats {

	String name  ;

	int zero = 0;
	int one = 1 ;

	public ColorStats(String name) {
		this.name = name ;
	}

	public void add(int val) {
		if(val>0) {
			one++ ;
		}
		else {
			zero++ ;
		}
	}

	public void print() {
		float zero_p = (float) (100 * zero) / (float) (one + zero);
		float one_p = (float) (100 * one) / (float) (one + zero);
		System.out.println(name+" zero: " + zero + " " + zero_p);
		System.out.println(name+ " one:  " + one + " " + one_p);
	}
}

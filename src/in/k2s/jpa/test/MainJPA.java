package in.k2s.jpa.test;

import in.k2s.jpa.sequence.SequenceGenerator;

public class MainJPA {
	
	public static void main(String[] args) {
		for (int i = 0; i < 300; i++) {
			System.out.println(SequenceGenerator.generate(null));
		}
	}

}

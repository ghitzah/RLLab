package ComparativeAnalysis;

import java.lang.Exception;

public class Metric {
	
	private double[] dists;
	private int size;
	
	public Metric(int size) throws OOBException{ //TODO: maybe another exception
		this.size = size;
		if (size <= 0 ) throw new OOBException(); 
		dists = new double[size * (size-1) / 2];
	}
	
	public double dist(int s1, int s2) throws OOBException{
		if (s1 < 0 || s2 < 0 || s1 >= size || s2 >= size) throw new OOBException();
		if ( s1 == s2) return 0;
		if( s1 < s2 ) { int tmp = s1; s1 = s2; s2 = tmp; } //swap
		return dists[(s1-1)*s1/2 + s2];
	}
	
	
	
	public class OOBException extends Exception {
		//TODO: maybe add some stuff
	}
}

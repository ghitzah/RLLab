package ComparativeAnalysis;

import java.lang.Exception;

public class Metric {
	
	private double[] dists;
	private int size;
	
	public Metric(int size) throws OOBException{ //TODO: maybe another exception
		this.size = size;
		if (size <= 0 ) throw new OOBException("Size is " + size); 
		dists = new double[size * (size-1) / 2];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = 0.0;
		}
	} //DONE
	
	public double dist(int s1, int s2) throws OOBException{
		if (!check_bounds(s1, s2)) throw new OOBException("States are " + s1 +  " "  + s2);
		if ( s1 == s2) return 0;
		if( s1 < s2 ) { int tmp = s1; s1 = s2; s2 = tmp; } //swap
		return dists[(s1-1)*s1/2 + s2];
	} //DONE
	
	public void set(int s1, int s2, double new_val) throws OOBException {
		if (!check_bounds(s1, s2) || (s1 == s2 && new_val != 0)) throw new OOBException("States are " + s1 +  " "  + s2);
		if( s1 < s2 ) { int tmp = s1; s1 = s2; s2 = tmp; } //swap
		dists[(s1-1)*s1/2 + s2] = new_val;
	}
	
	@SuppressWarnings("serial")
	public class OOBException extends Exception {
		String s;
		public OOBException(String s) {
			this.s = s;
		}
		
		public void printS() { System.out.println(s);}
	}
	
	
	
	
	private boolean check_bounds(int s1, int s2) {
		if (s1 < 0 || s2 < 0 || s1 >= size || s2 >= size) return false;
		else return true;
	}
	
	
	@Override
	public String toString() {
		String ss = "";
		for (int i = 1; i < size; i++) {
			for (int j = 0; j < i-1; j++) {
				ss += dists[(i-1)*i/2 + j] + " ";
			}ss += dists[(i-1)*i/2 + i-1] + "\n";
		}
		return ss;
	}
}

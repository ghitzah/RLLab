package ComparativeAnalysis;

import java.lang.Exception;
import java.util.ArrayList;

public class Metric {
	
	/**
	 * dists: contains the distance values, but only the lower triangular part of the
	 * distance matrix
	 */
	private ArrayList<ArrayList<Double>> dists;
	private int size;
	
	public Metric(int size) throws OOBException{
		this.size = size;
		if (size <= 0 ) throw new OOBException("Size is " + size);
		
		dists = new ArrayList<ArrayList<Double>>(size-1);
		
		for (int i = 0; i < size-1; i++) {
			dists.add(new ArrayList<Double>(i+1));
			ArrayList<Double> a = dists.get(i);
			for (int j = 0; j < i+1; j++) {
				a.add(0.0);
			}
		}
	} 
	
	
	public void add_entry() {
		dists.add(new ArrayList<Double>(size));
		ArrayList<Double> a = dists.get(dists.size()-1);
		for (int j = 0; j < size; j++) {
			a.add(0.0);
		}
		size++;
	}
	
	
	public double dist(int s1, int s2) throws OOBException{
		if (!check_bounds(s1, s2)) throw new OOBException("States are " + s1 +  " "  + s2);
		if ( s1 == s2) return 0;
		if( s1 < s2 ) { int tmp = s1; s1 = s2; s2 = tmp; } //swap
		return dists.get(s1-1).get(s2);
	} //DONE
	
	public void set(int s1, int s2, double new_val) throws OOBException {
		if (!check_bounds(s1, s2) || (s1 == s2 && new_val != 0)) throw new OOBException("States are " + s1 +  " "  + s2);
		if( s1 < s2 ) { int tmp = s1; s1 = s2; s2 = tmp; } //swap
		dists.get(s1-1).set(s2, new_val);
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
		for(ArrayList<Double> al : dists) {
			for (int i = 0; i < al.size()-1; i++) {
				ss += al.get(i) + " ";
			}ss += al.get(al.size()-1) + "\n";
		}
		return ss;
	}
}

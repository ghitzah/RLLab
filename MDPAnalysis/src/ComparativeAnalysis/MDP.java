package ComparativeAnalysis;
import java.util.Map;
import java.util.TreeMap;

import lecture1.*;
import ComparativeAnalysis.Metric.OOBException;







/**
 * Abstract class to be used as an interface for writing Markov Decision Models
 * @author gcoman
 *
 */
public abstract class MDP {

	

	public class State implements Feature {

		int idx;
		
		@Override
		public double groundDist(Feature f) {
			State ss = (State) f;
			try {
				return met.dist(idx, ss.idx);
			} catch (OOBException e) {
				System.out.println("WTF");
				e.printStackTrace();
				return 0;
			}
		}
		
	}
	
	/**
	 * A class that implements a histogram (to be used in the  
	 * @author gcoman
	 */	
	public class Histogram extends Signature{

		private int total = 0;		
		private TreeMap<Integer, Double> heapStorage;


		public Histogram(Map<Integer,Double> raw_hist) {
			heapStorage = new TreeMap<Integer, Double>();
			for (Integer j : raw_hist.keySet()) {
				double d = raw_hist.get(j);
				heapStorage.put(j, d);
				total += d;			
			}			
			setSignature(); //TODO what is this?
		}

		public int compareTo(Histogram h2) {
			if(Math.abs(total - h2.total)  > 0.00001) return -1; //TODO magic number
			if(heapStorage.size() != h2.heapStorage.size()) { return 1;}
			for (Integer s : h2.heapStorage.keySet()) {
				if(!heapStorage.containsKey(s))	{return 1; }				
				if(heapStorage.get(s) - h2.heapStorage.get(s) > 0.000001) {return 1; } //TODO magic number
			}

			return 0;
		}

		// compare two histograms using JFastEMD
		public double compareToJFastEMD(Histogram h2){
			if(Math.abs(total - h2.total)  > 0.00001) return -1; //TODO magic number
			return JFastEMD.distance(this, h2, -1);	    
		}

		//get the signature of a histogram
		private void setSignature()
		{
			// compute features and weights
			int n = this.heapStorage.size();
			State[] features = new State[n];
			double[] weights = new double[n];

			int i = 0;
			for(Map.Entry<Integer,Double> entry : heapStorage.entrySet()) {
				State key = new State();
				key.idx = entry.getKey();
				Double value = entry.getValue();
				features[i] = key;
				weights[i] = value;
				i++;
			}	        	      
			setNumberOfFeatures(n);
			setFeatures(features);
			setWeights(weights);
		}
	}

	public Metric met;
	final double GAMMA = 0.9;


	/**
	 * returns the probability of transitioning from s to sn when taking action a
	 * @param s : start state 
	 * @param a : action taken
	 * @param sn : next state
	 * @return probability of transition, as a double
	 */
	public abstract double P(int c, int a, int cn);


	/**
	 * returns the reward function for an input state-action pair
	 * @param s : start state 
	 * @param a : action taken
	 * @return reward associated with this transition
	 */
	public abstract double R(int c, int a);



	/**
	 * Returns a histogram representing the transition from an input state under 
	 * a given input action.
	 * @param s : start state
	 * @param a : action taken
	 * @return an integer based histogram representing the transition map under the input s-a pair 
	 */
	public abstract Map<Integer, Double> getHistogram(int s, int a);

	/**
	 * Number of states in the current MDP
	 * @return number of states
	 */
	public abstract int number_states() ;

	/**
	 * Number of actions in the current MDP
	 * @return number of states
	 */
	public abstract int number_actions() ;

	public double gamma() {return GAMMA;}
	

}

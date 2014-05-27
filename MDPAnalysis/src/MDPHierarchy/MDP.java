package MDPHierarchy;
import jFastEMD.*;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;






/**
 * Abstract class to be used as an interface for writting Markov Decision Models
 * @author gcoman
 *
 */
public abstract class MDP {

	/**
	 * An MDP state representation
	 * @author gcoman
	 *
	 */
	public class State implements Feature {

		
		/**
		 * link to the MDP that the state is part of
		 */
		protected MDP mdp;
		/**
		 * the index of this state in its MDP, as an int from 0 to SIZE-1
		 */
		protected int index;
		/**
		 * If this MDP of the current state is to be abstracted, then this will 
		 * be a link to the cluster in which we place the state
		 */
		protected AggMDP.Cluster parent;
		
		
		/**
		 * Constructor of such a state. The MDP is a required parameter
		 * @param mdp : the MDP in which we use this state
		 */
		public State(MDP mdp, int index) {
			this.mdp = mdp;
			this.index = index;
		}
		
		/**
		 * This method is used in the recursive implementation of baseState for 
		 * A hierarchical structure
		 * @return
		 */
		public State baseState() {
			return this;
		}
		
		
		public MDP mdp() {
			return mdp;
		}
		
		public int idx() {
			return index;
		}
		
		public AggMDP.Cluster parent() {
			return parent;
		}

		@Override
		public double groundDist(Feature f2) {
			State s2 = (State)f2;
			// note the matrix D is a lower triangle matrix
			if(index < s2.idx()) return mdp.D[index][s2.idx()];
			else return mdp.D[s2.idx()][index];
		}
	}
	

	
	/**
	 * A class that implements a histogram (to be used in the  
	 * @author gcoman
	 *
	 */
	@SuppressWarnings("serial")
	public class Histogram extends TreeMap<State,Integer> implements Comparable<Histogram>{
		private int total = 0;
				
		public Integer put(State s, Integer v) {
			super.put(s, v);
			total += v;
			return v;
		}
		
		@Override
		public int compareTo(Histogram h2) {
			
			if(total != h2.total || size() != h2.size()) return -1;
			for (State s : h2.keySet()) {
				if(!this.containsKey(s))	return -1;
				if(this.get(s) != h2.get(s)) return -1;
			}
			
			return 0;
		}
		
		// compare two histograms using JFastEMD
		public double compareToJFastEMD(Histogram h2){
			if(total != h2.total ) return -1;
			
			Signature sig1 = getSignature(this);
	        Signature sig2 = getSignature(h2);

	        double dist = JFastEMD.distance(sig1, sig2, -1);

	        return dist;
		}
		
		// get the signature of a histogram
		private Signature getSignature(Histogram h)
	    {
	        // compute features and weights
			int n = h.size();
	        State[] features = new State[n];
	        double[] weights = new double[n];
	        
	        int i = 0;
	        for(Map.Entry<State,Integer> entry : h.entrySet()) {
	        	  State key = entry.getKey();
	        	  Integer value = entry.getValue();
	        	  features[i] = key;
	        	  weights[i] = value;
	        	  i++;
	        }
	        
	        Signature signature = new Signature();
	        signature.setNumberOfFeatures(n);
	        signature.setFeatures(features);
	        signature.setWeights(weights);

	        return signature;
	    }
	}
	
	//TODO document
	@SuppressWarnings("serial")
	public class InvalidMDPException extends Exception{
		
		public void printError() {
			System.out.println("InvalidMDP");
		}
		
		
	}
	
	//TODO: expand this
	@SuppressWarnings("serial")
	public class MissingAggParentLinkException extends InvalidMDPException{

		public void printError() {
			System.out.println("MissingParent");
		}		
	}
	
	// the distance matrix between two states
	// indexed by indices of two states
	protected double[][] D;
	
	final double GAMMA = 0.9;
	
	
	
	
	/**
	 * returns the reward function for an input state-action pair
	 * @param s : start state 
	 * @param a : action taken
	 * @return reward associated with this transition
	 */
	public abstract double R(State c, int a) throws InvalidMDPException;
	
	/**
	 * returns the probability of transitioning from s to sn when taking action a
	 * @param s : start state 
	 * @param a : action taken
	 * @param sn : next state
	 * @return probability of transition, as a double
	 */
	public abstract double P(State c, int a, State cn) throws InvalidMDPException;
	
	
	/**
	 * Returns a histogram representing the transition from an input state under 
	 * a given input action. Note that the transition probability is over 
	 * clusters in a possibly different aggregate MDP
	 * @param s : start state
	 * @param a : action taken
	 * @param m : MDP used to determine the sigma algebra for the histogram
	 * @return an integer based histogram representing the transition map under the input s-a pair 
	 */
	public abstract Histogram getHistogram(State s, int a, MDP m) throws InvalidMDPException;
	
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
	
	
	
	/**
	 * A list of all states associated with the current MDP
	 * @return an array of all states in the MDP
	 */
	public abstract Collection<State> getStates();
	
	
}

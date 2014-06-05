package MDPHierarchy;
import jFastEMD.*;


import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import MDPHierarchy.AggMDP.Cluster;







/**
 * Abstract class to be used as an interface for writing Markov Decision Models
 * @author gcoman
 *
 */
public abstract class MDP {

	/**
	 * An MDP state representation
	 * @author gcoman
	 *
	 */
	public abstract class State implements Feature, Comparable<State> {


		/**
		 * link to the MDP that the state is part of
		 */
		private MDP mdp;
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
		 * @return a state that is associated with this cluster
		 */
		public State baseState() {
			return this;
		}
		
		public AggMDP.Cluster getMemeber(AggMDP magg) {
			Cluster cl = parent;
			while (cl != null && !cl.sameMdp(magg)) cl = cl.parent;
			return cl;
		}


		public boolean sameMdp(MDP m) {
			return this.mdp == m  ;
		}

		public int idx() {
			return index;
		}

		public AggMDP.Cluster parent() {
			return parent;
		}

		@Override
		public double groundDist(Feature f2) {
			State s2 = (State) f2;
			// note the matrix D is a lower triangle matrix
			if(index == s2.index) return 0;
			if(index > s2.index) return mdp.D[index][s2.index];			
			else return mdp.D[s2.index][index];
		}

		@Override
		public int compareTo(State o) {
			return this.index - o.index;
		}


		/**
		 * returns the probability of transitioning from this state to sn when taking action a
		 * @param a : action taken
		 * @param sn : next state
		 * @return probability of transition, as a double
		 */
		public double P(int a, State cn)  throws InvalidMDPException{
			//TODO : make it to the cluster (maybe)
			if(cn.mdp != this.mdp) throw new InvalidMDPException();
			return mdp.P(this, a, cn);
		}


		/**
		 * returns the reward function when taking an input action from the current state
		 * @param a : action taken
		 * @return reward associated with this transition
		 */
		public double R(int a) {		
			try {
				return mdp.R(this, a);
			}catch (InvalidMDPException e) {
				return 0;
				//Nothing to catch
			}
		}


		/**
		 * Returns a histogram representing the transition from this state under 
		 * a given input action. Note that the transition probability is over 
		 * clusters in a possibly different aggregate MDP	
		 * @param a : action taken
		 * @param m : MDP used to determine the sigma algebra for the histogram
		 * @return an integer based histogram representing the transition map under the input s-a pair 
		 */
		public Map<State,Double> getHistogram(int a, MDP m) throws InvalidMDPException {

			State s = baseState();

			// transition map out of input state s to the input (possibly aggregate MDP)
			Map<State,Double> tm_s_this = s.mdp.getHistogram(s, a);

			TreeMap<State, Double> tm_s_mdp = new TreeMap<State, Double>();

			for (State sn_this : tm_s_this.keySet()) {
				//get the cluster to which it pertains
				State sn_mdp = sn_this;
				while(sn_mdp != null && !sn_mdp.sameMdp(m)) sn_mdp = sn_mdp.parent();			
				if(sn_mdp == null) 
				{				
					throw new MissingAggParentLinkException();
				}

				// index of the next cluster in the parent AggMDP
				if(tm_s_mdp.containsKey(sn_mdp)) {
					tm_s_mdp.put(sn_mdp, tm_s_this.get(sn_this) + tm_s_mdp.get(sn_mdp));
				}else tm_s_mdp.put(sn_mdp, tm_s_this.get(sn_this));
			}

			return tm_s_mdp;	
		}


	}

	/**
	 * A class that implements a histogram (to be used in the  
	 * @author gcoman
	 */	
	public class Histogram extends Signature{

		private int total = 0;		
		private TreeMap<State, Double> heapStorage;


		public Histogram(Map<State,Double> raw_hist) {
			heapStorage = new TreeMap<MDP.State, Double>();
			for (State j : raw_hist.keySet()) {
				double d = raw_hist.get(j);
				heapStorage.put(j, d);
				total += d;			
			}			
			setSignature();
		}

		public int compareTo(Histogram h2) {
			if(Math.abs(total - h2.total)  > 0.00001) return -1; //TODO magic number
			if(heapStorage.size() != h2.heapStorage.size()) { return 1;}
			for (State s : h2.heapStorage.keySet()) {
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

		// get the signature of a histogram
		private void setSignature()
		{
			// compute features and weights
			int n = this.heapStorage.size();
			State[] features = new State[n];
			double[] weights = new double[n];

			int i = 0;
			for(Map.Entry<State,Double> entry : heapStorage.entrySet()) {
				State key = entry.getKey();
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
	 * returns the probability of transitioning from s to sn when taking action a
	 * @param s : start state 
	 * @param a : action taken
	 * @param sn : next state
	 * @return probability of transition, as a double
	 */
	public abstract double P(State c, int a, State cn) throws InvalidMDPException;


	/**
	 * returns the reward function for an input state-action pair
	 * @param s : start state 
	 * @param a : action taken
	 * @return reward associated with this transition
	 */
	public abstract double R(State c, int a) throws InvalidMDPException;



	/**
	 * Returns a histogram representing the transition from an input state under 
	 * a given input action.
	 * @param s : start state
	 * @param a : action taken
	 * @return an integer based histogram representing the transition map under the input s-a pair 
	 */
	public abstract Map<State, Double> getHistogram(State s, int a) throws InvalidMDPException;

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

	/**
	 * Saves to a given file the membership class of the given state relative
	 * to a given input Aggregate MDP
	 * @param magg the Aggregate MDP containing the clusters we are interested in
	 * @param f a PrintWriter where the output should be directed
	 */
	public void printClMembership(AggMDP magg, PrintWriter f) {
		for(State s : getStates()) {
			f.println(s.index + ", " + s.getMemeber(magg).index);
		}
	}

	/**
	 * Prints the metric D of the current MDP
	 * @param w : a PrintWriter where the output should be directed
	 */
	public void printD(PrintWriter w) {
		for (int i=1 ; i < D.length ; i++) {			
			for (int j = 0; j < D[i].length; j++) {
				DecimalFormat df = new DecimalFormat("###.##");
				String d = df.format(D[i][j]);
				w.print(d + ((j < D[i].length -1) ? "," : ""));
			}w.println();
		}
	}

	

}

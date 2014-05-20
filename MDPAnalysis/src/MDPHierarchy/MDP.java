package MDPHierarchy;
import java.util.Collection;
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
	public class State {
		
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
		
	}
	

	
	/**
	 * A class that implements a histogram (to be used in the  
	 * @author gcoman
	 *
	 */
	@SuppressWarnings("serial")
	public class Histogram extends TreeMap<Integer,Integer> implements Comparable<Histogram>{
		private int total = 0;
				
		public Integer put(Integer k, Integer v) {
			Integer toRet = get(k);
			super.put(k, v);
			total += v;
			return toRet;
		}
		
		@Override
		public int compareTo(Histogram h2) {
			if(total != h2.total || size() != h2.size()) return -1;
			for (Integer k : h2.keySet()) {
				if(!containsKey(k))	return -1;
				if(get(k) != h2.get(k)) return -1;
			}
			return 0;
		}
		
	}
	
	//TODO document
	public class InvalidMDPException extends Exception{

		/**
		 * Eclipse complains... 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	//TODO: expand this
	public class MissingAggParentLinkException extends InvalidMDPException{

		/**
		 * Eclipse complains... 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	
	
	
	
	
	
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

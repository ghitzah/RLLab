package MDP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;





/**
 * Abstract class to be used as an interface for writing Markov Decision Models
 * @author gcoman
 *
 */
public abstract class MDP {
	
	/*******************************
	 * DATA
	 *******************************/
	
	
	/**
	 * Discount value over the MDP
	 */
	final double GAMMA = 0.9;

	
	

	/*******************************
	 * GETTERS AND SETTERS
	 *******************************/
	
	
	/**
	 * GETTER: the probability of transitioning from s to sn when taking action a
	 * @param s : start state 
	 * @param a : action taken
	 * @param sn : next state
	 * @return probability of transition, as a double
	 */
	public abstract double P(State s, Action a, State sn);

	
	/**
	 * GETTER: the measure probability of transitioning from s when taking action a
	 * @param s : start state 
	 * @param a : action taken
	 * @return probability of transition, as a double
	 */
	public abstract Measure P(State s, Action a);
	
	
	
	/**
	 * GETTER: the measure probability of transitioning from s 
	 * @param s : start state 
	 * @return probability of transition, as a double
	 */
	public Map<Action, Measure> P(State s) {
		Map<Action,Measure> toRet = new HashMap<Action,Measure>();
		Iterator<Action> ada = get_action_iterator();
		while(ada.hasNext()) {
			Action a = ada.next();
			toRet.put(a, P(s,a));
		}
		return toRet;
	}
	
	
	
	/**
	 * GETTER: the reward function for an input state-action pair
	 * @param s : start state
	 * @param a : action taken
	 * @return reward associated with this transition
	 */
	public abstract double R(State s, Action a);

	
	/**
	 * GETTER: the reward function for an input state
	 * @param s : start state
	 * @return reward associated with this transition
	 */
	public Map<Action,Double> R(State s) {
		Map<Action,Double> toRet = new HashMap<Action,Double>();
		Iterator<Action> ada = get_action_iterator();
		while(ada.hasNext()) {
			Action a = ada.next();
			toRet.put(a, R(s,a));
		}
		return toRet;
	}


	/**
	 * GETTER: Iterator over all states
	 * @return - iterator
	 */
	public abstract Iterator<State> get_state_iterator();
	
	/**
	 * GETTER: Iterator over all state pairs
	 * @return - iterator
	 */
	public abstract Iterator<StatePair> get_state_pair_iterator();
	
	/**
	 * GETTER: Iterator over all actions
	 * @return - iterator
	 */
	public abstract Iterator<Action> get_action_iterator();

	/**
	 * GETTER: Number of states in the current MDP 
	 * @return number of states (if -1, then there is an infinite number of states)
	 */
	public abstract int number_states();

	/**
	 * GETTER: Number of actions in the current MDP 
	 * @return number of states (if -1, then there is an infinite number of states)
	 */
	public abstract int number_actions() ;

	
	/**
	 * GETTER: The discount value for value function computation 
	 * @return the discount value >= 0 and < 1
	 */
	public double gamma() {return GAMMA;}

	
	private boolean checkMDP(MDP m) {
		return this == m;
	}
	
	/*******************************
	 * CLASSES
	 *******************************/
	
	
	/**
	 * Represents a state in the MDP
	 * @author gcoman
	 *
	 */
	public abstract class State {
		public boolean checkMDP(MDP m) {
			return MDP.this.checkMDP(m);
		}
	}

	/**
	 * Represents a feature (real valued function over the state space of the MDP)
	 * @author gcoman
	 *
	 */
	public abstract class Feature {
		
		/**
		 * evaluate the feature at a given state
		 * @param s - state at which we evaluate it
		 * @return : a real value associated with the given state
		 */
		public abstract double eval(State s);
		
	}
	
	/**
	 * Represents a measure over the state space of the MDP
	 * @author gcoman
	 *
	 */
	public class  Measure {
		
		final int NUM_SAMPLES = 100;
		
		/**
		 * Relative intiger mass associated with every state - if the state
		 * is not a key in this map, then the state has 0 mass associated to it
		 */
		Map<State,Integer> indiv_measures;
		/**
		 * The total measure (as we will use a finite total mass)
		 */
		int totalMeasure;
		
		
		public Measure(Map<State, Integer> indiv_measures, int totalMeasure) {
			this.indiv_measures = indiv_measures;
			this.totalMeasure = totalMeasure;
		}
		
		public Measure() {}
		
		
		@SuppressWarnings("serial")
		public class CannotDoExactExeption extends Exception { }
		
		public double integrateExact(Feature f){
			assert(indiv_measures != null);
			//if(indiv_measures == null) throw new CannotDoExactExeption();
			double d = 0;
			for(State s : indiv_measures.keySet()) {
				Integer ada = indiv_measures.get(s);
				d += ada * f.eval(s); 
			}
			return d / totalMeasure ;
		}
		
		 public double integrateSampled(Feature f) {
			double count = 0;
			for (int i = 0; i < NUM_SAMPLES; i++) {
				count +=f.eval(sample());
			}
			return count / NUM_SAMPLES;
		}
		
		public State sample() {
			return null; //TODO!
			
		}
		
		
		public boolean isGrounded() {
			return (indiv_measures != null);
		}
		
		
	}

	/**
	 * Represents an action over the state space of the MDP
	 * @author gcoman
	 */	
	public interface Action { }

	
	/**
	 * Class representing a feature for a finite state space MDP - it provides functionality 
	 * to have binary features (activated to 1 over a subset, and 0 everywhere else)
	 * @author gcoman
	 *
	 */
	public class Cluster extends Feature {

		private Set<State> all_members = new HashSet<State>();

		/**
		 * All the states that are members of this feature (are activated as 1)
		 * @return -  the members
		 */
		public Set<State> all_members() {
			HashSet<State> toRet = new HashSet<State>();
			for(State s : all_members) {
				toRet.add(s);
			}
			return toRet;
		}
		
		/**
		 * The number of members of this feature
		 * @return - number of members
		 */
		public int numberMembers() {
			return all_members.size();
		}
		
		
		/**
		 * Returns true is the given state is a member of the feature
		 * @param s - the state to check
		 * @return TRUE is the given state is a member of the feature
		 */
		public boolean contains(State s) {
			return all_members.contains(s);
		}
		
		/**
		 * Adds the state to the members of the feature
		 * @param s - state to add
		 */
		public void add_state(State s) {
			all_members.add(s);
		}
		
		/**
		 * Removes the state from the members of the feature
		 * @param s - state to remove
		 */
		public void remove_state(State s) {
			all_members.remove(s);
		}

		@Override
		public double eval(State s) {
			return (contains(s)) ? 1.0 : 0.0;
		}
	}
	
	/**
	 * Class implementing a simple pair of states
	 * @author gcoman
	 *
	 */
	public class StatePair {
		public final State s1,s2;
		
		public StatePair(State s1, State s2) {
			this.s1 = s1;
			this.s2 = s2;
		}
	}
	
	
	/**
	 * Simple model for the exact bisimulation computation algorithm. It contains
	 * the start state of the transition model that this object represents
	 * @author gcoman
	 *
	 */
	public class ExactStateModel extends Model {
		public final State s;
		
		public ExactStateModel(State s) {
			this.s = s;
		}

		@Override
		public double R(Action a) {
			return MDP.this.R(s,a);
		}

		@Override
		public Measure T(Action a) {
			return MDP.this.P(s,a);
		}
	}
	
	
	/**
	 * Class implements a Model that can be compared using such a comparator
	 * @author gcoman
	 */
	public abstract class Model { 
		public abstract double R(Action a);
		public abstract Measure T(Action a);
		
		
		public boolean checkMDP(MDP m) {
			return (MDP.this == m);
		}
	}
	
	
	/**
	 * Exception for MDP membership
	 */
	@SuppressWarnings("serial")
	public class IncorrectMDPException extends Exception { }
	
	
	
}

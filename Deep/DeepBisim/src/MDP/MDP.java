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

	
	
	/*******************************
	 * CLASSES
	 *******************************/
	
	
	/**
	 * Represents a state in the MDP
	 * @author gcoman
	 *
	 */
	public interface State { }

	/**
	 * Represents a feature (real valued function over the state space of the MDP)
	 * @author gcoman
	 *
	 */
	public interface Feature {
		public double eval(State s);
	}
	
	/**
	 * Represents a measure over the state space of the MDP
	 * @author gcoman
	 *
	 */
	public interface Measure {
		public interface Bset { }
		public double eval(Bset b);
		public double intergrate(Feature f);
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
	public abstract class FiniteSFeature implements Feature {

		private Set<State> all_members = new HashSet<State>();

		/**
		 * Returns true if this feature can be used as a binary feature
		 * @return TRUE if binary
		 */
		public abstract boolean isBinary();

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
	}
	
	/**
	 * Class implement an action for an MDP with a finite number of actions
	 * @author gcoman
	 *
	 */
	public class FiniteAction implements Action{
		private int idx;
		
		public FiniteAction(int idx) {
			this.idx = idx;
		}

		/**
		 * The index of the action in the action set
		 * @return - the index of the action 
		 */
		public int idx() {
			return idx;
		}
	}

	
	/**
	 * Class implementing a measure over a finite state space
	 * @author gcoman
	 *
	 */
	public class FiniteSMeasure implements Measure{

		/**
		 * The total measure (as we will use a finite total mass)
		 */
		int totalMeasure;
		
		/**
		 * Relative intiger mass associated with every state - if the state
		 * is not a key in this map, then the state has 0 mass associated to it
		 */
		Map<State,Integer> indiv_measures;

		/**
		 * Class implementing a Borel set  in a finite state space
		 * @author gcoman
		 *
		 */
		public class FiniteSBSet extends FiniteSFeature implements Bset{

			@Override
			public double eval(State s) { return contains(s) ? 1.0 : 0.0; }

			@Override
			public boolean isBinary() { return true; }

		}

		
		@Override
		public double intergrate(Feature f) {
			FiniteSFeature fg = (FiniteSFeature) f;
			if(fg.isBinary()) {
				int d = 0;
				for(State s : fg.all_members()) {
					Integer ada = indiv_measures.get(s);
					if(ada != null) { d += ada; }
				}
				return ((double) d) / totalMeasure ;
			}else {
				double d = 0;
				for(State s : indiv_measures.keySet()) {
					Integer ada = indiv_measures.get(s);
					d += ada * fg.eval(s); 
				}
				return d / totalMeasure ;
			}

		}

		@Override
		public double eval(Bset b) {
			Feature f = (FiniteSBSet) b;
			return intergrate(f);
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
	
}

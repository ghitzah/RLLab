package CompGraphs;

import MDP.MDP.State;

/**
 * Class implementing a model Comparator
 * @author gcoman
 *
 */
public abstract class ModelComparator {

	/**
	 * Class implements a Model that can be compared using such a comparator
	 * @author gcoman
	 */
	public interface Model { }
	
	/**
	 * Method returns the distace between the two models
	 * @param m1 : model 1
	 * @param m2 : model 2
	 * @return : distance between model 1 and model 2
	 */
	public abstract double dist(Model m1, Model m2);
	
	/**
	 * Returns a model attached to a state s
	 * @param s : the state for which we would like to compute the model
	 * @return : the model associated with the given state
	 */
	public abstract Model getModel(State s);

	/**
	 * Method returns the distace between the two models associated with the two given states
	 * @param s1 : state 1
	 * @param s2 : state 2
	 * @return : distance 
	 */
	public double dist(State s1, State s2) {
		return dist(getModel(s1), getModel(s2));
	}

}

package CompGraphs;

import java.util.Set;

import MDP.MDP.Model;
import MDP.MDP.Feature;

/**
 * Class implementing a model Comparator
 * @author gcoman
 *
 */
public abstract class ModelComparator {
	
	/**
	 * Method returns the distace between the two models
	 * @param m1 : model 1
	 * @param m2 : model 2
	 * @param features :  the features that one would like to match under transition
	 * @return : distance between model 1 and model 2
	 */
	public abstract double dist(Model m1, Model m2, Set<Feature> features);
	
	

}

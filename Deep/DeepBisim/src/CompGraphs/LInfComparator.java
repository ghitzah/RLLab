package CompGraphs;

import java.util.Iterator;
import java.util.Set;

import MDP.MDP;
import MDP.MDP.Model;
import MDP.MDP.Action;
import MDP.MDP.Feature;


/**
 * Implements a clas describing an maximum comparator over the reward and the expected value
 * of a finite number of features over next state transitions
 * @author gcoman
 *
 */
public class LInfComparator extends ModelComparator{
	
	/**
	 * The MDP over which this comparator works
	 */
	MDP m;
	
	/**
	 * default constructor
	 * @param m : The MDP over which this comparator works
	 */
	public LInfComparator(MDP m) {
		this.m = m;
	}
	
	@Override
	public double dist(Model m1, Model m2, Set<Feature> features) 
			throws IncorrectModelExpection{
		//check models compatibility
		if(!m1.checkMDP(m) || !m2.checkMDP(m)) throw new IncorrectModelExpection();
		
		double d = 0;
		Iterator<Action> ada = m.get_action_iterator();
		while(ada.hasNext()) { 
			Action a  = ada.next();
			
			// reward difference
			double diffR = Math.abs(m1.R(a)-m2.R(a));
			// max dist over all features
			double diffT = 0.0;
			if(features != null) {
				for(Feature f : features) {
					double tmp = m1.T(a).intergrate(f) 
								  - m2.T(a).intergrate(f);
					diffT = Math.max(diffT, Math.abs(tmp));
				} // for
			}
			d = Math.max(d, diffR + m.gamma() * diffT); //gamma ?
		}
		return d;
	}
}

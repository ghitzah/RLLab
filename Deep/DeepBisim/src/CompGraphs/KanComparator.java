package CompGraphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lecture1.JFastEMD;
import lecture1.Signature;
import MDP.MDP;
import MDP.MDP.Model;
import MDP.MDP.Action;
import MDP.MDP.Feature;
import MDP.MDP.Cluster;


/**
 * Implements a class describing the kantorovich metric
 * @author sherry
 *
 */
public class KanComparator extends ModelComparator{

	/**
	 * The MDP over which this comparator works
	 */
	MDP m;

	/**
	 * default constructor
	 * @param m : The MDP over which this comparator works
	 */
	public KanComparator(MDP m) {
		this.m = m;
	}

	
	@Override
	public double dist(Model m1, Model m2, Set<Feature> features) 
			throws IncorrectModelExpection {
		return dist(m1, m2, features, false);
	}
	
	double dist(Model m1, Model m2, Set<Feature> features, boolean sampled) 
			throws IncorrectModelExpection{
		//check models compatibility
		if(!m1.checkMDP(m) || !m2.checkMDP(m)) throw new IncorrectModelExpection();
		
		
		//compute the distance between clusts
		double val = 0;
		
		Iterator<Action> ada = m.get_action_iterator();
		while(ada.hasNext()) { 
			Action a  = ada.next();

			// update value to difference in reward
			double val_for_a = Math.abs(m1.R(a)-m2.R(a));

			// add difference in transition
			double probDistance = JFastEMD.distance(
					hist_stoc(m1, a, features), 
					hist_stoc(m2, a, features), 
					-1);
			val_for_a += m.gamma() * probDistance / 100.0;
			val = Math.max(val_for_a, val);
		}
		return val;
	}
	
	private static Signature hist_stoc(Model m, Action a, Set<Feature> features) {
		
		Map<Cluster, Double> h1c = new HashMap<Cluster, Double>();
		
		// build the map from clusters to doubles
		// TODO test it
		if(features != null) {
			for(Feature f : features) {
				double tmp;
				tmp = m.T(a).integrateExact(f);
				h1c.put((Cluster) f, tmp);
			} 
		}
			
		// same as before
		Signature s = new Signature();
		s.setNumberOfFeatures(h1c.size());

		Feature[] fs = new Feature[h1c.size()];
		double[] ds = new double[h1c.size()];
		int z=0;
		for(Cluster c : h1c.keySet()) {
			fs[z] = c;
			ds[z++] = h1c.get(c);
		}
		s.setFeatures((lecture1.Feature[]) fs);
		s.setWeights(ds);

		return s;
	}

}

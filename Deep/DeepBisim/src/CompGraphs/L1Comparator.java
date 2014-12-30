package CompGraphs;

import java.util.Iterator;
import java.util.Set;

import MDP.MDP;
import MDP.MDP.Model;
import MDP.MDP.Action;
import MDP.MDP.Feature;

public class L1Comparator extends ModelComparator{
	
	MDP m;
	public L1Comparator(MDP m) {
		this.m = m;
	}
	
	@Override
	public double dist(Model m1, Model m2, Set<Feature> features) {
		double d = 0;
		Iterator<Action> ada = m.get_action_iterator();
		while(ada.hasNext()) {
			Action a  = ada.next();
			double diffR = Math.abs(m1.R(a)-m2.R(a));
			
			double diffT = 0.0;
			for(Feature f : features) {
				double tmp = m1.T(a).intergrate(f) 
							  - m2.T(a).intergrate(f);
				diffT = Math.max(diffT, Math.abs(tmp));
			} // for
			d = Math.max(d, diffR + diffT);
		}
		return d;
	}
}

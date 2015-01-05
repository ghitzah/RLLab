package CompGraphs;

import java.util.Set;

import MDP.MDP;
import MDP.MDP.Feature;
import MDP.MDP.Model;

public class LInfSampledComparator extends LInfComparator{

	public LInfSampledComparator(MDP m) {
		super(m);
	}

	@Override
	public double dist(Model m1, Model m2, Set<Feature> features) 
			throws IncorrectModelExpection {
		return dist(m1, m2, features, true);
	}
}

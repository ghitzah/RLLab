package ComparativeAnalysis;

import java.util.LinkedList;
import java.util.List;

import ComparativeAnalysis.Metric.OOBException;
import MDPHierarchy.*;
import MDPHierarchy.MDP.Histogram;


public class algos {

	public static void vanilla_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		
		
		int num_states = m.number_states();
		List<Metric> toRet = new LinkedList<Metric>();
		
		m.met = new Metric(num_states);
		while(iterations-- > 0) {
			System.out.println("Iterations left " + iterations);
			Metric met = new Metric(num_states);
			for(int s1 = 1; s1 < num_states; s1++) {
				for (int s2 = 0; s2 < s1; s2++) {
					//compute new value
					double maxDistance = 0;
					int num_a = m.number_actions();					
					for (int i = 0; i < num_a; i++) {
						Histogram h1 = m.getHistogramID(s1, i);
						Histogram h2 = m.getHistogramID(s2, i);
						double probDistance = h1.compareToJFastEMD(h2);
						//double probDistance = h1.compareTo(h2);
						double distance = Math.abs(m.getRewardID(s1, i) - m.getRewardID(s2, i))  
								+ m.gamma() * probDistance / 100.0;						
						maxDistance = (maxDistance > distance) ? maxDistance : distance; 	
					}
					met.set(s1, s2, maxDistance);
				}
			}
			toRet.add(met);
			m.met = met;
			//System.out.println(met); //TODO debug 
		}
	}

	
	
}

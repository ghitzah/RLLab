package ComparativeAnalysis;

import jFastEMD.Feature;
import jFastEMD.JFastEMD;
import jFastEMD.Signature;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ComparativeAnalysis.MDP.Histogram;
import ComparativeAnalysis.Metric.OOBException;


public class algos {

	final static double EPSILON = 0.0001;
	
	
	public static void vanilla_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		if(iterations == 0) return;
		
		List<Metric> toRet = new LinkedList<Metric>(); //list to be returned 
		int num_states = m.number_states(); //used often 		
	
		/** Perform iterations **/
		/** First iteration based on reward **/
		System.out.println("Iterations left " + iterations); //TODO: sysout debug
		m.met = new Metric(num_states); // start with empty metric TODO, do as with transitions		
		for(int s1 = 1; s1 < num_states; s1++) { //for all pairs
			for (int s2 = 0; s2 < s1; s2++) {					
				double val = 0; //new value
				for (int i = 0; i < m.number_actions(); i++) {
					double distance = Math.abs(m.R(s1, i) - m.R(s2, i));
					//update value for this action
					val = (val > distance) ? val : distance; 	
				}
				//set the metric
				m.met.set(s1, s2, val);
			}
		}
		toRet.add(m.met);
		iterations--;

		/** Transition iterations**/
		while(iterations-- > 0) {
			System.out.println("Iterations left " + iterations); //TODO: sysout debug
			Metric met = new Metric(num_states); //new metric computed synchronously
			for(int s1 = 1; s1 < num_states; s1++) { //for all pairs
				for (int s2 = 0; s2 < s1; s2++) {					
					double new_val = 0; //new value
					for (int i = 0; i < m.number_actions(); i++) {
						//TODO maybe build these histograms otherwise
						// Kantorovich
						MDP.Histogram h1 = m.new Histogram(m.getHistogram(s1, i));
						MDP.Histogram h2 = m.new Histogram(m.getHistogram(s2, i));
						double probDistance = h1.compareToJFastEMD(h2);
						
						//F operator
						double distance = Math.abs(m.R(s1, i) - m.R(s2, i))  
								+ m.gamma() * probDistance / 100.0;						
						//update value for this action
						new_val = (new_val > distance) ? new_val : distance; 	
					}
					//set the metric
					met.set(s1, s2, new_val);
				}
			}
			toRet.add(met);
			m.met = met;
			//System.out.println(met); //TODO debug 
		}
	}

	
	
	public static void declust_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		if(iterations == 0) return;
		
		List<Metric> toRet = new LinkedList<Metric>(); //list to be returned 
		int num_states = m.number_states(); //used often 		
		
		/** Perform iterations **/
		/** Step 1: First iteration based on reward **/
		//Step 1.1 Declustering and Set-up
		Cluster[] membR = new Cluster[num_states];
		List<Cluster> clustsR = declust_R(m, membR);
		Metric met = new Metric(clustsR.size());
		//set up the index and the metric for subsequent iterations
		int c1_idx = 0; //index of cluster c1
		for(Cluster c1 : clustsR) {
			//update index and metric
			c1.idx = c1_idx++;
			c1.met = met;
		}
		
		//Step 1.2 Metric computation
		c1_idx = 0; //index of cluster c1
		for(Cluster c1 : clustsR) {
			int c2_idx = 0;  //index of cluster c2
			for(Cluster c2 : clustsR) {
				//only compute lower triangular part of the distance metric
				if(c2_idx == c1_idx) break;
				
				//compute value max_a (diff reward)
				double val = 0;
				for (int a = 0; a < m.number_actions(); a++) {
					val = Math.max(val, m.R(c1.elements.first(), a) - m.R(c2.elements.first(), a));
				}
				met.set(c1_idx, c2_idx, val);
				
				//increment index
				c2_idx++;
			}
			//increment index
			c1_idx++;
		}
		
		//TODO Step 1.3 add metric to toRet
		toRet.add(partition_met_to_whole_space_met(met, membR));
		
		
		/** Step 2 Transitions **/
		//Step 2.0 Setup for looping 
		Cluster[] membPr = membR; //previous membership
		List<Cluster> clustsPr = clustsR; // previous clustering
		
		//Step 2.1 Iterate transition based declustering
		while(iterations-- > 0) {
			//Step 2.1.1 : Declustering + Set-up
			List<Cluster> clustsCrt = new LinkedList<Cluster>();
			
			Cluster[] membCrt = new Cluster[num_states];
			//decluster each cluster
			for(Cluster c : clustsPr) {
				Cluster[] membLocal = new Cluster[c.elements.size()];
				List<Cluster> new_cls = declust_T(m, c, membLocal);
				//update new membership
				int z = 0;
				for(Integer k : c.elements) {
					membCrt[k] = membLocal[z];
					z++;
				}
				//update new clusters
				clustsCrt.addAll(new_cls);
			}
			
			met = new Metric(clustsCrt.size());
			c1_idx = 0; //index of cluster c1
			for(Cluster c1 : clustsCrt) {
				//update index and metric
				c1.idx = c1_idx++;
				c1.met = met;
			}
			
			//Step 2.1.2 Compute distances
			c1_idx = 0;
			for(Cluster c1 : clustsCrt) {
				int c2_idx = 0;
				for(Cluster c2 : clustsCrt) {
					//only compute lower triangular part of the distance metric
					if(c2_idx == c1_idx) return;

					//compute the distance between clusts
					double val = 0;
					for (int a = 0; a < m.number_actions(); a++) {
						double val_for_a = 0;
						int s1 = c1.elements.first();
						int s2 = c2.elements.first();
						
						// if the reward is not the same, the update value to difference in reward
						if(membR[s1] != membR[s2]) {
							val_for_a = Math.abs(m.R(s1, a) 
									- m.R(s2, a)); 
						}
						// add difference in transition
						double probDistance = JFastEMD.distance(
								hist_stoc(m.getHistogram(s1, a), membPr), 
								hist_stoc(m.getHistogram(s2, a), membPr), 
								-1);
						val_for_a += m.gamma() * probDistance / 100.0;
						val = Math.max(val_for_a, val);
					}
					met.set(c1_idx, c2_idx, val);
					c2_idx++;
				}
				c1_idx++;
			}	
			//Step 2.1.3 Create a metric over the entire state space
			toRet.add(partition_met_to_whole_space_met(met, membCrt));
		
			//Step 2.1.4 prepare for next loop
			membPr = membCrt;
			clustsPr = clustsCrt;
		
		}
	}
	
	
	
	private static Metric partition_met_to_whole_space_met(Metric m, Cluster[] memb) {
		Metric toRet;
		
		int num_states = memb.length;
		try {
		toRet = new Metric(num_states);
		for (int i = 0; i < num_states; i++) {
			for(int j = 0; j < i; j++) {
				toRet.set(i, j, m.dist(memb[i].idx, memb[j].idx));
			}
		}
		} catch (OOBException e) {			
			e.printStackTrace();
			return null;
		}
		return toRet;
	}
	
	 
	private static Signature hist_stoc(Map<Integer,Double> h1, Cluster[] membership) {
		Map<Cluster, Double> h1c = new HashMap<Cluster, Double>();

		for(Integer si : h1.keySet()) {
			if(h1c.containsKey(membership[si])) {
				h1c.put(membership[si], h1c.get(membership[si]) + h1.get(si));
			}else {
				h1c.put(membership[si], h1.get(si));
			}
		}
		
		Signature s = new Signature();
		s.setNumberOfFeatures(h1c.size());
		s.setFeatures((Feature[]) h1c.keySet().toArray());
		
		Double[] vals = (Double[]) h1c.values().toArray();
		double[] valsd = new double[vals.length];
		int z=0;
		for(Double d : vals) { valsd[z++] = d;}
		s.setWeights(valsd);
		
		
		return s;
	}
	
	private static List<Cluster> declust_T(MDP m, Cluster clarge, Cluster[] membership) {
		List<Cluster> toRet =  new LinkedList<Cluster>();
		/** Transitions **/
		List<Integer> lst = new LinkedList<Integer>();
		for (Integer i : clarge.elements) {
			lst.add(i);
		}
		java.util.Collections.shuffle(lst);
		
		//find clust to insert element
		for(Integer i : lst) {
			membership[i] = null;			
			for(Cluster c : toRet) {
				boolean toAdd = true;
				for (int a = 0; toAdd & a < m.number_actions(); a++) {
					Map<Integer, Double> h1 = m.getHistogram(c.elements.first(), a); 
					Map<Integer, Double> h2 = m.getHistogram(i, a); 
					
					toAdd = (h1.size() == h2.size());
					if(!toAdd) break;
					for(Integer z : h2.keySet()) {
						toAdd = h2.containsKey(z);
						if(!toAdd) break;
					}
					if(!toAdd) break;
					for(Integer z : h2.keySet()) {
						toAdd = Math.abs(h1.get(z) - h2.get(z)) < EPSILON;
						if(!toAdd) break;
					}
				} // for a
				if(toAdd) { c.elements.add(i); membership[i] = c; break; }				
			} //for Cluster c
			if(membership[i] != null) {
				Cluster c = new Cluster();
				c.elements.add(i);
				membership[i] = c;
				toRet.add(c);
			}
		}
		return toRet;
	
	}
	
	
	private static List<Cluster> declust_R(MDP m, Cluster[] membership) {
		List<Cluster> toRet =  new LinkedList<Cluster>();
		/** Rewards! **/
		List<Integer> lst = new LinkedList<Integer>();
		for (int i = 0; i < m.number_states(); i++) {
			lst.add(i);
		}
		java.util.Collections.shuffle(lst);
		
		//find clust to insert element
		for(Integer i : lst) {
			membership[i] = null;
			for(Cluster c : toRet) {
				boolean toAdd = true;
				for (int a = 0; toAdd & a < m.number_actions(); a++) {
					toAdd = (Math.abs(m.R(c.elements.first(),a) - m.R(i, a)) < EPSILON);
				}
				if(toAdd) { c.elements.add(i); membership[i] = c; break; }				
			}
			if(membership[i] != null) {
				Cluster c = new Cluster();
				c.elements.add(i);
				membership[i] = c;
				toRet.add(c);
			}
		}
		return toRet;
	}
	
	
	
	public static class Cluster implements Feature {

		TreeSet<Integer> elements;
		
		int idx;
		Metric met;
		
		public Cluster() {
			elements = new TreeSet<Integer>();			
		}
		
		@Override
		public double groundDist(Feature f) {
			Cluster c = (Cluster) f;
			try {
				return met.dist(idx,c.idx);
			} catch (OOBException e) {
				e.printStackTrace();
				return 0;				
			}			
		}
	}
	
	
	
}

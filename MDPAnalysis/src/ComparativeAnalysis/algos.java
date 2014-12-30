package ComparativeAnalysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import lecture1.Feature;
import lecture1.JFastEMD;
import lecture1.Signature;
import ComparativeAnalysis.Metric.OOBException;


public class algos {

	final static double EPSILON = 0.000001;


	public static double[] value_iteration(MDP m) {
		double[] vf = new double[m.number_states()];
		for (int i = 0; i < vf.length; i++) {
			vf[i] = 0;
		}

		int iterations = 1000;
		for (int i = 0; i < iterations; i++) {
			double[] vf_new = new double[vf.length];
			//compute value for each cluster
			for (int s = 0; s < vf_new.length; s++) {
				double val = 0;
				// max over a
				for (int a = 0; a < m.number_actions(); a++) {
					double a_val = 0;					
					// add reward
					a_val += m.R(s, a);
					// add probability to next state
					Map<Integer, Double> pss = m.getHistogram(s, a); 
					for(Integer ss : pss.keySet()) {							
						a_val += m.gamma() * pss.get(ss) * vf[ss] / 100.0;
					}					
					val = Math.max(val, a_val);
				}
				vf_new[s] = val;
			}
			vf = vf_new;
		} // for iterations

		//		for (int i = 0; i < vf.length; i++) {
		//			System.out.print(prec(vf[i]) + " ");
		//		}System.out.println();
		return vf;
	}

	public static double[] value_iteration(MDP m, Cluster[] memb, List<Cluster> cls) {
		double[] vf = new double[cls.size()];
		for (int i = 0; i < vf.length; i++) {
			vf[i] = 0;
		}

		int iterations = 1000;
		for (int i = 0; i < iterations; i++) {
			double[] vf_new = new double[vf.length];
			//compute value for each cluster
			for (Cluster c : cls) {
				double val = 0;
				// max over a
				for (int a = 0; a < m.number_actions(); a++) {
					double a_val = 0;
					for (Integer s : c.elements) {
						// add reward
						a_val += m.R(s, a);
						// add probability to next state
						Map<Integer, Double> pss = m.getHistogram(s, a); 
						for(Integer ss : pss.keySet()) {							
							a_val += m.gamma() * pss.get(ss) * vf[memb[ss].idx] / 100.0;
						}
					}
					a_val /= c.elements.size(); //normalize-average out
					val = Math.max(val, a_val);
				}
				vf_new[c.idx] = val;
			}
			vf = vf_new;
		} // for iterations

		//		for (int i = 0; i < vf.length; i++) {
		//			System.out.print(prec(vf[i]) + " ");
		//		}System.out.println();
		//		
		return vf;
	}


	private static double prec(double d) {
		return Double.parseDouble(String.format("%.2f", d));
	}






	public static void vanilla_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		if(iterations == 0) return;

		reward_metric(m);
		lm.add(m.met);

		int num_states = m.number_states(); //used often

		/** Transition iterations**/
		while(--iterations > 0) {
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
			lm.add(met);
			m.met = met;
		}
	}



	private static void reward_metric(MDP m) throws OOBException {
		int num_states = m.number_states(); //used often 		

		/** Perform iterations **/
		/** First iteration based on reward **/
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
	}

	public static void asy_state_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		if(iterations == 0) return;

		int num_states = m.number_states(); //used often 		

		//compute reward
		reward_metric(m);
		lm.add(m.met);


		/** Transition iterations**/
		while(--iterations > 0) {
			System.out.println("Iterations left " + iterations); //TODO: sysout debug

			int num_of_pairs =  (num_states-1)*num_states / 2;

			while(num_of_pairs-- > 0) {
				//new metric computed asynchronously
				// randomly pick two different states s1 and s2

				int[] pair = rand_pair(num_states);

				double new_val = 0; //new value
				for (int i = 0; i < m.number_actions(); i++) {
					//TODO maybe build these histograms otherwise
					// Kantorovich
					MDP.Histogram h1 = m.new Histogram(m.getHistogram(pair[0], i));
					MDP.Histogram h2 = m.new Histogram(m.getHistogram(pair[1], i));
					double probDistance = h1.compareToJFastEMD(h2);

					//F operator
					double distance = Math.abs(m.R(pair[0], i) - m.R(pair[1], i))  
							+ m.gamma() * probDistance / 100.0;						
					//update value for this action
					new_val = (new_val > distance) ? new_val : distance; 	
				}
				//set the metric
				m.met.set(pair[0], pair[1], new_val);
			}
			lm.add(m.met);
		}
	}

	private static int[] rand_pair(int num_states) {
		if(num_states < 2) {
			System.out.println("HEEEY");
		}
		Random rand = new Random(); //TODO think about the choice of rng
		int[] toRet = new int[2];
		toRet[0] = rand.nextInt(num_states);		
		do { toRet[1] = rand.nextInt(num_states); } while(toRet[0] == toRet[1]);
		return toRet;
	}



	private static void printMembership(String filename, Cluster[] membership, int num_clusts) {		
		try {
			PrintWriter p = new PrintWriter(filename, "UTF-8");		
			for (int i = 0; i < num_clusts; i++) {
				for (int j = 0; j < membership.length-1; j++) {
					if(membership[j].idx == i) p.write("1 ");
					else p.write("0 ");
				}
				if(membership[membership.length-1].idx == i) p.write("1\n");
				else p.write("0\n");
			}
			p.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}


	public static double l_infty(double[] vf_state, double[] vf_cl, Cluster[] membs){
		double max_err = Double.MIN_VALUE;
		for (int i = 0; i < vf_state.length; i++) {
			max_err = Math.max(max_err, Math.abs(vf_state[i] - vf_cl[membs[i].idx] ));
		}
		return max_err;

	}
	
	
	public static double avg_dist(Metric m, List<Cluster> lc, int num_states) throws OOBException {
		double toRet = 0.0;
		
		for (int i = 0; i < lc.size(); i++) {
			for (int j = 0; j < i; j++) {
				toRet += lc.get(i).elements.size() * lc.get(j).elements.size() 
						* m.dist(i, j);
			}
		}
		
		return toRet / ((num_states-1)*num_states/2);
	}
	
	public static void declust_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		if(iterations == 0) return;

		double[] vf_init = value_iteration(m);


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

		// Step 1.3 add metric to toRet
		
		
		//System.out.println("Average distance " + prec(avg_dist(met, clustsR, num_states)));
		
		//lm.add(partition_met_to_whole_space_met(met, membR)); //TODO uncomment if
		// want to save metric


		/** Step 2 Transitions **/
		//Step 2.0 Setup for looping 
		Cluster[] membPr = membR; //previous membership
		List<Cluster> clustsPr = clustsR; // previous clustering

		//int filenum = 1;
		//String filename = "decl" + num_states + "_" + filenum++ + ".txt"; 
		//printMembership(filename, membPr, clustsPr.size());

		//double error = l_infty(vf_init, value_iteration(m, membPr, clustsPr), membPr);
		//System.out.println("Error is: " + prec(error));


		//Step 2.1 Iterate transition based declustering
		while(--iterations > 0) {
			//System.out.println("Iterations left " + iterations); //TODO: sysout debug
			//Step 2.1.1 : Declustering + Set-up
			List<Cluster> clustsCrt = new LinkedList<Cluster>();

			Cluster[] membCrt = new Cluster[num_states];
			//decluster each cluster
			//System.out.println("Size of clustsPr " + clustsPr.size()); //TODO debug
			for(Cluster c : clustsPr) {
				//System.out.println("---Size of clusters " + c.elements.size());
				Map<Integer,Cluster> newMembs = new TreeMap<Integer,Cluster>();
				List<Cluster> new_cls = declust_T(m, c, newMembs, membPr);
				for(Integer i : newMembs.keySet()) {
					membCrt[i] = newMembs.get(i);
				}
				//System.out.println("---Number of new clusts " + new_cls.size());
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

			System.out.println("num iters left " + iterations);
			System.out.println("num clusts: " + clustsCrt.size());
			System.out.println("num states: " + num_states);
			System.out.println("Computing metric..."); //TODO debug


			//Step 2.1.2 Compute distances
			c1_idx = 0;
			for(Cluster c1 : clustsCrt) {
				int c2_idx = 0;
				for(Cluster c2 : clustsCrt) {
					//only compute lower triangular part of the distance metric
					if(c2_idx == c1_idx) continue;

					//compute the distance between clusts
					double val = 0;
					for (int a = 0; a < m.number_actions(); a++) {
						double val_for_a = 0;
						int s1 = c1.elements.first();
						int s2 = c2.elements.first();

						// if the reward is not the same, the update value to difference in reward
						if(membR[s1] != membR[s2]) {
							val_for_a = Math.abs(m.R(s1, a) - m.R(s2, a)); 
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
				} // for c2
				c1_idx++;
			} // for c1	
			System.out.println("done computing metric...\n"); //TODO debug
			//Step 2.1.3 Create a metric over the entire state space
			//System.out.println("Average distance " + prec(avg_dist(met, clustsCrt, num_states)));
			//lm.add(partition_met_to_whole_space_met(met, membCrt)); //TODO uncomment for metric

			//Step 2.1.4 prepare for next loop
			membPr = membCrt;
			clustsPr = clustsCrt;	

			//double error2 = l_infty(vf_init, value_iteration(m, membPr, clustsPr), membPr);
			//System.out.println("Error is: " + prec(error2));

			//filename = "decl" + num_states + "_" + filenum++ + ".txt"; 
			//printMembership(filename, membPr, clustsPr.size());
		} // while iterations
	}


	private static Comparator<Cluster> sizeComparator = new Comparator<Cluster>(){
		@Override
		public int compare(Cluster c1, Cluster c2) {
			return (int) (c2.elements.size() - c1.elements.size());
		}
	};

	private static Comparator<Cluster> distComparator = new Comparator<Cluster>(){
		@Override
		public int compare(Cluster c1, Cluster c2) {
			return (int) (c2.priority - c1.priority);
		}
	};
	
	
	private static List<Cluster> approx(Cluster[] membership, Metric m, double epsilon) throws OOBException {
		List<Cluster> toRet = new ArrayList<Cluster>();
		
		List<Integer> lst = new LinkedList<Integer>();
		for (int i = 0; i < membership.length; i++) {
			lst.add(i);
		}
		java.util.Collections.shuffle(lst);
		
		
		for (Integer i : lst) {
			for (Cluster z : toRet) {
				if(m.dist(membership[i].idx, membership[z.elements.first()].idx) < epsilon) {
					z.elements.add(i);
					break;
				}
				Cluster c = new Cluster();
				c.elements.add(i);					
				toRet.add(c);
			}
		}			
		return toRet;
	}
	
	
	
	public static double compute_priority(Cluster c1, Cluster[] membership){
		if (c1.elements.size() < 2) return 0.0;
				
		//System.out.println("Cluster dist diff is" + c1.dist_diff);
		double diff1 = 0.0;
		Integer[] arr1 = new Integer[c1.elements.size()];
		int i = 0;
		for (Integer s : c1.elements) {
			arr1[i] = s;
			i++;
		}
		for(int n = 0; n < 1000; n++) {
			int[] pair = rand_pair(arr1.length);
			for (int a = 0; a < c1.m.number_actions(); a++) {	
				Map<Integer, Double> f = c1.m.getHistogram(arr1[pair[0]], a);
				Map<Integer, Double> g = c1.m.getHistogram(arr1[pair[1]], a);
				//Map<Cluster, Double> ff = hist_clust(f, membership);
				//Map<Cluster, Double> gg = hist_clust(g, membership);
				for (Integer key : f.keySet()) {
					if (g.containsKey(key)) diff1 += Math.abs(f.get(key) - g.get(key));
					else diff1 += f.get(key);
				}
				for (Integer key : g.keySet()) {
					if (!f.containsKey(key))  diff1 += g.get(key);
				}
			}	
		}
		//System.out.println("Cluster dist diff is" + c1.priority);
		return diff1;	
	}

	public static void asy_declust_computation(MDP m, int iterations, List<Metric> lm) throws OOBException{
		
		
		if(iterations == 0) return;
			
		int num_states = m.number_states(); //used often 		
		

		double[] vf_init = value_iteration(m);

		/** Step 1 is same for both synchronous and asynchronous declust computation **/
		/** Step 1: First iteration based on reward **/
		//Step 1.1 Declustering and Set-up
		Cluster[] membR = new Cluster[num_states];
		List<Cluster> clustsR = declust_R(m, membR);
		Metric metricToRet = new Metric(clustsR.size());
		//set up the index and the metric for subsequent iterations
		int c1_idx = 0; //index of cluster c1
		for(Cluster c1 : clustsR) {
			//update index and metric
			c1.idx = c1_idx++;
			c1.met = metricToRet;
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
				metricToRet.set(c1_idx, c2_idx, val);

				//increment index
				c2_idx++;
			}
			//increment index
			c1_idx++;
		}

		//TODO Step 1.3 add metric to toRet
		//lm.add(partition_met_to_whole_space_met(metricToRet, membR));


		/** Step 2 Asynchronous Transitions **/
		//Step 2.0 Setup for looping 
		Cluster[] membership = membR; //previous membership


		// convert the list of clusters to an arraylist
		ArrayList<Cluster> clustsCrt = new ArrayList<Cluster>(clustsR);	

		/** Heuristic 2: Build a priority queue based on the size of each cluster **/	
//		PriorityQueue<Cluster> size_queue = new PriorityQueue<Cluster>(clustsCrt.size(), sizeComparator);
//		for(Cluster c : clustsCrt){
//		//if(c.elements.size() > 1){
//			size_queue.add(c);
//		//}
//		}
		
		/** Heuristic 3: Build a priority queue based on the distribution difference inside each cluster **/	
		//System.out.println("Size of clustsCrt is " + clustsCrt.size());
		
		
		
		
		//System.out.println("Done constructing queue");

		//Step 2.1 Iterate transition based declustering
		while(--iterations > 0) {
			System.out.println("Iterations left " + iterations); 
			

			int bound = 0;

			while(clustsCrt.size() < 3000) {
				
				PriorityQueue<Cluster> dist_queue = new PriorityQueue<Cluster>(clustsCrt.size(), distComparator);
				for(Cluster c : clustsCrt){
					if(c.elements.size() > 1){
						c.priority = compute_priority(c, membership);
						dist_queue.add(c);
					}
					//System.out.println("added!!");
				}

				//Step 2.1.1 : Declustering + Set-up

				// save for later use
				int old_size0 = clustsCrt.size();				

				/** Heuristic 1: randomly pick two different clusters **/
				//int[] pair = rand_pair(old_size0);

				/** Heuristic 2: decluster the largest two clusters **/
//				int[] pair = new int[2];
//				pair[0] = size_queue.remove().idx;
//				pair[1] = size_queue.remove().idx;
				
				//for(Cluster c : dist_queue) {
					//System.out.println(c.idx + " " + c.elements.size() + " " + c.priority);
				//}
				
				
				/** Heuristic 3: decluster the largest two clusters **/
				int[] pair = new int[2];
				if (dist_queue.size() < 2 ) pair = rand_pair(old_size0);
				else{ 
					pair[0] = dist_queue.remove().idx;
					pair[1] = dist_queue.remove().idx;
				}
				
				//System.out.println("pair: " + pair[0] + " " + pair[1]);
				//System.out.println("sizes " + clustsCrt.get(pair[0]).elements.size() + " " + clustsCrt.get(pair[1]).elements.size());
				



				int new_clusters_size = clustsCrt.get(pair[0]).elements.size() + 
						clustsCrt.get(pair[1]).elements.size();
				//int num_new_pairs = ( new_clusters_size *( new_clusters_size -1))/2;


				// Perform the  update for the first randomly picked cluster
				Map<Integer,Cluster> newMembs0 = new TreeMap<Integer,Cluster>();
				List<Cluster> new_cls0 = declust_T(m, clustsCrt.get(pair[0]), newMembs0, membership);


				for(Cluster c : new_cls0) {
					c.met = metricToRet;
				}

				boolean is_replaced0 = false;
				for(Cluster c : new_cls0) {
					if (!is_replaced0){
						// if the old larger cluster has not been removed yet, replace it with the first sub cluster in the list
						clustsCrt.set(pair[0], c);
						is_replaced0 = true;
						// set the index
						c.idx = pair[0];
					}
					else{
						// else put the sub cluster at the end of the tree map					
						clustsCrt.add(c);
						// set the index
						c.idx = clustsCrt.size()-1;				
					}
				}

				// Perform the same update for the second randomly picked cluster
				Map<Integer,Cluster> newMembs1 = new TreeMap<Integer,Cluster>();
				List<Cluster> new_cls1 = declust_T(m, clustsCrt.get(pair[1]), newMembs1, membership);

				for(Cluster c : new_cls1) {
					c.met = metricToRet;
				}

				boolean is_replaced1 = false;
				for(Cluster c : new_cls1) {
					if (!is_replaced1){
						// if the old larger cluster has not been removed yet, replace it with the first sub cluster in the list
						clustsCrt.set(pair[1], c);
						is_replaced1 = true;
						// set the index
						c.idx = pair[1];
					}
					else{						
						// else put the sub cluster at the end of the tree map
						clustsCrt.add(c);
						// set the index
						c.idx = clustsCrt.size()-1;	
					}
				}
				
//				/** Heuristic 2: add new_cls0 and 1 to the priority queue **/
//				for(Cluster c : new_cls0){
////					if (c.elements.size() > 1) 
//					size_queue.add(c);
//				}
//				for(Cluster c : new_cls1){
////					if (c.elements.size() > 1) 
//						size_queue.add(c);
//				}
				
				/** Heuristic 3: add new_cls0 and 1 to the priority queue **/
				for(Cluster c : new_cls0){
					if (c.elements.size() > 1) {
						c.priority = compute_priority(c, membership);
						dist_queue.add(c);
					}
						
				}
				for(Cluster c : new_cls1){
					if (c.elements.size() > 1) {
						c.priority = compute_priority(c, membership);
						dist_queue.add(c);
					}
				}

				//System.out.println("Computing metric");
				//compute new distances
				Metric tmp_met = new Metric(new_cls1.size() + new_cls0.size());
				int i=0;
				for (Cluster c1 : new_cls0) {
					int j=0;
					for (Cluster c2 : new_cls0) {
						if (i == j) continue;
						double val = dist_clusts(m, c1, c2, membership, membR);
						tmp_met.set(i, j, val);
						j++;
					}
					i++;
				}
				for (Cluster c1 : new_cls1) {
					int j=0;
					for (Cluster c2 : new_cls0) {						
						double val = dist_clusts(m, c1, c2, membership, membR);
						tmp_met.set(i, j, val);
						j++;
					}
					for (Cluster c2 : new_cls1) {
						if (i == j) continue;
						double val = dist_clusts(m, c1, c2, membership, membR);
						tmp_met.set(i, j, val);
						j++;
					}
					i++;	
				}


				//set new distances
				for(int k=0; k < new_cls0.size() + new_cls1.size() - 2 ; k++){
					metricToRet.add_entry();
				}
				i=0;
				for (Cluster c1 : new_cls0) {
					int j=0;
					for (Cluster c2 : new_cls0) {
						if (i == j) continue;
						metricToRet.set(c1.idx, c2.idx, tmp_met.dist(i, j));
						j++;
					}
					i++;
				}
				for (Cluster c1 : new_cls1) {
					int j=0;
					for (Cluster c2 : new_cls0) {												
						metricToRet.set(c1.idx, c2.idx, tmp_met.dist(i, j));
						j++;
					}
					for (Cluster c2 : new_cls1) {
						if (i == j) continue;
						metricToRet.set(c1.idx, c2.idx, tmp_met.dist(i, j));
						j++;
					}
					i++;	
				}
				//System.out.println("done computing metric...\n"); //TODO debug


				for(Integer k : newMembs0.keySet()) {
					membership[k] = newMembs0.get(k);
				}
				for(Integer k : newMembs1.keySet()) {
					membership[k] = newMembs1.get(k);
				}

				// Reduce the iteration step by number of pairs in these two new clusters
				//num_of_pairs -= num_new_pairs;

				if(clustsCrt.size() > bound) {
					bound += 50;
					double error = l_infty(vf_init, value_iteration(m, membership, clustsCrt), membership);
					System.out.println("Error is: " + prec(error));
					System.out.println("Size of clustsPr " + clustsCrt.size()); //TODO debug
					
					int top = 10;
					ArrayList<Cluster> z = new ArrayList<Cluster>(top);
					for (int j = 0; j < top && !dist_queue.isEmpty(); j++) {
						Cluster a = dist_queue.remove();
						z.add(a);
						//System.out.println("size " + a.elements.size() + " " + " prio " + a.priority);
					}
					for(Cluster c : z) {
						dist_queue.add(c);
					}
				}
				
				
				
				//System.out.println(clustsCrt.size() + " size queue " + dist_queue.size());
			}

			//Step 2.1.3 Create a metric over the entire state space
			//lm.add(partition_met_to_whole_space_met(metricToRet, membership));

			//Step 2.1.4 prepare for next loop
			//TODO: think about this!!!
		} // while iterations

	}





	private static double dist_clusts (MDP m, Cluster c1, Cluster c2, Cluster[] membPr, Cluster[] membR) {
		//compute the distance between clusts
		double val = 0;
		for (int a = 0; a < m.number_actions(); a++) {
			double val_for_a = 0;
			int s1 = c1.elements.first();
			int s2 = c2.elements.first();

			// if the reward is not the same, the update value to difference in reward
			if(membR[s1] != membR[s2]) {
				val_for_a = Math.abs(m.R(s1, a) - m.R(s2, a)); 
			}
			// add difference in transition
			double probDistance = JFastEMD.distance(
					hist_stoc(m.getHistogram(s1, a), membPr), 
					hist_stoc(m.getHistogram(s2, a), membPr), 
					-1);
			val_for_a += m.gamma() * probDistance / 100.0;
			val = Math.max(val_for_a, val);
		}
		return val;
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
		Map<Cluster, Double> h1c = hist_clust(h1, membership);

		Signature s = new Signature();
		s.setNumberOfFeatures(h1c.size());

		Feature[] fs = new Feature[h1c.size()];
		double[] ds = new double[h1c.size()];
		int z=0;
		for(Cluster c : h1c.keySet()) {
			fs[z] = c;
			ds[z++] = h1c.get(c);
		}
		s.setFeatures(fs);
		s.setWeights(ds);

		return s;
	}


	private static Map<Cluster, Double> hist_clust(Map<Integer,Double> h1, Cluster[] membership) {
		Map<Cluster, Double> h1c = new HashMap<Cluster, Double>();

		for(Integer si : h1.keySet()) {
			if(h1c.containsKey(membership[si])) {
				h1c.put(membership[si], h1c.get(membership[si]) + h1.get(si));
			}else {
				h1c.put(membership[si], h1.get(si));
			}
		}
		return h1c;
	}

	private static List<Cluster> declust_T(MDP m, Cluster clarge, Map<Integer,Cluster> membership, Cluster[] oldMembership) {
		List<Cluster> toRet =  new LinkedList<Cluster>();
		/** Transitions **/
		List<Integer> lst = new LinkedList<Integer>();
		for (Integer i : clarge.elements) {
			lst.add(i);
		}
		java.util.Collections.shuffle(lst);

		//find clust to insert element
		for(Integer i : lst) {
			boolean isSet = false;			
			for(Cluster c : toRet) {
				boolean toAdd = true;
				for (int a = 0; toAdd && a < m.number_actions(); a++) {					
					Map<Cluster, Double> h1 = hist_clust(m.getHistogram(c.elements.first(), a), oldMembership); 
					Map<Cluster, Double> h2 = hist_clust(m.getHistogram(i, a), oldMembership); 

					//printMap(h1);
					//printMap(h2);


					toAdd = (h1.size() == h2.size());
					if(!toAdd) break;
					for(Cluster z : h2.keySet()) {
						toAdd = h1.containsKey(z);
						if(!toAdd) break;
					}
					if(!toAdd) break;
					for(Cluster z : h2.keySet()) {
						toAdd = Math.abs(h1.get(z) - h2.get(z)) < EPSILON;
						if(!toAdd) break;
					}
				} // for a
				if(toAdd) { isSet = true; c.elements.add(i); membership.put(i, c); break; }				
			} //for Cluster c
			if(!isSet) {
				Cluster c = new Cluster();
				c.elements.add(i);
				isSet = true;
				membership.put(i, c);
				toRet.add(c);
				c.m = m;
				
			}
		}
		
		
		return toRet;

	}

	static void printMap(Map<Cluster,Double> mp) {
		System.out.println("Map:");
		for(Cluster z : mp.keySet()) {
			System.out.println(z.elements.first() + " -> " + mp.get(z));
		}
		System.out.println("------------");
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
				for (int a = 0; toAdd && a < m.number_actions(); a++) {
					toAdd = (Math.abs(m.R(c.elements.first(),a) - m.R(i, a)) < EPSILON);
				}
				if(toAdd) { c.elements.add(i); membership[i] = c; break; }				
			}
			if(membership[i] == null) {
				Cluster c = new Cluster();
				c.elements.add(i);
				membership[i] = c;
				toRet.add(c);
				c.m = m;
				
			}
		}

		//		System.out.println("I'm returning: \n");
		//		for(Cluster c : toRet) {
		//			System.out.println(c);
		//		}
		//		System.out.println(); //TODO debug

//		for (Cluster c : toRet) {
//			c.priority = compute_priority(c);
//		}
		return toRet; 
	}



	public static class Cluster implements Feature {

		TreeSet<Integer> elements;

		int idx;
		Metric met;
		MDP m;
		double priority;

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

		@Override
		public String toString() {
			String s = "Clust membs: ";
			for(Integer i : elements) {
				s += i + " ";
			}
			//s += "\n";
			return s;
		}
	}





	/******* MAIN FUNCTION ******/

	public static void main(String[] args) {
//		MDP m = new GridMDP(70, GridMDP.GridType.DEFAULT);
		MDP m = new PuddleMDP(80);
		//System.out.println(m);
		List<Metric> lm = new LinkedList<Metric>();
		try {
			//vanilla_computation(m, 7, lm);//TODO change num iters
			//asy_state_computation(m,8,lm);
			//declust_computation(m, 15, lm);
			asy_declust_computation(m,10,lm);
			System.out.println("main: Size lm " + lm.size());
		}catch (OOBException e) {
			e.printStackTrace();
			System.out.println(e.s);
			System.out.println("Bum");
		}
	}



	public static void mainTry(String[] args) {
		int[][] a = new int[0][];
		for (int i = 0; i < a.length; i++) {
			System.out.println("haha");
		}
	}





}

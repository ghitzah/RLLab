package MDPHierarchy;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.text.*;

import SpecificMDPs.*;

/**
 * Implementation of an MDP that is based on aggregating states from some larger MDP (possibly an aggregate MDP as well)
 * @author gcoman
 *
 */
public class AggMDP extends MDP{


	/**
	 * The special case of a State: a cluster of states from the underlying MDP
	 * @author gcoman
	 */
	public class Cluster extends MDP.State{

		/**
		 * A map that provides all "labels"(i.e. underlying sub-clusters) that are associated to a cluster
		 */
		private List<State> c_to_s;

		/**
		 * Main constructor 
		 * @param mdp : the MDP associated with this Cluster
		 * @param index : the index of the state as an integer from 0 to SIZE-1
		 */
		public Cluster(MDP mdp, int index) {
			super(mdp, index);
			c_to_s = new LinkedList<MDP.State>();
		}

		@Override
		public State baseState() {			
			return c_to_s.get(0).baseState();
		}
		
	}

	/**
	 * The actual MDP that is aggregated
	 */
	private MDP larger_mdp;



	/**
	 * all_clusts : A collection of all clusters in the current aggregate MDP 
	 */
	private Collection<Cluster> all_clusts;




	/**
	 * Main constructor : since this is a base constructor, the clusters are determined
	 * based on differences in reward
	 * @param m : the underlying MDP
	 */
	public AggMDP(MDP m) {
		// the underlying MDP is the actual MDP given as input
		larger_mdp = m;

		// set up state comparator based on reward being equal for all actions
		StateComp cmp = new StateComp() {
			@Override
			public double compare(State o1, State o2) {

				try {
					// Calculate the distance between two states (reward-based):
					// distance(o1, o2) = max_a |R(o1,a) - R(o2,a)|
					double maxDistance = 0;
					for (int i = 0; i < number_actions(); i++) {
						double distance = Math.abs(larger_mdp.R(o1, i) - larger_mdp.R(o2, i));
						maxDistance = (maxDistance > distance) ? maxDistance : distance; 	
					}
					return maxDistance;

				}catch (InvalidMDPException e) {
					System.out.println("ERROR\n");
					return -1;
				}
			}			
		};


		//generate the given clusters
		// distance matrix D represented in list of list form
		List<List<Double>> distances = new LinkedList<List<Double>>();

		all_clusts = decluster(larger_mdp.getStates(),cmp, distances);	

		// construct the distance matrix D
		D = convertToMatrix(distances);

		//set parent links
		for(Cluster c : all_clusts) {			
			for(State s : c.c_to_s) {				
				s.parent = c;
			}
		}
	}




	/**
	 * Constructor which builds clusters based on differences in the probability transition maps
	 * @param agg_m : the Aggregate MDP that becomes declustered. Note that this input Aggregate MDP will get modified. 
	 * The newly created Aggregate MDP will be layer between the input AggMDP m and its underlying MDP
	 */
	public AggMDP(AggMDP agg_m) {				
		// MDP needed for generating histograms - has to be final
		final AggMDP histogram_mdp = agg_m;


		//set up state comparator based on histograms being equal for all actions	
		StateComp cmp = new StateComp() {
			@Override
			public double compare(State o1, State o2) {

				try {
					// Calculate the distance between two states (probability-based):
					// distance(o1, o2) = max_a (|R(o1,a) - R(o2,a)| + r D(P_o1^a, P_o2^a))
					double maxDistance = 0;
					int num_a = histogram_mdp.number_actions();					
					for (int i = 0; i < num_a; i++) {
						Histogram h1 = new Histogram(o1.getHistogram(i, histogram_mdp));
						Histogram h2 = new Histogram(o2.getHistogram(i, histogram_mdp));		
						double probDistance = h1.compareToJFastEMD(h2);
						//double probDistance = h1.compareTo(h2);
						double distance = Math.abs(o1.R(i) - o2.R(i)) + GAMMA*probDistance / 100.0;						
						maxDistance = (maxDistance > distance) ? maxDistance : distance; 	
					}
					return maxDistance;

				}catch (InvalidMDPException e) {
					e.printError();
					return -1;
				}
			}
		};

		/** decluster each cluster */
		
		// distance matrix D represented in list of lists form
		List<List<Double>> all_distances = new LinkedList<List<Double>>();
		all_clusts = new LinkedList<AggMDP.Cluster>();
		
		
		for(State z : histogram_mdp.getStates()) {
			Cluster cl = (Cluster) z;  
			
			// distances between states belonging to same cluster
			List<List<Double>> local_dist = new LinkedList<List<Double>>();
			
			// distances between states belonging to different clusters
			List<List<Double>> global_dist = new LinkedList<List<Double>>();
			
			List<Cluster> lcs = decluster(cl.c_to_s, cmp, local_dist);
			
			Iterator<List<Double>> it_dists = local_dist.iterator();
			
			for(Cluster c : lcs) {
				// set index
				c.index += all_clusts.size();
				
				//tmp_lst will be added to the global distance metric
				List<Double> tmp_lst = new LinkedList<Double>();
				// compute the missing distances between states in the current cluster and all_clusts
				for (Cluster l: all_clusts){
					double dist = cmp.compare(c.c_to_s.get(0), l.c_to_s.get(0));
					tmp_lst.add(dist);
				}
				// append two lists representing distances inside and among clusters
				tmp_lst.addAll(it_dists.next());
				global_dist.add(tmp_lst);
			}
			all_distances.addAll(global_dist);
			all_clusts.addAll(lcs);
		}
		
		// construct the distance matrix D
		D = convertToMatrix(all_distances);

		// the newly created aggregate MDP is a layer between the agg_m provided as 
		// parameter and what used to be its larger_mdp
		this.larger_mdp = agg_m.larger_mdp;
		agg_m.larger_mdp = this;

		for(Cluster c : all_clusts) {
			c.parent = c.c_to_s.get(0).parent;
			for(State s : c.c_to_s) {				
				s.parent = c;
			}
		}
	}


	public interface StateComp {
		public double compare(State s1, State s2) ;
	}

	private List<Cluster> decluster(Collection<State> states, StateComp cmp, List<List<Double>> distances) {
		//initialize all_clusts to empty, then try to create new clusters using the states 
		// in the underlying MDP
		List<Cluster> toRet = new LinkedList<Cluster>();

		// find the cluster associated with each state
		for (State s : states) {
			// if no cluster has measure, then create new cluster
			boolean new_clust = true;

			// list storing all distances from all previous clusters to the current cluster
			List<Double> dist_list = new LinkedList<Double>();
			
			for (Cluster c : toRet) {
				double dist = cmp.compare(s,c.c_to_s.get(0));
				
				//check whether the state are the same or not
				if(dist < 0.00001) { // add state to cluster TODO: magic number(epsilon)
					c.c_to_s.add(s);
					new_clust = false; // don't create a new clust
					break;
				}
				else{ // add the distance to the list
					dist_list.add(dist);
				}
			} // for c
			
			if(new_clust) { //create new clust
				Cluster c_new = new Cluster(this, toRet.size());
				c_new.c_to_s.add(s); //add the only state it contains, for now				
				toRet.add(c_new); 
				distances.add(dist_list); // add the distances collected so far
			}						
		} // for s

		return toRet;
	}

	// convert the list of lists to an array of arrays
	private double[][] convertToMatrix(List<List<Double>> distances) {
		int n = distances.size();
		double D[][] = new double[n][];
		for (int i = 0; i < n; i++){
			int w = distances.get(i).size();
			if(w != 0) {
				D[i] = new double[w];
				int j = 0;
				for (Double d: distances.get(i)){
					D[i][j] = d;
					j++;
				}
			}
		}
		return D;
	}



	@Override
	public double R(State c, int a) throws InvalidMDPException{

		if (c.sameMdp(this)) {
			double d = 0.0;
			Cluster cc = (Cluster) c;
			Collection<State> sns = cc.c_to_s;
			for (State sn : sns) 
				d += larger_mdp.R(sn, a);
			return d / sns.size();
		}else throw new InvalidMDPException();

	}

	@Override
	public double P(State c, int a, State cn) throws InvalidMDPException{
		double d = 0.0;
		if (c.sameMdp(this) && cn.sameMdp(this)) {
			Cluster cc = (Cluster) c;
			Collection<State> ss = cc.c_to_s;
			cc = (Cluster) cn;
			Collection<State> sns = cc.c_to_s;

			for (State si : ss)
				for (State sni : sns)  
					d += larger_mdp.P(si, a, sni);				
			return d / ss.size();
		}else throw new InvalidMDPException();

	}

	@Override
	public int number_states() {		
		return all_clusts.size();
	}


	@Override
	public int number_actions() {
		return larger_mdp.number_actions();
	}	

	@Override
	public Map<State,Double> getHistogram(State c, int a) throws InvalidMDPException{
		State cb = c.baseState();
		return cb.getHistogram(a, this);
	}

	@Override
	public Collection<State> getStates() {
		Collection<State> toRet = new LinkedList<State>();
		for(State s : all_clusts) {
			toRet.add(s);
		}
		return toRet;
	}

	@Override
	public String toString() {
		//String toRet = "\n\n UNDERLYING MDP \n\n";		
		String toRet = "";
		toRet += larger_mdp.toString();
		toRet += "\n\n CLUSTERS \n\n";
		for (Cluster c : all_clusts) {
			toRet += "C-" + c.index + " ----> ";
			for(State s : c.c_to_s) {
				toRet += "S-" + s.index + ":" + s.toString()  + " _ "; 
			}toRet += "\n";
		}
		toRet += "\n\n METRIC \n\n";
		for (int i=1 ; i < D.length ; i++) {			
			for (int j = 0; j < D[i].length; j++) {
				DecimalFormat df = new DecimalFormat("###.##");
				String d = df.format(D[i][j]);
				toRet += d + " ";
			}toRet += "\n";
		}
		
		return toRet;
	}	
	

	public static void main(String[] args) {
		MDP m = new PuddleMDP(10);
		AggMDP mR = new AggMDP(m);
		AggMDP mP = new AggMDP(mR);
		mP = new AggMDP(mP);
		mP = new AggMDP(mP);		
		System.out.println(mR);
	}

}

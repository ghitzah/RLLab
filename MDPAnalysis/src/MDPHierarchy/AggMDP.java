package MDPHierarchy;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

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
		Comparator<State> cmp = new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				try {
					boolean all_equal = true;
					for (int i = 0; i < number_actions() && all_equal; i++) {
						all_equal = (larger_mdp.R(o1, i) == larger_mdp.R(o2, i));
					}
					if(all_equal) return 0;
					else return -1;
				}catch (InvalidMDPException e) {
					//TODO
					return -1;
				}
			}			
		};
		
		//generate the given clusters
		all_clusts = decluster(larger_mdp.getStates(),cmp);		
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
		Comparator<State> cmp = new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				try {
					boolean all_equal = true;
					for (int i = 0; i < number_actions() && all_equal; i++) {
						all_equal = (histogram_mdp.larger_mdp.getHistogram(o1, i, histogram_mdp) == 
								histogram_mdp.larger_mdp.getHistogram(o2, i, histogram_mdp));
					}
					if(all_equal) return 0;
					else return -1;
				}catch (InvalidMDPException e) {
					//TODO
					return -1;
				}
			}
		};
		
		//decluster each cluster 	
		all_clusts = new LinkedList<AggMDP.Cluster>();
		for(State z : histogram_mdp.getStates()) {
			Cluster cl = (Cluster) z;  
			List<Cluster> lcs = decluster(cl.c_to_s, cmp);
			for(Cluster c : lcs) {
				c.parent = cl;
			}
			all_clusts.addAll(lcs);
		}
		
		// the newly created aggregate MDP is a layer between the agg_m provided as 
		// parameter and what used to be its larger_mdp
		this.larger_mdp = agg_m.larger_mdp;
		agg_m.larger_mdp = this;
		
		
	}


	private List<Cluster> decluster(Collection<State> states, Comparator<State> cmp) {
		//initialize all_clusts to empty, then try to create new clusters using the states 
		// in the underlying MDP
		List<Cluster> toRet = new LinkedList<Cluster>();
		// find the cluster associated with each state
		for (State s : states) {
			// if no cluster has the same reward, then create new cluster
			boolean new_clust = true;
			for (Cluster c : toRet) {
				//check whether the state are the same or not
				if(cmp.compare(s,c.c_to_s.get(0)) == 0) { // add state to cluster
					c.c_to_s.add(s);
					s.parent = c;
					new_clust = false; // don't create a new clust
					break;
				}
			} // for c
			if(new_clust) { //create new clust
				Cluster c_new = new Cluster(this, toRet.size());
				c_new.c_to_s.add(s); //add the only state it contains, for now
				s.parent = c_new;
				toRet.add(c_new); 
			}						
		} // for s	
		return toRet;
	}
	
	
	@Override
	public double R(State c, int a) throws InvalidMDPException{
		
		if (c.mdp == this) {
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
		if (c.mdp == this && cn.mdp == this) {
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
	public Histogram getHistogram(State c, int a, MDP m) throws InvalidMDPException{
		State cb = c.baseState();
		return cb.mdp.getHistogram(cb, a, m);
	}

	@Override
	public Collection<State> getStates() {
		Collection<State> toRet = new LinkedList<State>();
		for(State s : all_clusts) {
			toRet.add(s);
		}
		return toRet;
	}
}

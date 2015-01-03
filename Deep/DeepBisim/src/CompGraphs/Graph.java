package CompGraphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import CompGraphs.ModelComparator.IncorrectModelExpection;
import MDP.MDP.Feature;
import MDP.MDP.State;
import MDP.MDP.*;
import MDP.MDP;

public class Graph {

	/*********************
	 * CLASSES
	 ********************/
	
	/**
	 * Class implementing a node in the computation graph
	 * @author gcoman
	 */
	public class Node implements Comparable<Node>{
	
		final int idx;
		
		/**
		 * The label of the node
		 */
		final Model label;
		
		/**
		 * Model distance comparator that can be used when computing 
		 * the activation of this function
		 */
		ModelComparator modelDist;
		
		/**
		 * Dependents that can be used when computing 
		 * the activation of this function
		 */
		Set<Node> progenitors;
		
		/**
		 * Activation function
		 */
		Feature activation;

		/**
		 * Main constructor
		 * @param idx :  index of node in the graph - used for presentation
		 * @param label : the model that this node is representing
		 * @param modelDist : the distance function used to compare models
		 * @param progenitors : progenitor nodes in the graph
		 * @param activation : the feature that this node represents
		 */
		public Node(int idx, Model label, ModelComparator modelDist, 
				Set<Node> progenitors, Feature activation) {
			this.idx = idx;
			this.label = label;
			this.modelDist = modelDist;
			this.progenitors = progenitors;
			this.activation = activation;
		}
		
		
		/**
		 * Activation at state s - based on the activation function
		 * @param s - state for which we want to compute the activation function 
		 * @return : the activation of this feature at s
		 */
		public double activation(State s) {
			return activation.eval(s);
		}
		
		
		/**
		 * Return set of dependent features
		 */
		public Set<Feature> getDependentFeatures() {
			HashSet<Feature> toRet = new HashSet<Feature>();
			for (Node n : progenitors) {
				toRet.add(n.activation);
			}
			return toRet;
		}


		@Override
		public int compareTo(Node o) {
			return this.idx - o.idx;
		}
	}
	
	
	
	/*********************
	 * MEMBER VARIABLES
	 ********************/
	
	/**
	 * MDP over which we compute bisimulation
	 */
	MDP m;
	
	/**
	 * The set of all nodes
	 */
	TreeSet<Node> allNodes;
	
	/**
	 * The final layer of the graph
	 */
	TreeSet<Node> finalLayer;
	
	
	
	public Graph(MDP m) {
		this.m = m;
		allNodes = new TreeSet<>();
		finalLayer = new TreeSet<>();
	}
	
	
	
	
	/**
	 * will be used for comparing doubles (for numerical stability)
	 */
	double EPSILON = 0.0000001;
	
	/**
	 * Returns a map that associates a value to each node
	 * - the nodes activation
	 * @param s - the state for which we want to compute the activation
	 * @return - the activation value for each node
	 */
	public Map<Node,Double> lastLayer(State s) 
			throws MDP.IncorrectMDPException{
		if(!s.checkMDP(m)) throw m.new IncorrectMDPException();
		HashMap<Node, Double> toRet = new HashMap<Node, Double>();
		for(Node n : finalLayer) {
			toRet.put(n, n.activation(s));
		}
		return toRet;
	}
	
	/**
	 * checks whether the graph satisfied the approximation condition
	 * @return true if the condition holds for all nodes
	 */
	public boolean check_graph() {
		for(Node n : allNodes) {
			Iterator<StatePair> ada = m.get_state_pair_iterator();
			while(ada.hasNext()) {
				StatePair p = ada.next();
				try {
					if(n.modelDist.dist(m.new ExactStateModel(p.s1),
							m.new ExactStateModel(p.s1),n.getDependentFeatures()) < EPSILON && 
						Math.abs(n.activation(p.s1) - n.activation(p.s2)) >= EPSILON) {
							return false; 
					}
				} catch (IncorrectModelExpection e) {
					System.err.println("Can't go wrong here");
					e.printStackTrace();
				} // if
			} // while
		} // for 
		return true;
	}

	
	public int graphSize() {
		return (allNodes == null) ? 0 : allNodes.size();
	}
	
	public int representationSize() {
		return (finalLayer == null) ? 0 : finalLayer.size();
	}
	

}

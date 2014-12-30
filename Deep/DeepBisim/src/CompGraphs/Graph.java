package CompGraphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
		Model label;
		
		/**
		 * Model distance comparator that can be used when computing 
		 * the activation of this function
		 */
		ModelComparator modelDist;
		
		/**
		 * Dependents that can be used when computing 
		 * the activation of this function
		 */
		Set<Node> dependents;
		
		/**
		 * Activation function
		 */
		Feature activation;

		
		public Node(int idx, Model label, ModelComparator modelDist, 
				Set<Node> dependents, Feature activation) {
			this.idx = idx;
			this.label = label;
			this.modelDist = modelDist;
			this.dependents = dependents;
			this.activation = activation;
		}
		
		
		/**
		 * Activation at state s - based on the activation function
		 * @param s - state for which we want to compute the activation function 
		 * @return
		 */
		public double activation(State s) {
			return activation.eval(s);
		}
		
		
		
		/**
		 * Return set of dependent features
		 */
		public Set<Feature> getDependentFeatures() {
			TreeSet<Feature> toRet = new TreeSet<Feature>();
			for (Node n : dependents) {
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
	public Map<Node,Double> lastLayer(State s) {
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
				if(n.modelDist.dist(m.new ExactStateModel(p.s1),
						m.new ExactStateModel(p.s1),n.getDependentFeatures()) < EPSILON && 
					Math.abs(n.activation(p.s1) - n.activation(p.s2)) >= EPSILON) {
						return false; 
				} // if
			} // while
		} // for 
		return true;
	}
}

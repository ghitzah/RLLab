package CompGraphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import CompGraphs.ModelComparator.Model;
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
	public class Node {
	
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

		/**
		 * Activation at state s - based on the activation function
		 * @param s - state for which we want to compute the activation function 
		 * @return
		 */
		public double activation(State s) {
			return activation.eval(s);
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
	Set<Node> allNodes;
	
	/**
	 * The final layer of the graph
	 */
	Set<Node> finalLayer;
	
	
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
		return null;
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
				if(n.modelDist.dist(p.s1,p.s2) < EPSILON && 
					Math.abs(n.activation(p.s1) - n.activation(p.s2)) >= EPSILON) {
						return false; 
				} // if
			} // while
		} // for 
		return true;
	}
}

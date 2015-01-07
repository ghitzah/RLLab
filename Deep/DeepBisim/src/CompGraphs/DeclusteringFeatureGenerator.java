package CompGraphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import CompGraphs.DeclustGraph.AlgorithmicException;
import CompGraphs.ModelComparator.IncorrectModelExpection;
import MDP.MDP;
import MDP.MDP.Model;
import MDP.MDP.State;

/**
 * This class implements the new algorithm that generalized the 
 * Exact Bisimulation Algorithm
 * @author gcoman
 *
 */
public class DeclusteringFeatureGenerator extends SimpleFeatureGenerator{


	public DeclusteringFeatureGenerator(MDP m, int numIters) {
		super(m, numIters);
	}

	public DeclusteringFeatureGenerator(MDP m) {
		this(m, DEFAULT_NUM_ITERS);
	}


	public void addNewLayer() throws AlgorithmicException{
		addNewLayer(allNodes.size() == 0);
	}


	private void addNewLayer(boolean firstLayer) {
		// this will maintain the index of new nodes
		int nextIndex = (allNodes == null) ? 0 : allNodes.size();

		// save the last layer from old graph - needed to test transition map
		Set<Node> prevLayer = finalLayer;

		//we will rebuild the final layer from 0
		finalLayer = new TreeSet<Node>();


		// select a subset of nodes to analyze
		//Set<Node> NodesSelected = new Heuristic(100, Heuristic.HeuristicType.RANDOM_PAIR).select(prevLayer);
		
		Set<Node> NodesSelected = prevLayer;
		if(firstLayer) {
			NodesSelected = new HashSet<Node>();
			NodesSelected.add(new Node(-1, null, null, null, null));	
		}
		
		// mapping each node to a list of nodes
		HashMap<Node, LinkedList<Node>> nodeToList = new HashMap<Node, LinkedList<Node>>();
		// put selected nodes into the map
		for(Node pNode : NodesSelected){
			nodeToList.put(pNode, new LinkedList<Node>());
		}

		// get a fixed number of states to determine which nodes to create
		Iterator<State> ada = m.get_state_iterator();
		int tmpNumIters = numIters;
		while(ada.hasNext() && tmpNumIters-- > 0) {

			Model modelIteratedState = new GroundedModel(ada.next(), 100);

			for(Node pNode : NodesSelected) {
				// saved model that we compare to
				try {
					// if it's in this cluster, then we decluster 
					if(firstLayer || pNode.dist(modelIteratedState) < EPSILON) { 

						boolean sameModel = false;

						if(!nodeToList.get(pNode).isEmpty()) {
							for(Node n : nodeToList.get(pNode)) {
								if(n.dist(modelIteratedState) < EPSILON){
									sameModel = true; // no need to create a new node
									break;
								}
							}
						}

						// if the associated list is empty 
						// or the examining state not in any cluster
						if(!sameModel){	
							// create a new node
							final Node nNew = new Node(nextIndex++, 
									modelIteratedState,
									new LInfComparator(m),
									prevLayer, 
									null /* done below*/);
							nNew.activation = new MemoizedFeature() {
								@Override
								double eval_non_existent(State s) {
									try {
										return Math.exp(- ALPHA * nNew.dist(new GroundedModel(s, 100)));
									} catch (IncorrectModelExpection e) {
										e.printStackTrace();
										return 0;
									}
								}
							};
							// add this new node to the list
							nodeToList.get(pNode).add(nNew);
						}	 
					}
				} catch (IncorrectModelExpection e) {
					e.printStackTrace();
				}
			} // for nFinalLayer
		}


		//construct the final layer
		for(Entry<Node, LinkedList<Node>> n : nodeToList.entrySet()) {
			if(n.getValue().isEmpty()){
				finalLayer.add(n.getKey());
			}
			else{
				finalLayer.addAll(n.getValue());
				allNodes.addAll(n.getValue());
			}
		}

		// copy the rest of the nodes which are not selected to the final layer
		for(Node n: prevLayer){
			if(!NodesSelected.contains(n)){
				finalLayer.add(n);
			}
		}
	}


}
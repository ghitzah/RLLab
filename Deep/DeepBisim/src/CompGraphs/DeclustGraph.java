package CompGraphs;


import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;

import MDP.MDP;
import MDP.MDP.Action;
import MDP.MDP.ExactStateModel;
import MDP.MDP.Cluster;
import MDP.MDP.Model;
import MDP.MDP.State;

public class DeclustGraph extends ExactBisimGraph{

	DeclustHeuristic heuristic;
	
	public DeclustGraph(MDP m) {
		super(m);
		// default synchronous declustering heuristic
		heuristic = new DeclustHeuristic() {

			@Override
			public Set<Node> select(Set<Node> allNodes) {
					return allNodes;
				}
		}; 
	}

	/**
	 * adds a new layer using the default heuristic - descluster all
	 * @throws AlgorithmicException 
	 */
	public void addNewLayer() throws AlgorithmicException{
		addNewLayer(heuristic);
	}
		
	
	/**
	 * Adds a new layer using a specific heuristic
	 * @param heuristic
	 * @throws AlgorithmicException
	 */
	public void addNewLayer(DeclustHeuristic heuristic) throws AlgorithmicException{
		int nextIndex = allNodes.size(); // next index
		
		// save the last layer from old graph
		Set<Node> prevLayer = finalLayer;
		
		// we will iteratively build the last layer
		finalLayer = new TreeSet<Node>();
		
		// will contain new nodes (nodes in new layer)
		Set<Node> newlyCreatedNodes = new TreeSet<Node>();
		
		Set<Node> toDecluster = heuristic.select(prevLayer);
		
		// decluster each node from the last layer of the previous graph
		for(Node nodePrevLayer : toDecluster) {
			// result of the declustering process will be saved here
			Set<Node> declusteringNodes = new HashSet<Node>(); 
			
			Cluster f = (Cluster) nodePrevLayer.activation;
			
			if( f.numberMembers() == 0) throw new AlgorithmicException();
			
			if( f.numberMembers() == 1) { 
				finalLayer.add(nodePrevLayer);
				continue;
			}
			
			// analyse all elements in the cluster
			for(State iteratedState : f.all_members()) {
				
				Model modelIteratedState = m.new ExactStateModel(iteratedState);
				
				boolean newNode = true; // true if a new node has to be created for s
				for(Node nClust : declusteringNodes) {
					ExactStateModel savedModel = (ExactStateModel) ((ExactBisimNode) nClust).label;
					
					boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
					
					Iterator<Action> ada = m.get_action_iterator();
					while(ada.hasNext()) {
						Action a  = ada.next();
						for(Node prevNode : prevLayer) {
							double tmp = modelIteratedState.T(a).integrateExact(prevNode.activation) 
									- savedModel.T(a).integrateExact(prevNode.activation);
							if(tmp > EPSILON) { sameModel = false; break; }
						} // for
					} // while
					if(sameModel) { // if we found a clust with the same model
						newNode = false; // no need to creat a new node
						((Cluster) nClust.activation).add_state(iteratedState);
						break;
					}
				}
				if(newNode) { // if new node has to be created
					Node nNew = new ExactBisimNode(m, iteratedState, prevLayer, nextIndex++); // create node
					declusteringNodes.add(nNew); // add it to result of declustering
				}
			} // for s
			
			if(declusteringNodes.size() == 0) throw new AlgorithmicException();
			if(declusteringNodes.size() == 1) { finalLayer.add(nodePrevLayer); }
			else {
				for( Node n : declusteringNodes) {
					newlyCreatedNodes.add(n); // add all nodes to the finalLayer
				}
			}
		} // for preFinalNode
		
		// add result to allNodes
		for(Node n : newlyCreatedNodes) {
			allNodes.add(n);
			finalLayer.add(n);
		}
		
		//prepare lastLayer
		prevLayer.removeAll(toDecluster);
		for(Node n : prevLayer) {
			finalLayer.add(n);
		}
	}
	
	
	public interface DeclustHeuristic {
		
		/**
		 * Selects a subset of nodes to use for declustering purposes
		 * @param allNodes : the nodes over which the selection has to 
		 * be done
		 * @return : a subset of allNodes
		 */
		public Set<Node> select(Set<Node> allNodes);
	}
	
	
	@SuppressWarnings("serial")
	public class AlgorithmicException extends Exception {}
}

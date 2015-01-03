package CompGraphs;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;

import CompGraphs.DeclustGraph.AlgorithmicException;
import MDP.MDP;
import MDP.MDP.Action;
import MDP.MDP.ExactStateModel;
import MDP.MDP.Model;
import MDP.MDP.State;
import MDP.MDP.FiniteSFeature;

/**
 * Class implementing the computation graph associated with the computing exact bisimulation relations
 * @author gcoman
 *
 */
public class ExactBisimGraph extends Graph{

	/**
	 * Constructor
	 * @param m : MDP for which we compute bisimulation 
	 */
	public ExactBisimGraph(MDP m) {
		super(m);
		
		/**CREATE THE FIRST LAYER BASED ON REWARD*/
		
		int nextIndex = 0; // index
		
		// we will iteratively build the last layer
		finalLayer = new TreeSet<Node>();
		
		// check all states for all necessary models labels
		Iterator<State> ada = m.get_state_iterator();
		while(ada.hasNext()) {
			State iteratedState = ada.next();
			
			Model modelIteratedState = m.new ExactStateModel(iteratedState);
			
			boolean newNode = true; // true if a new node has to be created for s
			
			for(Node nFinalLayer : finalLayer) {
				// saved model that we compare to
				ExactStateModel savedModel = (ExactStateModel) ((ExactBisimNode) nFinalLayer).label;

				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				
				Iterator<Action> bee = m.get_action_iterator();
				while(bee.hasNext()) {
					Action a  = bee.next();
					double diffR = Math.abs(modelIteratedState.R(a)-savedModel.R(a));
					if(diffR > EPSILON) { 
						sameModel = false; 
						break; 
					}
				} // while
				
				if(sameModel) { // if we found a clust with the same model
					newNode = false; // no need to creat a new node
					((FiniteSFeature) nFinalLayer.activation).add_state(iteratedState);
					break;
				}
			} // for

			// create new node
			if(newNode) { // if new node has to be created
				Node nNew = new ExactBisimNode(m, iteratedState, new HashSet<Node>(), nextIndex++); // create node
				finalLayer.add(nNew); // add it to result of declustering
			}
		}
		
		//create the set of all nodes
		allNodes = new TreeSet<Node>();
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
	}
	
	/**
	 * add a new layer to the graph
	 * @throws AlgorithmicException 
	 */
	public void addNewLayer() throws AlgorithmicException {
		int nextIndex = allNodes.size(); // next index 
		
		// save the last layer from old graph -  needed to test transition map
		Set<Node> prevLayer = finalLayer;
		
		// we will iteratively build the last layer
		finalLayer = new TreeSet<Node>();
		
		// check all states for all possible labels
		Iterator<State> ada = m.get_state_iterator();
		while(ada.hasNext()) {
			State iteratedState = ada.next();
			
			Model modelIteratedState = m.new ExactStateModel(iteratedState);
			
			boolean newNode = true; // true if a new node has to be created for s
			
			for(Node nFinalLayer : finalLayer) {
				ExactStateModel savedModel = (ExactStateModel) ((ExactBisimNode) nFinalLayer).label;
				
				Iterator<Action> bee = m.get_action_iterator();
				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				while(bee.hasNext()) {
					Action a  = bee.next();
					double diffR = Math.abs(modelIteratedState.R(a)-savedModel.R(a));
					if(diffR > EPSILON) { sameModel = false; break; }
					for(Node nodePrevLayer : prevLayer) {
						double tmp = modelIteratedState.T(a).intergrate(nodePrevLayer.activation) 
									  - savedModel.T(a).intergrate(nodePrevLayer.activation);
						if(tmp > EPSILON) { sameModel = false; break; }
					} // for
				} // while
				if(sameModel) { // if we found a clust with the same model
					newNode = false; // no need to creat a new node
					((FiniteSFeature) nFinalLayer.activation).add_state(iteratedState);
					break;
				}
			} // for
			
			// create new node
			if(newNode) { // if new node has to be created
				Node nNew = new ExactBisimNode(m, iteratedState, prevLayer, nextIndex++); // create node
				finalLayer.add(nNew); // add it to result of declustering
			}
		}
		
		// add new nodes to the final layer
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
	}
	
	
	@Override
	public String toString() {
		String toRet = "\n\nGRAPH!!\n\n";
		for(Node n : allNodes) {
			FiniteSFeature f = (FiniteSFeature) n.activation;
			toRet += "Node " + n.idx + ": " + f.all_members().size() + " members\n";
			toRet += "---Dependents: ";
			for (Node m : n.progenitors) {
				toRet += m.idx + " ";
			}
			toRet += "\n";
		}
		return toRet;
	}
	
	
	/**
	 * Class implements a node in a graph over a finite MDP and 
	 * an exact bisimulation computation graph
	 * @author gcoman
	 *
	 */
	public class ExactBisimNode extends Node {
		
		public ExactBisimNode(MDP m, State s, Set<Node> progenitors, int idx) {
			super(idx /* index */, 
				m.new ExactStateModel(s) /* label */, 
				new LInfComparator(m), /* distance comparator */
				progenitors, /* dependents*/ 
				null /* activation function - below*/);
			
			// activation function 
			FiniteSFeature activation = m.new FiniteSFeature() {

				@Override
				public double eval(State s) {
					return  contains(s) ? 1.0 : 0.0;
				}

				@Override
				public boolean isBinary() { return true;}
			}; 
			activation.add_state(s);
			this.activation = activation;
		} //constructor
			
		
	} // class
	
	
	
	
	
	
	
}

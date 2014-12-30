package CompGraphs;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import MDP.MDP;
import MDP.MDP.Action;
import MDP.MDP.FiniteSFeature;
import MDP.MDP.State;

public class SynchDeclustBisimGraph extends ExactBisimGraph{

	public SynchDeclustBisimGraph(MDP m) {
		super(m);
		finalLayer = new HashSet<Node>();
		Iterator<State> bee = m.get_state_iterator();
		while(bee.hasNext()) {
			State s = bee.next();
			boolean newNode = false; // true if a new node has to be created for s
			for(Node nClust : finalLayer) {
				ExactBisimModel m2e = (ExactBisimModel) ((ExactBisimNode) nClust).label;
				
				Iterator<Action> ada = m.get_action_iterator();
				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				while(ada.hasNext()) {
					Action a  = ada.next();
					double diffR = Math.abs(m.R(s,a)-m.R(m2e.s,a));
					if(diffR > EPSILON) { sameModel = false; break; }
				} // while
				if(sameModel) { // if we found a clust with the same model
					newNode = false; // no need to creat a new node
					((FiniteSFeature) nClust.activation).add_state(s);
					break;
				}
			}
			if(newNode) { // if new node has to be created
				Node nNew = new ExactBisimNode(m, s, null); // create node
				finalLayer.add(nNew); // add it to result of declustering
			}
		} // for s
		
		allNodes = new HashSet<Node>();
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
	}
	
	
	public void addNewLayer() {
		// save the last layer from old graph
		Set<Node> preFinalLayer = finalLayer;
		
		// we will iteratively build the last layer
		finalLayer = new HashSet<Node>();
		
		// decluster each node from the last layer of the previous graph
		for(Node preFinalNode : preFinalLayer) {
			// result of the declustering process will be saved here
			Set<Node> declust = new HashSet<Node>(); 
			
			// analyse all elements in the cluster
			FiniteSFeature ff = (FiniteSFeature) preFinalNode.activation;
			for(State s : ff.all_members()) {
				boolean newNode = false; // true if a new node has to be created for s
				for(Node nClust : declust) {
					ExactBisimModel m2e = (ExactBisimModel) ((ExactBisimNode) nClust).label;
					
					Iterator<Action> ada = m.get_action_iterator();
					boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
					while(ada.hasNext()) {
						Action a  = ada.next();
						for(Node n_int : preFinalLayer) {
							double tmp = m.P(s, a).intergrate(n_int.activation) 
										  - m.P(m2e.s, a).intergrate(n_int.activation);
							if(tmp > EPSILON) { sameModel = false; break; }
						} // for
					} // while
					if(sameModel) { // if we found a clust with the same model
						newNode = false; // no need to creat a new node
						((FiniteSFeature) nClust.activation).add_state(s);
						break;
					}
				}
				if(newNode) { // if new node has to be created
					Node nNew = new ExactBisimNode(m, s, preFinalLayer); // create node
					declust.add(nNew); // add it to result of declustering
				}
			} // for s
			for( Node n : declust) {
				finalLayer.add(n); // add all nodes to the finalLayer
			}
		} // for preFinalNode
		
		// add result to allNodes
		for(Node n : preFinalLayer) {
			allNodes.add(n);
		}
	}
	
	
}

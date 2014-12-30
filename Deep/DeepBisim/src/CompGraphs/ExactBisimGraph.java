package CompGraphs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;

import MDP.GridMDP;
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
		this.m = m;
		
		// index
		int nextIndex = 0;
		// we will iteratively build the last layer
		finalLayer = new TreeSet<Node>();
		// check all states for all possible labels
		Iterator<State> ada = m.get_state_iterator();
		while(ada.hasNext()) {
			State s = ada.next();

			boolean newNode = true; // true if a new node has to be created for s
			for(Node nFinalLayer : finalLayer) {
				ExactStateModel m2e = (ExactStateModel) ((ExactBisimNode) nFinalLayer).label;

				Iterator<Action> bee = m.get_action_iterator();
				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				while(bee.hasNext()) {
					Action a  = bee.next();
					double diffR = Math.abs(m.R(s,a)-m.R(m2e.s,a));
					if(diffR > EPSILON) { sameModel = false; break; }
				} // while
				if(sameModel) { // if we found a clust with the same model
					newNode = false; // no need to creat a new node
					((FiniteSFeature) nFinalLayer.activation).add_state(s);
					break;
				}
			} // for

			// create new node
			if(newNode) { // if new node has to be created
				Node nNew = new ExactBisimNode(m, s, new HashSet<Node>(), nextIndex++); // create node
				finalLayer.add(nNew); // add it to result of declustering
				System.out.print("");
			}
		}
		allNodes = new TreeSet<Node>();
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
		System.out.print("");
	}
	
	
	public void addNewLayer() {
		// next index 
		int nextIndex = allNodes.size();
		
		// save the last layer from old graph
		Set<Node> preFinalLayer = finalLayer;
		
		// we will iteratively build the last layer
		finalLayer = new TreeSet<Node>();
		
		// check all states for all possible labels
		Iterator<State> ada = m.get_state_iterator();
		while(ada.hasNext()) {
			State s = ada.next();
			Model ms = m.new ExactStateModel(s);
			
			boolean newNode = true; // true if a new node has to be created for s
			for(Node nFinalLayer : finalLayer) {
				ExactStateModel m2e = (ExactStateModel) ((ExactBisimNode) nFinalLayer).label;
				
				Iterator<Action> bee = m.get_action_iterator();
				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				while(bee.hasNext()) {
					Action a  = bee.next();
					double diffR = Math.abs(ms.R(a)-m2e.R(a));
					if(diffR > EPSILON) { sameModel = false; break; }
					for(Node n_int : preFinalLayer) {
						double tmp = ms.T(a).intergrate(n_int.activation) 
									  - m2e.T(a).intergrate(n_int.activation);
						if(tmp > EPSILON) { sameModel = false; break; }
					} // for
				} // while
				if(sameModel) { // if we found a clust with the same model
					newNode = false; // no need to creat a new node
					FiniteSFeature f = (FiniteSFeature) nFinalLayer.activation;
					f.add_state(s);
					break;
				}
			} // for
			
			// create new node
			if(newNode) { // if new node has to be created
				Node nNew = new ExactBisimNode(m, s, preFinalLayer, nextIndex++); // create node
				finalLayer.add(nNew); // add it to result of declustering
			}
		}
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
			for (Node m : n.dependents) {
				toRet += m.idx + " ";
			}
			toRet += "\n";
		}
		return toRet;
	}
	
	public class ExactBisimNode extends Node {
		
		public ExactBisimNode(MDP m, State s, Set<Node> dependents, int idx) {
			super(idx /* index */, 
				m.new ExactStateModel(s) /* label */, 
				new L1Comparator(m), /* distance comparator */
				dependents, /* dependents*/ 
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
	
	
	
	
	public static void main(String[] args) {
		GridMDP m = new GridMDP(10, GridMDP.GridType.DEFAULT);
		System.out.println(m);
		LinkedList<ExactBisimGraph> gset = new LinkedList<ExactBisimGraph>();
		gset.add(new ExactBisimGraph(m));
		gset.add(new SynchDeclustBisimGraph(m));
		for(ExactBisimGraph g : gset) {
			System.out.println("Size: " + g.allNodes.size());
			for (int ada = 0; ada < 5; ada++) {
				g.addNewLayer();
				System.out.println(g);
				System.out.println("Size: " + g.allNodes.size());
			}
			System.out.println();
			System.out.println();
		}
		
	}
	
	
	
	
	
}

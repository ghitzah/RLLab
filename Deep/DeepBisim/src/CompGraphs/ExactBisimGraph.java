package CompGraphs;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import CompGraphs.ModelComparator.Model;
import MDP.MDP;
import MDP.MDP.Action;
import MDP.MDP.State;
import MDP.MDP.FiniteSFeature;
import MDP.PuddleMDP;

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
		
		// create one initial node that evaluates to 1 all the time
		Node n0 = new ExactBisimNode(m, m.get_state_iterator().next(), null);
		
		allNodes = new HashSet<Graph.Node>();
		finalLayer = new HashSet<Node>();
		allNodes.add(n0);
		finalLayer.add(n0);
	}
	
	
	public void addNewLayer() {
		// save the last layer from old graph
		Set<Node> preFinalLayer = finalLayer;
		
		// we will iteratively build the last layer
		finalLayer = new HashSet<Node>();
		
		// check all states for all possible labels
		Iterator<State> ada = m.get_state_iterator();
		while(ada.hasNext()) {
			State s = ada.next();
			
			boolean newNode = true; // true if a new node has to be created for s
			for(Node nFinalLayer : finalLayer) {
				ExactBisimModel m2e = (ExactBisimModel) ((ExactBisimNode) nFinalLayer).label;
				
				Iterator<Action> bee = m.get_action_iterator();
				boolean sameModel = true; // true if s has the same model as all states in current cluster nClust
				while(bee.hasNext()) {
					Action a  = bee.next();
					double diffR = Math.abs(m.R(s,a)-m.R(m2e.s,a));
					if(diffR > EPSILON) { sameModel = false; break; }
					for(Node n_int : preFinalLayer) {
						double tmp = m.P(s, a).intergrate(n_int.activation) 
									  - m.P(m2e.s, a).intergrate(n_int.activation);
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
				Node nNew = new ExactBisimNode(m, s, preFinalLayer); // create node
				finalLayer.add(nNew); // add it to result of declustering
			}
		}
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
	}
	
	
	public class ExactBisimNode extends Node {
		
		
		public ExactBisimNode(MDP m, State s, Set<Node> dependents) {
			//label
			
			this.label = new ExactBisimModel(s);			
			
			//dependents
			this.dependents = dependents;
			
			
			// model distance function 
			final MDP mtemp = m;
			final Set<Node> dependantsTmp = dependents;
			this.modelDist = new ModelComparator() {
				
				@Override
				public Model getModel(State s) {
					return new ExactBisimModel(s);
				}
				
				@Override
				public double dist(Model m1, Model m2) {
					ExactBisimModel m1e = (ExactBisimModel) m1;
					ExactBisimModel m2e = (ExactBisimModel) m2;
					double d = 0;
					Iterator<Action> ada = mtemp.get_action_iterator();
					while(ada.hasNext()) {
						Action a  = ada.next();
						double diffR = Math.abs(mtemp.R(m1e.s,a)-mtemp.R(m2e.s,a));
						
						double diffT = 0.0;
						for(Node n : dependantsTmp) {
							double tmp = mtemp.P(m1e.s, a).intergrate(n.activation) 
										  - mtemp.P(m2e.s, a).intergrate(n.activation);
							diffT = Math.max(diffT, Math.abs(tmp));
						} // for
						d = Math.max(d, diffR + diffT);
					}
					return d;
				}
			};
			
			
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
		
		
		@Override
		public String toString() {
			String toRet = "";
			//TODO
			
			return toRet;
		}
		
		
		
	} // class
	
	
	/**
	 * Simple model for the exact bisimulation computation algorithm. It contains
	 * the start state of the transition model that this object represents
	 * @author gcoman
	 *
	 */
	public class ExactBisimModel implements Model {
		public final State s;
		
		public ExactBisimModel(State s) {
			this.s = s;
		}
	}
	
	
	
	
	public static void main(String[] args) {
		PuddleMDP m = new PuddleMDP(10);
		System.out.println(m);
		ExactBisimGraph g = new ExactBisimGraph(m);
		
		for (int ada = 0; ada < 10; ada++) {
			g.addNewLayer();
		}
		
	}
	
	
	
	
	
}

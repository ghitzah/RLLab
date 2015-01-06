package CompGraphs;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
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
import MDP.MDP.Action;
import MDP.MDP.Feature;
import MDP.MDP.Measure;
import MDP.MDP.Model;
import MDP.MDP.State;

/**
 * This class implements the new algorithm that generalized the 
 * Exact Bisimulation Algorithm
 * @author gcoman
 *
 */
public class NewNewAlgo extends Graph{


	private double ALPHA = 100.0;

	private static final int DEFAULT_NUM_ITERS  = 300;
	private final int numIters;

	public NewNewAlgo(MDP m, int numIters) {
		super(m);
		this.numIters = numIters;
		try {
			addNewLayer();
		} catch (AlgorithmicException e) {
			e.printStackTrace();
		}	
	}

	public NewNewAlgo(MDP m) {
		this(m, DEFAULT_NUM_ITERS);
	}





	public void addNewLayer() throws AlgorithmicException {
		// this will maintain the index of new nodes
		int nextIndex = (allNodes == null) ? 0 : allNodes.size();

		// save the last layer from old graph - needed to test transition map
		Set<Node> prevLayer = finalLayer;

		//we will rebuild the final layer from 0
		finalLayer = new TreeSet<Node>();

		// get a fixed number of states to determine which nodes to create
		Iterator<State> ada = m.get_state_iterator();
		int tmpNumIters = numIters;
		
		// heuristic used to select a subset of nodes to analyze
		Heuristic myHeuristic = new Heuristic(100, Heuristic.HeuristicType.RANDOM_PAIR);

		// select a subset of nodes to analyze
		//Set<Node> NodesSelected = myHeuristic.select(prevLayer);
		Set<Node> NodesSelected = prevLayer;
		
		// mapping each node to a list of nodes
		HashMap<Node, LinkedList<Node>> nodeToList = new HashMap();
		
		// put selected nodes into the map
		for(Node pNode : NodesSelected){
			nodeToList.put(pNode, new LinkedList());
		}
		
		while(ada.hasNext() && tmpNumIters-- > 0) {
			
			Model modelIteratedState = m.new ExactStateModel(ada.next());
			
			for(Node pNode : NodesSelected) {
				// saved model that we compare to
				try {
					// if it's in this cluster, then we decluster 
					if(pNode.dist(modelIteratedState) < EPSILON) { 
						
						boolean sameModel = false;
						
						if(!nodeToList.get(pNode).isEmpty()) {
							ListIterator<Node> listIterator = nodeToList.get(pNode).listIterator();
					        while (listIterator.hasNext()) {
					            if(listIterator.next().dist(modelIteratedState) < EPSILON){
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
									//new GroundedModel(modelIteratedState, 100),
									modelIteratedState,
									//ground the stateModel (so that integration can be solved faster)
									new LInfComparator(m),
									// TODO maybe use this - new LInfComparator(m), 
									prevLayer, 
									null /* done below*/);
							nNew.activation = new MemoizedFeature() {
								@Override
								double eval_non_existent(State s) {
									try {
										return Math.exp(- ALPHA * nNew.dist(m.new ExactStateModel(s)));
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
				allNodes.add(n.getKey());
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
				allNodes.add(n);
			}
		}
	}
	
	
	abstract class MemoizedFeature extends Feature {

		HashMap<State,Double> activation_memoized = new HashMap<State, Double>();
		//int max_capacity = 1000; //TODO : do something about this

		public MemoizedFeature() {
			m.super();
		}
		
		
		@Override
		public double eval(State s) {
			Double d = activation_memoized.get(s);
			if(d != null) {
				return d;
			}
			d = eval_non_existent(s);
			activation_memoized.put(s, d);
			return d;
		}
		
		abstract double eval_non_existent(State s);
		
	}

	public class GroundedModel extends Model {


		Map<Action, Double> R;
		Map<Action, Measure> T;

		public GroundedModel(Model model, int num_samples) {
			m.super();

			R = new HashMap<Action, Double>();
			T = new HashMap<Action, Measure>();

			Iterator<Action> ada = m.get_action_iterator();
			while(ada.hasNext()) {
				//reward
				Action a = ada.next();
				R.put(a, model.R(a));

				//transition
				Measure mtmp = model.T(a);
				HashMap<State, Integer> counts = new HashMap<State, Integer>();
				for (int i = 0; i < num_samples; i++) {
					State s = mtmp.sample();
					if(counts.containsKey(s)) { counts.put(s, counts.get(s)+1); }
					else counts.put(s, 1);
				}
				T.put(a, m.new Measure(counts, num_samples));
			}
		}

		@Override
		public double R(Action a) {
			return R.get(a);
		}

		@Override
		public Measure T(Action a) {
			return T.get(a);
		}

	}


}
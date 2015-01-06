package CompGraphs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class NewAlgoSimple extends Graph{

	
	private double ALPHA = 100.0;
	
	private static final int DEFAULT_NUM_ITERS  = 3000;
	private final int numIters;

	public NewAlgoSimple(MDP m, int numIters) {
		super(m);
		this.numIters = numIters;
		try {
			addNewLayer();
		} catch (AlgorithmicException e) {
			e.printStackTrace();
		}	
	}

	public NewAlgoSimple(MDP m) {
		this(m, DEFAULT_NUM_ITERS);
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
				T.put(a, m.new Measure(counts, num_samples) {
					@Override
					public State sample() {
						// TODO Auto-generated method stub
						return null;
					}
				});
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
		
		while(ada.hasNext() && tmpNumIters-- > 0) {
			//System.out.print(".");
			final Model modelIteratedState = m.new ExactStateModel(ada.next());

			//this will be true only if one of the newly created nodes 
			//has the same model as the crt model we inspect
			boolean sameModel = false; 
			for(Node nFinalLayer : finalLayer) {
				// saved model that we compare to
				try {
					if(nFinalLayer.dist(modelIteratedState) < EPSILON) { 
						sameModel = true; 
						break; 
					}
				} catch (IncorrectModelExpection e) {
					e.printStackTrace();
				}
			} // for nFinalLayer
			
			if(!sameModel) { //create a new node
				
				
				final Node nNew = new MemoizedNode(nextIndex++, 
						//TODO: maybe change this: new GroundedModel(modelIteratedState, 100),
						modelIteratedState,
						//ground the stateModel (so that integration can be solved faster)
						new LInfComparator(m),
						// TODO maybe use this - new LInfComparator(m), 
						prevLayer, 
						null /* done below*/);
				nNew.activation = m.new Feature() {
					@Override
					public double eval(State s) {
						try {
							return Math.exp(- ALPHA * nNew.dist(m.new ExactStateModel(s)));
						} catch (IncorrectModelExpection e) {
							e.printStackTrace();
							return 0;
						}
					}
				};
				finalLayer.add(nNew); // add it to result of declustering
			}
		}

		// add new nodes to the final layer
		for(Node n : finalLayer) {
			allNodes.add(n);
		}
	}
	
	
	
	private class MemoizedNode extends Node{
		
		HashMap<State,Double> activation_memoized = new HashMap<State, Double>();
		//int max_capacity = 1000; //TODO : do something about this
		
		
		public MemoizedNode(int idx, Model label, ModelComparator modelDist,
				Set<Node> progenitors, Feature activation) {
			super(idx, label, modelDist, progenitors, activation);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double activation(State s) {
			Double d = activation_memoized.get(s);
			if(d != null) return d;
			d = activation.eval(s);
			activation_memoized.put(s, d);
			return d;
		}
	}

}
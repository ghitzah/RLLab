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
public class SimpleFeatureGenerator extends Graph{


	final double ALPHA = 100.0;

	static final int DEFAULT_NUM_ITERS  = 300;
	final int numIters;

	public SimpleFeatureGenerator(MDP m, int numIters) {
		super(m);
		this.numIters = numIters;	
	}

	public SimpleFeatureGenerator(MDP m) {
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

		while(ada.hasNext() && tmpNumIters-- > 0) {
			
			final Model modelIteratedState = new GroundedModel(ada.next(), 100);
			
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
				final Node nNew = new Node(nextIndex++, 
						new GroundedModel(modelIteratedState, 100),
						new LInfComparator(m),
						prevLayer, 
						null /* done below*/);
				nNew.activation = new MemoizedFeature() {
					@Override
					double eval_non_existent(State s) {
						try {
							return Math.exp(- ALPHA * nNew.dist(new GroundedModel(s,100)));
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
		Model model;
		int numSamples;

		public GroundedModel(Model model, int num_samples) {
			m.super();
			this.model = model;
			this.numSamples = num_samples;
			R = new HashMap<Action, Double>();
			T = new HashMap<Action, Measure>();
		}
		
		public GroundedModel(State s, int num_samples) {
			this(m.new ExactStateModel(s), num_samples);
		}

		@Override
		public double R(Action a) {
			Double d = R.get(a);
			if(d == null) {
				d = model.R(a);
				R.put(a, d);
			}
			return d;
		}

		@Override
		public Measure T(Action a) {
			Measure mes = T.get(a);
			if(mes == null) {	
				mes = model.T(a);
				if(!mes.isGrounded()) {  
					HashMap<State, Integer> counts = new HashMap<State, Integer>();
					for (int i = 0; i < numSamples; i++) {
						State s = mes.sample();
						assert(s != null);
						if(counts.containsKey(s)) { counts.put(s, counts.get(s)+1); }
						else counts.put(s, 1);
					}
					mes = m.new Measure(counts, numSamples); 
				}
				T.put(a, mes);
			}
			return mes;
		}


	}


}
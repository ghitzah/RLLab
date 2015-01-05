package CompGraphs;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import MDP.MDP;
import MDP.MDP.Cluster;

/**
 * Class implementing a computation graph for a desclustering algorithm
 * that generates a declustering framework
 * @author gcoman
 *
 */
public class ASynchDeclustGraph extends DeclustGraph{

	public enum HeuristicType { RANDOM_PAIR, TOP_PAIR };


	public ASynchDeclustGraph(MDP m) {
		this(m,2);
	}


	public ASynchDeclustGraph(MDP m, HeuristicType type) {
		this(m,2,type);
	}

	public ASynchDeclustGraph(MDP m, int selectionSize) {
		this(m, selectionSize, HeuristicType.TOP_PAIR);
	}

	public ASynchDeclustGraph(MDP m, int selectionSize, HeuristicType type) {
		super(m);
		final int selectionSizeTmp = selectionSize;

		switch (type) {
		case RANDOM_PAIR:
			heuristic = new DeclustHeuristic() {
				final int SEED = 0;
				Random r1 = new Random(SEED);

				@Override
				public Set<Node> select(Set<Node> allNodes) {
					int size = allNodes.size();
					if(size <= selectionSizeTmp) {
						return allNodes;
					}
					Set<Integer> selections = new HashSet<Integer>();
					for (int ada = 0; ada < selectionSizeTmp; ada++) {
						int newInt = r1.nextInt(size);
						while(selections.contains(newInt)) 
							newInt = r1.nextInt(size);
						selections.add(newInt);
					}

					Set<Node> toRet = new HashSet<Graph.Node>();
					int ada = 0;
					for(Node n : allNodes) {
						if(selections.contains(ada)) {
							toRet.add(n);
						}
						ada++;
					}
					return toRet;
				}
			};
			break;
		default:
			heuristic = new DeclustHeuristic() {

				@Override
				public Set<Node> select(Set<Node> allNodes) {
					int size = allNodes.size();
					if(size <= selectionSizeTmp) { return allNodes; }

					TreeSet<Node> selections = new TreeSet<Node>(new Comparator<Node>() {
						@Override
						public int compare(Node o1, Node o2) {
							return ((Cluster) o2.activation).numberMembers() 
									- ((Cluster) o1.activation).numberMembers();
						}
					});
					for(Node n : allNodes) {
						selections.add(n);
					}

					Set<Node> toRet = new HashSet<Node>();
					int ada = selectionSizeTmp;
					for(Node n : selections) {
						toRet.add(n);
						if(--ada == 0) break;
					}
					return toRet;
				}
			};
			break;
		}//switch
	}


}

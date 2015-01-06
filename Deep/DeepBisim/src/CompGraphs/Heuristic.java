package CompGraphs;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


import CompGraphs.Graph.Node;
import MDP.MDP.Cluster;

public class Heuristic {

	public enum HeuristicType { RANDOM_PAIR, TOP_PAIR };
	
	private HeuristicType type;
	private int selectionSizeTmp;
	private Random r1;
	
	public Heuristic(int selectionSize, HeuristicType tp) {
		//super(m);
		selectionSizeTmp = selectionSize;
		type = tp;
		final int SEED = 0;
		r1 = new Random(SEED);
	}

	public Set<Node> select(Set<Node> allNodes) {
		int size = allNodes.size();
		
		if(type == HeuristicType.RANDOM_PAIR){
			if(size <= selectionSizeTmp) {
				return allNodes;
			}
			Set<Integer> selections = new HashSet<Integer>();
			for (int ada = 0; ada < selectionSizeTmp; ada++) {
				int newInt = r1.nextInt(size);
				//TODO select a random subset of integers (library exists?)
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
		else if(type == HeuristicType.TOP_PAIR){
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
		// if neither heuristic type is selected
		return null;
	}
}

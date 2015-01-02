package Tests;

import java.util.LinkedList;

import CompGraphs.ASynchDeclustGraph;
import CompGraphs.DeclustGraph;
import CompGraphs.DeclustGraph.AlgorithmicException;
import CompGraphs.ExactBisimGraph;
import CompGraphs.Graph;
import MDP.GridMDP;

public class InitialTest {
	public static void main(String[] args) {
		
		int sizeGrid = 30;
		int iterations = 10;
		
		GridMDP m = new GridMDP(sizeGrid, GridMDP.GridType.DEFAULT);
		//System.out.println(m);
		LinkedList<ExactBisimGraph> gset = new LinkedList<ExactBisimGraph>();
		gset.add(new ExactBisimGraph(m));
		gset.add(new DeclustGraph(m));
		gset.add(new ASynchDeclustGraph(m));
		gset.add(new ASynchDeclustGraph(m, ASynchDeclustGraph.HeuristicType.RANDOM_PAIR));
		for(ExactBisimGraph g : gset) {
			printInfo(g);
			for (int ada = 0; ada < iterations; ada++) {
				try {
					g.addNewLayer();
				} catch (AlgorithmicException e) {
					e.printStackTrace();
				}
				printInfo(g);
			}
			System.out.println();
			System.out.println();
		}
		
	}
	
	static void printInfo(Graph g) {
		System.out.println("Graph size: " + g.graphSize());
		System.out.println("Representation size: " + g.representationSize());
		
	}
}

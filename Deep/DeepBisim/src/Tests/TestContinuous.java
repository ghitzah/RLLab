package Tests;

import CompGraphs.DeclustGraph.AlgorithmicException;
import CompGraphs.NewAlgoSimple;
import MDP.GridMDP;

public class TestContinuous {

	public static void main(String[] args) {
		//Continuous2d m = new Continuous2d();
		GridMDP m = new GridMDP(10);
		NewAlgoSimple g = new NewAlgoSimple(m, m.number_states());
		System.out.println(g.graphSize());
		for (int i = 0; i < 13; i++) {
			try {
				g.addNewLayer();
			} catch (AlgorithmicException e) {
				e.printStackTrace();
			}
			System.out.println(g.graphSize());
		}
		
	}
}

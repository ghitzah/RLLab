package Tests;

import CompGraphs.DeclustGraph.AlgorithmicException;
import CompGraphs.NewAlgoSimple;
import CompGraphs.NewNewAlgo;
import MDP.Continuous2d;
import MDP.PuddleMDP;

public class TestContinuous {

	public static void main(String[] args) {
//		Continuous2d m = new Continuous2d();
		PuddleMDP m = new PuddleMDP(10);
//		NewAlgoSimple s = new NewAlgoSimple(m, 100/*m.number_states()*/);
		NewNewAlgo s = new NewNewAlgo(m, 100);
		//InitialTest.printInfo(g);
		InitialTest.printInfo(s);
		for (int i = 0; i < 13; i++) {
			try {
				//g.addNewLayer();
				s.addNewLayer();
			} catch (AlgorithmicException e) {
				e.printStackTrace();
			}
			//System.out.println(g.graphSize());
			//InitialTest.printInfo(g);
			InitialTest.printInfo(s);
		}
		
	}
}

package ComparativeAnalysis;


import java.util.List;
import java.util.LinkedList;

import ComparativeAnalysis.Metric.OOBException;

public class TestAlgos {
	
	
	public static void main(String[] args) {
		MDP m = new GridMDP(30);
		System.out.println(m);
		List<Metric> lm = new LinkedList<Metric>();
		try {
			algos.vanilla_computation(m, 3, lm);//TODO change num iters
			System.out.println(lm.size());
		}catch (OOBException e) {
			System.out.println("Bum");
		}
	}
}

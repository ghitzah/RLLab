package Runnables;


import ComparativeAnalysis.*;
import ComparativeAnalysis.GridMDP.GridType;
import ComparativeAnalysis.Metric.OOBException;

import java.util.List;
import java.util.LinkedList;

public class TestAlgos {
	
	
	public static void main(String[] args) {
		ComparativeAnalysis.MDP m = new ComparativeAnalysis.GridMDP(10, GridMDP.GridType.DEFAULT);
		List<Metric> lm = new LinkedList<Metric>();
		try {
			algos.vanilla_computation(m, 3, lm);
			System.out.println(lm.size());
		}catch (OOBException e) {
			System.out.println("Bum");
		}
	}
}

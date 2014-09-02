package Runnables;

import SpecificMDPs.GridMDP;
import MDPHierarchy.*;
import ComparativeAnalysis.*;
import ComparativeAnalysis.Metric.OOBException;

import java.util.List;
import java.util.LinkedList;

public class TestAlgos {
	
	
	public static void main(String[] args) {
		MDP m = new GridMDP(10);
		List<Metric> lm = new LinkedList<Metric>();
		try {
			algos.vanilla_computation(m, 3, lm);
			System.out.println(lm.size());
		}catch (OOBException e) {
			System.out.println("Bum");
		}
	}
}

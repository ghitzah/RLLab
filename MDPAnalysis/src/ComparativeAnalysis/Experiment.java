package ComparativeAnalysis;

import java.util.List;
import java.util.LinkedList;

public class Experiment {

	public static void main(String[] args) {
		for (int i = Integer.parseInt(args[0]); 
				i < Integer.parseInt(args[1]); i++){ 
			MDP m = new PuddleMDP(i*10);
			try {
				long startTime, endTime, duration;
				//System.out.println("Vanilla " + m.number_states() + " states start...");
				startTime = System.currentTimeMillis();
				List<Metric> lm = new LinkedList<Metric>();
				//algos.vanilla_computation(m,10,lm);
				endTime = System.currentTimeMillis();
				duration = (endTime - startTime);
				//System.out.println("Vanilla " + i*10 + " states: " + duration/1000000000);
				//System.out.println("Declust " + i*10 + " states start...");
				startTime = System.currentTimeMillis();
				lm = new LinkedList<Metric>();
				algos.declust_computation(m, Integer.parseInt(args[2]), lm);
				endTime = System.currentTimeMillis();
				duration = (endTime - startTime); 
				System.out.println("Declust " + m.number_states() + " states: " + duration/1000 + " seconds");
				
			} catch (Exception e) {
				System.err.println("Something went wrong");
			}
		}
		
		
		
	}
	
	
	public static void main2(String[] args) {
		for (int i = 1; i < 20; i++){ 
			MDP m = new GridMDP(i*10, GridMDP.GridType.EMPTY);
			System.out.println(m.number_states() + " " + i*i*100);
		}
	}
	
}

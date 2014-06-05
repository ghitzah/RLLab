package Runnables;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import MDPHierarchy.AggMDP;
import MDPHierarchy.MDP;
import SpecificMDPs.GridMDP;
import SpecificMDPs.PuddleMDP;

public class Tests {
	/**
	 * Simple main function to test the end product of a simple PuddleMDP
	 * @param args: ignored
	 */
	public static void main(String[] args) {
		int size = Integer.parseInt(args[0]);
		int levels = Integer.parseInt(args[1]);
		String path = args[2];
		PuddleMDP m = new PuddleMDP(size);
		//String path = "/Users/gcoman/ipython";
		m.saveMapRepresentation(path);
		aggSaveData(m, levels, path);
	}
	
	
	private static void miniTest(int size) {
		GridMDP m = new PuddleMDP(size);
		System.out.println(m);
	}
	
	private static void aggregateTest(MDP m, int levels) {		
		AggMDP mR = new AggMDP(m);
		//AggMDP mP = new AggMDP(mR);
		AggMDP magg = mR;
		//while(--levels > 0) {
			//mP = new AggMDP(mP);
		for (int j = 1; j < levels; j++) {
			magg = new AggMDP(magg); 
		}
		System.out.println(mR);
	}
	
	
	private static void aggSaveData(MDP m, int levels, String path) {
		PrintWriter out;
		AggMDP magg = null; //It will get initialized at i=0;
		for (int i = 0; i < levels; i++) {
			try{
				System.out.println("Level " + i);
				if(i==0) {
					magg = new AggMDP(m);
				}else {
					magg = new AggMDP(magg);
				}				
				String ss = (i == 0) ? "R" : i + "";
				out = new PrintWriter(path + "/coupling" + ss + ".csv");
				m.printClMembership(magg, out);
				out.close();
				out = new PrintWriter(path + "/dist" + ss + ".csv");
				magg.printD(out);
				out.close();
			}catch(FileNotFoundException e) {
				e.printStackTrace(); //TODO: maybe change this		
			}
		}
	}
	
}

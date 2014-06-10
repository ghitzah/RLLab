package Runnables;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

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
		int size = 30; //TODO default
		String path = "/Users/gcoman/ipython"; //TODO default
		int levels = 3; //TODO default 
		boolean puddle = true; //TODO other MDPS with enums
		boolean asynch = false; //TODO default
		boolean test = false; //TODO default
		for (int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				String param = args[i].substring(1);
				if( i < args.length - 1 ) {
					if(param.equals("size")) {
						size = Integer.parseInt(args[++i]);
					}else if(param.equals("path")) {
						path = args[++i];
					}else if(param.equals("mdp")) {
						if(args[++i] == "grid") {
							puddle = false;
						}
					}else if(param.equals("levels")) {						
						levels = Integer.parseInt(args[++i]);
					}
				}
				if(param.equals("asynch")) {
					asynch = true;
				}else if (param.equals("test")) {
					test = true;
				}
			}
		}
		
		GridMDP m = (puddle) ? new PuddleMDP(size) : new GridMDP(size);
		m.saveMapRepresentation(path);
		
		if(test) aggregateTest(m, levels);
		else if (!asynch) aggSaveData(m, levels, path);
		else aggSaveDataAsynch(m, levels, path);
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
	
	
	private static void aggSaveDataAsynch(MDP m, int levels, String path) {
		PrintWriter out;
		AggMDP magg = null; //It will get initialized at i=0;
		for (int i = 0; i < levels; i++) {
			try{
				System.out.println("Level " + i);
				if(i==0) {
					magg = new AggMDP(m);
				}else {
					Random rng = new Random();					
					int z = rng.nextInt(magg.number_states());				
					HashSet<AggMDP.Cluster> to_declust = new HashSet<AggMDP.Cluster>(); 
					to_declust.add((AggMDP.Cluster) magg.getStates().toArray()[z]);
					magg = new AggMDP(magg, to_declust);
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

package SpecificMDPs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import MDPHierarchy.MDP;

/**
 * Class implementing a Puddle Environment (PUDE)
 * @author gcoman
 *
 */
public class PuddleMDP extends GridMDP{

	/**
	 * Class implementing a state in the Puddle Environment
	 * @author gcoman
	 *
	 */
	public class PuddleState extends GridState {
		
		
		/**
		 * Main constructor
		 * @param mdp : the MDP associated with this state
		 * @param x : x-coordinate
		 * @param y : y-coordinate
		 * @param index : the index of the state as an integer from 0 to SIZE-1
		 */
		public PuddleState(MDP mdp, int x, int y) {
			super(mdp, x, y, 0);
			index = y_coord * W + x_coord; 
			
		}
		
		@Override
		protected boolean isGoal() {
			//TODO: maybe allow for a change in goal state
			return (x_coord == W-1 && y_coord == 0) ;
		}
	}
	
	
	
	/*** Default final parameters ****/	
	final private int DEFAULT_TOTAL_TRANSITION_WEIGHT = 100; 
	final private double GOAL_REWARD = 0.5;
	final private int WIDTH_LAKE = 1;
	
	
	
	
	
	/** rm : the reward model. For each action, we store only the states 
	 * that have non-zero reward 
	 */ 
	protected ArrayList< Map<Integer, Double> > rm;
	
	
	/**
	 * Creates a Puddle MDP with the default locations for the lake position and 
	 * directions. The environment will be a grid of 'size by size', parameter provided  
	 * @param size : the with and height of the squared environment
	 */
	public PuddleMDP(int size) {
		/*
		 * get default water position and direction. 
		 * - The position of the water is represented using lines: 4 
		 *   integers corresponding to the start and end points of the line
		 * - Each direction is represented by two integers: the x-coord and y-coord
		 *   offsets
		 */
		int[][] dirs = getDefaultDirections();
		A = dirs.length;
		
		total_sum_pm = DEFAULT_TOTAL_TRANSITION_WEIGHT;
		W = H = size;
		S = W*W;
		int[][] wpos = getDefaultWaterPosition();
		
		//initialize object fields
		setBlankMaps(S, A);
		
		
		allStates = new PuddleState[S]; 
		
		for (int i_a = 0; i_a < dirs.length; i_a++) {			
			for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
				for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
					State s = new PuddleState(this, x_ax, y_ax);
					int i_s = s.idx();
					allStates[i_s] =  s;
				}
			}
		}
		

		//for each action
		for (int i_a = 0; i_a < dirs.length; i_a++) {
			for(State s : allStates) {
				PuddleState sPud = (PuddleState) s;
				int y_ax = sPud.y_coord;
				int x_ax = sPud.x_coord;
				int i_s = s.idx();
				// get coords of the next state, as indicated by action a
				PuddleState next_s = new PuddleState(this, x_ax + dirs[i_a][0], 
						y_ax + dirs[i_a][1]);

				/* set up REWARD */	
				if(next_s.isGoal()) { // reach goal state						
					rm.get(i_a).put(i_s, GOAL_REWARD);
				}else {
					int d = WIDTH_LAKE; 
					//check if distance d is smaller than WIDTH_LAKE
					int xx = next_s.x_coord;
					int yy = next_s.y_coord;
					for (int[] line : wpos) {
						if(line[0] == line[2]) {
							d = Math.min(d, Math.abs(xx - line[0]) 
									+ Math.max(0, line[1] - yy) 
									+ Math.max(0, yy - line[3]));
						}else {
							d = Math.min(d, Math.abs(yy - line[1]) 
									+ Math.max(0, line[0] - xx) 
									+ Math.max(0, xx - line[2]));
						}
					}//for wpos
					if(d != WIDTH_LAKE) {
						//TODO: magic formula - maybe change it!
						rm.get(i_a).put(i_s, - (WIDTH_LAKE - d) * 0.1 );
					}
				} //end if then
				/* REWARD done */

				/* set up TRANSITION */
				//.85% desired transition, .05 stay put, .1 left or right
				Map<State, Double> tm_tmp = tm.get(i_a).get(i_s);
				//indeces of the 4 candidates for transition
				int[] idxs = new int[4];
				idxs[0] = i_s; //curent state
				idxs[1] = next_s.idx(); // next state
				for(int i=0; i<2;i++) { // left and right of crt state
					int a_n = (i_a + (2*i-1) + dirs.length) % dirs.length;	                	
					idxs[i+2] = new PuddleState(this, x_ax + dirs[a_n][0],y_ax + dirs[a_n][1]).idx();		            
				}
				int[] vals = {5,0,0,0}; 
				if(idxs[1] == i_s) {
					vals[0] += 85;
				}else vals[1] = 85;
				for (int i = 2; i < idxs.length; i++) {
					if(idxs[i] == i_s) {
						vals[0] += 5;
					}else {
						vals[i] = 5;
					}
				}
				//only add non-negative transitions
				for (int i = 0; i < idxs.length; i++) {
					if(vals[i] != 0) {
						tm_tmp.put(allStates[idxs[i]], (double) vals[i]);
					}
				}
				/**TRANSITION DONE*/
			}//for s			
		} //for i_a
	} // method

	

	
	@Override
	public double R(State s, int a) throws InvalidMDPException{
		if(!s.sameMdp(this)) throw new InvalidMDPException();
		Double d = rm.get(a).get(s.idx());
		if (d == null) {
			return 0;
		}else {
			return d;
		}	
	}
	
	@Override
	public double P(State s, int a, State sn /*s_next*/) throws InvalidMDPException{
		if(!s.sameMdp(this) || sn.sameMdp(this)) throw new InvalidMDPException();
		
		Double d = tm.get(a).get(s.idx()).get(sn.idx());
		if (d == null) {
			return 0;
		}else {
			return ((double) d) / total_sum_pm;
		}
	}
	
	
	@Override
	public String toString() {
		String toRet = "\nREWARD\n\n\n";
		for (int i = 0; i < rm.size(); i++) {
			toRet += "Action " + i + " " + "\n";
			for (Integer j : rm.get(i).keySet()) {
				toRet += "(" + j%W  + "," + j/W + ") :" + rm.get(i).get(j) + " ";
			}
			toRet += "\n";
		}
		toRet += "\n\nTRANSITION\n\n\n";
		for (int i = 0; i < tm.size(); i++) {
			toRet += "Action " + i + "\n";
			for (int k = 0; k < tm.get(i).size(); k++) {
				toRet += "S " + "(" + k%W  + "," + k/W + ") :" + " ---> ";
				for (State j : tm.get(i).get(k).keySet()) {					
					toRet += "(" + j.idx()%W  + "," + j.idx()/W + ") :" + ":"
							+ tm.get(i).get(k).get(j) / total_sum_pm 
							+ " ";
				}
				toRet += "\n";
			}
			toRet += "\n";
		}
		return toRet;
	}
	
	
	/****** HELPER METHODS **********/
	
	/**
	 * Returns the default lake water positions
	 * @return : The position of the water is represented using lines: 4 
	 * 		     integers corresponding to the start and end points of the line
	 */
	private int[][] getDefaultWaterPosition() {
		int[][] toRet = new int[1][];
		int i=0;
		toRet[i++] = new int[] {3, 3, 3, 7};
		//toRet[i++] = new int[] {3, 3, 7, 6};
		for (int j = 0; j < toRet.length; j++) {
			for (int j2 = 0; j2 < toRet[j].length; j2++) {
				toRet[j][j2] = resizeForDefaultWater(toRet[j][j2]);
			}
		}
		return toRet;
	}
	
	

	private int resizeForDefaultWater(int x) {
		//Resize position form a 10 by 10 to a size by size
		return (W * x) / 10;
	}
	
	
	
	/**
	 * Helper function that initializes blank maps for each state and action
	 * @param num_s : number of states
	 * @param num_a : number of actions
	 */
	protected void setBlankMaps(int num_s, int num_a) {
		rm = new ArrayList<Map <Integer, Double> >(num_a);
		for (int i = 0; i < num_a; i++) {
			rm.add( new HashMap<Integer,Double>());
		}
		tm = new ArrayList<ArrayList<Map<State,Double>>>(num_a);
		for (int i = 0; i < num_a; i++) {
			tm.add(new ArrayList<Map<State,Double>>(num_s));
			for (int j = 0; j < num_s; j++) {
				tm.get(i).add(new HashMap<State,Double>());
			}
		}
	}

	
	
	
	/**
	 * Simple main function to test the end product of a simple PuddleMDP
	 * @param args: ignored
	 */

	public static void main(String[] args) {
		PuddleMDP m = new PuddleMDP(10);
		System.out.println(m);
	}
	
}



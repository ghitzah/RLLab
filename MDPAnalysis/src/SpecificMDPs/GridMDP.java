package SpecificMDPs;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;






import MDPHierarchy.AggMDP;
import MDPHierarchy.MDP;

/**
 * Class implementing a Grid World Environment (GWE)
 * @author gcoman
 *
 */
public class GridMDP extends MDP{
	
	/**
	 * Class implementing a state in the Grid World Environment
	 * @author gcoman
	 *
	 */
	public class GridState extends State {
		/**
		 * y-coordinate on the grid
		 */
		protected int y_coord;
		
		/**
		 * x-coordinate on the grid
		 */
		protected int x_coord;
		
		/**
		 * Main constructor
		 * @param mdp : the MDP associated with this state
		 * @param x : x-coordinate
		 * @param y : y-coordinate
		 * @param index : the index of the state as an integer from 0 to SIZE-1
		 */
		public GridState(MDP mdp, int x, int y, int index) {
			super(mdp, index);
			x_coord = bound(x);
			y_coord = bound(y);				
		}

		/**
		 * Make sure index is not out of bounds
		 * @param value : value to be bounded
		 * @return if s is negative, returns 0, if s is larger than Width-1, it returns 
		 *         width-1. NOTE, only works for same width and height
		 */
		private int bound(int value) {
			if(value < 0) return 0;
			if(value >= W) return W-1;
			return value;
		}
		
		/**
		 * Returns true only if the state is a goal state (higher reward)
		 * @return boolean - goal or not
		 */
		protected boolean isGoal() {
			//TODO: maybe allow for a change in goal state
			return this.idx() == GOAL_STATE;
		}
		
		@Override
		public String toString() {
			return "(" + x_coord  + "," + y_coord + ")";
		}
	}

	
	/*** Default final parameters ****/	
	final private int DEFAULT_TOTAL_TRANSITION_WEIGHT = 100; 
	final private double GOAL_REWARD = 0.5;
	final private double PENALTY = -0.05;
	final private int GOAL_STATE;
	
	
	/**
	 * tm: the transition model. For each action, and each state, we store the states 
	 * to which we can transition with positive probability, as well as an integer weight
	 * associated with that state. We assume that all probs. in the model have a total 
	 * weight of total_sum_pm 
	 */
	protected ArrayList< ArrayList <Map<State, Double> > > tm; //probability model
	
	/**
	 * As to avoid numerical issues, we store transition probabilities as integers,
	 * assuming that the transition maps for each state-action pair 
	 * add up to total_sum_pm  
	 */
	protected int total_sum_pm;
	
	
	/**
	 * W : width, H : height, A : number of actions, S : number of states
	 */
	protected int W,H,A,S;
	
	/**
	 * Array holding all states in this MDP
	 */
	protected State[] allStates;
	
	
	/**
	 * array holding the index value for each of the states in the grid
	 */
	private int[][] idx_all;	
	
	
	
	protected GridMDP() {GOAL_STATE = 0;} 
	
	public GridMDP(int size) {
		//initial parameters
		W = H = size;
		int[][] wpos = getDefaultWallPosition();		
		int[][] dirs = getDefaultDirections();
		A = dirs.length;
		
		//obtain the number of states and the index at each position on the grid
		int i_s = 0; //will monitor the index of the state work
		idx_all = new int[W][H];
		for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
			for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
				if(isWall(x_ax, y_ax, wpos)) idx_all[x_ax][y_ax] = -1;
				else idx_all[x_ax][y_ax] = i_s++;
			}
		}
		S = i_s;
		//set up goal state
		GOAL_STATE = i_s - 1;
		// set up idx_to_coords
		allStates = new State[S];
		for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
			for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
				if(idx_all[x_ax][y_ax] != -1) {
					int i_ss = idx_all[x_ax][y_ax];
					allStates[i_ss] = new GridState(this, x_ax, y_ax, i_ss);					
				}
			}
		}
		
		
		total_sum_pm = DEFAULT_TOTAL_TRANSITION_WEIGHT;
		//initialize tm
		ArrayList<ArrayList<Map<Integer,Double>>> tm_int = new ArrayList<ArrayList<Map<Integer,Double>>>(A);
		for (int i = 0; i < A; i++) {
			tm_int.add(new ArrayList<Map<Integer,Double>>(S));
			for (int j = 0; j < S; j++) {
				tm_int.get(i).add(new HashMap<Integer,Double>());
			}
		}
		
		//for each action
		for (int i_a = 0; i_a < A; i_a++) {
			for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
				for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
					//ignore 'inexistent' states
					if(idx_all[x_ax][y_ax] == -1) continue;
					
					// get coords of the next state, as indicated by action a
					int xx = bound(x_ax + dirs[i_a][0]);
					int yy = bound(y_ax + dirs[i_a][1]);
					if(idx_all[xx][yy] == -1) {
						xx = x_ax;
						yy = y_ax;
					}

					int i_ss = idx_all[x_ax][y_ax];
					/* set up TRANSITION */
					//.85% desired transition, .05 stay put, .1 left or right
					Map<Integer, Double> tm_tmp = tm_int.get(i_a).get(i_ss);
					//indeces of the 4 candidates for transition
					int[] idxs = new int[4];
					idxs[0] = i_ss; //curent state
					idxs[1] = idx_all[xx][yy]; // next state
					for(int i=0; i<2;i++) { // left and right of crt state
						int a_n = (i_a + (2*i-1) + dirs.length) % dirs.length;
						int x_tmp = bound(x_ax + dirs[a_n][0]);
						int y_tmp = bound(y_ax + dirs[a_n][1]);
						idxs[i+2] = (idx_all[x_tmp][y_tmp] == -1) ?
							 idxs[0] : idx_all[x_tmp][y_tmp];				                		
					}
					int[] vals = {5,0,0,0}; 
					if(idxs[1] == i_ss) {
						vals[0] += 85;
					}else vals[1] = 85;
					for (int i = 2; i < idxs.length; i++) {
						if(idxs[i] == i_ss) {
							vals[0] += 5;
						}else {
							vals[i] = 5;
						}
					}
					//only add non-negative transitions
					for (int i = 0; i < idxs.length; i++) {
						if(vals[i] != 0) {
							tm_tmp.put(idxs[i], (double) vals[i]);
						}
					}
					/**TRANSITION DONE*/
				}//for x_ax
			}//for y_ax
		} //for i_a
		
		tm = new ArrayList<ArrayList<Map<State,Double>>>(A);
		for (int i = 0; i < A; i++) {
			tm.add(new ArrayList<Map<State,Double>>(S));
			for (int j = 0; j < S; j++) {
				tm.get(i).add(new HashMap<State,Double>());
				for(Integer k : tm_int.get(i).get(j).keySet()) {
					tm.get(i).get(j).put(allStates[k], tm_int.get(i).get(j).get(k));
				}
			}
		}
		
	}
	
	@Override
	public double R(State s, int a) throws InvalidMDPException{
		if(!s.sameMdp(this)) throw new InvalidMDPException();
		GridState ss = (GridState) s;
		if(ss.isGoal()) return GOAL_REWARD;
		else return PENALTY;
	}
	
	@Override
	public double P(State s, int a, State sn /*s_next*/) throws InvalidMDPException{
		if(!s.sameMdp(this) || !sn.sameMdp(this)) throw new InvalidMDPException();
		Double d = tm.get(a).get(s.idx()).get(sn.idx());
		if (d == null) {
			return 0;
		}else {
			return  d / total_sum_pm;
		}
	}
	
	
	@Override
	public int number_states() {
		return S;
	}
		
	@Override
	public int number_actions() {
		return A;
	}	
	
	@Override
	public Collection<State> getStates() {
		Collection<State> toRet = new LinkedList<State>();
		for(State s : allStates) {
			toRet.add(s);
		}
		return toRet;
	}
	
	@Override
	public Map<State,Double> getHistogram(State s, int a) throws InvalidMDPException {
		if(!s.sameMdp(this)) throw new InvalidMDPException();
		
		int si = s.idx();
		
		// transition map out of input state s to the base Grid MDP
		//Map<State, Integer> tm_s_this = tm.get(a).get(si);
		
		return tm.get(a).get(si);
	}
	
	
	
	@Override
	public String toString() {
		try {
		String toRet = "MAP\n\n\n";
		for (int i = 0; i < idx_all.length; i++) {
			for (int j = 0; j < idx_all[i].length; j++) {
				toRet += idx_all[i][j] + " ";
			}toRet += "\n";
		}
		
		toRet += "\n\nREWARD\n\n\n";
		for (int i = 0; i < A; i++) {
			toRet += "Action " + i + " " + "\n";
			for (int s = 0; s < S; s++) {
				toRet += allStates[s] + " :" + this.R(allStates[s], i) + " ";
			}
			toRet += "\n";
		}
		toRet += "\n\nTRANSITION\n\n\n";
		for (int i = 0; i < A; i++) {
			toRet += "Action " + i + "\n";
			for (int k = 0; k < tm.get(i).size(); k++) {
				toRet += "S " + allStates[k] + " :" + " ---> ";
				for (State j : tm.get(i).get(k).keySet()) {
					toRet += j + " :"
							+ ((double) tm.get(i).get(k).get(j)) / total_sum_pm 
							+ " ";
				}
				toRet += "\n";
			}
			toRet += "\n";
		}
		return toRet;
		}catch (InvalidMDPException e) {
			return "INVALID MDP";
		}
	}
	
	/********* HELPER FUNCTIONS *******/
	
	 /** The default directions N,W,S,E
	 * @return Each direction is represented by two integers: the x-coord 
	 *         and y-coord offsets
	 */
	protected int[][] getDefaultDirections() {
		int[][] toRet = new int[4][];
		int i=0;
		toRet[i++] = new int[] {-1,0};
		toRet[i++] = new int[] {0,1};
		toRet[i++] = new int[] {1,0};
		toRet[i++] = new int[] {0,-1};
		return toRet;
		
	}
	
	private int[][] getDefaultWallPosition() {
		int[][] toRet = new int[6][]; //change this if add/remove walls!
		//Note that these could be changed at will...
		//TODO: maybe read this from a file, but then it would not be 
		//the default positions
		int i=0;
		toRet[i++] = new int[] {4, 0, 4, 2};
		toRet[i++] = new int[] {4, 3, 4, 7};
		toRet[i++] = new int[] {4, 8, 4, 10};
		toRet[i++] = new int[] {0, 4, 2, 4};
		toRet[i++] = new int[] {3, 4, 7, 4};
		toRet[i++] = new int[] {8, 4, 10, 4};
		
		for (int j = 0; j < toRet.length; j++) {
			for (int j2 = 0; j2 < toRet[j].length; j2++) {
				toRet[j][j2] = resizeForDefaultWalls(toRet[j][j2]);
			}
		}
		return toRet;
	}
	
	private int resizeForDefaultWalls(int x) {
		//Resize position form a 10 by 10 to a size by size
	    return W * x / 10;
	}
	
	private int bound(int s) {
		// Make sure index is not out of bounds : if s is negative, returns 0, 
		//if s is larger than Width-1, it returns 
		//width-1. NOTE, only works for same width and height 
		if(s < 0) return 0;
		if(s >= W) return W-1;
		return s;
	}

	private boolean isWall(int xx, int yy, int[][] wpos) {
		// determined whether or not a given state is a wall, based on input wpos
		for (int i = 0; i < wpos.length; i++) {
			if(wpos[i][1] == wpos[i][3] && wpos[i][1] == yy) {
				if(wpos[i][0] <= xx && wpos[i][2] > xx ) {
					return true;
				}
			}else if(wpos[i][0] == wpos[i][2] && wpos[i][0] == xx){
				if(wpos[i][1] <= yy && wpos[i][3] > yy ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void saveMapRepresentation(String path) {
		try{
			PrintWriter out = new PrintWriter(path + "/map.csv");
			
			int[][] map = new int[W][H];
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					map[i][j] = -1;
				}
			}
			for (State s : allStates) {
				GridState ss = (GridState) s;
				map[ss.x_coord][ss.y_coord] = s.idx();
			}
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					out.print(map[i][j] + ((j < map[i].length - 1) ? ", " : ""));
				}out.println();
			}
			out.close();
		}catch(FileNotFoundException e) {
			e.printStackTrace(); //TODO maybe change
		}
	}
	
	
}

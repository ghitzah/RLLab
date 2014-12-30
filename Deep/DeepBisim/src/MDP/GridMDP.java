package MDP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class implementing a Grid World Environment (GWE)
 * @author gcoman
 *
 */
public class GridMDP extends MDP{

	/************************
	DATA
	 * ******************************/

	/*** Default final parameters ****/	
	final private int DEFAULT_TOTAL_TRANSITION_WEIGHT = 100; 
	final private double GOAL_REWARD = 0.5;
	final private double PENALTY = -0.05;
	final private int GOAL_STATE;

	/**
	 * W : width, H : height, A : number of actions, S : number of states
	 */
	final protected int W,H,A,S;

	/**
	 * tm: the transition model. For each action, and each state, we store the states 
	 * to which we can transition with positive probability, as well as an integer weight
	 * associated with that state. We assume that all probs. in the model have a total 
	 * weight of total_sum_pm 
	 */
	final protected ArrayList< ArrayList <Map<Integer, Integer> > > tm; //probability model

	/**
	 * As to avoid numerical issues, we store transition probabilities as integers,
	 * assuming that the transition maps for each state-action pair 
	 * add up to total_sum_pm  
	 */
	final protected int total_sum_pm;

	/**
	 * Array holding all states in this MDP
	 */
	final protected GridState[] allStates;

	/**
	 * Array holding all actions in this MDP
	 */
	final protected Action[] allActions;

	
	

	/**
	 * array holding the index value for each of the states in the grid
	 */
	final private int[][] idx_all;	





	/************************
			CONSTRUCTORS
	 * ******************************/


	public GridMDP(int size, GridType t) {
		//initial parameters
		W = H = size;

		int[][] wpos;
		switch(t) {
		case EMPTY:
			wpos = new int[0][];
			break;
		default	:
			wpos = getDefaultWallPosition();					
		}
		int[][] dirs = getDefaultDirections();
		A = dirs.length;


		//obtain the number of states and the index at each position on the grid
		int i_s = 0; //will monitor the index of the state work
		idx_all = new int[W][H];
		for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
			for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
				if(isWall(x_ax, y_ax, wpos)) { idx_all[x_ax][y_ax] = -1; 
				//System.out.print(".");
				}
				else idx_all[x_ax][y_ax] = i_s++;
			}
		}
		S = i_s;
		//set up goal state
		GOAL_STATE = i_s - 1;
		// set up idx_to_coords
		allStates = new GridState[S];
		for (int y_ax = 0; y_ax < W; y_ax++) { //y-coord
			for (int x_ax = 0; x_ax < W; x_ax++) { //x-coord
				if(idx_all[x_ax][y_ax] != -1) {
					int i_ss = idx_all[x_ax][y_ax];
					allStates[i_ss] = new GridState(x_ax, y_ax, i_ss);					
				}
			}
		}


		total_sum_pm = DEFAULT_TOTAL_TRANSITION_WEIGHT;
		//initialize tm
		ArrayList<ArrayList<Map<Integer,Integer>>> tm_int = new ArrayList<ArrayList<Map<Integer,Integer>>>(A);
		for (int i = 0; i < A; i++) {
			tm_int.add(new ArrayList<Map<Integer,Integer>>(S));
			for (int j = 0; j < S; j++) {
				tm_int.get(i).add(new HashMap<Integer,Integer>());
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
					Map<Integer, Integer> tm_tmp = tm_int.get(i_a).get(i_ss);
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
							tm_tmp.put(idxs[i], vals[i]);
						}
					}
					/**TRANSITION DONE*/
				}//for x_ax
			}//for y_ax
		} //for i_a

		tm = new ArrayList<ArrayList<Map<Integer,Integer>>>(A);
		for (int i = 0; i < A; i++) {
			tm.add(new ArrayList<Map<Integer,Integer>>(S));
			for (int j = 0; j < S; j++) {
				tm.get(i).add(new HashMap<Integer,Integer>());
				for(Integer k : tm_int.get(i).get(j).keySet()) {
					tm.get(i).get(j).put(k, tm_int.get(i).get(j).get(k));
				}
			}
		}

		
		// Actions 
		allActions = new Action[A];
		for (int ada = 0; ada < A; ada++) {
			allActions[ada] = new FiniteAction(ada);
		}
		
	}


	/************************
	 *	GETTERS
	 * ******************************/

	@Override
	public double P(State c, Action a, State cn) {
		GridState sg = (GridState) c;
		FiniteAction ag = (FiniteAction) a;
		GridState sng = (GridState) cn;

		Integer d = tm.get(ag.idx()).get(sg.idx).get(sng.idx);
		if (d == null) {
			return 0;
		}else {
			return  ((double) d) / total_sum_pm;
		}
	}

	@Override
	public Measure P(State c, Action a) {
		FiniteSMeasure gm = new FiniteSMeasure();
		gm.totalMeasure = total_sum_pm;
		gm.indiv_measures = new HashMap<State, Integer>();

		GridState sg = (GridState) c;
		FiniteAction ag = (FiniteAction) a;
		Map<Integer,Integer> tmtmp = tm.get(ag.idx()).get(sg.idx); 
		for(Integer ada : tmtmp.keySet()) {
			gm.indiv_measures.put(allStates[ada], tmtmp.get(ada));
		}
		return gm;
	}

	@Override
	public double R(State c, Action a) {
		GridState ss = (GridState) c;
		if(ss.isGoal()) return GOAL_REWARD;
		else return PENALTY;
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
	public String toString() {
		String toRet = "\n\nTRANSITION\n\n\n";
		for (int i = 0; i < tm.size(); i++) {
			toRet += "Action " + i + "\n";
			for (int k = 0; k < tm.get(i).size(); k++) {
				toRet += "S " + "(" + allStates[k].x_coord()  + "," + 
						allStates[k].y_coord() + ") :" + " ---> ";
				for (Integer j : tm.get(i).get(k).keySet()) {					
					toRet += "(" + allStates[j].x_coord()  + "," + allStates[j].y_coord() + ") :" + ":"
							+ ((double) tm.get(i).get(k).get(j)) / total_sum_pm 
							+ " ";
				}
				toRet += "\n";
			}
			toRet += "\n";
		}
		return toRet;
	}



	/**
	 * Simple main function to test the end product of a simple PuddleMDP
	 * param args: ignored
	 */
	public static void main(String[] args) {
		GridMDP m = new GridMDP(10, GridType.DEFAULT);
		System.out.println(m);
	}



	/************************
			PROTECTED AND PRIVATE METHODS
	 * ******************************/


	/** The default directions N,W,S,E
	 * @return Each direction is represented by two integers: the x-coord 
	 *         and y-coord offsets
	 */
	int[][] getDefaultDirections() {
		int[][] toRet = new int[4][];
		int i=0;
		toRet[i++] = new int[] {-1,0};
		toRet[i++] = new int[] {0,1};
		toRet[i++] = new int[] {1,0};
		toRet[i++] = new int[] {0,-1};
		return toRet;

	}	

	/**
	 * the default wall positions
	 * @return wall positions
	 */
	private int[][] getDefaultWallPosition() {
		int[][] toRet = new int[6][]; //change this if add/remove walls!
		//Note that these could be changed at will...
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

	/**
	 * Resize position form a 10 by 10 to a size by size
	 * @param x : the integer to be resized
	 * @return the resized value
	 */
	private int resizeForDefaultWalls(int x) {
		return W * x / 10;
	}

	/**
	 *  Make sure index is not out of bounds : if s is negative, returns 0, 
	    if s is larger than Width-1, it returns 
		width-1. NOTE, only works for same width and height
	 * @param s : value to be bounded
	 * @return : bounded value
	 */
	private int bound(int s) {
		if(s < 0) return 0;
		if(s >= W) return W-1;
		return s;
	}

	/**
	 * Check whether the indeces are hitting a wall
	 * @param xx : x coordinate
	 * @param yy : y coordinate
	 * @param wpos : the walls
	 * @return : TRUE if the coordinate is a wall
	 */
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



	/************************
	 * CLASSES AND ENUMS
	 ******************************/
	
	/**
	 * Type of grid: only used by the constructor
	 * @author gcoman
	 */
	public static enum GridType {EMPTY, DEFAULT};


	/**
	 * Class implementing a state for a finite Grid World
	 * @author gcoman
	 */
	public class GridState implements State {
		/**
		 * y-coordinate on the grid
		 */
		private int y_coord;

		/**
		 * x-coordinate on the grid
		 */
		private int x_coord;

		/**
		 * index in transition matrix
		 */
		private int idx;

		/**
		 * Main constructor
		 * @param x : x-coordinate
		 * @param y : y-coordinate
		 * @param index : the index of the state as an integer from 0 to SIZE-1
		 */
		public GridState(int x, int y, int index) {			
			this.idx = index;
			x_coord = bound(x);
			y_coord = bound(y);				
		}

		/** GETTERS */
		int x_coord(){
			return x_coord;
		}
		int y_coord(){
			return y_coord;
		}
		int idx() {
			return idx;
		}

		/**
		 * Returns true only if the state is a goal state (higher reward)
		 * @return boolean - goal or not
		 */
		boolean isGoal() {
			return idx == GOAL_STATE;
		}

		@Override
		public String toString() {
			return "(" + x_coord  + "," + y_coord + ")";
		}

	}


	@Override
	public Iterator<State> get_state_iterator() {
		return new Iterator<MDP.State>() {
			private int ada = 0;
			
			@Override
			public boolean hasNext() { return ada < S;}

			@Override
			public State next() { return hasNext() ? allStates[ada++] : null; }

			@Override
			public void remove() { }
		};
	}


	@Override
	public Iterator<StatePair> get_state_pair_iterator() {
		return new Iterator<StatePair>() {
			private int ada = 1;
			private int bea = 0;
			
			@Override
			public boolean hasNext() { return (ada < S) ;}

			@Override
			public StatePair next() { 
				if (hasNext()) {
					StatePair toRet = new StatePair(allStates[ada], allStates[bea]);
					bea++;
					if (bea == ada) { ada++; bea = 0; }
					return toRet;
				} else { return null; }
			}
				
			@Override
			public void remove() { }
		};
	}


	@Override
	public Iterator<Action> get_action_iterator() {
		return new Iterator<Action>() {
			private int ada = 0;
			
			@Override
			public boolean hasNext() { return ada < A;}

			@Override
			public Action next() { return hasNext() ? allActions[ada++] : null; }

			@Override
			public void remove() { }
		};
	}
}

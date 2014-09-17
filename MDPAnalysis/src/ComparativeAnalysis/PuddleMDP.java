package ComparativeAnalysis;
import java.util.HashMap;
import java.util.Map;

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
	final private int WIDTH_LAKE = 3;
	
	
	
	
	
	/** rm : the reward model. For each action, we store only the states 
	 * that have non-zero reward 
	 */ 
	protected Map<Integer, Map<Integer, Double> > rm;
	
	
	/**
	 * Creates a Puddle MDP with the default locations for the lake position and 
	 * directions. The environment will be a grid of 'size by size', parameter provided  
	 * @param size : the with and height of the squared environment
	 */
	public PuddleMDP(int size) {		
		super(size, GridType.EMPTY);
		int[][] dirs = getDefaultDirections();
		/*
		 * get default water position and direction. 
		 * - The position of the water is represented using lines: 4 
		 *   integers corresponding to the start and end points of the line
		 * - Each direction is represented by two integers: the x-coord and y-coord
		 *   offsets
		 */
		int[][] wpos = getDefaultWaterPosition();
		
		rm = new HashMap<Integer, Map<Integer,Double>>();
		//for each action
		for (int i_a = 0; i_a < dirs.length; i_a++) {
			for (int i_s = 0; i_s < allStates.length; i_s++) {
				GridState s = allStates[i_s];
				
				/* set up REWARD */	
				if(s.isGoal()) { // reach goal state						
					addReward(i_s, i_a, GOAL_REWARD);
				}else {
					int d = WIDTH_LAKE; 
					//check if distance d is smaller than WIDTH_LAKE
					int xx = s.x_coord;
					int yy = s.y_coord;
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
						addReward(i_s,i_a, - (WIDTH_LAKE - d) * 0.1 );
					}
				} //end if then
				/* REWARD done */
			}//for s			
		} //for i_a
	} // method

	

	private void addReward(int s, int a, double d) {
		if(!rm.containsKey(a)) {
			rm.put(a, new HashMap<Integer, Double>());
		}
		rm.get(a).put(s, d);
	}
	
	@Override
	public double R(int s, int a){
		return super.R(s,a) + 
				((!rm.containsKey(a) || !rm.get(a).containsKey(s)) ? 0 : rm.get(a).get(s));
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
				for (Integer j : tm.get(i).get(k).keySet()) {					
					toRet += "(" + j%W  + "," + j/W + ") :" + ":"
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
		int[][] toRet = new int[2][];
		int i=0;
		toRet[i++] = new int[] {3, 3, 3, 7};
		toRet[i++] = new int[] {3, 7, 7, 7};
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
	 * Simple main function to test the end product of a simple PuddleMDP
	 * @param args: ignored
	 */

	public static void main(String[] args) {
		PuddleMDP m = new PuddleMDP(10);
		System.out.println(m);
	}
	
	

	private int coord_to_idx(int x, int y) {
		return y*W+x;
	}
	
	
}



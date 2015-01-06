package MDP;

import java.util.Iterator;
import java.util.Random;

public class Continuous2d extends MDP{

	public class Cont2DState extends State {
		 /**
		  *  y-coordinate on the grid
		  */
		private double y_coord;

		/**
		 * x-coordinate on the grid
		 */
		private double x_coord;
		
		/**
		 * Main constructor
		 * @param x : x-coordinate
		 * @param y : y-coordinate
		 */
		public Cont2DState(double x, double y)  {
			x_coord = bound(x);
			y_coord = bound(y);				
		}

		/** GETTERS */
		double x_coord(){
			return x_coord;
		}
		double y_coord(){
			return y_coord;
		}
		
		private double bound(double d) {
			return Math.max(Math.min(d, 1), 0);
		}
		
		public double dist_sq(Cont2DState s) {
			return (x_coord - s.x_coord) * (x_coord - s.x_coord) + 
					(y_coord - s.y_coord) * (y_coord - s.y_coord);
		}
		
		
		@Override
		public boolean equals(Object obj) {
			double EPSILON = 0.000001;
			Cont2DState o = (Cont2DState) obj;
			return (x_coord - o.x_coord) < EPSILON && (y_coord - o.y_coord) < EPSILON;
		}
	}
	
	public class DirectinalAction implements Action{
		
		double x_dir;
		double y_dir;
		
		final int ACTION_SEED = 0;

		Random r = new Random(ACTION_SEED);
		
		public DirectinalAction(double x_dir, double y_dir) {
			this.x_dir = x_dir;
			this.y_dir = y_dir;
		}
		
		public State get_sample(Cont2DState s) {
			double d = r.nextDouble();
			return new Cont2DState(s.x_coord + x_dir * d, 
								s.y_coord + y_dir * d);
		}
	}
	
	
	
	final protected int A;
	/**
	 * Array holding all actions in this MDP
	 */
	final protected Action[] allActions;
	
	final double RADIUS_OF_MOVEMENT = 0.2;
	
	public Continuous2d() {
		A = 4;
		allActions = new DirectinalAction[4];
		allActions[0] = new DirectinalAction(RADIUS_OF_MOVEMENT, 0);
		allActions[1] = new DirectinalAction(0, RADIUS_OF_MOVEMENT);
		allActions[2] = new DirectinalAction(-RADIUS_OF_MOVEMENT, 0);
		allActions[3] = new DirectinalAction(0, -RADIUS_OF_MOVEMENT);
	}
	
	@Override
	public double P(State s, Action a, State sn) {
		return 0;
	}

	@Override
	public Measure P(State s, Action a) {
		assert(s == null);
		final DirectinalAction ad = (DirectinalAction) a;
		final Cont2DState sc = (Cont2DState) s;
		return new Measure() {
			@Override
			public State sample() {
				return ad.get_sample(sc);
			}
		};
	}

	
	final double REWARD = 1.0;
	final Cont2DState REWARD_CENTER = new Cont2DState(0.9, 0.9);
	final double REWARD_RADIUS_SQ = 0.01;
	
	
	@Override
	public double R(State s, Action a) {
		Cont2DState sc = (Cont2DState) s;
		return (sc.dist_sq(REWARD_CENTER) < REWARD_RADIUS_SQ ) ? REWARD : 0;
	}
	
	Random r = new Random(0);
	final int RAND_SEED_STATE_ITERATOR_1 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_2 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_3 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_4 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_5 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_6 = r.nextInt();
	final int RAND_SEED_STATE_ITERATOR_7 = r.nextInt();
	
	
	@Override
	public Iterator<State> get_state_iterator() {
		return new Iterator<MDP.State>() {
			private Random r1 = new Random(RAND_SEED_STATE_ITERATOR_1);
			private Random r2 = new Random(RAND_SEED_STATE_ITERATOR_2);
			
			@Override
			public boolean hasNext() { return true;}

			@Override
			public State next() { return new Cont2DState(r1.nextDouble(), r2.nextDouble()); }

			@Override
			public void remove() { /* NOTHING TO DO*/}
		};
	}

	@Override
	public Iterator<StatePair> get_state_pair_iterator() {
		return new Iterator<StatePair>() {
			private Random r1 = new Random(RAND_SEED_STATE_ITERATOR_3);
			private Random r2 = new Random(RAND_SEED_STATE_ITERATOR_4);
			private Random r3 = new Random(RAND_SEED_STATE_ITERATOR_5);
			private Random r4 = new Random(RAND_SEED_STATE_ITERATOR_6);
			
			@Override
			public boolean hasNext() { return true ;}

			@Override
			public StatePair next() { 
				return new StatePair(
						new Cont2DState(r1.nextDouble(), r2.nextDouble()), 
						new Cont2DState(r3.nextDouble(), r4.nextDouble()));
			}
				
			@Override
			public void remove() { /* NOTHING TO DO */}
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

	@Override
	public int number_states() {
		return -1;
	}

	@Override
	public int number_actions() {
		return A;
	}
 
	@SuppressWarnings("serial")
	public class ContinuousException extends Exception{ }
	
}

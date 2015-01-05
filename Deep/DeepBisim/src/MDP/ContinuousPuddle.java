package MDP;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that implements a Puddle MDP that is continuous
 * @author gcoman
 *
 */
public class ContinuousPuddle extends Continuous2d{

	/*** Default final parameters ****/	
	/**
	 * The largest distance to the obstacles that will incur a penalty
	 */
	final private double WIDTH_LAKE = 0.3;
	
	/**
	 * The largest penalty due to a point positioned inside a lake
	 */
	final private double MAX_LAKE_PENALTY = 1.0;
	
	final Set<StatePair> lakeCenter;
	
	/**
	 * Main constructor
	 */
	public ContinuousPuddle() {
		super();
		lakeCenter = getDefaultWaterPosition();
	}
	
	
	@Override
	public double R(State s, Action a) {
		double rewardWithoutLake = super.R(s,a);
		if(lakeCenter == null || lakeCenter.size() == 0) {
			return rewardWithoutLake;
		}
		
		double closestDistToLine = WIDTH_LAKE;
		Cont2DState s3 = (Cont2DState) s;
		for(StatePair v : lakeCenter) {
			Cont2DState s1 = (Cont2DState) v.s1;
			Cont2DState s2 = (Cont2DState) v.s1;
			double xx = (s2.x_coord() - s1.x_coord());
			xx *= xx;
			double yy = (s2.y_coord() - s1.y_coord());
			yy *= yy;
			double distSq = xx + yy;
			
			double twiceArea = Math.abs(
					  s1.x_coord() * s2.y_coord() 
					+ s2.x_coord() * s3.y_coord() 
					+ s3.x_coord() * s1.y_coord()
					- s1.x_coord() * s3.y_coord()
					- s3.x_coord() * s2.y_coord()
					- s2.x_coord() * s1.y_coord());
			closestDistToLine = Math.min(closestDistToLine, 
					twiceArea / Math.sqrt(distSq));
		}
		
		double lakePenalty = MAX_LAKE_PENALTY 
				* (WIDTH_LAKE - closestDistToLine) / WIDTH_LAKE;
		return rewardWithoutLake - lakePenalty;
	}
	
	
	
	 /** Returns the default lake water positions
	 * @return : The position of the water is represented using lines: 4 
	 * 		     integers corresponding to the start and end points of the line
	 */
	private Set<StatePair> getDefaultWaterPosition() {
		HashSet<StatePair> toRet = new HashSet<StatePair>();
		toRet.add(new StatePair(new Cont2DState(0.3, 0.3), 
				new Cont2DState(0.3, 0.7)));
		toRet.add(new StatePair(new Cont2DState(0.3, 0.7), 
				new Cont2DState(0.7, 0.7)));
		return toRet;
	}
	
	
}

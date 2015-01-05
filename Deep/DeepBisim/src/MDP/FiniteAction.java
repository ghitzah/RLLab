package MDP;

import MDP.MDP.Action;

/**
 * Class implement an action for an MDP with a finite number of actions
 * @author gcoman
 *
 */
public class FiniteAction implements Action{
	private int idx;

	public FiniteAction(int idx) {
		this.idx = idx;
	}

	/**
	 * The index of the action in the action set
	 * @return - the index of the action 
	 */
	public int idx() {
		return idx;
	}
}
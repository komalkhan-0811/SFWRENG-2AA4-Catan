package catan;

/**
 * Automaton states for one player turn
 * 
 * Legal state transitions are:
 * 
 *START: [Roll] -> ROLLED
 *ROLLED: [Build] -> ROLLED
 *ROLLED: [Go] ->  DONE
 *ANY: [List]
 *
 * LIST is allowed in any state without causing a transition.
 * Any command that is illegal in the current state is rejected with a message.
 * 
 * @author Rameen Tariq
 */

public enum TurnState {
	
	
	START,
    ROLLED,
    DONE;
	
	
	/**
     * Returns true if ROLL is a legal command in this state.
     * Only legal in START.
     *
     * @return true when the player has not yet rolled
     */
    public boolean canRoll() {
        return this == START;
    }

    /**
     * Returns true if BUILD commands are legsl in this state.
     * Only legal in ROLLED.
     *
     * @return true when the player has already rolled
     */
    public boolean canBuild() {
        return this == ROLLED;
    }

    /**
     * Returns true if GO is a legal command in this state.
     * Only legal in ROLLED (the player must have rolled before ending their turn).
     *
     * @return true when the player has already rolled
     */
    public boolean canGo() {
        return this == ROLLED;
    }

    /**
     * Returns true if LIST is a legal command in this state.
     * LIST is always permitted 
     *
     * @return always true
     */
    public boolean canList() {
        return true;
    }

    /**
     * Returns true if the turn is complete and the game should continue
     *
     * @return true when state is DONE
     */
    public boolean isDone() {
        return this == DONE;
    }
	
	

}

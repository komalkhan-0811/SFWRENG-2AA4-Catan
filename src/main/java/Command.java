package catan;

/**
 * Command interface for the Command Pattern (Behavioural).
 * 
 * Each game action implements this interface to support
 * undo/redo functionality - R3.1.
 *
 * The Command Pattern encapsulates a request as an object,
 * allowing parameterization of actions, queuing, and reversible
 * operations.
 *
 * @author Rameen Tariq
 */

public interface Command {
	
	 /**
     * Executes the command, applying the action to the game state.
     */
    void execute();
    
    /**
     * Reverses the effect of the command, restoring the game state to what it was before execute() was called.
     */
    void undo();
    
    /**
     * Returns a human-readable description of the command.
     * Used for logging and debugging purposes.
     *
     * @return a string describing the command
     */
    String getDescription();

}

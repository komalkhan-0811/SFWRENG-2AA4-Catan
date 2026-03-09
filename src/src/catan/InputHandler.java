package catan;

/**
 * Abstraction over any input/output source for the Catan simulator.
 *
 * Satisfies the Dependency Inversion Principle: HumanPlayer and Game
 * depend on this interface, never on a concrete I/O class (e.g. Scanner).
 *
 * Satisfies the Open/Closed Principle: new input mechanisms (GUI, network,
 * test stub) can be added by implementing this interface with zero changes
 * to HumanPlayer or Game.
 *
 * @author Rameen Tariq
 */

public interface InputHandler {
	
	/**
	 * Displays a message to the user ( no input needed)
	 * @param message the text to display
	 */
	void displayMessage(String message);
	
	/**
	 * Displays a prompt and blocks until a line of input is given.
	 * Never returns null but returns empty string
	 * 
	 * @param prompt text shown before the input cursor
	 * @return the line entered by the user, trimmed
	 */
	String readLine(String prompt);
	

}

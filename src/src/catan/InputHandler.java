package catan;

/**
 * Satisfies the Dependency Inversion Principle: HumanPLayer and Game rely on this interface
 * 
 * Satisfies the Open/Closed Principle
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

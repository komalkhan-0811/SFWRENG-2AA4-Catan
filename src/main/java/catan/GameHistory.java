package catan;

import java.util.Stack;

/**
 * Manages the execution history of game commands to support undo/redo functionality for R3.1.
 *
 * Acts as the Invoker in the Command Pattern (Behavioural). 
 * Maintains two stacks: one for executed commands (undo stack) and one for undone
 * commands (redo stack). Executing a new command clears the redo stack because a new action invalidates any previously undone history.
 *
 * Usage example:
 *     GameHistory history = new GameHistory();
 *     Command cmd = new BuildSettlementCommand(board, player, 5);
 *     history.executeCommand(cmd);
 *     history.undo();
 *     history.redo();
 *
 *
 * @author Rameen Tariq
 */

public class GameHistory {
	

	   
	     //Stack of executed commands that can be undone
	    private final Stack<Command> undoStack = new Stack<Command>();

	   
	    private final Stack<Command> redoStack = new Stack<Command>();

	    /**
	     * Executes the given command and records it on the undo stack,also clearing the redo stack
	     *
	     * Clearing the redo stack ensures that a new action doesn't allow the player to redo a previously undone action that is now
	     * inconsistent with the current game state
	     *
	     * @param command the command to execute and record
	     */
	    public void executeCommand(Command command) {
	        command.execute();
	        undoStack.push(command);
	        redoStack.clear();
	    }

	    /**
	     * Undoes the most recently executed command and moves it to the redo stack
	     *
	     * @return true if a command was successfully undone and false if there is nothing to undo
	     */
	    public boolean undo() {
	        if (undoStack.isEmpty()) {
	            return false;
	        }
	        Command command = undoStack.pop();
	        command.undo();
	        redoStack.push(command);
	        return true;
	    }

	    /**
	     * Redoes the most recently undone command and moves it back to the undo stack
	     *
	     * @return true if a command was successfully redone and false if there is nothing to redo
	     */
	    public boolean redo() {
	        if (redoStack.isEmpty()) {
	            return false;
	        }
	        Command command = redoStack.pop();
	        command.execute();
	        undoStack.push(command);
	        return true;
	    }

	    /**
	     * Returns whether there is a command available to undo.
	     *
	     * @return true if the undo stack is non-empty, false otherwise
	     */
	    public boolean canUndo() {
	        return !undoStack.isEmpty();
	    }

	    /**
	     * Returns whether there is a command available to redo.
	     *
	     * @return true if the redo stack is not empty and false otherwise
	     */
	    public boolean canRedo() {
	        return !redoStack.isEmpty();
	    }

	    /**
	     * Description of the most recently executed command without removing it from the undo stack
	     *
	     * @return a string describing the last command, or "None" if the undo stack is empty
	     */
	    public String getLastCommandDescription() {
	        if (undoStack.isEmpty()) {
	            return "None";
	        }
	        return undoStack.peek().getDescription();
	    }

}

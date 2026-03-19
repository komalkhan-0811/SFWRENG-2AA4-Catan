package catan;

import java.util.Scanner;

/**
 * Console based implementation of InputHandler.
 * Reads from System.in and writes to System.out.
 *
 * Swapping this for a test stub requires no changes to HumanPlayer
 * or Game (Dependency Inversion Principle).
 *
 * @author Rameen Tariq
 */
public class ConsoleInputHandler implements InputHandler {

    private final Scanner scanner;

    /**
     * Creates a ConsoleInputHandler backed by System.in.
     */
    public ConsoleInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Creates a ConsoleInputHandler backed by a custom Scanner.
     * Useful for redirecting input in automated tests.
     *
     * @param scanner the scanner to read from; must not be null
     * @throws IllegalArgumentException if scanner is null
     */
    public ConsoleInputHandler(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null.");
        }
        this.scanner = scanner;
    }
    
    /**
     * Prints message
     * 
     * @param message the message that is printed
     */
    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Prints the prompt without a newline, then blocks for input.
     * Returns empty string if the stream is exhausted.
     */
    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }
}
package catan;

/**
 * Handles all console output for the game simulator.
 *
 * All methods are static because logging is a stateless utility
 * that needs no instance data.
 *
 * @author Alisha Faridi
 */
public class GameLogger {

    /**
     * Prints a single turn action
     * Format: [TurnID] / [PlayerID]: [Action]
     *
     * @param roundNumber the current round number
     * @param playerId the ID of the player acting
     * @param actionText  human-readable description of the action
     */
    public static void printTurnAction(int roundNumber, int playerId, String actionText) {
        System.out.println(String.format("%d / %d: %s", roundNumber, playerId, actionText));
    }

    /**
     * Prints the game start banner.
     *
     * @param maxRounds the maximum number of rounds configured
     */
    public static void printGameStart(int maxRounds) {
        System.out.println("=== START GAME ===");
        System.out.println("Max Rounds: " + maxRounds);
        System.out.println("\n");
    }

    /**
     * Prints the game over message with winner info.
     *
     * @param winnerOrNull the winning Player, or null if no one reached the VP threshold
     * @param roundNumber  the round at which the game ended
     */
    public static void printGameOver(Player winnerOrNull, int roundNumber) {
        System.out.println("=== GAME OVER (Round " + roundNumber + ") ===");
        if (winnerOrNull == null) {
            System.out.println("No player reached the points needed to win.");
        } else {
            System.out.println("Winner: Player " + winnerOrNull.getPlayerId()
                    + " with " + winnerOrNull.getVictoryPoints() + " VP");
        }
        System.out.println("\n");
    }
}
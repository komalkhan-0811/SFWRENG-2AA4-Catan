package catan;

import java.util.ArrayList;
import java.util.List;

/**
 * A plain data snapshot of the game state at a point in time.
 *
 * This class holds only primitive data to enforce strict separation between the game logic
 * and the visualization layer. The visualizer reads this data but can never
 * modify the actual game state because it only ever sees this snapshot copy.
 *
 * @author Alisha Faridi
 */
public class GameSnapshot {

    private final int roundNumber;
    private final List<PlayerSnapshot> players;

    /**
     * Constructs a GameSnapshot with the given round number and player snapshots.
     *
     * @param roundNumber the current round number
     * @param players a list of player snapshots
     */
    public GameSnapshot(int roundNumber, List<PlayerSnapshot> players) {
        this.roundNumber = roundNumber;
        // Defensive copy — snapshot is immutable once created
        this.players = new ArrayList<>(players);
    }

    /**
     * Returns the round number this snapshot was taken at.
     *
     * @return the round number
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Returns the list of player snapshots.
     *
     * @return list of PlayerSnapshot objects
     */
    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    /**
     * A plain data snapshot of a single player's state.
     * Nested here since it only exists to support GameSnapshot.
     */
    public static class PlayerSnapshot {

        private final int playerId;
        private final int victoryPoints;
        private final int totalCards;
        private final String colour;

        /**
         * Constructs a PlayerSnapshot.
         *
         * @param playerId the player's ID
         * @param victoryPoints the player's current victory points
         * @param totalCards the player's current total resource cards
         * @param colour the player's colour as a string
         */
        public PlayerSnapshot(int playerId, int victoryPoints, int totalCards, String colour) {
            this.playerId = playerId;
            this.victoryPoints = victoryPoints;
            this.totalCards = totalCards;
            this.colour = colour;
        }

        public int getPlayerId()      { return playerId; }
        public int getVictoryPoints() { return victoryPoints; }
        public int getTotalCards()    { return totalCards; }
        public String getColour()     { return colour; }
    }
}
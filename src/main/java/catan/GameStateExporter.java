package catan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the current state from a Game object and produces a GameSnapshot.
 * 
 * Uses reflection to access private fields in Game, ensuring the exporter
 * does not require changes to the Game class itself.
 *
 * @author Alisha Faridi
 * 
 */

@SuppressWarnings("java:S3011") // reflection needed to access private fields
public class GameStateExporter {

    /**
     * Reads the current state from the given Game object and returns
     * a GameSnapshot containing a frozen copy of that state.
     *
     * The snapshot is a plain data object with no references back to
     * the live Game. This enforces that the visualizer cannot
     * accidentally modify game state by holding onto this snapshot.
     *
     * @param game the Game object to export state from
     * @return a GameSnapshot representing the current state
     * @throws Exception if reflection fails to access game fields
     */
    public GameSnapshot exportSnapshot(Game game) throws Exception {
    	 // Extract primitive state values via reflection
        int roundNumber = extractRoundNumber(game);
     // Convert each Player to a plain data snapshot
        List<GameSnapshot.PlayerSnapshot> playerSnapshots = extractPlayers(game);
        return new GameSnapshot(roundNumber, playerSnapshots);
    }

    /**
     * Extracts the round number from the Game object.
     *
     * @param game the Game object
     * @return the current round number
     * @throws Exception if the roundNumber field cannot be accessed
     */
    private int extractRoundNumber(Game game) throws NoSuchFieldException, IllegalAccessException {
        Field f = Game.class.getDeclaredField("roundNumber");
        f.setAccessible(true);
        return (int) f.get(game);
    }

    /**
     * Extracts the list of players from the Game object and converts
     * each into a PlayerSnapshot.
     *
     * @param game the Game object
     * @return list of PlayerSnapshot objects
     * @throws Exception if the players field cannot be accessed
     */
 // Game.players is always List<Player>
    @SuppressWarnings("unchecked")
    private List<GameSnapshot.PlayerSnapshot> extractPlayers(Game game) throws NoSuchFieldException, IllegalAccessException {
        Field f = Game.class.getDeclaredField("players");
     // setAccessible bypasses private modifier, which is required since Game fields are not exposed
        f.setAccessible(true);
        
        List<Player> players = (List<Player>) f.get(game);

        List<GameSnapshot.PlayerSnapshot> snapshots = new ArrayList<>();
        for (Player p : players) {
            snapshots.add(new GameSnapshot.PlayerSnapshot(
                p.getPlayerId(),
                p.getVictoryPoints(),
                p.getTotalCardsInHand(),
                p.getColour().toString()
            ));
        }
        return snapshots;
    }
}
package catan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the current state from a Game object and produces a GameSnapshot.
 *
 * @author Alisha Faridi
 * 
 */
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
        int roundNumber = extractRoundNumber(game);
        List<GameSnapshot.PlayerSnapshot> playerSnapshots = extractPlayers(game);
        return new GameSnapshot(roundNumber, playerSnapshots);
    }

    /**
     * Extracts the round number from the Game object.
     *
     * @param game the Game object
     * @return the current round number
     */
    private int extractRoundNumber(Game game) throws Exception {
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
     */
    @SuppressWarnings("unchecked")
    private List<GameSnapshot.PlayerSnapshot> extractPlayers(Game game) throws Exception {
        Field f = Game.class.getDeclaredField("players");
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
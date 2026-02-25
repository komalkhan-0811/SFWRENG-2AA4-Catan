// package catan; // what is this problem

import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.List;

public class PlayerInitializationTest {

    /**
     * Tests that initializeNewGame() creates exactly 4 players.
     */
    @Test(timeout = 2000)
    public void test_initializeNewGame_creates4Players() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        assertEquals(4, getPlayers(g).size());
    }

    /**
     * Tesyd that players are initialized with the correct IDs
     * and colours.
     */
    @Test(timeout = 2000)
    public void test_initializeNewGame_correctPlayerIdsAndColours() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();

        List<Player> players = getPlayers(g);

        assertEquals(1, players.get(0).getPlayerId());
        assertEquals(Colour.RED, players.get(0).getColour());

        assertEquals(2, players.get(1).getPlayerId());
        assertEquals(Colour.BLUE, players.get(1).getColour());

        assertEquals(3, players.get(2).getPlayerId());
        assertEquals(Colour.GREEN, players.get(2).getColour());

        assertEquals(4, players.get(3).getPlayerId());
        assertEquals(Colour.YELLOW, players.get(3).getColour());
    }

    /**
     * Tests that after initial placement,
     * each player has exactly 2 victory points
     * (one per settlement placed).
     */
    @Test(timeout = 2000)
    public void test_initializeNewGame_initialPlacementGives2VP() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        for (Player p : getPlayers(g)) {
            assertEquals(2, p.getVictoryPoints());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Player> getPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        return (List<Player>) f.get(g);
    }
}
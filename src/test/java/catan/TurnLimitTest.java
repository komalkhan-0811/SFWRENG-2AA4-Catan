package catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the logic turn limits.
 * 
 * @author Alisha Faridi
 *
 */

public class TurnLimitTest {

    /**
     * Tests that the game ends due to maxRounds.
     * The victoryPointsToWin value is high to ensure game ends
     * strictly because of the round limit.
     */
    @Test
    public void test_gameStopsBecauseOfMaxRounds() throws Exception {
        Game g = new Game(1);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.runSimulationUntilTermination();
        int roundNumber = getPrivateInt(g, "roundNumber");
        assertEquals(2, roundNumber);
    }

    /**
     * Uses reflection to read a private int field from a Game instance.
     *
     * @param g the Game instance
     * @param field the field name
     * @return the field's int value
     * @throws Exception if the field cannot be accessed
     */
    private static int getPrivateInt(Game g, String field) throws Exception {
        Field f = Game.class.getDeclaredField(field);
        f.setAccessible(true);
        return (int) f.get(g);
    }

    /**
     * Uses reflection to set a private int field on a Game instance.
     *
     * @param g the Game instance
     * @param field the field name
     * @param value the value to set
     * @throws Exception if the field cannot be accessed
     */
    private static void setPrivateInt(Game g, String field, int value) throws Exception {
        Field f = Game.class.getDeclaredField(field);
        f.setAccessible(true);
        f.setInt(g, value);
    }

    /**
     * Uses reflection to replace all HumanPlayer instances in the Game's
     * player list with AI player instances. 
     * 
     * @param g the Game instance whose players should be converted to AI players
     * @throws Exception if the private field cannot be accessed via reflection
    
    */

    private static void replaceWithAllAiPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        List<Player> original = (List<Player>) f.get(g);
        List<Player> aiOnly = new ArrayList<>();
        for (Player p : original) {
            if (p instanceof HumanPlayer) {
                aiOnly.add(new Player(p.getPlayerId(), p.getColour()));
            } else {
                aiOnly.add(p);
            }
        }
        f.set(g, aiOnly);
    }
}
package catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

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
}
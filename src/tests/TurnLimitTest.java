package catan;

import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Field;

public class TurnLimitTest {

    /**
     * Tests that the game ends due to maxRounds.
     * The victoryPointsToWin value is high to ensure game ends
     * strictly because of the round limit.
     */
    @Test(timeout = 5000)
    public void test_gameStopsBecauseOfMaxRounds() throws Exception {
        Game g = new Game(1);
        g.initializeNewGame();

        setPrivateInt(g, "victoryPointsToWin", 999);

        g.runSimulationUntilTermination();

        int roundNumber = getPrivateInt(g, "roundNumber");
        assertEquals(2, roundNumber);
    }

    private static int getPrivateInt(Game g, String field) throws Exception {
        Field f = Game.class.getDeclaredField(field);
        f.setAccessible(true);
        return (int) f.get(g);
    }

    private static void setPrivateInt(Game g, String field, int value) throws Exception {
        Field f = Game.class.getDeclaredField(field);
        f.setAccessible(true);
        f.setInt(g, value);
    }
}
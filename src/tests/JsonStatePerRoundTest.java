// package catan; // what is this problem

import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonStatePerRoundTest {

    /**
     * Tests that at the end of round 1:
     * - A JSON file is created
     * - The round number is correctly recorded
     * - Exactly 4 players are written
     * - Player IDs 1 through 4 are present
     * - Each player has a victoryPoints field
     */
    @Test(timeout = 5000)
    public void test_writesJsonFileForRound1_with4PlayersAndIds() throws Exception {
        Path tempDir = Files.createTempDirectory("catan_state_test");

        Game g = new Game(1);
        g.initializeNewGame();

        setPrivateInt(g, "victoryPointsToWin", 999);

        g.setStateOutputDir(tempDir);

        g.runSimulationUntilTermination();

        Path round1 = tempDir.resolve("gamestate_round_0001.json");
        assertTrue(Files.exists(round1));

        String json = new String(Files.readAllBytes(round1));

        assertTrue(json.contains("\"round\": 1"));
        assertEquals(4, countOccurrences(json, "\"id\":"));
        assertTrue(json.contains("\"id\": 1"));
        assertTrue(json.contains("\"id\": 2"));
        assertTrue(json.contains("\"id\": 3"));
        assertTrue(json.contains("\"id\": 4"));
        assertEquals(4, countOccurrences(json, "\"victoryPoints\":"));
    }

    private static void setPrivateInt(Game g, String fieldName, int value) throws Exception {
        Field f = Game.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(g, value);
    }

    private static int countOccurrences(String text, String literal) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(literal, idx)) != -1) {
            count++;
            idx += literal.length();
        }
        return count;
    }
}
package catan;
import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Tests that the game state JSON file is correctly written at the end of each round.
 *
 * Verifies the output of GameStateWriter by running a controlled 1-round simulation
 * and checking the contents of the generated JSON file.
 *
 * @author Alisha Faridi
 */

public class JsonStatePerRoundTest {

	/**
     * Tests that at the end of round 1:
     * - A JSON file named gamestate_round_0001.json is created
     * - The round number is correctly recorded as 1
     * - Exactly 4 players are written to the file
     * - Player IDs 1 through 4 are all present
     * - Each player has a victoryPoints field
     *
     * Uses reflection to set victoryPointsToWin to 999 so the game
     * runs exactly 1 round without any player winning early.
     */
    @Test(timeout = 5000)
    public void test_writesJsonFileForRound1_with4PlayersAndIds() throws Exception {
    	// Create a temp directory to capture the JSON output
        Path tempDir = Files.createTempDirectory("catan_state_test");

        // Set up a 1-round game and prevent early victory
        Game g = new Game(1);
        g.initializeNewGame();

        setPrivateInt(g, "victoryPointsToWin", 999);

        g.setStateOutputDir(tempDir);

        g.runSimulationUntilTermination();

        // Verify the JSON file was created
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

    /**
     * Uses reflection to set a private int field on a Game instance.
     * Used to override victoryPointsToWin without modifying Game's public API.
     *
     * @param g         the Game instance to modify
     * @param fieldName the name of the private field
     * @param value     the value to set
     * @throws Exception if the field cannot be accessed
     */
    private static void setPrivateInt(Game g, String fieldName, int value) throws Exception {
        Field f = Game.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(g, value);
    }

    /**
     * Counts the number of non-overlapping occurrences of a literal string
     * within a larger text. Used to verify repeated JSON fields.
     *
     * @param text    the string to search within
     * @param literal the substring to count
     * @return the number of occurrences
     */
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
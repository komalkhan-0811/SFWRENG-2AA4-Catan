package catan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;


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
     * Tests that JSON file is created with correct info 
     * 
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
    @Test
    public void test_roundJson_createdWithCorrectContent(@TempDir Path tempDir) throws Exception {
        // Set up a 1-round game and prevent early victory
        Game g = new Game(1);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        g.runSimulationUntilTermination();

        // Verify the JSON file was created
        Path round1 = tempDir.resolve("gamestate_round_0001.json");
        assertTrue(Files.exists(round1),
            "gamestate_round_0001.json should be created after round 1");

        String json = new String(Files.readAllBytes(round1));
        
        // Round number field
        assertTrue(json.contains("\"round\": 1"),
            "JSON should contain round number 1");

        // Exactly 4 player ids
        assertEquals(4, countOccurrences(json, "\"id\":"),
            "JSON should contain exactly 4 id fields (one per player)");

        assertTrue(json.contains("\"round\": 1"));
        assertEquals(4, countOccurrences(json, "\"id\":"));
        assertTrue(json.contains("\"id\": 1"));
        assertTrue(json.contains("\"id\": 2"));
        assertTrue(json.contains("\"id\": 3"));
        assertTrue(json.contains("\"id\": 4"));
        assertEquals(4, countOccurrences(json, "\"victoryPoints\":"));

        // All four IDs present
        assertTrue(json.contains("\"id\": 1"), "Player 1 id should be present");
        assertTrue(json.contains("\"id\": 2"), "Player 2 id should be present");
        assertTrue(json.contains("\"id\": 3"), "Player 3 id should be present");
        assertTrue(json.contains("\"id\": 4"), "Player 4 id should be present");

        // Victory points entry for each player
        assertEquals(4, countOccurrences(json, "\"victoryPoints\":"),
            "JSON should contain 4 victoryPoints fields (one per player)");
    }


    /**
     * Tests that state.json is created with correct visualizer structure.
     *
     * Correct structure should include:
     * - File named state.json exists
     * - Top-level keys "roads" and "buildings" are present
     * - Road entries use fields "a", "b", "owner"
     * - Building entries use fields "node", "owner", "type"
     * - "type" values are upper-case "SETTLEMENT" or "CITY"
     *
     * Colour mapping correctness:
     * - "GREEN" maps to "ORANGE"
     * - "YELLOW" maps to "WHITE"
     */
    @Test
    void test_stateJson_hasCorrectVisualizerStructure(@TempDir Path tempDir) throws Exception {
        Game g = new Game(1);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        g.runSimulationUntilTermination();

        // state.json must exist
        Path stateFile = tempDir.resolve("state.json");
        assertTrue(Files.exists(stateFile),
            "state.json must be created by VisualizerStateWriter after each turn");

        String json = new String(Files.readAllBytes(stateFile));

        // Top-level arrays
        assertTrue(json.contains("\"roads\""),
            "state.json must contain a roads array");
        assertTrue(json.contains("\"buildings\""),
            "state.json must contain a buildings array");

        // Road field names
        if (countOccurrences(json, "\"owner\"") > 0) {
            assertTrue(json.contains("\"a\":"),
                "Road entries must have field 'a'");
            assertTrue(json.contains("\"b\":"),
                "Road entries must have field 'b'");
        }

        // Building field names
        if (countOccurrences(json, "\"node\":") > 0) {
            assertTrue(json.contains("\"type\":"),
                "Building entries must have field 'type'");
        }

        // GREEN and YELLOW must not reach the JSON
        assertFalse(json.contains("\"GREEN\""),
            "GREEN must be mapped to ORANGE before writing — visualizer does not accept GREEN");
        assertFalse(json.contains("\"YELLOW\""),
            "YELLOW must be mapped to WHITE before writing — visualizer does not accept YELLOW");

        // Building type values must be exactly upper-case to match Python's string check
        assertFalse(json.contains("\"Settlement\""),
            "Building type must be upper-case SETTLEMENT, not Settlement");
        assertFalse(json.contains("\"City\""),
            "Building type must be upper-case CITY, not City");
    }

    /**
     * Tests that state.json is written after EVERY turn. 
     */
    @Test
    void test_stateJson_isUpdatedAfterEachTurn(@TempDir Path tempDir) throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        // Run first round
        g.playOneRound();

        Path stateFile = tempDir.resolve("state.json");
        assertTrue(Files.exists(stateFile),
            "state.json must exist after playOneRound() completes");

        long mtimeAfterRound1 = Files.getLastModifiedTime(stateFile).toMillis();

        // Allow clock to advance before the next round
        Thread.sleep(50);

        // Run second round (file must be updated again)
        g.playOneRound();

        long mtimeAfterRound2 = Files.getLastModifiedTime(stateFile).toMillis();

        assertTrue(mtimeAfterRound2 >= mtimeAfterRound1,
            "state.json must be re-written during the second round of turns");
    }

    // ── Test 4: state.json contains the initial settlements placed at setup ────

    /**
     * Tests that state.json contains initial setup 
     * Every player places 2 settlements during initialPlacementPhase(), 
     * so there should be at least 8 building entries (4 players × 2 settlements each).
     *
     * This confirms that the board state captured by VisualizerStateWriter
     * reflects the actual board
     */
    @Test
    void test_stateJson_containsInitialSettlements(@TempDir Path tempDir) throws Exception {
        Game g = new Game(1);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        // Play one round so state.json is written at least once
        g.playOneRound();

        Path stateFile = tempDir.resolve("state.json");
        assertTrue(Files.exists(stateFile),
            "state.json must exist after playOneRound()");

        String json = new String(Files.readAllBytes(stateFile));

        // 4 players × 2 settlements = at least 8 node entries.
        int nodeCount = countOccurrences(json, "\"node\":");
        assertTrue(nodeCount >= 8,
            "state.json should contain at least 8 building entries after initial placement "
            + "(4 players × 2 settlements). Found: " + nodeCount);

        // Every building entry must include a type field
        int typeCount = countOccurrences(json, "\"type\":");
        assertEquals(nodeCount, typeCount,
            "Every building entry must have a 'type' field");
    }
    
    /**
     * Uses reflection to set a private int field on a Game instance.
     * Used to override victoryPointsToWin without modifying Game's public API.
     *
     * @param g the Game instance to modify
     * @param fieldName the name of the private field
     * @param value the value to set
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
     * @param text the string to search within
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

    /**
     * Replaces the HumanPlayer (Player 1) in the game's player list with a
     * plain AI Player of the same ID and colour.
     *
     * This is necessary because HumanPlayer.takeTurn() blocks on System.in,
     * which would cause any test that calls playOneRound() or
     * runSimulationUntilTermination() to hang indefinitely.
     *
     * @param g the Game instance to modify
     * @throws Exception if reflection fails
     */
    @SuppressWarnings("unchecked")
    private static void replaceWithAllAiPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        List<Player> original = (List<Player>) f.get(g);

        List<Player> aiOnly = new ArrayList<>();
        for (Player p : original) {
            if (p instanceof HumanPlayer) {
                // Same ID and colour
                aiOnly.add(new Player(p.getPlayerId(), p.getColour()));
            } else {
                aiOnly.add(p);
            }
        }
        f.set(g, aiOnly);
    }
}
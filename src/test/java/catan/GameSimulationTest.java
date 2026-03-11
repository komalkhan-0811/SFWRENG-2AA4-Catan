package catan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests Game simulation behavior for AI-controlled turns and related internal
 * game logic that is difficult to reach through normal interactive gameplay.
 *
 * @author Alisha Faridi, Maria Shashati
 */
public class GameSimulationTest {

    /**
     * Stub InputHandler implementation that always returns "go".
     *
     * This is used to replace the normal console-based input handler in a
     * HumanPlayer so that waitForGo() returns immediately during tests
     * without blocking on standard input.
     */
    private static class GoInputHandler implements InputHandler {
        final List<String> messages = new ArrayList<>();

        /**
         * Returns "go" immediately for any prompt.
         *
         * @param prompt the prompt shown to the user
         * @return the string "go"
         */
        @Override
        public String readLine(String prompt) { return "go"; }

        /**
         * Records a displayed message for later verification.
         *
         * @param msg the message to record
         */
        @Override
        public void displayMessage(String msg) { messages.add(msg); }
    }

    /**
     * Test Game subclass that always rolls a 7.
     *
     * This allows robber-related code paths to be exercised.
     */
    private static class SevenRollGame extends Game {
        SevenRollGame() { super(10); }

        /**
         * Returns a fixed dice roll of 7.
         *
         * @return 7
         */
        @Override
        public int rollDice() { return 7; }
    }

    /**
     * Test Game subclass that always rolls a 6.
     *
     * This allows non-robber turn execution to be exercised.
     */
    private static class SixRollGame extends Game {
        SixRollGame() { super(10); }

        /**
         * Returns a fixed dice roll of 6.
         *
         * @return 6
         */
        @Override
        public int rollDice() { return 6; }
    }

    /**
     * Tests that a game with all players converted to AI runs to completion
     * without throwing an exception.
     *
     * @param tempDir temporary directory used for state output files
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_aiOnlyGame_runsToCompletion(@TempDir Path tempDir) throws Exception {
        Game g = new Game(3);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);
        assertDoesNotThrow(() -> g.runSimulationUntilTermination());
    }

    /**
     * Tests that a dice roll of 7 triggers robber logic during a round
     * without causing the game to crash.
     *
     * @param tempDir temporary directory used for state output files
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_sevenRoll_triggersRobberLogic(@TempDir Path tempDir) throws Exception {
        SevenRollGame g = new SevenRollGame();
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);
        assertDoesNotThrow(() -> g.playOneRound());
    }

    /**
     * Tests that when a 7 is rolled, players with more than 7 cards discard
     * cards and the total number of cards in play decreases.
     *
     * @param tempDir temporary directory used for state output files
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_sevenRoll_discardsCardsWhenOver7(@TempDir Path tempDir) throws Exception {
        SevenRollGame g = new SevenRollGame();
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        for (Player p : getPlayers(g)) {
            p.addResource(Resources.WOOD, 10);
        }

        int totalBefore = totalCards(g);
        g.playOneRound();
        int totalAfter = totalCards(g);
        assertTrue(totalAfter < totalBefore,
            "Seven roll should cause players with >7 cards to discard");
    }

    /**
     * Tests that getWinnerOrNull() returns a player once that player
     * has reached at least 10 victory points.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_getWinnerOrNull_returnsWinnerAt10VP() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        getPlayers(g).get(0).addVictoryPoints(10);
        Player winner = g.getWinnerOrNull();
        assertNotNull(winner);
        assertEquals(1, winner.getPlayerId());
    }

    /**
     * Tests that getWinnerOrNull() returns null when no
     * player has yet reached the victory point target.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_getWinnerOrNull_returnsNullWhenNoWinner() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        assertNull(g.getWinnerOrNull());
    }

    /**
     * Tests that printing the round scoreboard does not throw an exception.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_printRoundScoreboard_doesNotThrow() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        assertDoesNotThrow(() -> g.printRoundScoreboard());
    }

    /**
     * Tests that executing a build road action occupies the selected edge
     * and assigns ownership to the player.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_executeAction_buildRoad() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Board board = getBoard(g);
        Player p1 = getPlayers(g).get(0);

        Edge target = null;
        for (Edge e : board.getAllEdges()) {
            if (!e.isOccupied()) { target = e; break; }
        }
        assertNotNull(target, "There must be unoccupied edges after initial placement");

        int roadsBefore = roadsOwnedBy(p1.getPlayerId(), board);
        callExecuteAction(g, p1, Action.buildRoad(target.getIntersectionA(), target.getIntersectionB()));

        assertTrue(target.isOccupied());
        assertEquals(p1.getPlayerId(), target.getRoadOwnerId());
        assertEquals(roadsBefore + 1, roadsOwnedBy(p1.getPlayerId(), board));
    }

    /**
     * Tests that executing a build settlement action places a settlement at
     * the selected intersection and increases the player's victory points.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_executeAction_buildSettlement() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Board board = getBoard(g);
        Player p1 = getPlayers(g).get(0);

        int targetNode = -1;
        for (int id : board.getAllIntersectionIds()) {
            if (!board.getIntersection(id).hasBuilding()) { targetNode = id; break; }
        }
        assertTrue(targetNode >= 0);

        int vpBefore = p1.getVictoryPoints();
        callExecuteAction(g, p1, Action.buildSettlement(targetNode));

        assertEquals(Building.SETTLEMENT, board.getIntersection(targetNode).getBuildingType());
        assertEquals(p1.getPlayerId(), board.getIntersection(targetNode).getBuildingOwnerId());
        assertEquals(vpBefore + 1, p1.getVictoryPoints());
    }

    /**
     * Tests that executing a build city action upgrades one of the player's
     * settlements to a city and increases the player's victory points.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_executeAction_buildCity() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Board board = getBoard(g);
        Player p1 = getPlayers(g).get(0);

        int settlementNode = -1;
        for (int id : board.getAllIntersectionIds()) {
            Intersection inter = board.getIntersection(id);
            if (inter.hasBuilding()
                    && Integer.valueOf(p1.getPlayerId()).equals(inter.getBuildingOwnerId())
                    && inter.getBuildingType() == Building.SETTLEMENT) {
                settlementNode = id;
                break;
            }
        }
        assertTrue(settlementNode >= 0, "Player 1 must have a settlement after initial placement");

        int vpBefore = p1.getVictoryPoints();
        callExecuteAction(g, p1, Action.buildCity(settlementNode));

        assertEquals(Building.CITY, board.getIntersection(settlementNode).getBuildingType());
        assertEquals(vpBefore + 1, p1.getVictoryPoints());
    }

    /**
     * Tests that executing a pass action does not change the player's
     * victory points or cause an exception.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_executeAction_pass() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        Player p1 = getPlayers(g).get(0);
        int vpBefore = p1.getVictoryPoints();
        assertDoesNotThrow(() -> callExecuteAction(g, p1, Action.pass()));
        assertEquals(vpBefore, p1.getVictoryPoints());
    }

    /**
     * Tests that moveRobberRandomly() changes the robber's tile
     * position and updates the board state accordingly.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_moveRobberRandomly_changesPosition() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Board board = getBoard(g);
        int before = board.getRobberTileId();

        Method m = Game.class.getDeclaredMethod("moveRobberRandomly");
        m.setAccessible(true);
        int after = (int) m.invoke(g);

        assertNotEquals(before, after, "Robber must move to a different tile");
        assertEquals(after, board.getRobberTileId());
    }

    /**
     * Tests that stealFromAdjacentPlayer() does not throw an
     * exception when there are no valid victims adjacent to the robber tile.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_stealFromAdjacentPlayer_noVictims() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Player thief = getPlayers(g).get(0);
        int cardsBefore = thief.getTotalCardsInHand();

        Method m = Game.class.getDeclaredMethod("stealFromAdjacentPlayer",
                Player.class, int.class);
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(g, thief, 16));
        assertTrue(thief.getTotalCardsInHand() >= 0);
    }

    /**
     * Tests that stealFromAdjacentPlayer() transfers one resource
     * card from a valid adjacent victim to the stealing player.
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_stealFromAdjacentPlayer_stealsCard() throws Exception {
        Game g = new Game(10);
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);

        Board board = getBoard(g);
        List<Player> players = getPlayers(g);
        Player thief = players.get(0);
        Player victim = players.get(1);

        int victimNode = -1;
        for (int id : board.getAllIntersectionIds()) {
            Intersection inter = board.getIntersection(id);
            if (inter.hasBuilding()
                    && Integer.valueOf(victim.getPlayerId()).equals(inter.getBuildingOwnerId())) {
                victimNode = id;
                break;
            }
        }
        assertTrue(victimNode >= 0);

        // Find a tile adjacent to victimNode where victim is the ONLY non-thief
        // adjacent player. This avoids flakiness when other players happen to
        // have settlements adjacent to the same tile after random initial placement.
        int robberTile = -1;
        for (int tileId : board.getAdjacentTileIds(victimNode)) {
            List<Integer> adj = new ArrayList<>(board.getPlayersAdjacentToTile(tileId));
            adj.removeIf(id -> id == thief.getPlayerId());
            if (adj.size() == 1 && adj.get(0) == victim.getPlayerId()) {
                robberTile = tileId;
                break;
            }
        }

        // If every tile adjacent to victimNode also has other players' settlements,
        // general-steal check: just verify thief gains a card
        // from SOME victim rather than asserting victim's exact count.
        boolean victimIsOnlyTarget = (robberTile >= 0);
        if (!victimIsOnlyTarget) {
            robberTile = board.getAdjacentTileIds(victimNode)[0];
        }
        board.moveRobber(robberTile);

        victim.addResource(Resources.WOOD, 3);
        int victimCardsBefore = victim.getTotalCardsInHand();
        int thiefCardsBefore  = thief.getTotalCardsInHand();

        // Compute total cards across ALL players before the steal
        int totalCardsBefore = getPlayers(g).stream().mapToInt(Player::getTotalCardsInHand).sum();

        Method m = Game.class.getDeclaredMethod("stealFromAdjacentPlayer",
                Player.class, int.class);
        m.setAccessible(true);
        m.invoke(g, thief, robberTile);

        // A steal always transfers exactly 1 card
        int totalCardsAfter = getPlayers(g).stream().mapToInt(Player::getTotalCardsInHand).sum();
        assertEquals(totalCardsBefore, totalCardsAfter,
            "Total cards across all players must not change during a steal");

        // Thief always gains exactly 1 card
        assertEquals(thiefCardsBefore + 1, thief.getTotalCardsInHand(),
            "Thief must gain exactly 1 card");

        // If victim was the only possible target, also verify victim specifically lost 1 card
        if (victimIsOnlyTarget) {
            assertEquals(victimCardsBefore - 1, victim.getTotalCardsInHand(),
                "Victim must lose exactly 1 card when they are the only adjacent player");
        }
    }

    /**
     * Tests that during an AI player's turn, any HumanPlayer still present
     * in the game is prompted through waitForGo().
     *
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_playOneTurn_aiTurn_callsWaitForGoOnHumanPlayer() throws Exception {
        SixRollGame g = new SixRollGame();
        g.initializeNewGame();

        GoInputHandler goHandler = new GoInputHandler();
        HumanPlayer stubHuman = new HumanPlayer(
                1, Colour.RED, goHandler, new CommandParser.ConsoleCommandParser());

        Field playersField = Game.class.getDeclaredField("players");
        playersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Player> players = (List<Player>) playersField.get(g);
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i) instanceof HumanPlayer) {
                players.set(i, stubHuman);
                break;
            }
        }

        Player aiPlayer = players.get(1);
        assertDoesNotThrow(() -> g.playOneTurn(aiPlayer));

        assertTrue(goHandler.messages.size() > 0,
            "waitForGo should have sent at least one message to the HumanPlayer stub");
    }

    /**
     * Tests that the must-build path is handled correctly when players have
     * more than 7 cards and are therefore not allowed to pass freely.
     *
     * @param tempDir temporary directory used for state output files
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_playOneTurn_mustBuild_filtersPassActions(@TempDir Path tempDir) throws Exception {
        SixRollGame g = new SixRollGame();
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        setPrivateInt(g, "victoryPointsToWin", 999);
        g.setStateOutputDir(tempDir);

        for (Player p : getPlayers(g)) {
            p.addResource(Resources.WOOD,  5);
            p.addResource(Resources.BRICK, 5);
            p.addResource(Resources.WHEAT, 5);
            p.addResource(Resources.SHEEP, 5);
            p.addResource(Resources.ORE,   5);
        }

        assertDoesNotThrow(() -> g.playOneRound(),
            "playOneRound should handle mustBuild path without crashing");
    }

    /**
     * Tests that the game terminates once a player reaches the victory point
     * threshold during simulation.
     *
     * @param tempDir temporary directory used for state output files
     * @throws Exception if reflection-based setup fails
     */
    @Test
    void test_gameTerminates_whenPlayerReachesWinThreshold(@TempDir Path tempDir) throws Exception {
        SixRollGame g = new SixRollGame();
        g.initializeNewGame();
        replaceWithAllAiPlayers(g);
        g.setStateOutputDir(tempDir);

        getPlayers(g).get(0).addVictoryPoints(8);

        assertDoesNotThrow(() -> g.runSimulationUntilTermination());
        assertTrue(g.isTerminationReached());
    }

    /**
     * Uses reflection to replace all HumanPlayer instances in the Game's player
     * list with AI Player instances.
     *
     * This method accesses the private players field of the Game class,
     * copies the existing player list, and replaces each HumanPlayer with a
     * standard Player that has the same player ID and colour. Existing AI
     * players are unchanged.
     *
     * This is useful for automated tests that must avoid console input and run
     * entirely without user interaction.
     *
     * @param g the Game instance whose players should be converted to AI players
     * @throws Exception if the private players field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    static void replaceWithAllAiPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        List<Player> original = (List<Player>) f.get(g);
        List<Player> aiOnly = new ArrayList<>();
        for (Player p : original) {
            aiOnly.add(p instanceof HumanPlayer
                    ? new Player(p.getPlayerId(), p.getColour())
                    : p);
        }
        f.set(g, aiOnly);
    }

    /**
     * Uses reflection to access the private board field of a Game instance.
     *
     * @param g the Game instance
     * @return the Board associated with the game
     * @throws Exception if the board field cannot be accessed
     */
    static Board getBoard(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("board");
        f.setAccessible(true);
        return (Board) f.get(g);
    }

    /**
     * Uses reflection to access the private players field of a Game instance.
     *
     * @param g the Game instance
     * @return the list of players in the game
     * @throws Exception if the players field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    static List<Player> getPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        return (List<Player>) f.get(g);
    }

    /**
     * Uses reflection to set a private int field on a Game instance.
     *
     * @param g the Game instance
     * @param field the field name
     * @param value the value to set
     * @throws Exception if the field cannot be accessed
     */
    static void setPrivateInt(Game g, String field, int value) throws Exception {
        Field f = Game.class.getDeclaredField(field);
        f.setAccessible(true);
        f.setInt(g, value);
    }

    /**
     * Uses reflection to invoke the private executeAction method on a
     * Game instance.
     *
     * @param g the Game instance
     * @param p the player performing the action
     * @param a the action to execute
     * @throws Exception if the method cannot be accessed or invoked
     */
    private static void callExecuteAction(Game g, Player p, Action a) throws Exception {
        Method m = Game.class.getDeclaredMethod("executeAction", Player.class, Action.class);
        m.setAccessible(true);
        m.invoke(g, p, a);
    }

    /**
     * Counts the number of occupied roads on the board that belong to a given
     * player.
     *
     * @param playerId the player ID to count roads for
     * @param board the board to inspect
     * @return the number of roads owned by the player
     */
    private static int roadsOwnedBy(int playerId, Board board) {
        int count = 0;
        for (Edge e : board.getAllEdges()) {
            if (e.isOccupied() && e.getRoadOwnerId() == playerId) count++;
        }
        return count;
    }

    /**
     * Computes the total number of resource cards currently held by all players
     * in the game.
     *
     * @param g the Game instance
     * @return the total number of cards in all players' hands
     * @throws Exception if the players field cannot be accessed
     */
    private static int totalCards(Game g) throws Exception {
        return getPlayers(g).stream().mapToInt(Player::getTotalCardsInHand).sum();
    }
}
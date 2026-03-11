package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Tests HumanPlayer build-related command handling and other turn commands that
 * are processed during interactive turns.
 *
 * @author Rameen Tariq
 */
public class HumanPlayerBuildTest {

    /**
     * Stub InputHandler implementation that replays scripted input lines and
     * records displayed messages for later verification.
     */
    private static class StubInputHandler implements InputHandler {
        private final Queue<String> inputs;

        final List<String> messages = new ArrayList<>();

        /**
         * Creates a stub input handler with a fixed sequence of input lines.
         *
         * @param lines the scripted inputs to return one by one
         */
        StubInputHandler(String... lines) {
            inputs = new ArrayDeque<>(List.of(lines));
        }

        /**
         * Returns the next scripted input line, or "go" if no scripted
         * input remains.
         *
         * @param prompt the prompt shown to the user
         * @return the next scripted input line, or {@code "go"} if empty
         */
        @Override
        public String readLine(String prompt) {
            return inputs.isEmpty() ? "go" : inputs.poll();
        }

        /**
         * Records a displayed message for later inspection.
         *
         * @param msg the displayed message
         */
        @Override
        public void displayMessage(String msg) {
            messages.add(msg);
        }

        /**
         * Checks whether any recorded message contains a given substring.
         *
         * @param fragment the substring to search for
         * @return true if at least one message contains the fragment
         */
        boolean hasMessageContaining(String fragment) {
            return messages.stream().anyMatch(m -> m.contains(fragment));
        }
    }

    /**
     * Minimal Game subclass used to provide deterministic turn behavior during
     * tests.
     *
     * This stub always rolls a 6 and performs no resource distribution.
     */
    private static class StubGame extends Game {
        StubGame() { super(10); }

        /**
         * Returns a fixed dice roll of 6.
         *
         * @return 6
         */
        @Override
        public int rollDice() { return 6; }

        /**
         * Overrides resource distribution so that no resource changes occur
         * during tests.
         *
         * @param roll the dice roll value
         */
        @Override
        public void distributeResourcesForRoll(int roll) { /* no-op */ }
    }

    private StubGame game;
    private Board board;
    private Rules rules;

    /**
     * Initializes a fresh stub game, board, and rules object before each test.
     */
    @BeforeEach
    void setUp() {
        game  = new StubGame();
        board = new Board();
        board.initializeFixedMapLayout();
        board.initializeRobber();
        rules = new Rules();
    }

    /**
     * Tests that the LIST command displays the human player's current hand
     * contents during a turn.
     */
    @Test
    void test_listCommand_displaysHandContents() {
        StubInputHandler stub = new StubInputHandler("roll", "list", "go");
        HumanPlayer human = makeHuman(1, stub);
        human.addResource(Resources.WOOD, 2);
        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Hand —"));
    }

    /**
     * Tests that an unknown command produces an illegal command usage
     * message.
     */
    @Test
    void test_unknownCommand_showsUsageHint() {
        StubInputHandler stub = new StubInputHandler("roll", "attack the village", "go");
        HumanPlayer human = makeHuman(1, stub);
        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("[ILLEGAL] Unknown command."));
    }

    /**
     * Tests that attempting to roll a second time after already rolling is
     * illegal in the ROLLED state.
     */
    @Test
    void test_rollTwice_isIllegalInRolledState() {
        StubInputHandler stub = new StubInputHandler("roll", "roll", "go");
        HumanPlayer human = makeHuman(1, stub);
        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("[ILLEGAL in state ROLLED]"));
    }

    /**
     * Tests that attempting to use GO before rolling is illegal in the
     * START state.
     */
    @Test
    void test_goBeforeRoll_isIllegalInStartState() {
        StubInputHandler stub = new StubInputHandler("go", "roll", "go");
        HumanPlayer human = makeHuman(1, stub);
        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("[ILLEGAL in state START]"));
    }

    /**
     * Tests that attempting to build before rolling is illegal in the START
     * state.
     */
    @Test
    void test_buildBeforeRoll_isIllegalInStartState() {
        StubInputHandler stub = new StubInputHandler("build city 0", "roll", "go");
        HumanPlayer human = makeHuman(1, stub);
        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("[ILLEGAL in state START]"));
    }

    /**
     * Tests that building a settlement successfully places a settlement on
     * the board at the requested intersection.
     */
    @Test
    void test_buildSettlement_success_placesSettlementOnBoard() {
        StubInputHandler stub = new StubInputHandler("roll", "build settlement 0", "go");
        HumanPlayer human = makeHuman(1, stub);
        givSettlementResources(human);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));

        assertTrue(board.getIntersection(0).hasBuilding());
        assertEquals(Building.SETTLEMENT, board.getIntersection(0).getBuildingType());
        assertEquals(1, board.getIntersection(0).getBuildingOwnerId());
    }

    /**
     * Tests that attempting to build a settlement on an occupied
     * intersection fails and leaves the original owner unchanged.
     */
    @Test
    void test_buildSettlement_occupied_showsError() {
        board.placeSettlement(2, 0);
        StubInputHandler stub = new StubInputHandler("roll", "build settlement 0", "go");
        HumanPlayer human = makeHuman(1, stub);
        givSettlementResources(human);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Cannot build settlement"));
        assertEquals(2, board.getIntersection(0).getBuildingOwnerId());
    }

    /**
     * Tests that attempting to build a settlement without sufficient
     * resources shows an error and does not place a building.
     */
    @Test
    void test_buildSettlement_insufficientResources_showsError() {
        StubInputHandler stub = new StubInputHandler("roll", "build settlement 0", "go");
        HumanPlayer human = makeHuman(1, stub);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Not enough resources"));
        assertFalse(board.getIntersection(0).hasBuilding());
    }

    /**
     * Tests that building a city successfully upgrades the player's
     * settlement to a city.
     */
    @Test
    void test_buildCity_success_upgradesSettlement() {
        board.placeSettlement(1, 0);
        StubInputHandler stub = new StubInputHandler("roll", "build city 0", "go");
        HumanPlayer human = makeHuman(1, stub);
        human.recordPlacedSettlement(0);
        human.addResource(Resources.ORE,   3);
        human.addResource(Resources.WHEAT, 2);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertEquals(Building.CITY, board.getIntersection(0).getBuildingType());
    }

    /**
     * Tests that attempting to build a city where the player has no valid
     * settlement shows an error.
     */
    @Test
    void test_buildCity_noSettlementThere_showsError() {
        StubInputHandler stub = new StubInputHandler("roll", "build city 0", "go");
        HumanPlayer human = makeHuman(1, stub);
        human.addResource(Resources.ORE,   3);
        human.addResource(Resources.WHEAT, 2);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Cannot build city"));
    }

    /**
     * Tests that attempting to build a city without sufficient resources
     * shows an error and leaves the settlement unchanged.
     */
    @Test
    void test_buildCity_insufficientResources_showsError() {
        board.placeSettlement(1, 0);
        StubInputHandler stub = new StubInputHandler("roll", "build city 0", "go");
        HumanPlayer human = makeHuman(1, stub);
        human.recordPlacedSettlement(0);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Not enough resources"));
        assertEquals(Building.SETTLEMENT, board.getIntersection(0).getBuildingType());
    }

    /**
     * Tests that building a road successfully places a road on a valid
     * adjacent edge connected to the player's settlement.
     */
    @Test
    void test_buildRoad_success_placesRoadOnBoard() {
        board.placeSettlement(1, 0);
        Edge target = findEdgeTouching(0);
        assertNotNull(target);
        int a = target.getIntersectionA();
        int b = target.getIntersectionB();

        StubInputHandler stub = new StubInputHandler("roll", "build road " + a + " " + b, "go");
        HumanPlayer human = makeHuman(1, stub);
        human.recordPlacedSettlement(0);
        human.addResource(Resources.WOOD,  1);
        human.addResource(Resources.BRICK, 1);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(target.isOccupied(), "Road should be placed on the edge");
        assertEquals(1, target.getRoadOwnerId());
    }

    /**
     * Tests that attempting to build a road between two intersections that
     * do not share an edge shows an error.
     */
    @Test
    void test_buildRoad_noEdgeBetweenNodes_showsError() {
        StubInputHandler stub = new StubInputHandler("roll", "build road 0 53", "go");
        HumanPlayer human = makeHuman(1, stub);
        human.addResource(Resources.WOOD,  1);
        human.addResource(Resources.BRICK, 1);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("No edge exists between node"));
    }

    /**
     * Tests that attempting to build a road on an already occupied edge
     * shows an error and does not change ownership.
     */
    @Test
    void test_buildRoad_edgeAlreadyOccupied_showsError() {
        Edge target = findEdgeTouching(0);
        assertNotNull(target);
        board.placeRoad(2, target.getIntersectionA(), target.getIntersectionB());
        int a = target.getIntersectionA();
        int b = target.getIntersectionB();

        StubInputHandler stub = new StubInputHandler("roll", "build road " + a + " " + b, "go");
        HumanPlayer human = makeHuman(1, stub);
        human.addResource(Resources.WOOD,  1);
        human.addResource(Resources.BRICK, 1);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Cannot build road there"));
        assertEquals(2, target.getRoadOwnerId());
    }

    /**
     * Tests that attempting to build a road without sufficient resources
     * shows an error and does not occupy the edge.
     */
    @Test
    void test_buildRoad_insufficientResources_showsError() {
        board.placeSettlement(1, 0);
        Edge target = findEdgeTouching(0);
        assertNotNull(target);
        int a = target.getIntersectionA();
        int b = target.getIntersectionB();

        StubInputHandler stub = new StubInputHandler("roll", "build road " + a + " " + b, "go");
        HumanPlayer human = makeHuman(1, stub);
        human.recordPlacedSettlement(0);

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("Not enough resources"));
        assertFalse(target.isOccupied());
    }

    /**
     * Creates a HumanPlayer with a stub input handler and a console command
     * parser.
     *
     * @param id the player ID
     * @param stub the stub input handler to inject
     * @return a HumanPlayer configured for scripted test input
     */
    private HumanPlayer makeHuman(int id, StubInputHandler stub) {
        return new HumanPlayer(id, Colour.RED, stub, new CommandParser.ConsoleCommandParser());
    }

    /**
     * Gives a HumanPlayer the resources required to build one settlement.
     *
     * @param human the player to receive the resources
     */
    private void givSettlementResources(HumanPlayer human) {
        human.addResource(Resources.WOOD,  1);
        human.addResource(Resources.BRICK, 1);
        human.addResource(Resources.WHEAT, 1);
        human.addResource(Resources.SHEEP, 1);
    }

    /**
     * Finds the first unoccupied edge that touches a given intersection.
     *
     * @param nodeId the intersection ID to search from
     * @return an unoccupied adjacent edge, or null if none is found
     */
    private Edge findEdgeTouching(int nodeId) {
        for (Edge e : board.getAllEdges()) {
            if (e.touchesIntersection(nodeId) && !e.isOccupied()) return e;
        }
        return null;
    }
}
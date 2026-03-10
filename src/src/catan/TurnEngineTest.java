package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TurnState automaton enforcement in HumanPlayer.
 *
 * These tests use a stub InputHandler that replays a scripted sequence of
 * commands so no real console I/O is needed. 
 *
 * Test 1 – Illegal action rejection (BUILD before ROLL)
 *   Verifies that issuing a Build command while in the START state produces
 *   the "[ILLEGAL in state START]" rejection message and does NOT advance
 *   the automaton to DONE. The player is then forced to Roll and Go.
 *
 * Test 2 – Go-blocking (waitForGo rejects everything except "go")
 *   Verifies that waitForGo() keeps looping and re-prompts when the human
 *   types anything other than "go", and only returns once "go" is received.
 *
 * @author Rameen Tariq
 */

class TurnEngineTest {

	// Stub InputHandler 

    /**
     * A test InputHandler that:
     * adds pre-scripted lines in order using readLine()
     * and captures every displayMessage() call into a list
     *
     * This avoids any dependency on System.in / System.out and makes
     * tests deterministic and fast.
     */
    private static class StubInputHandler implements InputHandler {

        private final Queue<String> inputs;
        final List<String> messages = new ArrayList<>();

        StubInputHandler(String... lines) {
            inputs = new ArrayDeque<>(List.of(lines));
        }

        @Override
        public String readLine(String prompt) {
            if (inputs.isEmpty()) {
                // Safety check:  return "go" so the turn eventually ends instead of hanging the test thread
                return "go";
            }
            return inputs.poll();
        }

        @Override
        public void displayMessage(String message) {
            messages.add(message);
        }

        //Returns true if any captured message contains the given substring. 
        boolean hasMessageContaining(String fragment) {
            for (String m : messages) {
                if (m.contains(fragment)) return true;
            }
            return false;
        }
    }

    // StubGame — overrides rollDice() and distributeResourcesForRoll() 

    /**
     * Minimal Game subclass for testing.
     *
     * - rollDice() always returns 6 (no robber, deterministic).
     * - distributeResourcesForRoll() (no live board needed).
     * - The constructor does not call initializeNewGame(), so no ConsoleInputHandler is ever created and System.in is never read.
     */
    private static class StubGame extends Game {

        StubGame() {
            super(10);
            // Deliberately NOT calling initializeNewGame() because only rollDice() and distributeResourcesForRoll() in takeTurn() is needed
        }

        // Always returns 6 — avoids robber logic, fully deterministic.
        @Override
        public int rollDice() {
            return 6;
        }

        /**
         * no live board in this stub so resource so distribution is skipped. The test is focused on automaton, state transitions, not resource accounting.
         */
        @Override
        public void distributeResourcesForRoll(int roll) {
            // intentionally empty
        }
    }

    // Shared fixtures

    private StubGame game;
    private Board board;
    private Rules rules;

    @BeforeEach
    void setUp() {
        game  = new StubGame();
        board = new Board();
        board.initializeFixedMapLayout();
        board.initializeRobber();
        rules = new Rules();
    }

    // Test 1: BUILD before ROLL must be rejected by the automaton

    /**
     * Verifies that the automaton rejects BUILD commands issued in the START
     * state (before Roll).
     *
     * Script:
     *   1. "build settlement 5"  — illegal in START  → must be rejected
     *   2. "roll"                — legal in START    → transitions to ROLLED
     *   3. "go"                  — legal in ROLLED   → transitions to DONE
     *
     * Expected: the rejection message contains "[ILLEGAL in state START]"
     * and the turn still completes normally after the player rolls and goes.
     */
    @Test
    void test_buildBeforeRoll_isRejected() {
        StubInputHandler stub = new StubInputHandler(
            "build settlement 5",   // illegal – in START state
            "roll",                 // legal   – START → ROLLED
            "go"                    // legal   – ROLLED → DONE
        );

        HumanPlayer human = new HumanPlayer(1, Colour.RED, stub, new CommandParser.ConsoleCommandParser());

        assertDoesNotThrow(() -> human.takeTurn(1, game, board, rules));
        assertTrue(stub.hasMessageContaining("[ILLEGAL in state START]"));
    }

    // Test 2: waitForGo must block until exactly "go" is received

    /**
     * Verifies that waitForGo() keeps looping when the player types anything
     * other than "go", and returns (unblocks) exactly when "go" is received.
     *
     * Script fed to waitForGo():
     *   1. "roll"  -- not "go" -- must re-prompt
     *   2. "list"  -- not "go" -- must re-prompt
     *   3. "go"    -- accepted -- method must return
     *
     * Expected: the "Type 'Go' to proceed." re-prompt appears exactly twice.
     */
    @Test
    void test_waitForGo_blocksUntilGoReceived() {
        StubInputHandler stub = new StubInputHandler("roll", "list", "go");

        HumanPlayer human = new HumanPlayer(1, Colour.RED, stub, new CommandParser.ConsoleCommandParser());

        assertDoesNotThrow(() -> human.waitForGo("Computer player finished."));

        long repromptCount = stub.messages.stream().filter(m -> m.contains("Type 'Go' to proceed.")).count();
        assertEquals(2, repromptCount);
    }

}

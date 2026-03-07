package tests;
import catan.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

/**
 * These tests validate core Game Flow functionality
 * 
 * It covers:
 * Valid turn progression
 * Invalid turn progression
 * Resource distribution after dice roll
 * Edge case: no resource generation
 */

class GameFlowTest {
	
	private Game game;
	private Player player1;
	private Player player2;
	
	/**
	 * Initalizes a new game instance before each tests
	 * Creates 2 players and preps the game state
	 */

	@BeforeEach
	void setUp() {
		game = new Game(5);
		game.initalizeNewGame();
		
		//player1 = new HumanPlayer("P1");
		//player2 = new HumanPlayer("P2");
		
		//game.addPlayer(player1);
		//game.addPlayer(player2);
		//game.initialize();
	}
	
	/**
	 * Tests that a valid turn progresses correctly
	 * 
	 * Expected:
	 * Current player changes 
	 * Turn ID increments
	 * 
	 
	
	@Test
	void testValidTurnProgression() {
		
	

		 Player startingPlayer = game.getCurrentPlayer();
		int startingTurn = game.getTurnId();
		
		game.rollDice(6);
		game.nextTurn();

        assertNotEquals(startingPlayer, game.getCurrentPlayer());
        assertEquals(startingTurn + 1, game.getTurnId());
		 
		
		
	}
	*/
	
	@Test
    void testTerminationNotReached_afterInit() {
     
        assertFalse(game.isTerminationReached(),
            "Game should not be over immediately after initialization");
    }

	
	/**
	 * Tests that an invalid turn progression is prevented
	 * 
	 * If nextTurn() is called before rolling the dice, then the IllegalStateException is thrown
	 * 
	 * 
	 
	
	@Test
	void testInvalidTurnProgression() {
		
		boolean exceptionThrown = false;
		
		try {
			game.nextTurn();
		
		} catch (IllegalStateException e) {
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}

	*/
	
	
	@Test
    void testTerminationReached_whenRoundsExceeded() throws Exception {
     
        setPrivateInt(game, "roundNumber", 6); 
        assertTrue(game.isTerminationReached(),
            "Game should terminate when roundNumber exceeds maxRounds");
    }

	
	  @Test
	    void testTerminationReached_whenPlayerHits10VP() throws Exception {
	        
	        setPrivateInt(game, "roundNumber", 1);
	        getFirstPlayer().addVictoryPoints(10);
	        assertTrue(game.isTerminationReached(),
	            "Game should terminate when any player reaches 10 VP");
	    }

	
	/**
	 * Tests that resources are distributed correctly after a dice roll that matches a settlement
	 * The player resource count should increase by the correct amount
	 
	
	@Test
	void testResourceDistribution() {
		game.placeSettlement(player1, 5); // example node
		int before = player1.getTotalResources();
		
		game.rollDice(8);
		
		int after = player1.getTotalResources();
		
		
		assertEquals(before + 1, after);
	}
	*/
	  
	  @Test
	    void testRollSeven_noResourcesDistributed() {
	       
	        int totalBefore = getTotalResourcesAllPlayers();
	        assertDoesNotThrow(() -> game.distributeResourcesForRoll(7),
	            "distributeResourcesForRoll(7) should not throw");
	        int totalAfter = getTotalResourcesAllPlayers();
	        assertEquals(totalBefore, totalAfter,
	            "No resources should be distributed on a roll of 7");
	    }

	
	/**
	 * Test edge cases where no resources should be generates
	 * If a dice roll does not match any tile with settlements then the resource count should remain unchanged
	 * 
	 * 
	 
	@Test
	void testNoResourceGeneration() {
		
		int p1Before = player1.getTotalResources();
		int p2Before = player2.getTotalResources();
		
		game.rollDice(2); //assume no settlements produce on 2
		
		assertEquals(p1Before, player1.getTotalResources());
		assertEquals(p2Before, player2.getTotalResources());
		
	}
	*/
	  
	  
	  
	  
	    /**
	     * BOUNDARY TEST: Tests the exact boundary of the round limit.
	     * roundNumber == maxRounds (5)     → NOT over yet (condition is strictly greater than)
	     * roundNumber == maxRounds + 1 (6) → IS over
	     */
	    @Test
	    void testTermination_roundLimitBoundaryValues() throws Exception {
	        setPrivateInt(game, "victoryPointsToWin", 999); // prevent VP from triggering end

	        // At the boundary — equal to maxRounds, not yet over
	        setPrivateInt(game, "roundNumber", 5);
	        assertFalse(game.isTerminationReached(), "Round 5 of 5: should NOT be terminated");

	        // One past the boundary — now over
	        setPrivateInt(game, "roundNumber", 6);
	        assertTrue(game.isTerminationReached(), "Round 6 of 5: SHOULD be terminated");
	    }

	    /**
	     * PARTITION TEST: Dice values split into three partitions.
	     * Partition A: low valid rolls  (2–6)
	     * Partition B: high valid rolls (8–12)
	     * Partition C: robber roll      (7)
	     * All three should execute without throwing.
	     */
	    @Test
	    void testDiceRollDistribution_partitions() {
	        assertDoesNotThrow(() -> game.distributeResourcesForRoll(4),  "Partition A (low): roll 4");
	        assertDoesNotThrow(() -> game.distributeResourcesForRoll(10), "Partition B (high): roll 10");
	        assertDoesNotThrow(() -> game.distributeResourcesForRoll(7),  "Partition C (robber): roll 7");
	    }

	    // ---- Helpers ----

	    private void setPrivateInt(Game g, String fieldName, int value) throws Exception {
	        Field f = Game.class.getDeclaredField(fieldName);
	        f.setAccessible(true);
	        f.setInt(g, value);
	    }

	    private Player getFirstPlayer() throws Exception {
	        Field f = Game.class.getDeclaredField("players");
	        f.setAccessible(true);
	        // FIX: A previous version of this method incorrectly used f.get(g) where
	        // "g" was not a variable in scope. Corrected to f.get(game).
	        java.util.List<Player> players = (java.util.List<Player>) f.get(game);
	        return players.get(0);
	    }

	    private int getTotalResourcesAllPlayers() {
	        try {
	            Field f = Game.class.getDeclaredField("players");
	            f.setAccessible(true);
	            java.util.List<Player> players = (java.util.List<Player>) f.get(game);
	            int total = 0;
	            for (Player p : players) total += p.getTotalCardsInHand();
	            return total;
	        } catch (Exception e) {
	            return 0;
	        }
	    }


}

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		game = new Game(10); //max = 10 turns is an example
		player1 = new HumanPlayer("P1");
		player2 = new HumanPlayer("P2");
		
		game.addPlayer(player1);
		game.addPlayer(player2);
		game.initialize();
	}
	
	/**
	 * Tests that a valid turn progresses correctly
	 * 
	 * Expected:
	 * Current player changes 
	 * Turn ID increments
	 * 
	 */
	
	@Test
	void testValidTurnProgression() {
		
		Player startingPlayer = game.getCurrentPlayer();
		int startingTurn = game.getTurnId();
		
		game.rollDice(6);
		game.nextTurn();

        assertNotEquals(startingPlayer, game.getCurrentPlayer());
        assertEquals(startingTurn + 1, game.getTurnId());
		
	}
	
	/**
	 * Tests that an invalid turn progression is prevented
	 * 
	 * If nextTurn() is called before rolling the dice, then the IllegalStateException is thrown
	 * 
	 * 
	 */
	
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
	
	/**
	 * Tests that resources are distributed correctly after a dice roll that matches a settlement
	 * The player resource count should increase by the correct amount
	 */
	
	@Test
	void testResourceDistribution() {
		game.placeSettlement(player1, 5); // example node
		int before = player1.getTotalResources();
		
		game.rollDice(8);
		
		int after = player1.getTotalResources();
		
		
		assertEquals(before + 1, after);
	}
	
	/**
	 * Test edge cases where no resources should be generates
	 * If a dice roll does not match any tile with settlements then the resource count should remain unchanged
	 * 
	 * 
	 */
	@Test
	void testNoResourceGeneration() {
		
		int p1Before = player1.getTotalResources();
		int p2Before = player2.getTotalResources();
		
		game.rollDice(2); //assume no settlements produce on 2
		
		assertEquals(p1Before, player1.getTotalResources());
		assertEquals(p2Before, player2.getTotalResources());
		
	}

}

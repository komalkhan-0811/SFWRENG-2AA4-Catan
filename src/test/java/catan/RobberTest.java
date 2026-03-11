package catan;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Robber aspect
 * Tests covers:
 * - Exactly 7 cards
 * - more than 7 cards (discard half)
 * - no adjacent players, therefore no stealing
 * 
 */
public class RobberTest {
	
	
	private Player player;
	private Board board;
	
	
	@BeforeEach
	void setUp() {
		player = new Player(1, Colour.RED);
		board = new Board ();
		board.initializeFixedMapLayout();
		board.initializeRobber();
		
	}
	
	
	
	/**
	 * TEST 1: Exactly 7 cards -> NO DISCARDING 
	 * Test boundary condition: PLayer with exactly 7 cards should not discard
	 * 
	 */
	
	@Test
	void exactlySevenCardsNoDiscard() {
		
		player.addResource(Resources.WOOD, 3);
		player.addResource(Resources.BRICK, 2);
		player.addResource(Resources.WHEAT, 2);
		
		assertEquals(7, player.getTotalCardsInHand(), "Player should have exactly 7 cards ");
		
		int discarded = player.discardHalfCards();
		
		assertEquals(0, discarded, "No cards should be discared when player has 7 cards");
		
		assertEquals(7, player.getTotalCardsInHand(), "Player should still have 7 cards");
		
	}
	
	/**
	 * Test 2: More than 7 cards -> Discard half of the deck in hand
	 * 
	 */
	
	@Test
	void moreThanSevenCardsDiscard() {
		
		player.addResource(Resources.WOOD, 4);
		player.addResource(Resources.BRICK, 3);
		player.addResource(Resources.WHEAT, 2);
		player.addResource(Resources.SHEEP, 1);
		
		assertEquals(10, player.getTotalCardsInHand(), "Player should have 10 cards");
		
		int discarded = player.discardHalfCards();
		
		assertEquals(5, discarded, "Should discard exactly 5 cards");
		assertEquals(5, player.getTotalCardsInHand(), "Player shoudl have 5 cards remaining");
		
	
	}
	
	/**
	 * Test discard with odd number of cards
	 * 9 cards should discard 4 cards
	 */
	
	@Test
	void oddNumberCardsDiscard() {
		
		player.addResource(Resources.WOOD, 9);
		
		assertEquals(9, player.getTotalCardsInHand());
		
		int discarded = player.discardHalfCards();
		
		assertEquals(4, discarded, "Should discard 4 cards from 9");
		assertEquals(5, player.getTotalCardsInHand(), "Should have 5 cards left");
		
		
	}
	
	
	/**
	 * Test 3: No adjacent players -> No stealing takes place
	 */
	@Test
	void noAdjacentPLayersNoSteal() {
		
		board.moveRobber(0);
		List<Integer> adjacentPlayers = board.getPlayersAdjacentToTile(0);
		
		assertEquals(0, adjacentPlayers.size(), "No players should be adjacent to tile 0 initially");
		
		Player victim = new Player(2, Colour.BLUE);
		Resources stolen = victim.getRandomResource();
		
		assertNull(stolen, "Should not be able to steal from player with no cards");
		
		
		
	}
	
	
	/**
	 * 
	 * Edge case: test getRandomResource returns null when player has no cards
	 */
	@Test
	void stealFromEmptyHand() {
		
		assertEquals(0, player.getTotalCardsInHand(), "Player should start with 0 cards ");
		
		Resources stolen = player.getRandomResource();
		
		assertNull(stolen, "Cannot steal from player with no cards");
	}
	

}

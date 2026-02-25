package catan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Board and Building operations
 * The following tests are covering:
 * - Valid settlement placement
 * - Invalid settlement placement -> occupied nodes and invalid IDs
 * - Validation fo the road placement 
 * - Upgrading to city logic
 */

public class BoardBuildingTest{
	
	private Board board;
	
	//initializing the board
	@BeforeEach
	void setUp() {
		board = new Board();
		board.initializeFixedMapLayout();
		
	}
	
	
	/**
	 * TESTING FOR: valid settlement placement
	 * - verifies that a settlement is placed at a valid, empty intersection
	 */
	 
	@Test
	void validSettlementPlacement() {

		int playerId = 1;
		int intersectionId = 5;
		
		board.placeSettlement(playerId, intersectionId);
		
		Intersection inter = board.getIntersection(intersectionId);
		
		assertTrue(inter.hasBuilding(), "intersection should have building");
		assertEquals(Building.SETTLEMENT, inter.getBuilding(), "building should be a settlement");
		assetEquals(playerId, inter.getOwnerPlayerId(), "owner should be a player " + playerId);
	}
	
	/**
	 * TESTING FOR: invalid node id
	 * - validiating that attempts to place settlements at invaid intersection ids 
	 * does not crash and gets handled correctly
	 */
	@Test
	void invalidNodeIdTest() {
		
		//TESTING BOUNDARIES 0-53
		board.placeSettlement(1,-1);
		board.placeSettlement(1,54);
		board.placeSettlement(1,999);
		
		//checking that the invalid ids return null and doesnt crash
		Intersection invalid1 = board.getIntersection(-1);
		Intersection invalid2 = board.getIntersection(54);
		Intersection invalid3 = board.getIntersection(999);
		
		assertNull(invalid1, "invalid negative Id");
		assertNull(invalid2, "invalid id above range");
		assertNull(invalid3, "invalid large Id");
		

	}
	
	/**
	 * TESTING FOR: occupied nodes
	 * - verifies that attempting to place a settlement on an already occupied
	 * intersection does not overwrite the existing building
	 * 
	 */
	@Test
	void occupiedNodeTest() {

		int intersectionId = 3;
		
		//This is player 1 for example, placing settlement first then player 2 attempts on the same spot
		board.placeSettlement(1, intersectionId);
		board.placeSettlement(2, intersectionId);
		
		Intersection inter = board.getIntersection(intersectionId);
		
		assertEquals(1, inter.getOwnerPlayerId(), "intersection should still belong to P1");
		assertEquals(Building.SETTLEMENT, inter.getBuilding(),"Building should still be a settlement" );
		
		
	}
	
	/**
	 *TESTING FOR: upgrading to city logic
	 * - checking whether a settlement can be upgraded to a city and building type and owner stays the same
	 */
	@Test
	void cityUpgradeLogic() {

		int playerId = 1;
		int intersectionId = 6;
		
		
		board.placeSettlement(1, intersectionId);
		
		//upgrading to city
		board.upgradeSettlementToCity(1,intersectionId);
		
		Intersection inter = board.getIntersection(intersectionId);
		
		assertEquals(Building.CITY, inter.getBuilding(), "building should be upgraded to city");
		assertEquals(1, inter.getOwnerPlayerId());
		
	}
	
	/**
	 * 
	 * TESTING FOR: valid road placement
	 * - verifies that a road is correctly placed and if overwritten the existing road and owner doesnt change
	 */
	@Test
	void validRoadPlacement() {
	
		int player1 = 1;
		int player2 = 2;
		int intersectionA = 2;
		int intersectionB = 3;
		
		//Player 1 is placing road
		board.placeRoad(player1, intersectionA, intersectionB);
		
		//finding the edge
		Edge targetEdge = null;
		
		for (Edge e : board.getAllEdges()) {
			if((e.getIntersectionA() == intersectionA && e.getIntersectionB()== intersectionB) || 
					(e.getIntersectionA() == intersectionB && e.getIntersectionB() == intersectionA)) {
				targetEdge = e;
				break;
			}
		}
		
		assertNotNull(targetEdge, "edge should exist in the board");
		assertTrue(targetEdge.isOccupied(), "Edge should be occupied");
		assertEquals(1, targetEdge.getRoadOwnerId(), "road should belong to player 1");
		
		//trying to overwrite
		board.placeRoad(player2, intersectionA, intersectionB);
		assertEquals(player1, targetEdge.getRoadOwnerId(), "road should still belong to player 1");
	}
	
}
package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoadGapConstraintTest {

	private RoadGapConstraint constraint;
    private Player player;
    private Board board;
    
    @BeforeEach
    void setUp() {
        constraint = new RoadGapConstraint();
        player = new Player(1, Colour.RED);
        board = new Board();
    }
    
    @Test
    void test_constraintActive_twoSegments_roadConnects() {
    	
        // Create two disconnected segments: (1,2) and (3,4)
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 3, 4);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(3, 4);
        
        // Road 2 and 3 connects the segments
        Action action = Action.buildRoad(2, 3);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(150.0, value, "Should return 150.0 when road connects segments");
    }
    
    
    @Test
    void test_constraintActive_multipleSegments_roadConnectsTwoOfThem() {
    	
        // Create three segments
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 3, 4);
        board.placeRoad(player.getPlayerId(), 10, 11);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(3, 4);
        player.recordPlacedRoad(10, 11);
        
        // Road 2 and 3 connects first two segments
        Action action = Action.buildRoad(2, 3);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(150.0, value, "Should return 150.0 when connecting any two segments");
    }
    
    
   
    
    
    @Test
    void test_constraintInactive_noSegments() {
    	
        
        Action action = Action.buildRoad(1, 2);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should return -1.0 when no segments exist");
    }
    
    
    @Test
    void test_constraintInactive_roadDoesntConnectSegments() {
    	
        // Two segments
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 10, 11);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(10, 11);
        
        // Road 5 and 6 doesn't connect either segment
        Action action = Action.buildRoad(5, 6);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should return -1.0 when road doesn't connect segments");
    }
    
    @Test
    void test_nonRoadAction_settlement_returnsNegative() {
    	
        // Even with segments, settlement doesn't trigger
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 3, 4);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(3, 4);
        
        Action action = Action.buildSettlement(5);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Settlement action should never trigger constraint");
    }
    
    
    @Test
    void test_nonRoadAction_pass_returnsNegative() {
    	
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 3, 4);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(3, 4);
        
        Action action = Action.pass();
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Pass action should never trigger constraint");
    }
    
    @Test
    void test_singleRoadInEachSegment() {
    	
        // Edge case: two single road segments
        board.placeRoad(player.getPlayerId(), 1, 2);
        board.placeRoad(player.getPlayerId(), 4, 5);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(4, 5);
        
        // Connecting them
        Action action = Action.buildRoad(2, 4);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(150.0, value, "Should work with single-road segments");
    }
    
    
    
    
    
    
}

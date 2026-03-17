package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LongestRoadTest {
	
	
	private LongestRoadDefense constraint;
    private Player player;
    private Player opponent;
    private Board board;
    
    @BeforeEach
    void setUp() {
        constraint = new LongestRoadDefense();
        player = new Player(1, Colour.RED);
        opponent = new Player(2, Colour.BLUE);
        board = new Board();
    }
	
    
    @Test
    void test_constraintActive_playerHas7_opponentHas6_roadExtends() {
    	
        // Player has longest road of 7
        setupRoadChain(player, 1, 8);
        
        // Opponent has 6 roads
        setupRoadChain(opponent, 20, 26);
        
        // Player extends their road
        Action action = Action.buildRoad(8, 9);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(120.0, value, "Should return 120.0 when defending against close opponent");
    }
    
    @Test
    void test_constraintActive_playerHas5_opponentHas4_roadExtends() {
    	
        // Boundary: exactly 5 roads
        setupRoadChain(player, 1, 6);  // 5 roads
        
        // Opponent 1 behind
        setupRoadChain(opponent, 20, 24);  // 4 roads
        
        // Player extends
        Action action = Action.buildRoad(6, 7);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(120.0, value, "Should trigger at exactly 5 roads");
    }
    
    @Test
    void test_constraintActive_opponentExactlyOneBehind() {
    	
   
        setupRoadChain(player, 1, 9);
        
 
        setupRoadChain(opponent, 20, 27);
        
        Action action = Action.buildRoad(9, 10);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(120.0, value, "Should trigger when opponent exactly 1 behind");
    }
    
    
    @Test
    void test_constraintActive_playerHas10_opponentHas9_roadExtends() {
    	
        // Large longest roads
        setupRoadChain(player, 1, 11);  // 10 roads
        setupRoadChain(opponent, 20, 29);  // 9 roads
        
        Action action = Action.buildRoad(11, 12);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(120.0, value, "Should work with very long roads");
    }
    
    @Test
    void test_constraintInactive_playerHas7_opponentHas5_tooFarBehind() {
     
        setupRoadChain(player, 1, 8);
        
        //More than 1 behind
        setupRoadChain(opponent, 20, 25);
        
        Action action = Action.buildRoad(8, 9);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should not trigger when opponent >1 behind");
    }
    
    
    @Test
    void test_constraintInactive_playerHas6_noOpponentRoads() {
    	

        setupRoadChain(player, 1, 7);
        
        // No opponents have roads
        Action action = Action.buildRoad(7, 8);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should not trigger with no opponents");
    }
    
    
    void test_nonRoadAction_settlement_returnsNegative() {
    	
        setupRoadChain(player, 1, 8);
        setupRoadChain(opponent, 20, 26);
        
        Action action = Action.buildSettlement(5);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Settlement should never trigger constraint");
    }
    
    
    
    @Test
    void test_nonRoadAction_pass_returnsNegative() {
    	
        setupRoadChain(player, 1, 8);
        setupRoadChain(opponent, 20, 26);
        
        Action action = Action.pass();
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Pass should never trigger constraint");
    }
    
    
    
    
    @Test
    void test_constraintInactive_playerHas4Roads_belowThreshold() {
    	
        // Player only has 4 roads (< 5 minimum)
        setupRoadChain(player, 1, 5);  // 4 roads
        
        // Even if opponent has 3
        setupRoadChain(opponent, 20, 23);  // 3 roads
        
        Action action = Action.buildRoad(5, 6);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should not trigger below 5 roads");
    }
    
    
    
    @Test
    void test_multipleOpponents_anyOneBehind_triggersConstraint() {
        Player opponent2 = new Player(3, Colour.GREEN);
        
  
        setupRoadChain(player, 1, 8);
        
        //3 roads (far behind)
        setupRoadChain(opponent, 20, 23);
        
        //6 roads (1 behind therefore threat)
        setupRoadChain(opponent2, 30, 36);
        
        Action action = Action.buildRoad(8, 9);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(120.0, value, "Should trigger if ANY opponent is 1 behind");
    }
    
    
    
    /**
     * Creates a connected chain of roads from start to end
     * 
     */
    private void setupRoadChain(Player p, int start, int end) {
        for (int i = start; i < end; i++) {
            board.placeRoad(p.getPlayerId(), i, i + 1);
            p.recordPlacedRoad(i, i + 1);
            
            // Also place a settlement at each intersection to make it valid on board
            board.placeSettlement(p.getPlayerId(), i);
            p.recordPlacedSettlement(i);
        }
        
        // Place final settlement
        board.placeSettlement(p.getPlayerId(), end);
        p.recordPlacedSettlement(end);
    }
    
    
}

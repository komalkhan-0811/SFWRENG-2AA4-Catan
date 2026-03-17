package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SevenCardConstrainTest {
	
	private SevenCardConstraint constraint;
    private Player player;
    private Board board;
    
    
    @BeforeEach
    void setUp() {
        constraint = new SevenCardConstraint();
        player = new Player(1, Colour.RED);
        board = new Board();
    }
    
    @Test
    void test_constraintActive_withExactly8Cards_buildRoad() {
        // Player has exactly 8 cards (>7)
        player.addResource(Resources.WOOD, 4);
        player.addResource(Resources.BRICK, 4);
        
        Action action = Action.buildRoad(1, 2);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(100.0, value, "Should return 100.0 when player has 8 cards");
    }
    
    @Test
    void test_constraintActive_with10Cards_buildSettlement() {
        // Player has 10 cards (>7)
        player.addResource(Resources.WOOD, 3);
        player.addResource(Resources.BRICK, 3);
        player.addResource(Resources.WHEAT, 2);
        player.addResource(Resources.SHEEP, 2);
        
        Action action = Action.buildSettlement(5);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(100.0, value, "Should return 100.0 when player has 10 cards");
    }
    
    
    
    @Test
    void test_constraintActive_with15Cards_buildCity() {
        // Player has 15 cards (>7)
        player.addResource(Resources.WHEAT, 7);
        player.addResource(Resources.ORE, 8);
        
        Action action = Action.buildCity(10);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(100.0, value, "Should return 100.0 when player has 15 cards");
    }
    
    
    @Test
    void test_constraintInactive_withExactly7Cards() {
        // Player has exactly 7 cards (not >7)
        player.addResource(Resources.WOOD, 2);
        player.addResource(Resources.BRICK, 2);
        player.addResource(Resources.WHEAT, 2);
        player.addResource(Resources.SHEEP, 1);
        
        Action action = Action.buildRoad(1, 2);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should return -1.0 when player has exactly 7 cards");
    }

    @Test
    void test_constraintInactive_with0Cards() {
        // Player has no cards
        Action action = Action.buildRoad(1, 2);
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "Should return -1.0 when player has no cards");
    }
    
    @Test
    void test_passAction_alwaysReturnsNegative() {
    	
        // Even with >7 cards, PASS should not trigger constraint
        player.addResource(Resources.WOOD, 10);
        
        Action action = Action.pass();
        
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(-1.0, value, "PASS action should never trigger constraint");
    }
    
    
    @Test
    void test_mixedResources_totalOver7() {
    	
        // Test with diverse resource mix
        player.addResource(Resources.WOOD, 2);
        player.addResource(Resources.BRICK, 1);
        player.addResource(Resources.WHEAT, 2);
        player.addResource(Resources.SHEEP, 2);
        player.addResource(Resources.ORE, 2);
      
        
        Action action = Action.buildRoad(1, 2);
        double value = constraint.evaluate(player, board, action);
        
        assertEquals(100.0, value, "Mixed resources totaling 9 should trigger constraint");
    }
    
    
    
}

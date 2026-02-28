package tests;

import catan.Player;
import catan.Resources;
import catan.Colour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerCardLogicTest {

    private Player player;
    
    
    // Initializing a new Player before each test

    @BeforeEach
    void setup() {
        player = new Player(1, Colour.BLUE);
    }

    // Test 1 tests that adding two resource cards correctly updates the player's total card count
    @Test
    void test1_add2() {

        assertEquals(0, player.getTotalCardsInHand());

        player.addResource(Resources.WOOD, 1);
        player.addResource(Resources.BRICK, 1);

        assertEquals(2, player.getTotalCardsInHand());
    }

    // Test 2 tests that payCost removes the correct amount of resources when the player has enough to afford the cost
    @Test
    void test2_cardRemoval_AfterPayment() {

        player.addResource(Resources.WHEAT, 2);

        Map<Resources, Integer> cost = new HashMap<>();
        cost.put(Resources.WHEAT, 1);

        player.payCost(cost);

        assertEquals(1, player.getTotalCardsInHand());
    }

    // Test 3 tests that payCost throws an exception when the player does not have enough resources to cover the cost
    @Test
    void test3_cardRemovalInsufficient() {

        Map<Resources, Integer> cost = new HashMap<>();
        cost.put(Resources.SHEEP, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            player.payCost(cost);
        });
    }

    // Tests the robber discard rule using boundary value analysis and partition testing
    // Verifies behavior for card counts below 7, exactly 7 (boundary), and above 7
    @Test
    void test4_robberBoundary() {

        // less than 7 cards
        Player p6 = new Player(2, Colour.RED);
        p6.addResource(Resources.WOOD, 6);
        assertEquals(0, robberDiscardAmount(p6));

        // exactly 7 
        Player p7 = new Player(3, Colour.GREEN);
        p7.addResource(Resources.WOOD, 7);
        assertEquals(0, robberDiscardAmount(p7));

        // greater than 7 
        Player p8 = new Player(4, Colour.YELLOW);
        p8.addResource(Resources.WOOD, 8);
        assertEquals(4, robberDiscardAmount(p8));
    }

    // Temporary helper to model robber discard logic for testing purposes
    // replace later when robber logic implemented
    private int robberDiscardAmount(Player p) {
        int total = p.getTotalCardsInHand();
        if (total <= 7) return 0;
        return total / 2;
    }
}
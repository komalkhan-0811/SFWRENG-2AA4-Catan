package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoadNetworkAnalyzerTest {

    private RoadNetworkAnalyzer analyzer;
    private Player player;
    private Board board;

    void setUp() {
        analyzer = new RoadNetworkAnalyzer();
        player = new Player(1, Colour.RED);
        board = new Board();
    }

    @Test
    void testCalculateLongestRoad_noRoads_returnsZero() {
        assertEquals(0, analyzer.calculateLongestRoad(player, board));
    }

    @Test
    void testCalculateLongestRoad_chainOf3() {
        player.recordPlacedRoad(0, 1);
        player.recordPlacedRoad(1, 2);
        player.recordPlacedRoad(2, 3);
        assertEquals(3, analyzer.calculateLongestRoad(player, board));
    }

    @Test
    void testExtendsLongestRoad_noExistingRoads_returnsTrue() {
        Action action = Action.buildRoad(0, 1);
        assertTrue(analyzer.extendsLongestRoad(player, board, action));
    }

    @Test
    void testExtendsLongestRoad_connectedRoad_returnsTrue() {
        player.recordPlacedRoad(0, 1);
        player.recordPlacedRoad(1, 2);
        // 2-3 touches existing road at 2
        Action action = Action.buildRoad(2, 3);
        assertTrue(analyzer.extendsLongestRoad(player, board, action));
    }

    @Test
    void testExtendsLongestRoad_disconnectedRoad_returnsFalse() {
        player.recordPlacedRoad(0, 1);
        // 10-11 does not touch 0 or 1
        Action action = Action.buildRoad(10, 11);
        assertFalse(analyzer.extendsLongestRoad(player, board, action));
    }

    @Test
    void testExtendsLongestRoad_nonRoadAction_returnsFalse() {
        Action action = Action.buildSettlement(5);
        assertFalse(analyzer.extendsLongestRoad(player, board, action));
    }

    @Test
    void testWouldConnectNearbySegments_nonRoadAction_returnsFalse() {
        Action action = Action.pass();
        assertFalse(analyzer.wouldConnectNearbySegments(player, board, action));
    }

    @Test
    void testWouldConnectNearbySegments_fewerThanTwoRoads_returnsFalse() {
        player.recordPlacedRoad(0, 1);
        Action action = Action.buildRoad(1, 2);
        assertFalse(analyzer.wouldConnectNearbySegments(player, board, action));
    }

    @Test
    void testWouldConnectNearbySegments_bridgesGap_returnsTrue() {
        // Two disconnected segments: 0-1 and 3-4
        player.recordPlacedRoad(0, 1);
        player.recordPlacedRoad(3, 4);

        // Use 0 and 3 as endpoints of the bridging road
        Action action = Action.buildRoad(0, 3);
        
        // 0 touches segment 0-1; 3 touches segment 3-4 — they're not connected
        assertTrue(analyzer.wouldConnectNearbySegments(player, board, action));
    }

    @Test
    void testWouldConnectNearbySegments_alreadyConnected_returnsFalse() {
        // Single connected chain: 0-1-2
        player.recordPlacedRoad(0, 1);
        player.recordPlacedRoad(1, 2);
        // Road 0-2 touches already-connected segments
        Action action = Action.buildRoad(0, 2);
        assertFalse(analyzer.wouldConnectNearbySegments(player, board, action));
    }

    @Test
    void testWouldConnectNearbySegments_onlyOneSideHasRoads_returnsFalse() {
        player.recordPlacedRoad(0, 1);
        player.recordPlacedRoad(1, 2);
        
        // Road 2-10: endpoint 2 has roads, endpoint 10 has none
        Action action = Action.buildRoad(2, 10);
        assertFalse(analyzer.wouldConnectNearbySegments(player, board, action));
    }
}
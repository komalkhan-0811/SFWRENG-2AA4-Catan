package catan;

import java.util.*;

/**
 * A helper class used in RoadGapConstraint class to help figure out the neary connecting road segments if available
 * A "calculation" class for the possible road constraints/scenarios
 * 
 * @author Komal Khan
 */
public class RoadNetworkAnalyzer {

    /**
     * Checks if a road would connect two currently-disconnected road groups.
     * A road connects segments if it touches roads and both endpoints that aren't already connected to each other.
     *
     * @param player the player whose roads to analyze
     * @param board the current board state
     * @param action the road action to evaluate
     * @return true if this road connects disconnected roads
     */
    public boolean wouldConnectNearbySegments(Player player, Board board, Action action) {
        if (action.getType() != ActionType.BUILD_ROAD){
            return false;
        }
        Set<String> playerRoads = getPlayerRoads(player, board);

        //Need to have atleast 2
        if (playerRoads.size() < 2){
            return false;
        }

        int a = action.getEdgeIntersectionA();
        int b = action.getEdgeIntersectionB();

        Set<String> roadsAtA = getRoadsTouchingIntersection(a, playerRoads);
        Set<String> roadsAtB = getRoadsTouchingIntersection(b, playerRoads);

        // If both endpoints have roads and those roads aren't connected, this road bridges a gap
        if (!roadsAtA.isEmpty() && !roadsAtB.isEmpty()) {
            for (String roadA : roadsAtA) {
                for (String roadB : roadsAtB) {
                    if (areRoadsConnected(roadA, roadB, playerRoads)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates the longest road
     * Instead of finding the exact longest path, this counts the maximum
     * number of connected roads from any starting point.
     *
     * @param player the player to analyze
     * @param board the current board state
     * @return approximate longest connected road count
     */
    public int calculateLongestRoad(Player player, Board board) {
        Set<String> playerRoads = getPlayerRoads(player, board);
        if (playerRoads.isEmpty()){
            return 0;
        }
        int maxConnected = 0;

        // Starting from each road and count how many roads are connected
        for (String startRoad : playerRoads) {
            int connected = countConnectedRoads(startRoad, playerRoads);
            maxConnected = Math.max(maxConnected, connected);
        }
        return maxConnected;
    }

    /**
     * Checks if a road would extend or connect to the existing road network.
     *
     * @param player the player considering the road
     * @param board the current board state
     * @param action the road action to evaluate
     * @return true if this road connects to existing network
     */
    public boolean extendsLongestRoad(Player player, Board board, Action action) {
        if (action.getType() != ActionType.BUILD_ROAD){
            return false;
        }

        Set<String> playerRoads = getPlayerRoads(player, board);
        if (playerRoads.isEmpty()){
            return true;
        }

        int a = action.getEdgeIntersectionA();
        int b = action.getEdgeIntersectionB();

        // Check if new road connects to existing network at either endpoint
        Set<String> roadsAtA = getRoadsTouchingIntersection(a, playerRoads);
        Set<String> roadsAtB = getRoadsTouchingIntersection(b, playerRoads);

        // Extends if it touches existing roads at either end
        return !roadsAtA.isEmpty() || !roadsAtB.isEmpty();
    }


    /**
     * Gets all road edge keys owned by a player.
     * Uses the player's own record (ownedRoadEdgeKeys) as the correct source.
     *
     * @param player
     * @param board 
     * @return the set of road edge keys for this player
     */
    private Set<String> getPlayerRoads(Player player, Board board) {
        Set<String> fromPlayer = player.getOwnedRoadEdgeKeys();
        if (!fromPlayer.isEmpty()) {
            return new HashSet<>(fromPlayer);
        }
        Set<String> roads = new HashSet<>();
        for (Edge edge : board.getAllEdges()) {
            if (edge.isOccupied() && edge.getRoadOwnerId() == player.getPlayerId()) {
                roads.add(edgeKey(edge.getIntersectionA(), edge.getIntersectionB()));
            }
        }
        return roads;
    }

    /**
     * Finds all roads that touch a given intersection.
     */
    private Set<String> getRoadsTouchingIntersection(int intersection, Set<String> allRoads) {
        Set<String> touching = new HashSet<>();
        for (String road : allRoads) {
            int[] endpoints = parseEdgeKey(road);
            if (endpoints[0] == intersection || endpoints[1] == intersection) {
                touching.add(road);
            }
        }
        return touching;
    }

    /**
     * Checks if two roads are connected (share an intersection or can reach each other).
     */
    private boolean areRoadsConnected(String road1, String road2, Set<String> allRoads) {
        if (road1.equals(road2)){
            return true;
        }

        int[] endpoints1 = parseEdgeKey(road1);
        int[] endpoints2 = parseEdgeKey(road2);

        // If there is a direct connection that means roads share an intersection
        if (endpoints1[0] == endpoints2[0] || endpoints1[0] == endpoints2[1] ||
                endpoints1[1] == endpoints2[0] || endpoints1[1] == endpoints2[1]) {
            return true;
        }

        // If thers is only one way connection that means there exists a road connecting them
        for (String bridgeRoad : allRoads) {
            if (bridgeRoad.equals(road1) || bridgeRoad.equals(road2)) continue;

            int[] bridgeEndpoints = parseEdgeKey(bridgeRoad);
            boolean touchesRoad1 = (bridgeEndpoints[0] == endpoints1[0] || bridgeEndpoints[0] == endpoints1[1] || bridgeEndpoints[1] == endpoints1[0] || bridgeEndpoints[1] == endpoints1[1]);
            boolean touchesRoad2 = (bridgeEndpoints[0] == endpoints2[0] || bridgeEndpoints[0] == endpoints2[1] || bridgeEndpoints[1] == endpoints2[0] || bridgeEndpoints[1] == endpoints2[1]);

            if (touchesRoad1 && touchesRoad2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts how many roads are connected to a starting road.
     *
     */
    private int countConnectedRoads(String startRoad, Set<String> allRoads) {
        Set<String> connected = new HashSet<>();
        Queue<String> toExplore = new LinkedList<>();

        toExplore.add(startRoad);
        connected.add(startRoad);

        // Iteratively find all connected roads
        while (!toExplore.isEmpty()) {
            String currentRoad = toExplore.poll();
            int[] currentEndpoints = parseEdgeKey(currentRoad);

            // Find all roads that share an intersection with current road
            for (String otherRoad : allRoads) {
                if (connected.contains(otherRoad)){
                    continue;
                }

                int[] otherEndpoints = parseEdgeKey(otherRoad);

                // Check if they share an intersection
                if (currentEndpoints[0] == otherEndpoints[0] || currentEndpoints[0] == otherEndpoints[1] || currentEndpoints[1] == otherEndpoints[0] || currentEndpoints[1] == otherEndpoints[1]) {
                    connected.add(otherRoad);
                    toExplore.add(otherRoad);
                }
            }
        }

        return connected.size();
    }

    /**
     * Creates normalized edge key (smaller ID first).
     */
    private String edgeKey(int a, int b) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return min + "-" + max;
    }

    /**
     * Parses edge key string into two intersection IDs.
     */
    private int[] parseEdgeKey(String key) {
        String[] parts = key.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}
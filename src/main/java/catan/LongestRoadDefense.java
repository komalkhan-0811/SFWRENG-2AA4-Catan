package catan;
import java.util.List;

/**
 *
 * Constraint checker for R3.3: If other players have a longest road that is at most one road shorter
 * than the agents, the agent should buy a connected road segement
 *
 * @author Komal Khan
 */
public class LongestRoadDefense {

    private final RoadNetworkAnalyzer analyzer;

    public LongestRoadDefense(){
        this.analyzer = new RoadNetworkAnalyzer();
    }


    /**
     * evaluates if this road defends the players 1 longest road advantage
     * @param player
     * @param board
     * @param action
     * @return
     * 120.0 if:
     * - Action is BUILD_ROAD and Player has longest road >= 5 and opponent is only 1 road behind and road extends the players longest path
     *
     * otherwise returns -1.0
     */
    public double evaluate(Player player, Board board, Action action){
        if (action.getType() != ActionType.BUILD_ROAD){
            return -1.0;
        }

        //calculate players current longest road
        int myLongestRoad = analyzer.calculateLongestRoad(player, board);

        //need atleast 5 road to be considered as a threat/competition
        if (myLongestRoad < 5){
            return -1.0;
        }

        boolean underThreat = isLongestRoadUnderThreat(player, board, myLongestRoad);

        if (!underThreat){
            return -1.0;
        }

        //check if this road extends longest path
        boolean extendsPath = analyzer.extendsLongestRoad(player, board, action);

        if (extendsPath){
            return 120.0;
        }

        return -1.0;
    }

    /**
     * checks if any opponents longest road is at most 1 shorter
     * @param player
     * @param board
     * @param myLongestRoad
     * @return
     */

    private boolean isLongestRoadUnderThreat(Player player, Board board, int myLongestRoad) {
        List<Integer> allPlayerIds = getAllPlayerIds(board);

        for (int opponentId : allPlayerIds) {
            if (opponentId == player.getPlayerId()) continue;

            Player opponent = createPlayerProxy(opponentId);
            int opponentLongest = analyzer.calculateLongestRoad(opponent, board);

            // Opponent is at most 1 road behind
            if (opponentLongest >= myLongestRoad - 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * gets all player IDS from the board
     * @param board
     * @return
     */
    private List<Integer> getAllPlayerIds(Board board) {
        java.util.Set<Integer> playerIds = new java.util.HashSet<>();

        // Collect from buildings
        for (int id : board.getAllIntersectionIds()) {
            Intersection inter = board.getIntersection(id);
            if (inter != null && inter.hasBuilding()) {
                Integer ownerId = inter.getBuildingOwnerId();
                if (ownerId != null) {
                    playerIds.add(ownerId);
                }
            }
        }

        // Collect from roads
        for (Edge edge : board.getAllEdges()) {
            if (edge.isOccupied()) {
                playerIds.add(edge.getRoadOwnerId());
            }
        }

        return new java.util.ArrayList<>(playerIds);
    }

    /**
     * Creates a minimal player proxy for road analysis.
     */
    private Player createPlayerProxy(int playerId) {
        return new Player(playerId, Colour.RED);
    }

}

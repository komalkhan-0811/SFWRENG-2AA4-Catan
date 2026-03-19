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
     * Evaluates if this road defends the player's 1 longest road advantage
     * @param player
     * @param board
     * @param action
     * @return
     * 120.0 if:
     *  Action is BUILD_ROAD and Player has longest road >= 5 and opponent is only 1 road behind and road extends the players longest path
     *
     * otherwise returns -1.0
     */
    public double evaluate(Player player, List<Player> allPlayers, Board board, Action action) {
    	
        if (action.getType() != ActionType.BUILD_ROAD) {
        	return -1.0;
        }

        int myLongestRoad = analyzer.calculateLongestRoad(player, board);
        if (myLongestRoad < 5) {
        	return -1.0;
        }

        for (Player opponent : allPlayers) {
            if (opponent.getPlayerId() == player.getPlayerId()) {
            	continue;
            }
            int opponentLongest = analyzer.calculateLongestRoad(opponent, board);
            if (opponentLongest >= myLongestRoad - 1 && opponentLongest <= myLongestRoad) {
                return analyzer.extendsLongestRoad(player, board, action) ? 120.0 : -1.0;
            }
        }
        return -1.0;
    }

    // Fallback for callers that don't pass a player list
    public double evaluate(Player player, Board board, Action action) {
        return evaluate(player, java.util.Collections.emptyList(), board, action);
    }

}

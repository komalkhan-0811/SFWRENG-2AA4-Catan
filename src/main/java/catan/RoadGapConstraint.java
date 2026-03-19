package catan;

/**
 *
 * Constraint Checker for R3.3: If there exists two road segements that are
 * max 2 units away then the agents should try to buy roads to connect segements
 *
 * This class analyzes the players road network and returns high priority value like 150.0 for roads that would connect
 *
 * @author Komal Khan
 */
public class RoadGapConstraint {
    private final RoadNetworkAnalyzer analyzer;

    public RoadGapConstraint(){
        this.analyzer = new RoadNetworkAnalyzer();
    }


    /**
     * evaluates if this road would connect nearby road segements
     *
     * @param player
     * @param board
     * @param action
     * @return
     * 150.0 if:
     *  Action is BUILD ROAD and road connects two segemetns that are close together
     * otherwise returns -1.0;
     */
    public double evaluate(Player player, Board board, Action action){
        if (action.getType() != ActionType.BUILD_ROAD){
            return -1.0;
        }

        //check if this road connect nearby segements
        boolean connectsSegments = analyzer.wouldConnectNearbySegments(player, board, action);

        if (connectsSegments){
            return 150.0;
        }

        return -1.0;
    }


}

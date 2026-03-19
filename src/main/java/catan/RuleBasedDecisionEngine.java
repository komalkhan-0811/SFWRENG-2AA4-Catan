package catan;

import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.SystemFlavorMap;
import java.security.SecureRandom;
/**
 * Evaluates all legal actions using a list of Rule objects and chooses
 * the highest value action.
 *
 * If multiple actions tie for the highest value, one is chosen randomly.
 *
 * This supports R3.2 by replacing random legal-action selection with
 * value based rule evaluation.
 *
 * @author Maria Shashati
 */

@SuppressWarnings("java:S106")
public class RuleBasedDecisionEngine {

    private final List<Rule> rules;
    private final SecureRandom random;


    //R3.3 Constraint checkers
    private final SevenCardConstraint cardConstraint;
    private final RoadGapConstraint gapConstraint;
    private final LongestRoadDefense longestRoad;

    /**
     * Constructs a decision engine with all supported evaluation rules.
     */
    public RuleBasedDecisionEngine() {
        this.rules = new ArrayList<>();
        this.random = new SecureRandom();


        //initializign constraint checkers
        this.cardConstraint = new SevenCardConstraint();
        this.gapConstraint = new RoadGapConstraint();
        this.longestRoad = new LongestRoadDefense();


        rules.add(new BuildCityRule());
        rules.add(new BuildSettlementRule());
        rules.add(new BuildRoadRule());
        rules.add(new PassRule());
    }

    /**
     * Chooses the best action from the list of legal actions.
     *
     * @param player the player taking the turn
     * @param board the current board state
     * @param actions the legal actions available to the player
     * @return the highest-value action, with random tie-breaking
     */
    public Action chooseBestAction(Player player, Board board, List<Action> actions) {
        if (actions == null || actions.isEmpty()) {
            return Action.pass();
        }

        double bestValue = -1.0;
        List<Action> bestActions = new ArrayList<>();

        for (Action action : actions) {
            double value = evaluateAction(player, board, action);

            // temporary line im just seeing if ai is acctually doing anyhting 
            //System.out.println("AI evaluating: " + action.getType() + " value=" + value);

            if (value > bestValue) {
                bestValue = value;
                bestActions.clear();
                bestActions.add(action);
            }
            else if (value == bestValue) {
                bestActions.add(action);
            }
        }

        Action chosen = bestActions.get(random.nextInt(bestActions.size()));


        if (bestValue >= 100.0){
           displayConstraintMessage(player, board, chosen, bestValue);
        }
        return chosen;

    }
    
    /**
     * Displays a detailed message explaining which constraint was triggered
     * @param player
     * @param board
     * @param action
     * @return
     */
    private void displayConstraintMessage(Player player, Board board, Action action, double value) {
    	if(Math.abs(value - 150.0) < 0.01) {
    		System.out.println("--> ROAD GAP CONSTRAINT ACTIVE: Connecting the disconneted road segements");
    	}
    	else if(Math.abs(value - 120.0) < 0.01){
    		System.out.println("--> LONGEST ROAD DEFENSE ACTIVE: Defending against close opponent");
    	}
    	else if(Math.abs(value - 100.0) < 0.01) {
    		int cardCount = player.getTotalCardsInHand();
    		System.out.println("--> SEVEN CARD CONSTRAINT ACTIVE: Player has " + cardCount + " cards, must build");
    	}
    	else {
    		System.out.println("--> CONSTRAINT ACTIVE: Resolving with priority " + value);
    	}
    }

    private double evaluateAction(Player player, Board board, Action action){
        double value = -1.0;

        value = Math.max(value, cardConstraint.evaluate(player, board, action));
        value = Math.max(value, gapConstraint.evaluate(player, board, action));
        value = Math.max(value, longestRoad.evaluate(player, java.util.Collections.emptyList(), board, action));

        if (value >= 100.0){
            return value;
        }


        for (Rule rule: rules){
            double ruleValue = rule.evaluate(player, board, action);
            value = Math.max(value, ruleValue);
        }

        return value;
    }

    private String getActionLocation(Action action){
        switch (action.getType()){
            case BUILD_ROAD:
                return "(" + action.getEdgeIntersectionA() + "-" + action.getEdgeIntersectionB() + ")";
            case BUILD_SETTLEMENT:
            case BUILD_CITY:
                return "[" + action.getIntersectionId() + "]";
            case PASS:
            default:
                return "";

        }
    }
}
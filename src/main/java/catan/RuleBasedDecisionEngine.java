package catan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Evaluates all legal actions using a list of Rule objects and chooses
 * the highest-value action.
 *
 * If multiple actions tie for the highest value, one is chosen randomly.
 *
 * This supports R3.2 by replacing random legal-action selection with
 * value-based rule evaluation.
 *
 * @author Maria Shashati
 */
public class RuleBasedDecisionEngine {

    private final List<Rule> rules;
    private final Random random;

    /**
     * Constructs a decision engine with all supported evaluation rules.
     */
    public RuleBasedDecisionEngine() {
        this.rules = new ArrayList<>();
        this.random = new Random();

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
            double value = -1.0;

            for (Rule rule : rules) {
                value = Math.max(value, rule.evaluate(player, board, action));
            }

            // temporary line im just seeing if ai is acctually doing anyhting 
            System.out.println("AI evaluating: " + action.getType() + " value=" + value);

            if (value > bestValue) {
                bestValue = value;
                bestActions.clear();
                bestActions.add(action);
            } else if (value == bestValue) {
                bestActions.add(action);
            }
        }

        return bestActions.get(random.nextInt(bestActions.size()));
    }
}
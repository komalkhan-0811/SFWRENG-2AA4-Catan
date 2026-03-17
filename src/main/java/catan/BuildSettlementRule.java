package catan;

/**
 * Rule that evaluates build-settlement actions.
 *
 * A settlement earns a victory point immediately, so its value is 1.0.
 *
 * @author Maria Shashati
 */
public class BuildSettlementRule extends Rule {

    /**
     * Returns whether this rule applies to the given action.
     *
     * @param action the candidate action
     * @return true if the action is BUILD_SETTLEMENT
     */
    @Override
    protected boolean isApplicable(Action action) {
        return action.getType() == ActionType.BUILD_SETTLEMENT;
    }

    /**
     * Computes the value of a settlement-building action.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return 1.0 because building a settlement earns a VP immediately
     */
    @Override
    protected double computeValue(Player player, Board board, Action action) {
        return 1.0;
    }
}
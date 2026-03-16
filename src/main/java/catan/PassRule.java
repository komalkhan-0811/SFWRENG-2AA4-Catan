package catan;

/**
 * Rule that evaluates pass actions.
 *
 * Passing provides no immediate benefit, so its value is 0.0.
 *
 * @author Maria Shashati
 */
public class PassRule extends Rule {

    /**
     * Returns whether this rule applies to the given action.
     *
     * @param action the candidate action
     * @return true if the action is PASS
     */
    @Override
    protected boolean isApplicable(Action action) {
        return action.getType() == ActionType.PASS;
    }

    /**
     * Computes the value of a pass action.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return 0.0 because passing provides no immediate value
     */
    @Override
    protected double computeValue(Player player, Board board, Action action) {
        return 0.0;
    }
}
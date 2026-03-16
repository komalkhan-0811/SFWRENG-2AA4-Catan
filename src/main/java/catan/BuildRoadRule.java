package catan;

/**
 * Rule that evaluates build road actions
 *
 * Building a road does not earn a VP immediately, but it is still valuable, so its value is 0.8
 *
 * @author Maria Shashati
 */
public class BuildRoadRule extends Rule {

    /**
     * Returns whether this rule applies to the given action.
     *
     * @param action the candidate action
     * @return true if the action is BUILD_ROAD
     */
    @Override
    protected boolean isApplicable(Action action) {
        return action.getType() == ActionType.BUILD_ROAD;
    }

    /**
     * Computes the value of a road-building action.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return 0.8 because building a road is valuable but does not earn a VP immediately
     */
    @Override
    protected double computeValue(Player player, Board board, Action action) {
        return 0.8;
    }
}
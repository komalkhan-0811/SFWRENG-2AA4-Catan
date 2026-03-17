package catan;

/**
 * Rule that evaluates build-city actions.
 *
 * A city earns a victory point immediately, so its value is 1.0.
 *
 * @author Maria Shashati
 */
public class BuildCityRule extends Rule {

    /**
     * Returns whether this rule applies to the given action.
     *
     * @param action the candidate action
     * @return true if the action is BUILD_CITY
     */
    @Override
    protected boolean isApplicable(Action action) {
        return action.getType() == ActionType.BUILD_CITY;
    }

    /**
     * Computes the value of a city-building action.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return 1.0 because building a city earns a VP immediately
     */
    @Override
    protected double computeValue(Player player, Board board, Action action) {
        return 1.0;
    }
}
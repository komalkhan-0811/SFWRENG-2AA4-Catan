package catan;

import java.util.Map;

/**
 * Abstract rule used by the Template Method pattern for AI action evaluation.
 *
 * The template method is evaluate(...):
 * 1. Check whether this rule applies to the action
 * 2. Compute the base value of the action
 * 3. Apply the R3.2 bonus rule if spending cards leaves < 5 cards in hand
 *
 * Concrete subclasses override the variable steps.
 *
 * @author Maria Shashati
 */
public abstract class Rule {

    /**
     * Template method for evaluating an action.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return the value of the action for this rule, or -1.0 if not applicable
     */
    public double evaluate(Player player, Board board, Action action) {

        if (!isApplicable(action)) {
            return -1.0;
        }

        double value = computeValue(player, board, action);
        value = applySpendingBonus(player, action, value);

        return value;
    }

    /**
     * Returns whether this rule applies to the given action.
     *
     * @param action the candidate action
     * @return true if this rule should evaluate the action
     */
    protected abstract boolean isApplicable(Action action);

    /**
     * Computes the base value of the given action under this rule.
     *
     * @param player the player whose action is being evaluated
     * @param board the current board state
     * @param action the candidate action
     * @return the value assigned to this action
     */
    protected abstract double computeValue(Player player, Board board, Action action);

    /**
     * Applies the R3.2 bonus rule:
     * If executing the action spends cards and leaves the player
     * with fewer than 5 cards in hand, the value becomes at least 0.5.
     *
     * @param player the player taking the action
     * @param action the candidate action
     * @param currentValue the action's current base value
     * @return the adjusted value after applying the spending bonus rule
     */
    private double applySpendingBonus(Player player, Action action, double currentValue) {

        if (spendsResourcesLeavingUnderFive(player, action)) {
            return Math.max(currentValue, 0.5);
        }

        return currentValue;
    }

    /**
     * Checks whether executing the action would leave the player with
     * fewer than 5 cards in hand after paying its cost.
     *
     * @param player the player taking the action
     * @param action the candidate action
     * @return true if remaining cards would be less than 5
     */
    private boolean spendsResourcesLeavingUnderFive(Player player, Action action) {

        Rules rules = new Rules();
        Map<Resources, Integer> cost = rules.getCost(action.getType());

        if (cost == null || cost.isEmpty()) {
            return false;
        }

        int totalCost = 0;

        for (Integer amount : cost.values()) {
            totalCost += amount;
        }

        int remainingCards = player.getTotalCardsInHand() - totalCost;

        return remainingCards < 5;
    }
}
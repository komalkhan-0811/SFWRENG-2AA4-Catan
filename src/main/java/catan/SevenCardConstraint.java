package catan;

/**
 *
 * Constraint checker for R3.3: If there are more than 7 cards the agent my spend them
 *
 * This class checks if the player has > 7 cards and returns a high priority value (100.0)
 * for builidng actions and to force spending
 *
 * @author Komal Khan
 */
public class SevenCardConstraint {

    /**
     * Constraint active returns a value 100.0, meaning PLayer has more than 7 cards and
     * Action is a building action
     *
     * @param player
     * @param board
     * @param action
     * @return 100.0 if constraint is active otherwise -1.0
     */
    public double evaluate(Player player, Board board, Action action){
        ActionType type = action.getType();
        if (type!= ActionType.BUILD_ROAD && type!=ActionType.BUILD_SETTLEMENT && type!= ActionType.BUILD_CITY){
            return -1.0;
        }

        //checking if the constraint is active
        int cardCount = player.getTotalCardsInHand();
        if (cardCount > 7){
            //this means its high priority therefore need to spend cards
            return 100.0;
        }

        return -1.0;

    }
}


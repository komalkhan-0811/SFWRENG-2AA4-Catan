package catan;

/**
 * Concrete Command that encapsulates the action of upgrading a settlement to a city at a specified intersection on the board.
 *
 * Implements the Command interface as part of the Command Pattern (R3.1).
 * Supports undo by downgrading the city back to a settlement and reversing
 * the victory point gain.
 *
 * @author Rameen Tariq
 */

public class BuildCityCommand implements Command{
	
	//The game board on which the city is upgraded
    private final Board board;

    //The player who is upgrading the settlement to a city
    private final Player player;

    //The intersection ID where the city is going to be built
    private final int intersectionId;

    /**
     * Constructs a BuildCityCommand with the given board, player,
     * and target intersection.
     *
     * @param board          the game board on which to upgrade the settlement
     * @param player         the player upgrading to a city
     * @param intersectionId the ID of the intersection where the city is built
     */
    public BuildCityCommand(Board board, Player player, int intersectionId) {
        this.board = board;
        this.player = player;
        this.intersectionId = intersectionId;
    }

    /**
     * Executes the command by upgrading the settlement at the specified
     * intersection to a city and giving the player a victory point.
     */
    @Override
    public void execute() {
        board.upgradeSettlementToCity(player.getPlayerId(), intersectionId);
        player.addVictoryPoints(1);
    }

    /**
     * Undoes the command by downgrading the city back to a settlement and
     * deducting the awarded victory point from the player
     */
    @Override
    public void undo() {
        board.downgradeCityToSettlement(intersectionId);
        player.addVictoryPoints(-1);
    }

    /**
     * Returns a description of this command including the target intersection
     *
     * @return a string describing the build city action
     */
    @Override
    public String getDescription() {
        return "Build city at " + intersectionId;
    }

}

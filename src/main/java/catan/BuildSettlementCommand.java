package catan;

/**
 * Encapsulates the action of building a settlement at a specified intersection on the board.
 *
 * Implements the Command interface as part of the Command Pattern (R3.1).
 * Supports undo by removing the placed settlement and reversing the associated victory point gain
 *
 * @author Rameen Tariq
 */

public class BuildSettlementCommand implements Command {
	
    private final Board board;

    private final Player player;

    private final int intersectionId;
    
    /**
     * Constructs a BuildSettlementCommand with the given board, player, and target intersection.
     *
     * @param board: the game board on which to place the settlement
     * @param player: the player placing the settlement
     * @param intersectionId the ID of the intersection where the settlement is placed
     */
    public BuildSettlementCommand(Board board, Player player, int intersectionId) {
        this.board = board;
        this.player = player;
        this.intersectionId = intersectionId;
    }

    /**
     * Executes the command by placing a settlement on the board at the
     * specified intersection and giving player 1 victory point
     */
    @Override
    public void execute() {
        board.placeSettlement(player.getPlayerId(), intersectionId);
        player.addVictoryPoints(1);
    }

    /**
     * Undoes the command by removing the settlement from the board and
     * deducting the given victory point from the player
     */
    @Override
    public void undo() {
        board.removeSettlement(intersectionId);
        player.addVictoryPoints(-1);
    }

    /**
     * Returns a description of this command including the target intersection.
     *
     * @return a string describing the build settlement action
     */
    @Override
    public String getDescription() {
        return "Build settlement at " + intersectionId;
    }

}
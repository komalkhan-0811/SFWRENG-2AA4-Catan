package catan;

/**
 * encapsulates the action of building a road
 * between two intersections on the board
 *
 * Implements the Command interface as part of the Command Pattern (R3.1).
 * Supports undo by removing the placed road from the edge between the two intersections
 *
 * @author Rameen Tariq
 */

public class BuildRoadCommand implements Command {
	
	private final Board board;

    private final Player player;

    private final int intersectionA;

    private final int intersectionB;
    
    /**
     * Constructs a BuildRoadCommand with the given board, player, and the two intersections that define the road's edge.
     *
     * @param board: the game board to place the road
     * @param player: the player placing the road
     * @param a: the ID of the first intersection
     * @param b: the ID of the second intersection
     */
    public BuildRoadCommand(Board board, Player player, int a, int b) {
        this.board = board;
        this.player = player;
        this.intersectionA = a;
        this.intersectionB = b;
    }

    /**
     * Executes the command by placing a road on the edge between the two intersections.
     */
    @Override
    public void execute() {
        board.placeRoad(player.getPlayerId(), intersectionA, intersectionB);
    }

    /**
     * Undoes the command by removing the road from the edge between the two intersections
     */
    @Override
    public void undo() {
        board.removeRoad(intersectionA, intersectionB);
    }

    /**
     * Returns a description of this command including the two intersections that define the road's edge.
     *
     * @return a string describing the build road action
     */
    @Override
    public String getDescription() {
        return "Build road between " + intersectionA + " and " + intersectionB;
    }

}

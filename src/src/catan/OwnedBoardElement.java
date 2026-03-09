package catan;

/**
 * Interface for any board element that can be owned by a player.
 * 
 * Any future ownable board element just implements
 * this interface.
 * 
 * @author Alisha Faridi
 * 
 */
public interface OwnedBoardElement {

    /**
     * Returns the ID of the player who owns this element.
     * Returns -1 if no player owns it.
     *
     * @return owner's player ID, or -1 if unowned
     */
    int getOwnerId();

    /**
     * Returns whether this element is currently owned by any player.
     *
     * @return true if owned, false if free
     */
    boolean isOccupied();
}
public class Tile {
    private final int tileId;
    private final Resource resource;
    private final int diceNumberToken;

    public Tile(int tileId, Resource resource, int diceNumberToken) {
        this.tileId = tileId;
        this.resource = resource;
        this.diceNumberToken = diceNumberToken;
    }

    public int getTileId() { return tileId; }
    public Resource getResource() { return resource; }
    public int getDiceNumberToken() { return diceNumberToken; }

    public boolean producesOnRoll(int roll) {
        return resource != Resource.DESERT && diceNumberToken == roll;
    }
}

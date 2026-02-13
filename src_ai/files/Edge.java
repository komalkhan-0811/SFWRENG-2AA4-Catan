public class Edge {
    private final int intersectionA;
    private final int intersectionB;
    private Integer roadOwnerId;

    public Edge(int intersectionA, int intersectionB) {
        this.intersectionA = Math.min(intersectionA, intersectionB);
        this.intersectionB = Math.max(intersectionA, intersectionB);
        this.roadOwnerId = null;
    }

    public int getIntersectionA() { return intersectionA; }
    public int getIntersectionB() { return intersectionB; }

    public boolean isOccupied() { return roadOwnerId != null; }

    public Integer getRoadOwnerId() { return roadOwnerId; }

    public void placeRoad(int playerId) { this.roadOwnerId = playerId; }

    public boolean touchesIntersection(int intersectionId) {
        return intersectionA == intersectionId || intersectionB == intersectionId;
    }
}

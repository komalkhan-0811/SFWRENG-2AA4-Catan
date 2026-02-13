public class Intersection {
    private final int intersectionId;
    private Integer buildingOwnerId;
    private Building building;

    public Intersection(int intersectionId) {
        this.intersectionId = intersectionId;
        this.buildingOwnerId = null;
        this.building = null;
    }

    public int getIntersectionId() { return intersectionId; }

    public boolean isEmpty() { return building == null; }

    public Building getBuilding() { return building; }

    public Integer getBuildingOwnerId() { return buildingOwnerId; }

    public void placeSettlement(int playerId) {
        this.buildingOwnerId = playerId;
        this.building = Building.SETTLEMENT;
    }

    public void upgradeToCity(int playerId) {
        this.buildingOwnerId = playerId;
        this.building = Building.CITY;
    }
}

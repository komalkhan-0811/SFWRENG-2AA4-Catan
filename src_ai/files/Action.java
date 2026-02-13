public class Action {
    private final ActionType type;
    private final int intersectionId;
    private final int edgeIntersectionA;
    private final int edgeIntersectionB;

    public Action(ActionType type, int intersectionId, int edgeIntersectionA, int edgeIntersectionB) {
        this.type = type;
        this.intersectionId = intersectionId;
        this.edgeIntersectionA = edgeIntersectionA;
        this.edgeIntersectionB = edgeIntersectionB;
    }

    public ActionType getType() { return type; }
    public int getIntersectionId() { return intersectionId; }
    public int getEdgeIntersectionA() { return edgeIntersectionA; }
    public int getEdgeIntersectionB() { return edgeIntersectionB; }

    public String describeForLogin() {
        switch (type) {
            case BUILD_ROAD:
                return "BUILD_ROAD on edge (" + edgeIntersectionA + " - " + edgeIntersectionB + ")";
            case BUILD_SETTLEMENT:
                return "BUILD_SETTLEMENT at intersection " + intersectionId;
            case BUILD_CITY:
                return "BUILD_CITY at intersection " + intersectionId;
            case PASS:
                return "PASS";
            default:
                return "UNKNOWN ACTION";
        }
    }
}

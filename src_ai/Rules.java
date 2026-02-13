import java.util.*;

public class Rules {
    private final Map<ActionType, Map<Resource, Integer>> actionCosts;

    public Rules() {
        actionCosts = new EnumMap<>(ActionType.class);

        Map<Resource, Integer> roadCost = new EnumMap<>(Resource.class);
        roadCost.put(Resource.BRICK, 1);
        roadCost.put(Resource.WOOD, 1);
        actionCosts.put(ActionType.BUILD_ROAD, roadCost);

        Map<Resource, Integer> settlementCost = new EnumMap<>(Resource.class);
        settlementCost.put(Resource.BRICK, 1);
        settlementCost.put(Resource.WOOD, 1);
        settlementCost.put(Resource.WHEAT, 1);
        settlementCost.put(Resource.SHEEP, 1);
        actionCosts.put(ActionType.BUILD_SETTLEMENT, settlementCost);

        Map<Resource, Integer> cityCost = new EnumMap<>(Resource.class);
        cityCost.put(Resource.WHEAT, 2);
        cityCost.put(Resource.ORE, 3);
        actionCosts.put(ActionType.BUILD_CITY, cityCost);

        actionCosts.put(ActionType.PASS, new EnumMap<>(Resource.class));
    }

    public boolean canBuildRoad(Player player, Board board, Edge edge) {
        if (edge == null || edge.isOccupied()) return false;
        if (!player.hasEnoughResources(actionCosts.get(ActionType.BUILD_ROAD))) return false;
        return isRoadConnectedToPlayerNetwork(player, board, edge);
    }

    public boolean canBuildSettlement(Player player, Board board, int intersectionId) {
        if (board.isIntersectionOccupied(intersectionId)) return false;
        if (!player.hasEnoughResources(actionCosts.get(ActionType.BUILD_SETTLEMENT))) return false;
        if (!isDistanceRuleSatisfied(board, intersectionId)) return false;
        return player.ownsRoadConnectedTo(intersectionId, board);
    }

    public boolean canBuildCity(Player player, Board board, int intersectionId) {
        if (!board.isIntersectionOccupied(intersectionId)) return false;
        Intersection inter = board.getIntersection(intersectionId);
        if (inter.getBuilding() != Building.SETTLEMENT) return false;
        if (inter.getBuildingOwnerId() != player.getPlayerId()) return false;
        return player.hasEnoughResources(actionCosts.get(ActionType.BUILD_CITY));
    }

    public List<Action> getValidActionsByLinearScan(Player player, Board board) {
        List<Action> validActions = new ArrayList<>();

        // Check all edges for road building
        for (Edge edge : board.getAllEdges()) {
            if (canBuildRoad(player, board, edge)) {
                validActions.add(new Action(ActionType.BUILD_ROAD, -1,
                        edge.getIntersectionA(), edge.getIntersectionB()));
            }
        }

        // Check all intersections for settlement and city building
        for (int intId : board.getAllIntersectionIds()) {
            if (canBuildSettlement(player, board, intId)) {
                validActions.add(new Action(ActionType.BUILD_SETTLEMENT, intId, -1, -1));
            }
            if (canBuildCity(player, board, intId)) {
                validActions.add(new Action(ActionType.BUILD_CITY, intId, -1, -1));
            }
        }

        // PASS is always available
        validActions.add(new Action(ActionType.PASS, -1, -1, -1));
        return validActions;
    }

    public Map<Resource, Integer> getCost(ActionType actionType) {
        return Collections.unmodifiableMap(actionCosts.getOrDefault(actionType, Collections.emptyMap()));
    }

    public Action chooseRandomAction(List<Action> validActions, Random rng) {
        return validActions.get(rng.nextInt(validActions.size()));
    }

    private boolean isDistanceRuleSatisfied(Board board, int intersectionId) {
        // No building may be placed on an intersection adjacent to an existing building
        for (int neighborId : board.getAdjacentIntersectionIds(intersectionId)) {
            if (board.isIntersectionOccupied(neighborId)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRoadConnectedToPlayerNetwork(Player player, Board board, Edge edge) {
        int intA = edge.getIntersectionA();
        int intB = edge.getIntersectionB();
        // Connected if the player owns a building at either endpoint
        // or owns a road that touches either endpoint
        return player.ownsSettlementAt(intA)
                || player.ownsSettlementAt(intB)
                || player.ownsRoadConnectedTo(intA, board)
                || player.ownsRoadConnectedTo(intB, board);
    }
}

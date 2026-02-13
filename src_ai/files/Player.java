import java.util.*;

public class Player {
    private final int playerId;
    private final Colour colour;
    private int victoryPoints;
    private final Map<Resource, Integer> resourceCards;
    private final Map<Integer, Building> ownedBuildings;
    private final Set<String> ownedRoadEdgeKeys;

    public Player(int playerId, Colour colour) {
        this.playerId = playerId;
        this.colour = colour;
        this.victoryPoints = 0;
        this.resourceCards = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) {
            if (r != Resource.DESERT) {
                resourceCards.put(r, 0);
            }
        }
        this.ownedBuildings = new HashMap<>();
        this.ownedRoadEdgeKeys = new HashSet<>();
    }

    public int getPlayerId() { return playerId; }
    public Colour getColour() { return colour; }
    public int getVictoryPoints() { return victoryPoints; }

    public void addVictoryPoints(int points) { this.victoryPoints += points; }

    public int getTotalCardsInHand() {
        int total = 0;
        for (int count : resourceCards.values()) {
            total += count;
        }
        return total;
    }

    public void addResource(Resource type, int amount) {
        resourceCards.merge(type, amount, Integer::sum);
    }

    public boolean hasEnoughResources(Map<Resource, Integer> cost) {
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            if (resourceCards.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void payCost(Map<Resource, Integer> cost) {
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            resourceCards.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
    }

    public boolean ownsSettlementAt(int intersectionId) {
        return ownedBuildings.containsKey(intersectionId);
    }

    public boolean ownsRoadConnectedTo(int intersectionId, Board board) {
        for (Edge e : board.getAllEdges()) {
            if (e.isOccupied()
                    && e.getRoadOwnerId() == playerId
                    && e.touchesIntersection(intersectionId)) {
                return true;
            }
        }
        return false;
    }

    public void recordPlacedSettlement(int intersectionId) {
        ownedBuildings.put(intersectionId, Building.SETTLEMENT);
    }

    public void recordUpgradedCity(int intersectionId) {
        ownedBuildings.put(intersectionId, Building.CITY);
    }

    public void recordPlacedRoad(int intersectionA, int intersectionB) {
        ownedRoadEdgeKeys.add(edgeKey(intersectionA, intersectionB));
    }

    public Map<Resource, Integer> getResourceCards() {
        return Collections.unmodifiableMap(resourceCards);
    }

    public Map<Integer, Building> getOwnedBuildings() {
        return Collections.unmodifiableMap(ownedBuildings);
    }

    private String edgeKey(int intersectionA, int intersectionB) {
        int a = Math.min(intersectionA, intersectionB);
        int b = Math.max(intersectionA, intersectionB);
        return a + "-" + b;
    }
}

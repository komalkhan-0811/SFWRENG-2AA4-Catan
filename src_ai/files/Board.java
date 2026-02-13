import java.util.*;

public class Board {
    private final List<Tile> tiles;
    private final Map<Integer, Intersection> intersections;
    private final List<Edge> edges;
    private final Map<Integer, List<Integer>> adjacentIntersectionIdsByIntersectionId;
    private final Map<Integer, List<Integer>> adjacentTileIdsByIntersectionId;

    // Each tile's 6 adjacent intersection ids (clockwise from top)
    private static final int[][] TILE_INTERSECTIONS = {
        {0, 1, 9, 10, 8, 7},           // Tile 0
        {2, 3, 11, 12, 10, 9},         // Tile 1
        {4, 5, 13, 14, 12, 11},        // Tile 2
        {7, 8, 18, 19, 17, 16},        // Tile 3
        {8, 10, 20, 21, 19, 18},       // Tile 4  (note: 8-10 share edge with tile 0)
        {10, 12, 22, 23, 21, 20},      // Tile 5
        {12, 14, 24, 25, 23, 22},      // Tile 6
        {16, 17, 28, 29, 27, 26},      // Tile 7
        {17, 19, 30, 31, 29, 28},      // Tile 8
        {19, 21, 32, 33, 31, 30},      // Tile 9
        {21, 23, 34, 35, 33, 32},      // Tile 10
        {23, 25, 36, 37, 35, 34},      // Tile 11
        {27, 29, 39, 40, 38, 6},       // Tile 12 — uses corner 6 as bottom-left anchor
        {29, 31, 41, 42, 40, 39},      // Tile 13
        {31, 33, 43, 44, 42, 41},      // Tile 14
        {33, 35, 45, 46, 44, 43},      // Tile 15
        {38, 40, 48, 49, 47, 15},      // Tile 16
        {40, 42, 50, 51, 49, 48},      // Tile 17
        {42, 44, 52, 53, 51, 50},      // Tile 18
    };

    // Standard beginner layout resources (tile 0-18)
    private static final Resource[] TILE_RESOURCES = {
        Resource.ORE,    Resource.SHEEP,  Resource.WOOD,
        Resource.WHEAT,  Resource.BRICK,  Resource.SHEEP, Resource.BRICK,
        Resource.WHEAT,  Resource.WOOD,   Resource.DESERT, Resource.WOOD, Resource.ORE,
        Resource.WOOD,   Resource.ORE,    Resource.WHEAT, Resource.SHEEP,
        Resource.BRICK,  Resource.WHEAT,  Resource.SHEEP
    };

    // Standard beginner layout dice tokens (0 = desert)
    private static final int[] TILE_DICE_TOKENS = {
        10, 2, 9,
        12, 6, 4, 10,
        9, 11, 0, 3, 8,
        8, 3, 4, 5,
        5, 6, 11
    };

    public Board() {
        this.tiles = new ArrayList<>();
        this.intersections = new HashMap<>();
        this.edges = new ArrayList<>();
        this.adjacentIntersectionIdsByIntersectionId = new HashMap<>();
        this.adjacentTileIdsByIntersectionId = new HashMap<>();
        initializeFixedMapLayout();
    }

    public void initializeFixedMapLayout() {
        // Create tiles
        for (int i = 0; i < 19; i++) {
            tiles.add(new Tile(i, TILE_RESOURCES[i], TILE_DICE_TOKENS[i]));
        }

        // Collect all intersection ids and build intersection-to-tile mapping
        Set<Integer> allIntersectionIds = new TreeSet<>();
        for (int tileId = 0; tileId < TILE_INTERSECTIONS.length; tileId++) {
            for (int intId : TILE_INTERSECTIONS[tileId]) {
                allIntersectionIds.add(intId);
                adjacentTileIdsByIntersectionId
                        .computeIfAbsent(intId, k -> new ArrayList<>())
                        .add(tileId);
            }
        }

        // Create intersection objects
        for (int id : allIntersectionIds) {
            intersections.put(id, new Intersection(id));
        }

        // Build edges and intersection adjacency from tile corner sequences
        Set<String> edgeKeysAdded = new HashSet<>();
        for (int[] corners : TILE_INTERSECTIONS) {
            for (int i = 0; i < corners.length; i++) {
                int a = corners[i];
                int b = corners[(i + 1) % corners.length];
                String key = edgeKey(a, b);
                if (edgeKeysAdded.add(key)) {
                    edges.add(new Edge(a, b));
                }
                // Record adjacency (bidirectional)
                adjacentIntersectionIdsByIntersectionId
                        .computeIfAbsent(a, k -> new ArrayList<>());
                adjacentIntersectionIdsByIntersectionId
                        .computeIfAbsent(b, k -> new ArrayList<>());
                if (!adjacentIntersectionIdsByIntersectionId.get(a).contains(b)) {
                    adjacentIntersectionIdsByIntersectionId.get(a).add(b);
                }
                if (!adjacentIntersectionIdsByIntersectionId.get(b).contains(a)) {
                    adjacentIntersectionIdsByIntersectionId.get(b).add(a);
                }
            }
        }

        // Ensure all intersections have adjacency entries (even isolated ones)
        for (int id : allIntersectionIds) {
            adjacentIntersectionIdsByIntersectionId.putIfAbsent(id, new ArrayList<>());
            adjacentTileIdsByIntersectionId.putIfAbsent(id, new ArrayList<>());
        }
    }

    public Tile getTile(int tileId) { return tiles.get(tileId); }

    public Intersection getIntersection(int intersectionId) { return intersections.get(intersectionId); }

    public List<Integer> getAllIntersectionIds() { return new ArrayList<>(intersections.keySet()); }

    public List<Edge> getAllEdges() { return Collections.unmodifiableList(edges); }

    public List<Integer> getAdjacentIntersectionIds(int intersectionId) {
        return adjacentIntersectionIdsByIntersectionId.getOrDefault(intersectionId, Collections.emptyList());
    }

    public List<Integer> getAdjacentTileIds(int intersectionId) {
        return adjacentTileIdsByIntersectionId.getOrDefault(intersectionId, Collections.emptyList());
    }

    public boolean isIntersectionOccupied(int intersectionId) {
        Intersection inter = intersections.get(intersectionId);
        return inter != null && !inter.isEmpty();
    }

    public boolean isEdgeOccupied(int intersectionA, int intersectionB) {
        String key = edgeKey(intersectionA, intersectionB);
        for (Edge e : edges) {
            if (edgeKey(e.getIntersectionA(), e.getIntersectionB()).equals(key)) {
                return e.isOccupied();
            }
        }
        return false;
    }

    public void placeSettlement(int playerId, int intersectionId) {
        intersections.get(intersectionId).placeSettlement(playerId);
    }

    public void upgradeSettlementToCity(int playerId, int intersectionId) {
        intersections.get(intersectionId).upgradeToCity(playerId);
    }

    public void placeRoad(int playerId, int intersectionA, int intersectionB) {
        String key = edgeKey(intersectionA, intersectionB);
        for (Edge e : edges) {
            if (edgeKey(e.getIntersectionA(), e.getIntersectionB()).equals(key)) {
                e.placeRoad(playerId);
                return;
            }
        }
    }

    public Edge getEdge(int intersectionA, int intersectionB) {
        String key = edgeKey(intersectionA, intersectionB);
        for (Edge e : edges) {
            if (edgeKey(e.getIntersectionA(), e.getIntersectionB()).equals(key)) {
                return e;
            }
        }
        return null;
    }

    public List<Tile> getTiles() { return Collections.unmodifiableList(tiles); }

    private String edgeKey(int intersectionA, int intersectionB) {
        int a = Math.min(intersectionA, intersectionB);
        int b = Math.max(intersectionA, intersectionB);
        return a + "-" + b;
    }
}

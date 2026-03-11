package catan;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes the current board state to state.json after every player turn,
 * in the exact format consumed by light_visualizer.py script.
 *
 * @author Alisha Faridi
 */
public class VisualizerStateWriter {

    /**
     * Maps each of our Colour enum values to the colour string token that
     * light_visualizer.py's _parse_color() method will accept.
     *
     * Loaded once into a static map for O(1) look-up on every turn write.
     */
    private static final Map<Colour, String> COLOUR_TO_VISUALIZER = new HashMap<>();

    static {
        COLOUR_TO_VISUALIZER.put(Colour.RED,    "RED");
        COLOUR_TO_VISUALIZER.put(Colour.BLUE,   "BLUE");
        COLOUR_TO_VISUALIZER.put(Colour.GREEN,  "ORANGE"); // Catanatron has no GREEN
        COLOUR_TO_VISUALIZER.put(Colour.YELLOW, "WHITE");  // Catanatron has no YELLOW
    }

    /**
     * Writes the current board state from the given Game to state.json
     * in the specified output directory.
     *
     * Called from Game.playOneRound() after every single player turn so the
     * visualizer always reflects the latest board, not just end-of-round state.
     *
     * @param game the live Game object to read board state from
     * @param outputDir the directory to write state.json into
     * @throws Exception if reflection fails or the file cannot be written
     */
    public static void write(Game game, Path outputDir) throws Exception {
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }
        Files.createDirectories(outputDir);

        Board board = extractBoard(game);
        List<Player> players = extractPlayers(game);

        Map<Integer, String> playerColours = buildColourMap(players);

        String json = buildJson(board, playerColours);
        Path outputPath = outputDir.resolve("state.json");
        Files.write(outputPath, json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the private board field from the Game object via reflection.
     *
     * @param game the Game instance
     * @return the Board currently in use
     * @throws Exception if the field cannot be accessed
     */
    private static Board extractBoard(Game game) throws Exception {
        Field f = Game.class.getDeclaredField("board");
        f.setAccessible(true);
        return (Board) f.get(game);
    }

    /**
     * Extracts the private players list from the Game object via reflection.
     *
     * @param game the Game instance
     * @return the list of all players in the game
     * @throws Exception if the field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    private static List<Player> extractPlayers(Game game) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        return (List<Player>) f.get(game);
    }

    /**
     * Builds a map from each player's ID to the colour token that
     * light_visualizer.py will accept without crashing.
     *
     * Falls back to "RED" if a Colour value is somehow not in the map
     *
     * @param players all players in the game
     * @return map from player ID to visualizer-compatible colour string
     */
    private static Map<Integer, String> buildColourMap(List<Player> players) {
        Map<Integer, String> map = new HashMap<>();
        for (Player p : players) {
            String vis = COLOUR_TO_VISUALIZER.getOrDefault(p.getColour(), "RED");
            map.put(p.getPlayerId(), vis);
        }
        return map;
    }

    /**
     * Builds the complete state.json string from the current board state.
     *
     * Iterates once over all edges (roads) and once over all intersections
     * (buildings).
     * 
     * @param board the current board
     * @param playerColours map from player ID to visualizer colour string
     * @return a JSON string
     */
    private static String buildJson(Board board, Map<Integer, String> playerColours) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // Roads
        sb.append("  \"roads\": [\n");
        boolean firstRoad = true;
        for (Edge e : board.getAllEdges()) {
            if (e.isOccupied()) {
                String colour = playerColours.getOrDefault(e.getRoadOwnerId(), "RED");
                if (!firstRoad) sb.append(",\n");
                sb.append("    { \"a\": ").append(e.getIntersectionA())
                  .append(", \"b\": ").append(e.getIntersectionB())
                  .append(", \"owner\": \"").append(colour).append("\" }");
                firstRoad = false;
            }
        }
        if (!firstRoad) sb.append("\n");
        sb.append("  ],\n");

        //buildings 
        sb.append("  \"buildings\": [\n");
        boolean firstBuilding = true;
        for (int id : board.getAllIntersectionIds()) {
            Intersection inter = board.getIntersection(id);
            if (inter != null && inter.hasBuilding()) {
                Integer ownerId       = inter.getBuildingOwnerId();
                Building buildingType = inter.getBuildingType();
                if (ownerId != null && buildingType != null) {
                    String colour = playerColours.getOrDefault(ownerId, "RED");
                    if (!firstBuilding) sb.append(",\n");
                    sb.append("    { \"node\": ").append(id)
                      .append(", \"owner\": \"").append(colour).append("\"")
                      .append(", \"type\": \"").append(buildingType.name()).append("\" }");
                    firstBuilding = false;
                }
            }
        }
        if (!firstBuilding) sb.append("\n");
        sb.append("  ]\n");

        sb.append("}\n");
        return sb.toString();
    }
}

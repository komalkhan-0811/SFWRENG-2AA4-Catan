package catan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Converts a GameSnapshot into a JSON string and writes it to disk.
 * 
 * Responsible only for formatting and file I/O. It never reads from
 * the live Game object directly.
 * 
 * @author Alisha Faridi
 * 
 */
public class JSONWriter {

    /**
     * Writes the given GameSnapshot to a JSON file in the specified output directory.
     *
     * The filename format is: gamestate_round_0001.json, gamestate_round_0002.json, etc.
     * This format is required by the visualizer (R2.3).
     *
     * When JSON is updated:
     * - This method is called at the END of each round, after all players have taken turns
     * - It is never called mid-turn to avoid writing incomplete state
     * - The visualizer reads the file after each round completes
     *
     * @param snapshot the GameSnapshot to write
     * @param outputDir the directory to write the file into
     * @throws IOException if the file cannot be written
     */
    public void write(GameSnapshot snapshot, Path outputDir) throws IOException {
    	
    	// Default to current directory if no output path provided
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }

        // Ensure the output directory exists
        Files.createDirectories(outputDir);

        // Build the filename from the round number
        String filename = String.format("gamestate_round_%04d.json", snapshot.getRoundNumber());
        Path outputPath = outputDir.resolve(filename);

        // Build and write the JSON
        String json = buildJson(snapshot);
        Files.write(outputPath, json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds a JSON string from the given GameSnapshot.
     *
     * @param snapshot the snapshot to serialize
     * @return a JSON string representing the snapshot
     */
    private String buildJson(GameSnapshot snapshot) {
        List<GameSnapshot.PlayerSnapshot> players = snapshot.getPlayers();

        StringBuilder sb = new StringBuilder();
        
     // Opening brace and round number
        sb.append("{\n");
        sb.append("  \"round\": ").append(snapshot.getRoundNumber()).append(",\n");
        sb.append("  \"players\": [\n");

        for (int i = 0; i < players.size(); i++) {
            GameSnapshot.PlayerSnapshot p = players.get(i);
            
         // One player object per line with all relevant fields
            sb.append("    {")
              .append(" \"id\": ").append(p.getPlayerId())
              .append(", \"colour\": \"").append(p.getColour()).append("\"")
              .append(", \"victoryPoints\": ").append(p.getVictoryPoints())
              .append(", \"totalCards\": ").append(p.getTotalCards())
              .append(" }");
            
            // Add comma after every player except the last
            if (i < players.size() - 1) sb.append(",");
            sb.append("\n");
        }


        // Close players array and root object
        sb.append("  ]\n");
        sb.append("}\n");
        
        return sb.toString();
    }
}
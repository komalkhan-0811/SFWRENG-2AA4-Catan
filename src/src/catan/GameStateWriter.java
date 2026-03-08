package catan;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Coordinates the export of game state to a JSON file.
 *
 * It connects GameStateExporter and JSONWriter together. It exists 
 * so that Game.java does not need to change its existing call to writeBasicRoundState().
 * While GameStateExporter is reposnsible for extracting Game data and JSONWriter
 * is responsible for formatting and writing the file, this class connects them. 
 * 
 * @author Alisha Faridi 
 * 
 */
public class GameStateWriter {

    /**
     * Exports the current game state to a JSON file.
     *
     * @param game the current Game object
     * @param outputDir the directory to write the JSON file into
     * @throws Exception if export or file writing fails
     */
    public static void writeBasicRoundState(Game game, Path outputDir) throws Exception {
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }

        // Extract game state into a plain snapshot object
        GameStateExporter exporter = new GameStateExporter();
        GameSnapshot snapshot = exporter.exportSnapshot(game);

        // Write the snapshot to a JSON file
        JSONWriter writer = new JSONWriter();
        writer.write(snapshot, outputDir);
    }
}
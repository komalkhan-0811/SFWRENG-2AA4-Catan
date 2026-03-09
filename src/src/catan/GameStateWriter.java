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
     * Delegates extraction to GameStateExporter and file writing
     * to JSONWriter, keeping each class focused on a single responsibility
     * (Single Responsibility Principle).
     *
     * @param game the current Game object
     * @param outputDir the directory to write the JSON file into
     * @throws Exception if export or file writing fails
     */
    public static void writeBasicRoundState(Game game, Path outputDir) throws Exception {
    	// Default to current directory if no output path is provided
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }

        // Extract game state into a plain snapshot object
        // GameStateExporter uses reflection to read private Game fields
        GameStateExporter exporter = new GameStateExporter();
        GameSnapshot snapshot = exporter.exportSnapshot(game);

        // Write the snapshot to a JSON file
        // JSONWriter handles all formatting and file I/O
        JSONWriter writer = new JSONWriter();
        writer.write(snapshot, outputDir);
    }
}
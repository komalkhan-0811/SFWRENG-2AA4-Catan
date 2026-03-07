package catan;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GameStateWriter {

    /**
     * Writes a basic JSON state file for the current round.
     * Filename: gamestate_round_0001.json, gamestate_round_0002.json, ...
     */
    public static void writeBasicRoundState(Game game, Path outputDir) throws Exception {
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }
        Files.createDirectories(outputDir);

        int round = getPrivateInt(game, "roundNumber");
        List<Player> players = getPlayers(game);

        String filename = String.format("gamestate_round_%04d.json", round);
        Path out = outputDir.resolve(filename);

        String json = buildBasicJson(round, players);
        writeUtf8(out, json);
    }

    private static void writeUtf8(Path out, String content) throws IOException {
        Files.write(out, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildBasicJson(int round, List<Player> players) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"round\": ").append(round).append(",\n");
        sb.append("  \"players\": [\n");

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            sb.append("    { \"id\": ").append(p.getPlayerId())
              .append(", \"victoryPoints\": ").append(p.getVictoryPoints())
              .append(" }");
            if (i < players.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static int getPrivateInt(Game g, String fieldName) throws Exception {
        Field f = Game.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (int) f.get(g);
    }

    @SuppressWarnings("unchecked")
    private static List<Player> getPlayers(Game g) throws Exception {
        Field f = Game.class.getDeclaredField("players");
        f.setAccessible(true);
        return (List<Player>) f.get(g);
    }
}

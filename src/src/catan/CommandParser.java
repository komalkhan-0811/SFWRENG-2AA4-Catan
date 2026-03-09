package catan;

/**
 * Command parsing interface for the Catan simulator.
 *
 * Defines the contract for parsing raw console strings into ParsedCommand
 * objects. Keeping this as an interface allows alternative parsers
 * to be swapped in without touching HumanPlayer (Open/Closed Principle).
 *
 * Supported commands (case-insensitive):
 *   Roll
 *   Go
 *   List
 *   Build settlement <nodeId>
 *   Build city <nodeId>
 *   Build road <fromNodeId> <toNodeId>
 *
 * @author Rameen Tariq
 */


public interface CommandParser {

	/**
     * All valid command types a human player can issue during their turn.
     */
    enum CommandType {
        ROLL,
        GO,
        LIST,
        BUILD_SETTLEMENT,
        BUILD_CITY,
        BUILD_ROAD,
        /** Unrecognised or malformed input. */
        UNKNOWN
    }

    /**
     * Immutable result of parsing one line of player input.
     */
    class ParsedCommand {
        public final CommandType type;
        /** Primary node ID — settlement/city target or road fromNode. -1 if unused. */
        public final int nodeA;
        /** Secondary node ID — road toNode. -1 if unused. */
        public final int nodeB;
        

        /** Convenience constructor for commands with no node arguments. */
        public ParsedCommand(CommandType type, int nodeA, int nodeB) {
            this.type  = type;
            this.nodeA = nodeA;
            this.nodeB = nodeB;
        }

        public ParsedCommand(CommandType type) {
            this(type, -1, -1);
        }
    }

    /**
     * Parses a raw input string into a ParsedCommand.
     * Never returns null — returns UNKNOWN if unrecognised or malformed.
     *
     * @param raw the string typed by the player
     * @return a ParsedCommand, never null
     */
    ParsedCommand parse(String raw);

    /**
     * Returns a usage hint to display when the player enters an invalid command.
     *
     * @return formatted usage string
     */
    String usageHint();


    /**
     * Default console implementation of CommandParser.
     */
    class ConsoleCommandParser implements CommandParser {

        @Override
        public ParsedCommand parse(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return new ParsedCommand(CommandType.UNKNOWN);
            }

            String[] parts = raw.trim().split("\\s+");
            String first = parts[0].toLowerCase();

            switch (first) {
                case "roll":  return new ParsedCommand(CommandType.ROLL);
                case "go":    return new ParsedCommand(CommandType.GO);
                case "list":  return new ParsedCommand(CommandType.LIST);
                case "build": return parseBuild(parts);
                default:      return new ParsedCommand(CommandType.UNKNOWN);
            }
        }

        /**
         * Parses the sub-command after "build".
         * Valid forms: build settlement/city <nodeId>, build road <fromNodeId> <toNodeId>
         *
         * @param parts tokenized input
         * @return ParsedCommand for the build type, or UNKNOWN if malformed
         */
        private ParsedCommand parseBuild(String[] parts) {
            if (parts.length < 2) {
                return new ParsedCommand(CommandType.UNKNOWN);
            }
            try {
                switch (parts[1].toLowerCase()) {
                    case "settlement":
                        if (parts.length == 3) {
                            return new ParsedCommand(
                                CommandType.BUILD_SETTLEMENT,
                                Integer.parseInt(parts[2]), -1);
                        }
                        break;
                    case "city":
                        if (parts.length == 3) {
                            return new ParsedCommand(
                                CommandType.BUILD_CITY,
                                Integer.parseInt(parts[2]), -1);
                        }
                        break;
                    case "road":
                        if (parts.length == 4) {
                            return new ParsedCommand(
                                CommandType.BUILD_ROAD,
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]));
                        }
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
            	// Node ID was not a valid integer
            }
            return new ParsedCommand(CommandType.UNKNOWN);
        }

        @Override
        public String usageHint() {
            return "Valid commands:\n"
                 + "  Roll                               - roll the dice and collect resources\n"
                 + "  Go                                 - end your turn / step to next agent\n"
                 + "  List                               - show cards in your hand\n"
                 + "  Build settlement <nodeId>          - build a settlement\n"
                 + "  Build city <nodeId>                - upgrade a settlement to a city\n"
                 + "  Build road <fromNodeId> <toNodeId> - build a road";
        }
    }
}
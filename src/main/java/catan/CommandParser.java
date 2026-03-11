package catan;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * Command parsing interface for the Catan simulator.
 *
 * R2.1: Parsing functionality implemented using regular expressions.
 *
 * Supported commands (case-insensitive):
 *   Roll
 *   Go
 *   List
 *   Build settlement <nodeId>
 *   Build city <nodeId>
 *   Build road <fromNodeId> <toNodeId>
 *
 * @author Rameen Tariq, Komal Khan
 */
public interface CommandParser {

    enum CommandType {
        ROLL,
        GO,
        LIST,
        BUILD_SETTLEMENT,
        BUILD_CITY,
        BUILD_ROAD,
        UNKNOWN
    }

    class ParsedCommand {
        public final CommandType type;
        public final int nodeA;
        public final int nodeB;

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
     * Never returns null, and returns UNKNOWN if the command is unrecognized.
     *
     * @param raw the string typed by the player
     * @return a ParsedCommand
     */
    ParsedCommand parse(String raw);

    /**
     * Returns a usage hint to display when the player enters an invalid command.
     *
     * @return formatted usage string
     */
    String usageHint();


    /**
     * R2.1: Regex-based implementation of CommandParser.
     * All parsing uses regular expressions with Pattern and Matcher.
     */
    class ConsoleCommandParser implements CommandParser {
    	
        /**
         * Pattern for "roll" command.
         * Matches: "roll", "  roll  ", "ROLL", "RoLl", etc.
         * Regex: ^\\s*roll\\s*$
         *   ^ = start of string
         *   \\s* = zero or more whitespace
         *   roll = literal text (case-insensitive flag)
         *   \\s* = zero or more whitespace
         *   $ = end of string
         */
        private static final Pattern ROLL_PATTERN = 
            Pattern.compile("^\\s*roll\\s*$", Pattern.CASE_INSENSITIVE);

        /**
         * Pattern for "go" command.
         * Matches: "go", "  go  ", "GO", "Go", etc.
         */
        private static final Pattern GO_PATTERN = 
            Pattern.compile("^\\s*go\\s*$", Pattern.CASE_INSENSITIVE);

        /**
         * Pattern for "list" command.
         * Matches: "list", "  list  ", "LIST", "LiSt", etc.
         */
        private static final Pattern LIST_PATTERN = 
            Pattern.compile("^\\s*list\\s*$", Pattern.CASE_INSENSITIVE);

        /**
         * Pattern for "build settlement <nodeId>" command.
         * Matches: "build settlement 5", "  BUILD   SETTLEMENT   10  ", etc.
         * Regex: ^\\s*build\\s+settlement\\s+(\\d+)\\s*$
         * 
         */
        private static final Pattern BUILD_SETTLEMENT_PATTERN = 
            Pattern.compile("^\\s*build\\s+settlement\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);

        /**
         * Pattern for "build city <nodeId>" command.
         * Matches: "build city 3", "  BUILD   CITY   8  ", etc.
         */
        private static final Pattern BUILD_CITY_PATTERN = 
            Pattern.compile("^\\s*build\\s+city\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);

        /**
         * Pattern for "build road <fromNodeId> <toNodeId>" command.
         * Matches: "build road 5 12", "  BUILD   ROAD   10   20  ", etc.
         * Regex: ^\\s*build\\s+road\\s+(\\d+)\\s+(\\d+)\\s*$

         */
        private static final Pattern BUILD_ROAD_PATTERN = 
            Pattern.compile("^\\s*build\\s+road\\s+(\\d+)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);


        @Override
        public ParsedCommand parse(String raw) {
        	
            // Handle null/empty input
            if (raw == null || raw.trim().isEmpty()) {
                return new ParsedCommand(CommandType.UNKNOWN);
            }

            Matcher matcher;


            matcher = ROLL_PATTERN.matcher(raw);
            if (matcher.matches()) {
                return new ParsedCommand(CommandType.ROLL);
            }

            matcher = GO_PATTERN.matcher(raw);
            if (matcher.matches()) {
                return new ParsedCommand(CommandType.GO);
            }

            matcher = LIST_PATTERN.matcher(raw);
            if (matcher.matches()) {
                return new ParsedCommand(CommandType.LIST);
            }

            matcher = BUILD_SETTLEMENT_PATTERN.matcher(raw);
            if (matcher.matches()) {
                try {
                    int nodeId = Integer.parseInt(matcher.group(1));
                    return new ParsedCommand(CommandType.BUILD_SETTLEMENT, nodeId, -1);
                } catch (NumberFormatException e) {
                    return new ParsedCommand(CommandType.UNKNOWN);
                }
            }

            // Check for "build city <nodeId>"
            matcher = BUILD_CITY_PATTERN.matcher(raw);
            if (matcher.matches()) {
                try {
                    int nodeId = Integer.parseInt(matcher.group(1));
                    return new ParsedCommand(CommandType.BUILD_CITY, nodeId, -1);
                } catch (NumberFormatException e) {
                    return new ParsedCommand(CommandType.UNKNOWN);
                }
            }

            // Check for "build road <fromNodeId> <toNodeId>"
            matcher = BUILD_ROAD_PATTERN.matcher(raw);
            if (matcher.matches()) {
                try {
                    int fromNode = Integer.parseInt(matcher.group(1));
                    int toNode = Integer.parseInt(matcher.group(2));
                    return new ParsedCommand(CommandType.BUILD_ROAD, fromNode, toNode);
                } catch (NumberFormatException e) {
                    return new ParsedCommand(CommandType.UNKNOWN);
                }
            }

            // No pattern matched
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
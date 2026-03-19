package catan;

import java.util.Map;

/**
 * Represents a human controlled player in the Catan simulator.
 *
 * Extends Player (Open/Closed Principle: Player is not modified).
 * Depends on InputHandler and CommandParser abstractions, never on
 * concrete I/O classes (Dependency Inversion Principle).
 *
 *Turn flow is governed by the {@link TurnState} automaton:
 *START → (Roll) → ROLLED → (Build*) → ROLLED → (Go) → DONE
 *Illegal commands for the current state are rejected with a clear message and do not advance the state.
 * 
 * @author Rameen Tariq
 */
public class HumanPlayer extends Player {

    private final InputHandler inputHandler;
    private final CommandParser parser;

    /**
     * Constructs a HumanPlayer with console I/O.
     *
     * @param playerId unique player identifier
     * @param colour colour assigned to this player
     * @param inputHandler abstraction over the input/output source
     * @throws IllegalArgumentException if inputHandler is null
     */
    public HumanPlayer(int playerId, Colour colour, InputHandler inputHandler) {
        super(playerId, colour);
        if (inputHandler == null) {
            throw new IllegalArgumentException("InputHandler must not be null.");
        }
        this.inputHandler = inputHandler;
        this.parser = new CommandParser.ConsoleCommandParser();
    }

    /**
     * Constructs a HumanPlayer with the parser
     *
     * @param playerId
     * @param colour 
     * @param inputHandler: abstraction over the input/output source
     * @param parser: command parser implementation to use
     * @throws IllegalArgumentException if inputHandler or parser is null
     */
    public HumanPlayer(int playerId, Colour colour, InputHandler inputHandler, CommandParser parser) {
        super(playerId, colour);
        if (inputHandler == null)
            throw new IllegalArgumentException("InputHandler must not be null.");
        if (parser == null)
            throw new IllegalArgumentException("CommandParser must not be null.");
        this.inputHandler = inputHandler;
        this.parser = parser;
    }

    /**
     * Runs a full interactive turn for this human player.
     *
     * The turn is governed by the {@link TurnState} automaton.
     * The state starts at START and only advances to DONE when the player
     * types Go after having rolled. Every command is validated against the
     * current state before being executed
     * illegal commands are rejected with a descriptive message and the state is not changed.
     *
     * Legal transitions:
     * START: [Roll] -> ROLLED
     * ROLLED:[Build] -> ROLLED
     * ROLLED: [Go]  -> DONE
     * ANY: [List]
     *
     * @param roundNumber the current round number
     * @param game the Game instance
     * @param board the Board instance
     * @param rules the Rules instance
     */
    public void takeTurn(int roundNumber, Game game, Board board, Rules rules) {
        // Automaton starts in START state for every fresh turn
        TurnState state = TurnState.START;

        inputHandler.displayMessage(
            "\n=== Player " + getPlayerId() + " (" + getColour().getDisplayName() + ") — your turn ===");

        inputHandler.displayMessage("State: " + state + " | " + parser.usageHint());

        while (!state.isDone()) {
            String raw = inputHandler.readLine("> ");
            CommandParser.ParsedCommand cmd = parser.parse(raw);
            state = handleTurnCommand(state, cmd, roundNumber, game, board, rules);
        }
    }

    /**
     * Dispatches a parsed command to the appropriate handler based on its type.
     *
     * @param state the current turn state
     * @param cmd the parsed command entered by the user
     * @param roundNumber the current round number
     * @param game the Game instance
     * @param board the Board instance
     * @param rules the Rules instance
     * @return the updated turn state after executing the command
     */
    private TurnState handleTurnCommand(TurnState state, CommandParser.ParsedCommand cmd,
            int roundNumber, Game game, Board board, Rules rules) {

        switch (cmd.type) {
            case ROLL:
                return handleRollCommand(state, roundNumber, game);

            case LIST:
                return handleListCommand(state);

            case BUILD_SETTLEMENT:
                return handleBuildSettlementCommand(state, roundNumber, game, board, rules, cmd.nodeA);

            case BUILD_CITY:
                return handleBuildCityCommand(state, roundNumber, game, board, rules, cmd.nodeA);

            case BUILD_ROAD:
                return handleBuildRoadCommand(state, roundNumber, game, board, rules, cmd.nodeA, cmd.nodeB);

            case UNDO:
                return handleUndoCommand(state, game);

            case REDO:
                return handleRedoCommand(state, game);

            case GO:
                return handleGoCommand(state, roundNumber);

            case UNKNOWN:
            default:
                inputHandler.displayMessage("[ILLEGAL] Unknown command.\n" + parser.usageHint());
                return state;
        }
    }

    /**
     * Handles the ROLL command for a human player.
     *
     * R2.5: If a 7 is rolled, the robber is activated and no resources are distributed.
     * Otherwise, resources are distributed according to the roll.
     *
     * Only valid in the START state.
     *
     * @param state the current turn state
     * @param roundNumber the current round number
     * @param game the Game instance
     * @return the updated turn state (ROLLED if successful, unchanged otherwise)
     */
    private TurnState handleRollCommand(TurnState state, int roundNumber, Game game) {
        // ROLL: only legal in START
        if (!state.canRoll()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] You have already rolled this turn.");
            return state;
        }

        int roll = game.rollDice();
        GameLogger.printTurnAction(roundNumber, getPlayerId(), "Rolled " + roll);

        if (roll == 7) {
            inputHandler.displayMessage("Rolled 7 — robber activated, no resources distributed.");
        } else {
            game.distributeResourcesForRoll(roll);
            inputHandler.displayMessage("Resources distributed for roll of " + roll + ".");
        }

        TurnState nextState = TurnState.ROLLED;
        inputHandler.displayMessage("State:" + nextState);
        return nextState;
    }

    /**
     * Handles the LIST command.
     *
     * Displays the player's current hand without changing the turn state.
     * This command is always legal in any state.
     *
     * @param state the current turn state
     * @return the unchanged turn state
     */
    private TurnState handleListCommand(TurnState state) {
        // LIST: always legal, no state change
        if (!state.canList()) {
            inputHandler.displayMessage("[ILLEGAL] Cannot list in state " + state);
            return state;
        }

        inputHandler.displayMessage(describeHand());
        return state;
    }

    /**
     * Handles the BUILD SETTLEMENT command.
     *
     * Only valid in the ROLLED state.
     *
     * @param state the current turn state
     * @param roundNumber the current round number
     * @param game the Game instance
     * @param board the Board instance
     * @param rules the Rules instance
     * @param nodeId the intersection ID for the settlement
     * @return the unchanged turn state
     */
    private TurnState handleBuildSettlementCommand(TurnState state, int roundNumber,
            Game game, Board board, Rules rules, int nodeId) {

        // BUILD commands: only legal in ROLLED
        if (!state.canBuild()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] You must Roll before building.");
            return state;
        }

        handleBuildSettlement(roundNumber, game, board, rules, nodeId);
        return state;
    }

    /**
     * Handles the BUILD CITY command.
     *
     * Only valid in the ROLLED state. 
     *
     * @param state the current turn state
     * @param roundNumber the current round number
     * @param game the Game instance
     * @param board the Board instance
     * @param rules the Rules instance
     * @param nodeId the intersection ID for the city
     * @return the unchanged turn state
     */
    private TurnState handleBuildCityCommand(TurnState state, int roundNumber,
            Game game, Board board, Rules rules, int nodeId) {

        // BUILD commands: only legal in ROLLED
        if (!state.canBuild()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] You must Roll before building.");
            return state;
        }

        handleBuildCity(roundNumber, game, board, rules, nodeId);
        return state;
    }

    /**
     * Handles the BUILD ROAD command.
     *
     * Only valid in the ROLLED state. 
     *
     * @param state the current turn state
     * @param roundNumber the current round number
     * @param game the Game instance
     * @param board the Board instance
     * @param rules the Rules instance
     * @param fromNode the starting intersection ID
     * @param toNode the ending intersection ID
     * @return the unchanged turn state
     */
    private TurnState handleBuildRoadCommand(TurnState state, int roundNumber,
            Game game, Board board, Rules rules, int fromNode, int toNode) {

        // BUILD commands: only legal in ROLLED
        if (!state.canBuild()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] You must Roll before building.");
            return state;
        }

        handleBuildRoad(roundNumber, game, board, rules, fromNode, toNode);
        return state;
    }

    /**
     * Handles the UNDO command.
     *
     * R3.1: Uses the Command Pattern via GameHistory to undo the last action.
     * Only valid after rolling (ROLLED state).
     *
     * @param state the current turn state
     * @param game the Game instance
     * @return the unchanged turn state
     */
    private TurnState handleUndoCommand(TurnState state, Game game) {
        if (!state.canBuild()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] Cannot undo before rolling.");
            return state;
        }

        if (game.getGameHistory().canUndo()) {
            game.getGameHistory().undo();
            inputHandler.displayMessage("Undo successful. Last action reversed.");
        } else {
            inputHandler.displayMessage("Nothing to undo.");
        }

        return state;
    }

    /**
     * Handles the REDO command.
     *
     * R3.1: Uses the Command Pattern via GameHistory to redo the last undone action.
     * Only valid after rolling (ROLLED state).
     *
     * @param state the current turn state
     * @param game the Game instance
     * @return the unchanged turn state
     */
    private TurnState handleRedoCommand(TurnState state, Game game) {
        if (!state.canBuild()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] Cannot redo before rolling.");
            return state;
        }

        if (game.getGameHistory().canRedo()) {
            game.getGameHistory().redo();
            inputHandler.displayMessage("Redo successful. Action re-applied.");
        } else {
            inputHandler.displayMessage("Nothing to redo.");
        }

        return state;
    }

    /**
     * Handles the GO command to end the player's turn.
     *
     * Only valid in the ROLLED state. Transitions the turn to DONE.
     *
     * @param state the current turn state
     * @param roundNumber the current round number
     * @return the updated turn state (DONE if successful, unchanged otherwise)
     */
    private TurnState handleGoCommand(TurnState state, int roundNumber) {
        // GO: only legal in ROLLED
        if (!state.canGo()) {
            inputHandler.displayMessage(
                "[ILLEGAL in state " + state + "] You must Roll before ending your turn.");
            return state;
        }

        GameLogger.printTurnAction(roundNumber, getPlayerId(), "Passed");
        return TurnState.DONE;
    }

    /**
     * Blocks until the human types "go"
     * 
     *Implements R2.4: called after each computer player turn so the human
     *can read what happened before the next turn begins. Only a GO command
     *will unblock; any other input is rejected with a prompt to type "go".
     *
     * @param message summary of the computer player action just performed
     */
    public void waitForGo(String message) {
        if (message != null && !message.isEmpty()) {
            inputHandler.displayMessage(message);
        }
        inputHandler.displayMessage("--- Type Go to continue to the next turn ---");
        while (true) {
            String raw = inputHandler.readLine("> ");
            CommandParser.ParsedCommand cmd = parser.parse(raw);
            if (cmd.type == CommandParser.CommandType.GO) return;
            inputHandler.displayMessage("Type 'Go' to proceed.");
        }
    }

    /**
     * Handles the "Build settlement" command.
     * Validates placement rules and resources, then places the settlement.
     *
     * @param roundNumber current round number for logging
     * @param board the board
     * @param rules the rules engine
     * @param nodeId the target intersection ID
     */
    private void handleBuildSettlement(int roundNumber, Game game, Board board, Rules rules, int nodeId) {
        Map<Resources, Integer> cost = rules.getCost(ActionType.BUILD_SETTLEMENT);
        if (!rules.canBuildSettlement(this, board, nodeId)) {
            inputHandler.displayMessage("Cannot build settlement at node " + nodeId + " — occupied or distance rule violated.");
            return;
        }
        
        if (!hasEnoughResources(cost)) {
            inputHandler.displayMessage("Not enough resources. Need: " + describeCost(cost) + " | You have: " + describeHand());
            return;
        }
        
        payCost(cost);
        Command buildCmd = new BuildSettlementCommand(board, this, nodeId);
        game.getGameHistory().executeCommand(buildCmd);
        recordPlacedSettlement(nodeId);
        GameLogger.printTurnAction(roundNumber, getPlayerId(), "Built settlement at intersection " + nodeId);
    }

    /**
     * Handles the "Build city" command.
     * Validates that the player owns a settlement there, then upgrades it.
     *
     * @param roundNumber current round number for logging
     * @param board the board
     * @param rules the rules engine
     * @param nodeId the target intersection ID
     */
    private void handleBuildCity(int roundNumber, Game game, Board board, Rules rules, int nodeId) {
    	
        Map<Resources, Integer> cost = rules.getCost(ActionType.BUILD_CITY);
        if (!rules.canBuildCity(this, board, nodeId)) {
            inputHandler.displayMessage("Cannot build city at node " + nodeId + " — you must own a settlement there.");
            return;
        }
        if (!hasEnoughResources(cost)) {
            inputHandler.displayMessage("Not enough resources. Need: " + describeCost(cost) + " | You have: " + describeHand());
            return;
        }
        payCost(cost);
        Command buildCmd = new BuildCityCommand(board, this, nodeId);
        game.getGameHistory().executeCommand(buildCmd);
        recordUpgradedCity(nodeId);
        GameLogger.printTurnAction(roundNumber, getPlayerId(), "Upgraded to city at intersection " + nodeId);
    
    }

    /**
     * Handles the "Build road" command.
     * Validates connectivity and resources, then places the road.
     *
     * @param roundNumber current round number for logging
     * @param board the board
     * @param rules the rules engine
     * @param fromNode first endpoint intersection ID
     * @param toNode second endpoint intersection ID
     */
    private void handleBuildRoad(int roundNumber, Game game, Board board, Rules rules, int fromNode, int toNode) {
        
    	Map<Resources, Integer> cost = rules.getCost(ActionType.BUILD_ROAD);
        Edge targetEdge = findEdge(board, fromNode, toNode);
        if (targetEdge == null) {
            inputHandler.displayMessage("No edge exists between node " + fromNode + " and " + toNode + ".");
            return;
        }
        if (!rules.canBuildRoad(this, board, targetEdge)) {
            inputHandler.displayMessage("Cannot build road there — must connect to your network.");
            return;
        }
        if (!hasEnoughResources(cost)) {
            inputHandler.displayMessage("Not enough resources. Need: " + describeCost(cost) + " | You have: " + describeHand());
            return;
        }
        payCost(cost);
        Command buildCmd = new BuildRoadCommand(board, this, fromNode, toNode);
        game.getGameHistory().executeCommand(buildCmd);
        recordPlacedRoad(fromNode, toNode);
        GameLogger.printTurnAction(roundNumber, getPlayerId(), "Built road between " + fromNode + " and " + toNode);
    }

    //Private utility helpers
    
    /**
     * Finds the Edge object between two nodes, or null if none exists.
     *
     * @param board the board
     * @param fromNode first endpoint
     * @param toNode second endpoint
     * @return the matching Edge, or null
     */
    private Edge findEdge(Board board, int fromNode, int toNode) {
        for (Edge e : board.getAllEdges()) {
            if ((e.getIntersectionA() == fromNode && e.getIntersectionB() == toNode) || (e.getIntersectionA() == toNode   && e.getIntersectionB() == fromNode)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns true if this player has not yet placed any roads.
     * Used to skip road connectivity check during initial placement.
     *
     * @param board the board
     * @return true if player has no roads
     */
    private boolean hasNoRoadsYet(Board board) {
        for (Edge e : board.getAllEdges()) {
            if (e.getRoadOwnerId() == getPlayerId()) return false;
        }
        return true;
    }

    /**
     * Returns a readable summary of all resource cards in hand.
     * Used by the List command.
     *
     * @return formatted hand string e.g. "Hand - WOOD: 2, BRICK: 1"
     */
    private String describeHand() {
        StringBuilder sb = new StringBuilder("Hand — ");
        Resources[] all = {Resources.WOOD, Resources.BRICK, Resources.WHEAT, Resources.SHEEP, Resources.ORE};
        for (int i = 0; i < all.length; i++) {
            sb.append(all[i].name()).append(": ").append(getResourceCount(all[i]));
            if (i < all.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Returns the count of a specific resource in hand.
     * Probes using hasEnoughResources to avoid exposing the private map.
     *
     * @param resource the resource type to count
     * @return number of cards of that resource
     */
    private int getResourceCount(Resources resource) {
        java.util.Map<Resources, Integer> probe = new java.util.EnumMap<>(Resources.class);
        for (int n = 1; n <= 50; n++) {
            probe.put(resource, n);
            if (!hasEnoughResources(probe)) return n - 1;
        }
        return 50;
    }

    /**
     * Returns a readable description of a cost map.
     *
     * @param cost the cost to describe
     * @return formatted string e.g. "WOOD x1, BRICK x1"
     */
    private String describeCost(Map<Resources, Integer> cost) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Resources, Integer> entry : cost.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getKey().name()).append(" x").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Returns the injected InputHandler.
     *
     * @return the input handler
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }
}

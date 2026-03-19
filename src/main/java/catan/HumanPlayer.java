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
     * @param game        the Game instance
     * @param board       the Board instance
     * @param rules       the Rules instance
     */
    public void takeTurn(int roundNumber, Game game, Board board, Rules rules) {
        //Automaton starts in START state for every fresh turn
    	TurnState state = TurnState.START;

    	inputHandler.displayMessage("\n=== Player " + getPlayerId()+ " (" + getColour().getDisplayName() + ") — your turn ===");
           
    	inputHandler.displayMessage("State: " + state + " | " + parser.usageHint());

        while (!state.isDone()) {
            String raw = inputHandler.readLine("> ");
            CommandParser.ParsedCommand cmd = parser.parse(raw);

            switch (cmd.type) {
            
            	//ROLL: only legal in START
                case ROLL:
                    if (!state.canRoll()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] You have already rolled this turn.");
                        break;
                    }
                    int roll = game.rollDice();
                    GameLogger.printTurnAction(roundNumber, getPlayerId(), "Rolled " + roll);
                    if (roll == 7) {
                        inputHandler.displayMessage("Rolled 7 — robber activated, no resources distributed.");
                    } 
                    else {
                        game.distributeResourcesForRoll(roll);
                        inputHandler.displayMessage("Resources distributed for roll of " + roll + ".");
                    }
                    
                    //Transition
                    state = TurnState.ROLLED;
                    inputHandler.displayMessage("State:" + state);
                    break;
                    
                 //LIST : always legal, no state change
                case LIST:
                	
                	if(!state.canList()) {
                		inputHandler.displayMessage("[ILLEGAL] Cannot list in state " + state);
                		break;
                	}
                    inputHandler.displayMessage(describeHand());
                    break;
                    
                //BUILD commands: only legal is ROLLED
                case BUILD_SETTLEMENT:
                    if (!state.canBuild()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] You must Roll before building.");
                        break;
                    }
                    handleBuildSettlement(roundNumber, game, board, rules, cmd.nodeA);
                    break;

                case BUILD_CITY:
                    if (!state.canBuild()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] You must Roll before building.");
                        break;
                    }
                    handleBuildCity(roundNumber, game, board, rules, cmd.nodeA);
                    break;

                case BUILD_ROAD:
                    if (!state.canBuild()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] You must Roll before building.");
                        break;
                    }
                    handleBuildRoad(roundNumber, game, board, rules, cmd.nodeA, cmd.nodeB);
                    break;
                    
                case UNDO:
                    if (!state.canBuild()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] Cannot undo before rolling.");
                        break;
                    }
                    if (game.getGameHistory().canUndo()) {
                        game.getGameHistory().undo();
                        inputHandler.displayMessage("Undo successful. Last action reversed.");
                    } else {
                        inputHandler.displayMessage("Nothing to undo.");
                    }
                    break;

                case REDO:
                    if (!state.canBuild()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] Cannot redo before rolling.");
                        break;
                    }
                    if (game.getGameHistory().canRedo()) {
                        game.getGameHistory().redo();
                        inputHandler.displayMessage("Redo successful. Action re-applied.");
                    } else {
                        inputHandler.displayMessage("Nothing to redo.");
                    }
                    break;

                // GO: Only legal in ROLLED
                case GO:
                    if (!state.canGo()) {
                        inputHandler.displayMessage("[ILLEGAL in state " + state + "] You must Roll before ending your turn.");
                        break;
                    }
                    GameLogger.printTurnAction(roundNumber, getPlayerId(), "Passed");
                    state = TurnState.DONE;
                    break;

                //UNKNOWN: reject with usage hint no state change
                case UNKNOWN:
                default:
                    inputHandler.displayMessage("[ILLEGAL] Unknown command.\n" + parser.usageHint());
                    break;
            }
        }
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

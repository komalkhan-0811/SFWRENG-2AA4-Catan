import java.util.*;

public class Game {
    private Board board;
    private List<Player> players;
    private Rules rules;
    private Dice dice;
    private Random rng;
    private int roundNumber;
    private final int maxRounds;
    private final int victoryPointsToWin;
    private final Login login;

    private static final int NUM_PLAYERS = 4;
    private static final int INITIAL_SETTLEMENTS = 2;
    private static final int VICTORY_POINTS_DEFAULT = 10;

    // Fixed starting intersections for the 4 players' initial 2 settlements (beginner layout)
    // Each player gets 2 settlements; chosen to satisfy distance rule on the fixed board
    private static final int[][] INITIAL_SETTLEMENT_POSITIONS = {
        {0, 32},   // Player 0
        {4, 30},   // Player 1
        {11, 40},  // Player 2
        {14, 48},  // Player 3
    };

    // Each initial settlement gets one adjacent road
    private static final int[][][] INITIAL_ROAD_POSITIONS = {
        {{0, 1}, {32, 33}},   // Player 0 roads
        {{4, 5}, {30, 31}},   // Player 1 roads
        {{11, 12}, {40, 42}}, // Player 2 roads
        {{14, 24}, {48, 49}}, // Player 3 roads
    };

    public Game(int maxRounds) {
        this.maxRounds = maxRounds;
        this.victoryPointsToWin = VICTORY_POINTS_DEFAULT;
        this.login = new Login();
    }

    public void initializeNewGame() {
        rng = new Random();
        board = new Board();
        rules = new Rules();
        dice = new Dice(rng);
        roundNumber = 0;

        // Create players
        Colour[] colours = Colour.values();
        players = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            players.add(new Player(i, colours[i]));
        }

        // Place initial settlements and roads (free, no resource cost)
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Player player = players.get(p);
            for (int s = 0; s < INITIAL_SETTLEMENTS; s++) {
                int intId = INITIAL_SETTLEMENT_POSITIONS[p][s];
                board.placeSettlement(player.getPlayerId(), intId);
                player.recordPlacedSettlement(intId);
                player.addVictoryPoints(1);

                int roadA = INITIAL_ROAD_POSITIONS[p][s][0];
                int roadB = INITIAL_ROAD_POSITIONS[p][s][1];
                board.placeRoad(player.getPlayerId(), roadA, roadB);
                player.recordPlacedRoad(roadA, roadB);
            }
            // Give starting resources from second settlement's adjacent tiles
            int secondSettlement = INITIAL_SETTLEMENT_POSITIONS[p][1];
            for (int tileId : board.getAdjacentTileIds(secondSettlement)) {
                Tile tile = board.getTile(tileId);
                if (tile.getResource() != Resource.DESERT) {
                    player.addResource(tile.getResource(), 1);
                }
            }
        }

        login.printGameStart(maxRounds);
    }

    public void runSimulationUntilTermination() {
        while (!isTerminationReached()) {
            playOneRound();
        }
        Player winner = getWinnerOrNull();
        login.printGameOver(winner, roundNumber);
    }

    public void playOneRound() {
        roundNumber++;
        int roll = rollDice();
        distributeResourcesForRoll(roll);
        for (Player player : players) {
            playOneTurn(player);
            if (isTerminationReached()) break;
        }
        printRoundScoreboard();
    }

    public void playOneTurn(Player player) {
        List<Action> validActions = rules.getValidActionsByLinearScan(player, board);
        Action chosen = rules.chooseRandomAction(validActions, rng);

        switch (chosen.getType()) {
            case BUILD_ROAD:
                player.payCost(rules.getCost(ActionType.BUILD_ROAD));
                board.placeRoad(player.getPlayerId(),
                        chosen.getEdgeIntersectionA(), chosen.getEdgeIntersectionB());
                player.recordPlacedRoad(chosen.getEdgeIntersectionA(), chosen.getEdgeIntersectionB());
                break;
            case BUILD_SETTLEMENT:
                player.payCost(rules.getCost(ActionType.BUILD_SETTLEMENT));
                board.placeSettlement(player.getPlayerId(), chosen.getIntersectionId());
                player.recordPlacedSettlement(chosen.getIntersectionId());
                player.addVictoryPoints(1);
                break;
            case BUILD_CITY:
                player.payCost(rules.getCost(ActionType.BUILD_CITY));
                board.upgradeSettlementToCity(player.getPlayerId(), chosen.getIntersectionId());
                player.recordUpgradedCity(chosen.getIntersectionId());
                player.addVictoryPoints(1); // Settlement was 1 VP, city is 2 VP → net +1
                break;
            case PASS:
                break;
        }

        login.printTurnAction(roundNumber, player.getPlayerId(), chosen.describeForLogin());
    }

    public int rollDice() {
        return dice.rollTwoSixSidedDice();
    }

    public void distributeResourcesForRoll(int roll) {
        for (Tile tile : board.getTiles()) {
            if (!tile.producesOnRoll(roll)) continue;
            Resource resource = tile.getResource();
            int tileId = tile.getTileId();
            // Find all intersections adjacent to this tile that have buildings
            for (int intId : board.getAllIntersectionIds()) {
                if (!board.getAdjacentTileIds(intId).contains(tileId)) continue;
                Intersection inter = board.getIntersection(intId);
                if (inter.isEmpty()) continue;
                int ownerId = inter.getBuildingOwnerId();
                Player owner = players.get(ownerId);
                int amount = (inter.getBuilding() == Building.CITY) ? 2 : 1;
                owner.addResource(resource, amount);
            }
        }
    }

    public boolean isTerminationReached() {
        if (roundNumber >= maxRounds) return true;
        return getWinnerOrNull() != null;
    }

    public Player getWinnerOrNull() {
        for (Player player : players) {
            if (player.getVictoryPoints() >= victoryPointsToWin) {
                return player;
            }
        }
        return null;
    }

    public void printRoundScoreboard() {
        System.out.println("--- Round " + roundNumber + " Scoreboard ---");
        for (Player player : players) {
            System.out.println("  Player " + player.getPlayerId()
                    + " (" + player.getColour() + "): "
                    + player.getVictoryPoints() + " VP, "
                    + player.getTotalCardsInHand() + " cards");
        }
    }
}

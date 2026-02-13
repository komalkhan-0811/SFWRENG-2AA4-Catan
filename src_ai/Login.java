public class Login {
    private final String actionFormatTemplate;

    public Login() {
        this.actionFormatTemplate = "[Round %d] Player %d: %s";
    }

    public void printTurnAction(int roundNumber, int playerId, String actionText) {
        System.out.println(String.format(actionFormatTemplate, roundNumber, playerId, actionText));
    }

    public void printGameStart(int maxRounds) {
        System.out.println("=== CATAN SIMULATION STARTED (max " + maxRounds + " rounds) ===");
    }

    public void printGameOver(Player winnerOrNull, int roundNumber) {
        System.out.println("=== GAME OVER at round " + roundNumber + " ===");
        if (winnerOrNull != null) {
            System.out.println("Winner: Player " + winnerOrNull.getPlayerId()
                    + " (" + winnerOrNull.getColour() + ") with "
                    + winnerOrNull.getVictoryPoints() + " victory points!");
        } else {
            System.out.println("No winner - maximum rounds reached.");
        }
    }
}

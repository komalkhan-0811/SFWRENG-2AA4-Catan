public class Demonstrator {
    public static void main(String[] args) {
        int maxRounds = 100;
        if (args.length > 0) {
            try {
                maxRounds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument, using default max rounds: " + maxRounds);
            }
        }

        Game game = new Game(maxRounds);
        game.initializeNewGame();
        game.runSimulationUntilTermination();
    }
}

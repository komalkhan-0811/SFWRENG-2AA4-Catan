import java.util.Random;

public class Dice {
    private final Random rng;

    public Dice(Random rng) {
        this.rng = rng;
    }

    public int rollTwoSixSidedDice() {
        return (rng.nextInt(6) + 1) + (rng.nextInt(6) + 1);
    }
}

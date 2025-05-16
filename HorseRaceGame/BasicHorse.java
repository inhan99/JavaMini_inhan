package HorseRace;

import java.util.Random;

class BasicHorse implements Horse {
    private static final Random RNG = new Random();

    private double x;
    private final int y;
    private final int number;
    private final String name;
    private final double speedMin;
    private final double speedMax;
    private final double boostChance;
    private final double boostAmount;

    BasicHorse(int number, HorseSpec spec, int y) {
        this.x = 0;
        this.y = y;
        this.number = number;
        this.name = spec.name();
        this.speedMin = spec.speedMin();
        this.speedMax = spec.speedMax();
        this.boostChance = spec.boostChance();
        this.boostAmount = spec.boostAmount();
    }

    @Override
    public void run() {
        double move = RNG.nextDouble() * (speedMax - speedMin) + speedMin;
        if (RNG.nextDouble() < boostChance) {
            move += boostAmount;
        }
        x += Math.max(move, 0);
    }

    @Override public double getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getNumber() { return number; }
    @Override public String getName() { return name; }
}
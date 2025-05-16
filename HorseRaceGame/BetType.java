package HorseRace;

enum BetType {
    단승식(1), 복승식(2), 쌍승식(2), 삼복승식(3), 삼쌍승식(3);

    public final int pickCount;

    BetType(int pickCount) { this.pickCount = pickCount; }

    @Override
    public String toString() { return name(); }
}
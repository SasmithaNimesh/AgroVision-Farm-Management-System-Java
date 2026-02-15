public class FertilizerTank {
    private double level = 50 + (Math.random() * 30);

    public double getLevel() { return level; }

    public void setLevel(double l) {
        level = Math.max(0, Math.min(100, l));
    }

    public void update(boolean fertilized) {
        if (fertilized)
            level = Math.min(100, level + 20);
        else
            level = Math.max(0, level - 1);
    }
}

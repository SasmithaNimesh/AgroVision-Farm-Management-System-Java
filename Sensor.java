public class Sensor {
    private double moisture = 50 + (Math.random() * 30);

    public double getMoisture() { return moisture; }

    public void setMoisture(double m) {
        moisture = Math.max(0, Math.min(100, m));
    }

    public void update(Climate climate, boolean wasWatered) {
        if (wasWatered)
            moisture = Math.min(100, moisture + 30);
        else
            moisture = Math.max(0, moisture - (climate == Climate.DRY_SEASON ? 2.5 : 1.0));
    }
}

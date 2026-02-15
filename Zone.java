public class Zone {
    private final int id;
    private final ZoneType type;
    private final Crop crop;
    private volatile String lastAction = "Idle";
    private Sensor sensor;
    private FertilizerTank fertilizerTank;

    public Zone(int id, ZoneType type, Crop crop) {
        this.id = id;
        this.type = type;
        this.crop = crop;
        this.sensor = new Sensor();
        this.fertilizerTank = new FertilizerTank();
    }

    public int getId() { return id; }
    public ZoneType getType() { return type; }
    public Crop getCrop() { return crop; }
    public String getLastAction() { return lastAction; }
    public void setLastAction(String s) { lastAction = s; }
    public Sensor getSensor() { return sensor; }
    public FertilizerTank getFertilizerTank() { return fertilizerTank; }
}

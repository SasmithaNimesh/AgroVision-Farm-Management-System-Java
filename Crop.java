public class Crop {
    private final String name;
    private final double waterRequired;
    private final double fertilizerRequired;

    public Crop(String name, double waterRequired, double fertilizerRequired) {
        this.name = name;
        this.waterRequired = waterRequired;
        this.fertilizerRequired = fertilizerRequired;
    }

    public String getName() { return name; }
    public double getWaterRequired() { return waterRequired; }
    public double getFertilizerRequired() { return fertilizerRequired; }
}

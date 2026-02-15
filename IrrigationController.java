import java.util.*;
import java.util.concurrent.*;

public class IrrigationController {
    private List<Zone> zones = new ArrayList<>();
    private NotificationManager notifier;
    private Climate climate = Climate.NORMAL;
    private boolean autoMode = true;
    private ScheduledExecutorService executor;

    public IrrigationController(NotificationManager notifier) {
        this.notifier = notifier;
    }

    public void startSimulation() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::simulateCycle, 0, 2, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        if (executor != null) executor.shutdownNow();
    }

    private void simulateCycle() {
        for (Zone z : zones) {
            boolean watered = false, fertilized = false;

            if (autoMode) {
                if (z.getSensor().getMoisture() < 40) {
                    manualWater(z.getId());
                    watered = true;
                }
                if (z.getFertilizerTank().getLevel() < 40) {
                    manualFertilize(z.getId());
                    fertilized = true;
                }
            }

            z.getSensor().update(climate, watered);
            z.getFertilizerTank().update(fertilized);
        }
    }

    public void manualWater(int id) {
        getZoneById(id).ifPresent(z -> {
            z.getSensor().setMoisture(z.getSensor().getMoisture() + 30);
            z.setLastAction("Watered");
            notifier.add("Zone " + id + " watered manually.");
        });
    }

    public void manualFertilize(int id) {
        getZoneById(id).ifPresent(z -> {
            z.getFertilizerTank().setLevel(z.getFertilizerTank().getLevel() + 20);
            z.setLastAction("Fertilized");
            notifier.add("Zone " + id + " fertilized manually.");
        });
    }

    public void manualFixZone(int id) {
        manualWater(id);
        manualFertilize(id);
    }

    public void addZone(Zone z) { zones.add(z); notifier.add("Zone " + z.getId() + " added."); }
    public void removeZoneById(int id) { zones.removeIf(z -> z.getId() == id); notifier.add("Zone " + id + " removed."); }
    public List<Zone> getZones() { return zones; }
    public Optional<Zone> getZoneById(int id) { return zones.stream().filter(z -> z.getId() == id).findFirst(); }

    public void setClimate(Climate c) { climate = c; }
    public void setAutoMode(boolean m) { autoMode = m; }
}

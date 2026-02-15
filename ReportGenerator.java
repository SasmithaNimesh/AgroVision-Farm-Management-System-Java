public class ReportGenerator {
    private IrrigationController controller;
    private NotificationManager notifier;

    public ReportGenerator(IrrigationController c, NotificationManager n) {
        controller = c;
        notifier = n;
    }

    public String generateDailyReport() {
        return "Daily Report: " + controller.getZones().size() + " zones monitored.";
    }

    public String generateMonthlyReport() {
        return "Monthly Report: " + controller.getZones().size() + " zones analyzed.";
    }

    public String generateHarvestReport() {
        return "Harvest Report: crops growing successfully.";
    }
}

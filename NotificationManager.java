import java.util.*;

public class NotificationManager {
    private List<String> logs = new ArrayList<>();

    public void add(String msg) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        logs.add("[" + time + "] " + msg);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clear() {
        logs.clear();
    }

    public String exportLog() {
        return "Alerts exported successfully (" + logs.size() + " items)";
    }
}

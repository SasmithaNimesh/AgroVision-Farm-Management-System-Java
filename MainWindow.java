import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class MainWindow extends JFrame {

    private IrrigationController controller;
    private NotificationManager notifier;
    private ReportGenerator reportGenerator;

    private JPanel zonesPanel;
    private DefaultListModel<String> notificationListModel;
    private JList<String> notificationList;
    private JComboBox<String> climateCombo;
    private JToggleButton modeToggle;
    private JButton addZoneBtn, removeZoneBtn, startStopBtn;
    private JTextField zoneIdField, cropNameField, waterReqField, fertReqField;
    private JComboBox<String> zoneTypeCombo;
    private JLabel statusLabel;
    private JButton dailyReportBtn, monthlyReportBtn, harvestReportBtn, exportAlertsBtn;

    private volatile boolean simulationRunning = false;
    private List<ZoneCard> zoneCards = new ArrayList<>();

    public MainWindow() {
        super("AgroVision - Smart Farming System (Simulation)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        notifier = new NotificationManager();
        controller = new IrrigationController(notifier);
        reportGenerator = new ReportGenerator(controller, notifier);

        buildUI();
        setSimulationRunning(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));

        // ---------- TOP BAR ----------
        JPanel topBar = new JPanel(new BorderLayout(8,8));
        topBar.setBorder(new EmptyBorder(8,8,8,8));
        JLabel title = new JLabel("AgroVision — Smart Farming Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(10, 90, 10));
        topBar.add(title, BorderLayout.WEST);
        statusLabel = new JLabel("Status: Idle");
        topBar.add(statusLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ---------- LEFT PANEL (Controls) ----------
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(320,0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JPanel addPanel = new JPanel(new GridLayout(0,2,6,6));
        addPanel.setBorder(new EmptyBorder(8,8,8,8));
        addPanel.add(new JLabel("Zone ID:"));
        zoneIdField = new JTextField(); addPanel.add(zoneIdField);

        addPanel.add(new JLabel("Zone Type:"));
        zoneTypeCombo = new JComboBox<>(new String[]{"DRY","WET","NORMAL"});
        addPanel.add(zoneTypeCombo);

        addPanel.add(new JLabel("Assign Crop? (optional)")); addPanel.add(new JLabel(""));

        addPanel.add(new JLabel("Crop Name:"));
        cropNameField = new JTextField(); addPanel.add(cropNameField);

        addPanel.add(new JLabel("Water Req (L/day):"));
        waterReqField = new JTextField(); addPanel.add(waterReqField);

        addPanel.add(new JLabel("Fertilizer Req (kg/day):"));
        fertReqField = new JTextField(); addPanel.add(fertReqField);

        addZoneBtn = new JButton("Add Zone");
        removeZoneBtn = new JButton("Remove Zone");
        JPanel addBtnRow = new JPanel(new GridLayout(1,2,5,6));
        addBtnRow.add(addZoneBtn); addBtnRow.add(removeZoneBtn);
        addPanel.add(addBtnRow);
        leftPanel.add(addPanel);

        // Simulation Settings
        JPanel modePanel = new JPanel(new GridLayout(0,1,6,6));
        modePanel.setBorder(new CompoundBorder(new EmptyBorder(6,6,6,6),
                BorderFactory.createTitledBorder("Simulation Settings")));

        climateCombo = new JComboBox<>(new String[]{"DRY_SEASON","RAINY_SEASON"});
        modePanel.add(new JLabel("Climate:")); modePanel.add(climateCombo);

        modePanel.add(new JLabel("Mode:"));
        modeToggle = new JToggleButton("Auto Mode (ON)",true);
        modePanel.add(modeToggle);

        startStopBtn = new JButton("Stop Simulation"); modePanel.add(startStopBtn);
        leftPanel.add(modePanel);

        // Manual actions
        JPanel manualPanel = new JPanel(new GridLayout(1,2,6,6));
        manualPanel.setBorder(new CompoundBorder(new EmptyBorder(6,6,6,6),
                BorderFactory.createTitledBorder("Manual Actions")));
        JButton waterBtn = new JButton("Water Selected Zone");
        JButton fertBtn = new JButton("Fertilize Selected Zone");
        manualPanel.add(waterBtn); manualPanel.add(fertBtn);
        leftPanel.add(manualPanel);

        add(leftPanel, BorderLayout.WEST);

        // ---------- CENTER PANEL (Zones) ----------
        zonesPanel = new JPanel(new GridLayout(0,3,12,12));
        JScrollPane zonesScroll = new JScrollPane(zonesPanel);
        zonesScroll.setBorder(BorderFactory.createTitledBorder("Zones"));
        add(zonesScroll, BorderLayout.CENTER);

        // ---------- RIGHT PANEL (Reports & Alerts) ----------
        JPanel rightPanel = new JPanel(new BorderLayout(8,8));
        rightPanel.setPreferredSize(new Dimension(360,0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Reports & Alerts"));

        JPanel reportPanel = new JPanel(new GridLayout(0,1,6,6));
        reportPanel.setBorder(new CompoundBorder(new EmptyBorder(6,6,6,6),
                BorderFactory.createTitledBorder("Reports & Exports")));

        dailyReportBtn = new JButton("Generate Daily Report");
        monthlyReportBtn = new JButton("Generate Monthly Report");
        harvestReportBtn = new JButton("Generate Harvest Report");
        exportAlertsBtn = new JButton("Export Alerts Log");

        reportPanel.add(dailyReportBtn);
        reportPanel.add(monthlyReportBtn);
        reportPanel.add(harvestReportBtn);
        reportPanel.add(exportAlertsBtn);

        rightPanel.add(reportPanel, BorderLayout.NORTH);

        notificationListModel = new DefaultListModel<>();
        notificationList = new JList<>(notificationListModel);
        JScrollPane notifScroll = new JScrollPane(notificationList);
        notifScroll.setBorder(BorderFactory.createTitledBorder("Alerts & Logs"));
        rightPanel.add(notifScroll, BorderLayout.CENTER);

        JButton clearAlertsBtn = new JButton("Clear Alerts");
        rightPanel.add(clearAlertsBtn, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // ---------- ACTIONS ----------
        addZoneBtn.addActionListener(e -> onAddZone());
        removeZoneBtn.addActionListener(e -> onRemoveZone());
        startStopBtn.addActionListener(e -> setSimulationRunning(!simulationRunning));

        modeToggle.addActionListener(e -> {
            controller.setAutoMode(modeToggle.isSelected());
            modeToggle.setText(modeToggle.isSelected() ? "Auto Mode (ON)" : "Auto Mode (OFF)");
        });

        waterBtn.addActionListener(e -> { ZoneCard z = chooseZoneCard(); if(z!=null) { controller.manualWater(z.zone.getId()); refreshAllZoneCards(); }});
        fertBtn.addActionListener(e -> { ZoneCard z = chooseZoneCard(); if(z!=null) { controller.manualFertilize(z.zone.getId()); refreshAllZoneCards(); }});

        dailyReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Daily report: "+reportGenerator.generateDailyReport()));
        monthlyReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Monthly report: "+reportGenerator.generateMonthlyReport()));
        harvestReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Harvest report: "+reportGenerator.generateHarvestReport()));
        exportAlertsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Alerts exported: "+notifier.exportLog()));
        clearAlertsBtn.addActionListener(e -> { notifier.clear(); rebuildNotifications(); });

        ScheduledExecutorService uiPush = Executors.newSingleThreadScheduledExecutor();
        uiPush.scheduleAtFixedRate(this::updateAllUI,0,1,TimeUnit.SECONDS);

        controller.setClimate(Climate.valueOf((String) climateCombo.getSelectedItem()));
        climateCombo.addActionListener(e -> controller.setClimate(Climate.valueOf((String) climateCombo.getSelectedItem())));
        controller.setAutoMode(modeToggle.isSelected());
    }

    // ------------ Methods for updating and managing zones ------------
    private void setSimulationRunning(boolean running) {
        simulationRunning = running;
        if(running) {
            statusLabel.setText("Status: Running");
            startStopBtn.setText("Stop Simulation");
            controller.startSimulation();
        } else {
            statusLabel.setText("Status: Stopped");
            startStopBtn.setText("Start Simulation");
            controller.stopSimulation();
        }
    }

    private void onAddZone() {
        try {
            int id = Integer.parseInt(zoneIdField.getText().trim());
            ZoneType zt = ZoneType.valueOf((String) zoneTypeCombo.getSelectedItem());
            Crop crop = null;
            if(!cropNameField.getText().trim().isEmpty())
                crop = new Crop(cropNameField.getText().trim(), Double.parseDouble(waterReqField.getText().trim()), Double.parseDouble(fertReqField.getText().trim()));
            for(Zone z : controller.getZones())
                if(z.getId()==id){ JOptionPane.showMessageDialog(this,"Zone ID exists","Error",JOptionPane.ERROR_MESSAGE); return;}
            Zone zone = new Zone(id, zt, crop);
            controller.addZone(zone);
            refreshZonesUI();
            zoneIdField.setText(""); cropNameField.setText(""); waterReqField.setText(""); fertReqField.setText("");
        } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid input","Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void onRemoveZone() {
        List<Zone> zones = controller.getZones();
        if(zones.isEmpty()){ JOptionPane.showMessageDialog(this,"No zones available","Info",JOptionPane.INFORMATION_MESSAGE); return;}
        String[] ids = zones.stream().map(z->String.valueOf(z.getId())).toArray(String[]::new);
        JComboBox<String> combo = new JComboBox<>(ids); combo.setSelectedIndex(0);
        int result = JOptionPane.showConfirmDialog(this, combo, "Remove Zone", JOptionPane.OK_CANCEL_OPTION);
        if(result==JOptionPane.OK_OPTION){
            int id = Integer.parseInt((String)combo.getSelectedItem());
            controller.removeZoneById(id); refreshZonesUI(); rebuildNotifications();
        }
    }

    private ZoneCard chooseZoneCard() {
        List<Zone> zones = controller.getZones();
        if(zones.isEmpty()) return null;
        String[] options = zones.stream().map(z->"Zone "+z.getId()).toArray(String[]::new);
        String choice = (String)JOptionPane.showInputDialog(this,"Select zone:","Choose Zone",JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        if(choice==null) return null;
        int id = Integer.parseInt(choice.replace("Zone ",""));
        for(ZoneCard zc: zoneCards) if(zc.zone.getId()==id) return zc;
        return null;
    }

    private void refreshZonesUI() {
        zonesPanel.removeAll(); zoneCards.clear();
        for(Zone z: controller.getZones()){
            ZoneCard card = new ZoneCard(z, controller, notifier);
            zonesPanel.add(card); zoneCards.add(card);
        }
        zonesPanel.revalidate(); zonesPanel.repaint();
    }

    private void refreshAllZoneCards(){ for(ZoneCard c: zoneCards) c.updateUIFromZone();}
    private void updateAllUI(){ rebuildNotifications(); refreshAllZoneCards();}
    private void rebuildNotifications(){
        List<String> logs = notifier.getLogs();
        SwingUtilities.invokeLater(()->{ notificationListModel.clear(); logs.forEach(notificationListModel::addElement); });
    }

    // --------- INNER CLASS (ZoneCard) ----------
    static class ZoneCard extends JPanel {
        Zone zone; private IrrigationController controller; private NotificationManager notifier;
        private JProgressBar moistureBar, fertBar; private JLabel cropLabel,statusLabel,idLabel;

        public ZoneCard(Zone z,IrrigationController c,NotificationManager n){
            zone=z; controller=c; notifier=n; build();
        }

        private void build(){
            setPreferredSize(new Dimension(260,180)); setMaximumSize(new Dimension(260,180));
            setBorder(new CompoundBorder(new LineBorder(Color.GRAY),new EmptyBorder(8,8,8,8)));
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setBackground(Color.WHITE);

            idLabel = new JLabel("Zone "+zone.getId()+" ("+zone.getType()+")");
            idLabel.setFont(new Font("Segoe UI",Font.BOLD,14)); idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(idLabel); add(Box.createVerticalStrut(5));

            cropLabel = new JLabel("Crop: "+(zone.getCrop()==null?"—":zone.getCrop().getName()));
            cropLabel.setAlignmentX(Component.CENTER_ALIGNMENT); add(cropLabel); add(Box.createVerticalStrut(5));

            moistureBar = new JProgressBar(0,100); moistureBar.setStringPainted(true); add(new LabeledPanel("Moisture",moistureBar)); add(Box.createVerticalStrut(1));
            fertBar = new JProgressBar(0,100); fertBar.setStringPainted(true); add(new LabeledPanel("Fertilizer",fertBar)); add(Box.createVerticalStrut(1));

            JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false);
            statusLabel = new JLabel("Status: Idle"); bottom.add(statusLabel,BorderLayout.WEST);
            JButton manualFix = new JButton("Auto-Fix Now");
            manualFix.addActionListener(e->{ controller.manualFixZone(zone.getId()); updateUIFromZone(); });
            bottom.add(manualFix,BorderLayout.EAST); add(bottom);

            updateUIFromZone();
        }

        public void updateUIFromZone(){
            SwingUtilities.invokeLater(()->{
                moistureBar.setValue((int)zone.getSensor().getMoisture());
                moistureBar.setString(String.format("%.0f%%",zone.getSensor().getMoisture()));
                fertBar.setValue((int)zone.getFertilizerTank().getLevel());
                fertBar.setString(String.format("%.0f%%",zone.getFertilizerTank().getLevel()));
                cropLabel.setText("Crop: "+(zone.getCrop()==null?"—":zone.getCrop().getName()));
                statusLabel.setText("Status: "+(zone.getLastAction()==null?"Idle":zone.getLastAction()));

                double worst = Math.min(zone.getSensor().getMoisture(),zone.getFertilizerTank().getLevel());
                if(worst<25) setBackground(new Color(255,210,210));
                else if(worst<50) setBackground(new Color(255,245,210));
                else setBackground(new Color(220,255,220));
            });
        }
    }

    static class LabeledPanel extends JPanel {
        public LabeledPanel(String title,JComponent comp){
            setLayout(new BorderLayout(6,6));
            add(new JLabel(title),BorderLayout.WEST);
            add(comp,BorderLayout.CENTER);
            setOpaque(false);
        }
    }
}

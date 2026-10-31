package jmri.jmrix.marklin.swing.devicetable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.McanDeviceService;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.McanDeviceManager;
import jmri.jmrix.marklin.WeichenChefMagnetService;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.jmrix.marklin.swing.MarklinPanel;
import jmri.jmrix.marklin.swing.configure.ReporterConfigPanel;
import jmri.jmrix.marklin.swing.configure.WeichenChefKombiConfigPanel;
import jmri.util.JmriJFrame;

/**
 * Panel displaying discovered MCAN devices (Märklin and CdB) with discovery controls.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanDevicePanel extends MarklinPanel {

    /** CdB class IDs for which a config panel is available. */
    private static final Set<Integer> CONFIGURABLE_CLASSES = Set.of(0x0B, 0x0D, 0x27);

    private JButton discoverMarklinButton;
    private JButton discoverCdbButton;
    private JButton configureButton;
    private JButton forgetButton;
    private JButton probeButton;
    private final Map<CdBDeviceClass, JCheckBox> cdbClassCheckBoxes = new HashMap<>();

    private McanDeviceTableDataModel dataModel;
    private JTable deviceTable;

    public McanDevicePanel() {
        super();
    }

    @Override
    public void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        discoverMarklinButton = new JButton("Discover Marklin");
        discoverMarklinButton.setToolTipText("Send MCAN PING query to discover Märklin devices (CS2, MS2, Gleisbox, etc.)");
        discoverMarklinButton.addActionListener(this::discoverMarklinActionPerformed);
        buttonRow.add(discoverMarklinButton);

        discoverCdbButton = new JButton("Discover CdB");
        discoverCdbButton.setToolTipText("Send CdB PING query for selected CdB classes");
        discoverCdbButton.addActionListener(this::discoverCdbActionPerformed);
        buttonRow.add(discoverCdbButton);

        configureButton = new JButton("Configure");
        configureButton.setToolTipText("Open configuration panel for the selected CdB device");
        configureButton.setEnabled(false);
        configureButton.addActionListener(this::configureActionPerformed);
        buttonRow.add(configureButton);

        forgetButton = new JButton("Forget");
        forgetButton.setToolTipText("Remove the selected device from the device list");
        forgetButton.setEnabled(false);
        forgetButton.addActionListener(this::forgetActionPerformed);
        buttonRow.add(forgetButton);

        probeButton = new JButton("Probe");
        probeButton.setToolTipText("Send firmware probe to determine WeichenChef Magnet generation");
        probeButton.setEnabled(false);
        probeButton.addActionListener(this::probeActionPerformed);
        buttonRow.add(probeButton);

        topPanel.add(buttonRow);

        JPanel cdbCheckBoxRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (CdBDeviceClass deviceClass : CdBDeviceClass.values()) {
            String label = String.format("%s (0x%02X)", deviceClass.getDisplayName(), deviceClass.getClassId());
            JCheckBox checkBox = new JCheckBox(label, true);
            cdbClassCheckBoxes.put(deviceClass, checkBox);
            cdbCheckBoxRow.add(checkBox);
        }
        topPanel.add(cdbCheckBoxRow);

        add(topPanel, BorderLayout.NORTH);

        deviceTable = new JTable();
        add(new JScrollPane(deviceTable), BorderLayout.CENTER);
    }

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
        initComponents();

        McanDeviceManager mcanDeviceManager = null;
        if (memo != null) {
            mcanDeviceManager = memo.getMcanDeviceManager();
        }

        dataModel = new McanDeviceTableDataModel(mcanDeviceManager);
        deviceTable.setModel(dataModel);

        deviceTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateConfigureButtonState();
            }
        });
    }

    private void updateConfigureButtonState() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow < 0) {
            configureButton.setEnabled(false);
            forgetButton.setEnabled(false);
            probeButton.setEnabled(false);
            return;
        }
        McanDevice device = dataModel.getDeviceAt(selectedRow);
        if (device == null) {
            configureButton.setEnabled(false);
            forgetButton.setEnabled(false);
            probeButton.setEnabled(false);
            return;
        }
        forgetButton.setEnabled(true);
        McanDeviceIdentity identity = device.getIdentity();
        if (!identity.isCdB()) {
            configureButton.setEnabled(false);
            probeButton.setEnabled(false);
            return;
        }
        int canClass = ((McanDeviceIdentity.CdB) identity).getCanClass();
        configureButton.setEnabled(CONFIGURABLE_CLASSES.contains(canClass));
        probeButton.setEnabled(canClass == WeichenChefMagnetService.CDB_CLASS_ID);
    }

    private void discoverMarklinActionPerformed(ActionEvent e) {
        if (memo == null || memo.getMcanDeviceManager() == null) {
            return;
        }
        memo.getMcanDeviceManager().discoverMarklinDevices();
    }

    private void discoverCdbActionPerformed(ActionEvent e) {
        if (memo == null || memo.getMcanDeviceManager() == null) {
            return;
        }

        List<CdBDeviceClass> selected = new ArrayList<>();
        for (CdBDeviceClass deviceClass : CdBDeviceClass.values()) {
            JCheckBox cb = cdbClassCheckBoxes.get(deviceClass);
            if (cb != null && cb.isSelected()) {
                selected.add(deviceClass);
            }
        }
        memo.getMcanDeviceManager().discoverCdBDevices(selected);
    }

    private void forgetActionPerformed(ActionEvent e) {
        if (memo == null || memo.getMcanDeviceManager() == null) {
            return;
        }
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow < 0) return;
        McanDevice device = dataModel.getDeviceAt(selectedRow);
        if (device == null) return;
        memo.getMcanDeviceManager().deregister(device);
    }

    private void configureActionPerformed(ActionEvent e) {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow < 0) return;
        McanDevice device = dataModel.getDeviceAt(selectedRow);
        if (device == null) return;
        openConfigurePanel(device);
    }

    private void probeActionPerformed(ActionEvent e) {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow < 0) return;
        McanDevice device = dataModel.getDeviceAt(selectedRow);
        if (device == null) return;
        McanDeviceService svc = device.getDeviceService();
        if (svc instanceof WeichenChefMagnetService) {
            ((WeichenChefMagnetService) svc).sendGenerationProbe();
        }
    }

    private void openConfigurePanel(McanDevice device) {
        McanDeviceIdentity identity = device.getIdentity();
        if (!identity.isCdB()) return;
        int canClass = ((McanDeviceIdentity.CdB) identity).getCanClass();

        MarklinPanel configPanel;
        String title;
        if (canClass == 0x27) {
            configPanel = new WeichenChefKombiConfigPanel(device, memo);
            title = "WeichenChef Kombi – " + device.getSystemName();
        } else {
            configPanel = new ReporterConfigPanel(device, memo);
            title = "Reporter – " + device.getSystemName();
        }

        JmriJFrame frame = new JmriJFrame(title);
        frame.add(configPanel);
        frame.pack();
        frame.setVisible(true);
        MarklinPanel panelRef = configPanel;
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                panelRef.dispose();
            }
        });
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return "MCAN Devices (" + memo.getUserName() + ")";
        }
        return "MCAN Devices";
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.devicetable.McanDevicePanel";
    }

    public JButton getDiscoverMarklinButton() {
        return discoverMarklinButton;
    }

    public JButton getDiscoverCdbButton() {
        return discoverCdbButton;
    }

    public JButton getConfigureButton() {
        return configureButton;
    }

    public JButton getForgetButton() {
        return forgetButton;
    }

    public JButton getProbeButton() {
        return probeButton;
    }

    public Map<CdBDeviceClass, JCheckBox> getCdbClassCheckBoxes() {
        return cdbClassCheckBoxes;
    }

    public JTable getDeviceTable() {
        return deviceTable;
    }

    public McanDeviceTableDataModel getDataModel() {
        return dataModel;
    }

    public static class Default extends jmri.jmrix.marklin.swing.MarklinNamedPaneAction {

        public Default() {
            super("MCAN Devices",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    McanDevicePanel.class.getName(),
                    jmri.InstanceManager.getDefault(MarklinSystemConnectionMemo.class));
        }
    }
}

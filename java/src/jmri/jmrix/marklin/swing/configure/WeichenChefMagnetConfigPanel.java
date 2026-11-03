package jmri.jmrix.marklin.swing.configure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.WeichenChefMagnetGeneration;
import jmri.jmrix.marklin.WeichenChefMagnetService;
import jmri.jmrix.marklin.WeichenChefMagnetService.SaveScope;
import jmri.jmrix.marklin.WeichenChefMagnetService.State;
import jmri.jmrix.marklin.mcan.address.AddressEncoding;
import jmri.jmrix.marklin.mcan.address.CdBAddressCodec;
import jmri.jmrix.marklin.mcan.cdb.CdBDeviceClass;
import jmri.jmrix.marklin.swing.MarklinPanel;

/**
 * WeichenChef Magnet configuration panel
 *
 * This tool is for the light grey case Marklin CAN versions of the modules.
 * Do not use this panel for the black case ZCAN versions, do not mix light grey and black
 * They use a different protocol and a different RJ45 pinout.
 * You might break your hardware.
 *
 * At least two main generations of WeichenChef Magnet devices exist.
 *
 * All generations of WeichenChef Magnet config tools only support "MM" and "DCC" but not "CAN" format.
 * I did not test if the hardware would support "CAN" address space.
 *
 * The vendor provides three generations of config program 001, 002 and 012 and a separate reset tool.
 * 002 and 012 seem to target WeichenChef Magnet 2.0 and 2.1 devices.
 * See https://can-digital-bahn.com/modul.php?system=all&amp;modul=62#Mod_Top
 *
 * 001 seems to target WeichenChef Magnet 1.x and WeichenChef Deluxe.
 * https://can-digital-bahn.com/modul.php?system=all&amp;modul=8#Mod_Top
 *
 * Production of WeichenChef Magnet seems to have ceased in favour of the newer lineup:
 * WeichenChef Basic (externally powered, magnet only) and WeichenChef Kombi (bus powered, motor and magnet). These support "MM", "DCC" and "CAN" each.
 *
 * The labels on the boxes do not reliably identify the devices.
 * Many devices have labels which just say "Weichenchef".
 *
 * Visual clues are the older vendor address (Itzehoe), missing status LED, colour of the PCB.
 * Some older revisions may have the programming button hidden inside the case rather than reachable next to the connectors.
 *
 * Both generations may have the traditional layout, RJ45 connectors left and right, the lower edge has a 2-wire power terminal and 4* screw terminals for three-wire turnouts.
 *
 * At least one revision of the newer generation exists which is labeled "Weichenchef Magnet" and uses a newer module layout.
 * Both RJ45 CAN ports on the left side, turnout connectors distributed on the upper and lower edge, push button opposite of the power connector.
 *
 * Advanced addressing:
 *
 * WeichenChef Magnet uses up to 9 accessory addresses.

 * Slots 1-4 normally address the first four turnouts for switching both directions thrown and closed.
 * Slot 5 activates and deactivates WeichenChef's own automation mode. In automation mode, each turnout slot reacts to a reporter/sensor signal on a specific address.
 * Slots 6-9 (labeled 1a through 4a) are only used in second address mode.
 *
 * In second address mode, address 1-4 power the left and center cords while address 1a-4a power the right and center cords.
 * This is useful for powering two separate 2-wire in-track decouplers in the same port.
 *
 * The newer generation supports separate timings for the 1a-4a slots, the older generation reuses the timings from the 1-4 slots.
 *
 * When the panel is opened it registers a listener and starts a configuration session, putting the device into "selected" mode.
 * When the panel is closed it puts the device out of selected mode (deselect command) and then unregisters the listener.
 *
 * The panel relies on the WeichenChefMagnetService to communicate with the device.
 *
 * For now JMRI relies on a feature detection mechanism to distinguish between older and newer devices.
 * It sends a request for reading a variable from the device. If a response is received and carries the expected shape, the device is assumed to have the feature and be a "second generation" device.
 *
 */
public class WeichenChefMagnetConfigPanel extends MarklinPanel implements MarklinListener {

    private final Map<Integer, JComponent> editors = new LinkedHashMap<>();
    private final List<JButton> readyButtons = new ArrayList<>();
    private final List<JPanel> extendedRows = new ArrayList<>();
    private final JLabel[] primaryTimingLabels = new JLabel[4];
    private final JLabel[] secondaryAddressLabels = new JLabel[4];
    private final JLabel[] positions = new JLabel[4];
    private final JLabel[] primarySummaries = new JLabel[4];
    private final JLabel[] secondarySummaries = new JLabel[4];
    private final JLabel[] automationSummaries = new JLabel[4];
    private final JLabel stateLabel = new JLabel();
    private final JLabel firmwareLabel = new JLabel();
    private final JLabel firmwareWarning = new JLabel();
    private final JLabel detectedLabel = new JLabel();
    private final JRadioButton automatic = new JRadioButton(Bundle.getMessage("WcMagnetAutomatic"));
    private final JRadioButton older = new JRadioButton(Bundle.getMessage("WcMagnetOlder"));
    private final JRadioButton newer = new JRadioButton(Bundle.getMessage("WcMagnetNewer"));
    private final JButton retest = new JButton(Bundle.getMessage("WcMagnetRetest"));
    private final JButton reload = new JButton(Bundle.getMessage("WcMagnetReload"));
    private final JButton reset = new JButton(Bundle.getMessage("WcKombiButtonReset"));
    private final JButton applyId = new JButton(Bundle.getMessage("WcKombiButtonApplyId"));
    private final JSpinner moduleId = new JSpinner(new SpinnerNumberModel(1, 1, 63, 1));
    private WeichenChefMagnetService service;
    private boolean ownsSession;
    private boolean updating;

    private final WeichenChefMagnetService.Listener listener = new WeichenChefMagnetService.Listener() {
        @Override
        public void stateChanged() {
            updateState();
        }

        @Override
        public void valueReceived(int register, int slot, int value) {
            updateValue(register, slot, value);
        }

        @Override
        public void positionReceived(int turnout, int status) {
            JLabel label = positions[turnout - 1];
            label.setText(Bundle.getMessage(status == 255 ? "WcKombiPositionFault"
                    : status == 253 ? "WcMagnetGreen" : "WcMagnetRed"));
            label.setBackground(status == 255 ? Color.YELLOW : status == 253 ? Color.GREEN : Color.RED);
        }
    };

    public WeichenChefMagnetConfigPanel(McanDevice device, MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
        setLayout(new BorderLayout(5, 5));
        if (memo == null || memo.getTrafficController() == null || memo.getMcanDeviceManager() == null) {
            add(new JLabel(Bundle.getMessage("WcMagnetNoConnection")));
            return;
        }
        if (!(device.getDeviceService() instanceof WeichenChefMagnetService)) {
            service = new WeichenChefMagnetService(device, frame ->
                    memo.getTrafficController().sendMarklinMessage(new MarklinMessage(frame), null));
            device.setDeviceService(service);
        } else {
            service = (WeichenChefMagnetService) device.getDeviceService();
        }
        if (service.isConfigurationOpen()) {
            add(new JLabel(Bundle.getMessage("WcMagnetAlreadyOpen")));
            return;
        }
        buildUI();
        service.addListener(listener);
        memo.getTrafficController().addMarklinListener(this);
        ownsSession = true;
        service.openConfiguration();
    }

    private void buildUI() {
        JPanel top = new JPanel(new GridLayout(0, 1));
        JPanel info = row();
        info.add(new JLabel(Bundle.getMessage("WcKombiLabelFirmware")));
        info.add(firmwareLabel);
        info.add(firmwareWarning);
        firmwareWarning.setForeground(Color.RED);
        top.add(info);
        JPanel profiles = row();
        profiles.add(detectedLabel);
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton button : new JRadioButton[]{automatic, older, newer}) {
            group.add(button);
            profiles.add(button);
        }
        automatic.setSelected(true);
        automatic.addActionListener(e -> service.sendGenerationProbe());
        older.addActionListener(e -> chooseProfile(WeichenChefMagnetGeneration.GEN_001));
        newer.addActionListener(e -> chooseProfile(WeichenChefMagnetGeneration.GEN_002_PLUS));
        retest.setName("retest");
        retest.addActionListener(e -> service.sendGenerationProbe());
        profiles.add(retest);
        top.add(profiles);
        top.add(stateLabel);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(Bundle.getMessage("WcMagnetProperties"), new JScrollPane(buildProperties()));
        tabs.addTab(Bundle.getMessage("WcMagnetAddresses"), new JScrollPane(buildAddresses()));
        tabs.addTab(Bundle.getMessage("WcMagnetAutomation"), new JScrollPane(buildFeedback()));
        add(tabs, BorderLayout.CENTER);

        JPanel buttons = row();
        reload.addActionListener(e -> service.reload());
        buttons.add(reload);
        reset.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("WcMagnetResetConfirm"),
                    getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                service.resetModule();
                rediscover();
            }
        });
        buttons.add(reset);
        buttons.add(new JLabel(Bundle.getMessage("WcKombiLabelNewModuleId")));
        buttons.add(moduleId);
        applyId.addActionListener(e -> {
            try {
                moduleId.commitEdit();
                service.writeNewModuleId((Integer) moduleId.getValue());
                rediscover();
            } catch (ParseException ex) {
                showInputError();
            }
        });
        buttons.add(applyId);
        JButton diagram = new JButton(Bundle.getMessage("WcKombiButtonWiringDiagram"));
        diagram.setEnabled(false);
        diagram.setToolTipText(Bundle.getMessage("WcMagnetDiagramUnavailable"));
        buttons.add(diagram);
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel buildProperties() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (int n = 1; n <= 4; n++) {
            final int turnout = n;
            JPanel output = new JPanel(new BorderLayout());
            output.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("WcMagnetTurnout", n)));
            JPanel flags = row();
            for (int reg : new int[]{3, 4, 5, 18, 9}) {
                JCheckBox check = new JCheckBox(Bundle.getMessage("WcMagnetFlag" + reg));
                editors.put(key(reg, n), check);
                check.setName("reg" + reg + "slot" + n);
                flags.add(check);
                check.addActionListener(e -> {
                    if (reg == 3 && check.isSelected()
                            && service.getGeneration() == WeichenChefMagnetGeneration.GEN_002_PLUS) {
                        ((JCheckBox) editors.get(key(4, turnout))).setSelected(false);
                    }
                    if (reg == 4 && check.isSelected()) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("WcMagnetK84Warning"),
                                getTitle(), JOptionPane.WARNING_MESSAGE);
                    }
                    if (reg == 4 || (reg == 5 && !check.isSelected())) {
                        clearPosition(turnout);
                    }
                    updateState();
                });
            }
            output.add(flags, BorderLayout.NORTH);
            JPanel timing = new JPanel(new GridLayout(0, 1));
            for (int slot : new int[]{n, n + 4}) {
                JPanel line = row();
                JLabel label = new JLabel(Bundle.getMessage(
                        slot <= 4 ? "WcMagnetPrimaryOutput" : "WcMagnetSecondaryOutput", n));
                label.setName("timingLabel" + slot);
                line.add(label);
                if (slot <= 4) {
                    primaryTimingLabels[n - 1] = label;
                }
                for (int reg : new int[]{2, 16, 17}) {
                    line.add(new JLabel(Bundle.getMessage("WcMagnetTiming" + reg)));
                    line.add(number(reg, slot, reg == 2 ? 100 : reg == 16 ? 40 : 35,
                            reg == 2 ? 5 : reg == 16 ? 10 : 1, reg == 2 ? 10000 : 65000));
                }
                if (slot > 4) {
                    extendedRows.add(line);
                }
                timing.add(line);
            }
            output.add(timing, BorderLayout.CENTER);
            JPanel tests = row();
            primarySummaries[n - 1] = new JLabel();
            secondarySummaries[n - 1] = new JLabel();
            tests.add(primarySummaries[n - 1]);
            tests.add(secondarySummaries[n - 1]);
            positions[n - 1] = new JLabel();
            positions[n - 1].setOpaque(true);
            clearPosition(n);
            tests.add(positions[n - 1]);
            for (boolean green : new boolean[]{false, true}) {
                JButton test = new JButton(Bundle.getMessage(green ? "WcMagnetGreen" : "WcMagnetRed"));
                test.setName("test" + n + (green ? "green" : "red"));
                test.addActionListener(e -> service.testOutput(turnout, green));
                readyButtons.add(test);
                tests.add(test);
            }
            output.add(tests, BorderLayout.SOUTH);
            panel.add(output);
        }
        panel.add(new JLabel(Bundle.getMessage("WcMagnetSecondAddressSaveHelp")));
        panel.add(saveButton(SaveScope.PROPERTIES));
        return panel;
    }

    private JPanel buildAddresses() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (int n = 1; n <= 5; n++) {
            JPanel line = row();
            line.add(new JLabel(Bundle.getMessage(n == 5 ? "WcMagnetAutomationAddress" : "WcMagnetTurnout", n)));
            AddressEditor primary = new AddressEditor();
            primary.setName("reg6slot" + n);
            editors.put(key(6, n), primary);
            line.add(primary);
            if (n <= 4) {
                secondaryAddressLabels[n - 1] = new JLabel(Bundle.getMessage("WcMagnetSecondaryOutput", n));
                line.add(secondaryAddressLabels[n - 1]);
                AddressEditor secondary = new AddressEditor();
                secondary.setName("reg8slot" + n);
                editors.put(key(8, n), secondary);
                line.add(secondary);
            }
            panel.add(line);
        }
        panel.add(new JLabel(Bundle.getMessage("WcMagnetSecondAddressSaveHelp")));
        panel.add(saveButton(SaveScope.ADDRESSES));
        return panel;
    }

    private JPanel buildFeedback() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (int n = 1; n <= 4; n++) {
            JPanel line = row();
            line.add(new JLabel(Bundle.getMessage("WcMagnetTurnout", n)));
            automationSummaries[n - 1] = new JLabel();
            line.add(automationSummaries[n - 1]);
            for (int direction = 0; direction < 2; direction++) {
                line.add(new JLabel(Bundle.getMessage(direction == 0 ? "WcMagnetRed" : "WcMagnetGreen")));
                line.add(number(7, 2 * n - 1 + direction, 1, 1, 1024));
            }
            panel.add(line);
        }
        panel.add(saveButton(SaveScope.FEEDBACK));
        return panel;
    }

    private JButton saveButton(SaveScope scope) {
        JButton save = new JButton(Bundle.getMessage("WcMagnetSave" + scope.name()));
        save.setName("save" + scope);
        save.addActionListener(e -> {
            Map<Integer, Integer> config = service.configuration(scope);
            try {
                for (int k : config.keySet()) {
                    JComponent editor = editors.get(k);
                    int value;
                    if (editor instanceof AddressEditor) {
                        value = ((AddressEditor) editor).rawValue();
                    } else if (editor instanceof JSpinner) {
                        ((JSpinner) editor).commitEdit();
                        value = (Integer) ((JSpinner) editor).getValue();
                    } else {
                        value = ((JCheckBox) editor).isSelected() ? 1 : 0;
                    }
                    config.put(k, value);
                }
                service.save(scope, config);
            } catch (ParseException | IllegalArgumentException ex) {
                showInputError();
            }
        });
        readyButtons.add(save);
        return save;
    }

    private void chooseProfile(WeichenChefMagnetGeneration selected) {
        if (selected != service.getDetectedGeneration()
                && JOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("WcMagnetOverrideWarning", profileName(service.getDetectedGeneration())),
                        getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            selectRadio();
            return;
        }
        service.useProfile(selected);
    }

    private void selectRadio() {
        WeichenChefMagnetGeneration selected = service.getGeneration();
        if (selected == WeichenChefMagnetGeneration.GEN_001) {
            older.setSelected(true);
        } else if (selected == WeichenChefMagnetGeneration.GEN_002_PLUS) {
            newer.setSelected(true);
        } else {
            automatic.setSelected(true);
        }
    }

    private String profileName(WeichenChefMagnetGeneration generation) {
        return Bundle.getMessage(generation == WeichenChefMagnetGeneration.UNKNOWN ? "WcMagnetUnknown"
                : generation == WeichenChefMagnetGeneration.GEN_001 ? "WcMagnetOlder" : "WcMagnetNewer");
    }

    private void updateState() {
        selectRadio();
        boolean ready = service.isReady();
        boolean extended = service.getGeneration() == WeichenChefMagnetGeneration.GEN_002_PLUS;
        boolean writing = service.getState() == State.WRITING;
        boolean reconnect = service.getState() == State.RECONNECT;
        stateLabel.setText(Bundle.getMessage("WcMagnetState" + service.getState().name()));
        detectedLabel.setText(Bundle.getMessage("WcMagnetDetected", profileName(service.getDetectedGeneration())));
        readyButtons.forEach(b -> b.setEnabled(ready));
        extendedRows.forEach(row -> row.setVisible(extended));
        for (int n = 1; n <= 4; n++) {
            boolean secondAddress = ((JCheckBox) editors.get(key(9, n))).isSelected();
            primaryTimingLabels[n - 1].setText(Bundle.getMessage(
                    service.getGeneration() == WeichenChefMagnetGeneration.GEN_001
                            ? "WcMagnetSharedOutputs" : "WcMagnetPrimaryOutput", n));
            secondaryAddressLabels[n - 1].setEnabled(ready && secondAddress);
            secondarySummaries[n - 1].setEnabled(ready && secondAddress);
            for (java.awt.Component component : extendedRows.get(n - 1).getComponents()) {
                if (component instanceof JLabel) {
                    component.setEnabled(ready && secondAddress);
                }
            }
        }
        editors.forEach((k, editor) -> {
            boolean enabled = ready;
            int reg = k >> 8;
            if (reg == 2 || reg == 16 || reg == 17) {
                int turnout = ((k & 0xFF) - 1) % 4 + 1;
                enabled &= ((JCheckBox) editors.get(key(reg == 2 ? 3 : 18, turnout))).isSelected();
                enabled &= (k & 0xFF) <= 4 || (extended
                        && ((JCheckBox) editors.get(key(9, turnout))).isSelected());
            } else if (reg == 8) {
                enabled &= ((JCheckBox) editors.get(key(9, k & 0xFF))).isSelected();
            }
            editor.setEnabled(enabled);
            if (editor instanceof AddressEditor) {
                ((AddressEditor) editor).setEditable(enabled);
            }
        });
        older.setEnabled(!writing && !reconnect);
        newer.setEnabled(!writing && !reconnect);
        automatic.setEnabled(!writing && !reconnect);
        retest.setEnabled(!writing && !reconnect && service.getModuleId() > 0);
        reload.setEnabled(!writing && !reconnect);
        reset.setEnabled(!writing && !reconnect);
        applyId.setEnabled(!writing && !service.isDisposed());
        moduleId.setEnabled(!writing && !service.isDisposed());
        int version = service.getFirmware();
        firmwareLabel.setText(version < 0 ? "--" : (version >> 8) + "." + (version & 0xFF));
        firmwareWarning.setText(version == 0x020F || version == 0x0210
                ? Bundle.getMessage("WcMagnetFirmwareWarning") : "");
        revalidate();
    }

    private void updateValue(int reg, int slot, int value) {
        JComponent editor = editors.get(key(reg, slot));
        updating = true;
        try {
            if (editor instanceof JSpinner) {
                ((JSpinner) editor).setValue(value);
            } else if (editor instanceof JCheckBox) {
                ((JCheckBox) editor).setSelected(value != 0);
            } else if (editor instanceof AddressEditor) {
                ((AddressEditor) editor).setRawValue(value);
                if (slot <= 4) {
                    AddressEncoding address = CdBAddressCodec.INSTANCE.decodeType2(value);
                    JLabel label = (reg == 6 ? primarySummaries : secondarySummaries)[slot - 1];
                    label.setText((reg == 6 ? "" : Bundle.getMessage("WcMagnetSecondary") + ": ")
                            + address.address() + " " + address.format());
                    automationSummaries[slot - 1].setText(primarySummaries[slot - 1].getText()
                            + " / " + secondarySummaries[slot - 1].getText());
                }
            }
        } finally {
            updating = false;
        }
        updateState();
    }

    private JSpinner number(int reg, int slot, int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.setName("reg" + reg + "slot" + slot);
        editors.put(key(reg, slot), spinner);
        return spinner;
    }

    private void clearPosition(int turnout) {
        positions[turnout - 1].setText(Bundle.getMessage("WcKombiLabelPosition"));
        positions[turnout - 1].setBackground(Color.LIGHT_GRAY);
    }

    private void rediscover() {
        if (memo.getMcanDeviceManager() != null) {
            memo.getMcanDeviceManager().discoverCdBDevice(CdBDeviceClass.WEICHEN_CHEF_MAGNET);
        }
    }

    private void showInputError() {
        JOptionPane.showMessageDialog(this, Bundle.getMessage("WcMagnetInvalidInput"),
                getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    private static JPanel row() {
        return new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
    }

    private static int key(int reg, int slot) {
        return (reg << 8) | slot;
    }

    private class AddressEditor extends JPanel {
        private final JComboBox<String> format = new JComboBox<>(new String[]{"DCC", "MM"});
        private final JSpinner address = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        AddressEditor() {
            super(new FlowLayout(FlowLayout.LEFT, 3, 0));
            add(address);
            add(format);
            format.addActionListener(e -> {
                if (!updating) {
                    int max = "MM".equals(format.getSelectedItem()) ? 320 : 9999;
                    SpinnerNumberModel model = (SpinnerNumberModel) address.getModel();
                    model.setMaximum(max);
                    if ((Integer) address.getValue() > max) {
                        address.setValue(max);
                    }
                }
            });
        }

        void setRawValue(int raw) {
            AddressEncoding decoded = CdBAddressCodec.INSTANCE.decodeType2(raw);
            format.setSelectedItem(decoded.format());
            ((SpinnerNumberModel) address.getModel()).setMaximum("MM".equals(decoded.format()) ? 320 : 9999);
            address.setValue(decoded.address());
        }

        int rawValue() throws ParseException {
            address.commitEdit();
            return CdBAddressCodec.INSTANCE.encodeType2((String) format.getSelectedItem(), (Integer) address.getValue());
        }

        void setEditable(boolean editable) {
            address.setEnabled(editable);
            format.setEnabled(editable);
        }
    }

    @Override
    public void message(MarklinMessage message) {
        // Only incoming position reports are relevant.
    }

    @Override
    public void reply(MarklinReply reply) {
        if (ownsSession) {
            service.handleStatusFrame(reply);
        }
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("WcMagnetTitle");
    }

    @Override
    public void dispose() {
        if (ownsSession) {
            service.removeListener(listener);
            memo.getTrafficController().removeMarklinListener(this);
            service.closeConfiguration();
            ownsSession = false;
        }
        super.dispose();
    }
}

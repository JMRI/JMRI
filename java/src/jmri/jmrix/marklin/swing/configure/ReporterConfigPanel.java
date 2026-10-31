package jmri.jmrix.marklin.swing.configure;

import java.awt.*;
import javax.swing.*;
import jmri.jmrix.marklin.*;
import jmri.jmrix.marklin.swing.MarklinPanel;

/**
 * Configuration panel for GleisReporter deLuxe (CdB class 0x0D) and
 * StromSniffer (CdB class 0x0B) modules.
 * <p>
 * Ported from the standalone ReporterPanel.java tool. Communicates through
 * {@link ReporterParameterService}. No COM-port, ENDE button or scan controls.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class ReporterConfigPanel extends MarklinPanel implements MarklinListener {

    private final McanDevice device;
    private final int canClass;
    private ReporterParameterService service;

    // Info area
    private JLabel fwLabel;
    private JLabel hwLabel;
    private JLabel voltageLabel;
    private JLabel moduleAddressLabel;
    private JTextField modulnameField;
    private JTextField anleitungField;

    // Control buttons
    private JCheckBox selbstTestCheck;
    private JButton resetBtn;
    private JButton saveBtn;

    // Slave row (conditionally visible)
    private JPanel slavePanel;
    private JCheckBox slveCS2Check;
    private JSpinner slaveAdresseSpinner;

    // Special function (firmware >= 8)
    private JPanel sonderfunktionPanel;
    private JComboBox<String> sonderfunktionCombo;

    // Factory ID row
    private JPanel factoryIdPanel;
    private JSpinner newIdSpinner;

    // Fault detection
    private JSpinner rmkStoerungSpinner;
    private JCheckBox stoerungAktCheck;
    private JLabel stoerungStatusLabel;

    // Freeze row (StromSniffer only)
    private JPanel freezePanel;
    private JSpinner rmkAdresFreezSpinner;
    private JCheckBox freezeCheck;
    private JLabel freezeStatusLabel;

    // Per-channel grid (8 channels)
    private final JLabel[] inputNrLabels = new JLabel[8];
    private final JLabel[] statusLabels = new JLabel[8];
    private final JSpinner[] einSchaltzeitSpinners = new JSpinner[8];
    private final JSpinner[] ausSchaltzeitSpinners = new JSpinner[8];

    private int currentRawId;
    private boolean sonderfunktionActive = false;

    public ReporterConfigPanel(McanDevice device, MarklinSystemConnectionMemo memo) {
        super();
        this.device = device;
        McanDeviceIdentity identity = device.getIdentity();
        this.canClass = identity.isCdB() ? ((McanDeviceIdentity.CdB) identity).getCanClass() : 0x0D;
        buildUI();
        if (memo != null) {
            initComponents(memo);
        }
    }

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
        wireService(memo);
    }

    /**
     * Returns true if the freeze row is visible (StromSniffer only).
     * Package-visible for tests.
     */
    public boolean isFreezeRowVisible() {
        return canClass == 0x0B;
    }

    private void buildUI() {
        setLayout(new BorderLayout(5, 5));

        // ---- top info bar ----
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        infoBar.add(new JLabel(Bundle.getMessage("RepLabelFirmware") + ":"));
        fwLabel = new JLabel("--.-");
        infoBar.add(fwLabel);
        infoBar.add(new JLabel(Bundle.getMessage("RepLabelHardware") + ":"));
        hwLabel = new JLabel("--.-");
        infoBar.add(hwLabel);
        infoBar.add(new JLabel(Bundle.getMessage("RepLabelVoltage") + ":"));
        voltageLabel = new JLabel("--");
        voltageLabel.setOpaque(true);
        voltageLabel.setBackground(new Color(173, 216, 230));
        infoBar.add(voltageLabel);
        infoBar.add(new JLabel(Bundle.getMessage("RepLabelModuleAddress") + ":"));
        moduleAddressLabel = new JLabel("--");
        infoBar.add(moduleAddressLabel);

        // Module name (GleisReporter only  -  not meaningful for StromSniffer but shown)
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        namePanel.add(new JLabel(Bundle.getMessage("RepLabelModuleName") + ":"));
        modulnameField = new JTextField(20);
        modulnameField.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str != null && (getLength() + str.length()) <= 29) super.insertString(offs, str, a);
            }
        });
        namePanel.add(modulnameField);

        // Instruction text
        anleitungField = new JTextField(30);
        anleitungField.setEditable(false);
        anleitungField.setVisible(false);
        namePanel.add(anleitungField);

        // Control row
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        selbstTestCheck = new JCheckBox(Bundle.getMessage("RepLabelSelfTest"));
        selbstTestCheck.addActionListener(e -> onSelbstTest());
        controlRow.add(selbstTestCheck);
        JButton wiringBtn = new JButton(Bundle.getMessage("RepButtonWiringDiagram"));
        wiringBtn.addActionListener(e -> onWiringDiagram());
        controlRow.add(wiringBtn);
        resetBtn = new JButton(Bundle.getMessage("RepButtonReset"));
        resetBtn.setBackground(new Color(255, 128, 128));
        resetBtn.addActionListener(e -> onReset());
        controlRow.add(resetBtn);
        saveBtn = new JButton(Bundle.getMessage("RepButtonSave"));
        saveBtn.addActionListener(e -> onSave());
        controlRow.add(saveBtn);

        // Slave row
        slavePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        slveCS2Check = new JCheckBox(Bundle.getMessage("RepLabelSlaveCsTwo"));
        slveCS2Check.addActionListener(e -> onSlveCS2Changed());
        slavePanel.add(slveCS2Check);
        slavePanel.add(new JLabel(Bundle.getMessage("RepLabelSlaveAddress") + ":"));
        slaveAdresseSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 254, 1));
        slavePanel.add(slaveAdresseSpinner);

        // Special function row
        sonderfunktionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        sonderfunktionPanel.add(new JLabel(Bundle.getMessage("RepLabelSpecialFunction") + ":"));
        sonderfunktionCombo = new JComboBox<>(new String[]{
            Bundle.getMessage("RepSpecFuncNormal"),
            Bundle.getMessage("RepSpecFuncOne"),
            Bundle.getMessage("RepSpecFuncTwo")
        });
        sonderfunktionPanel.add(sonderfunktionCombo);
        sonderfunktionPanel.setVisible(false);

        // Factory ID row
        factoryIdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        factoryIdPanel.add(new JLabel(Bundle.getMessage("RepLabelNewModuleId") + ":"));
        newIdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 127, 1));
        factoryIdPanel.add(newIdSpinner);
        JButton writeIdBtn = new JButton(Bundle.getMessage("RepButtonWriteId"));
        writeIdBtn.addActionListener(e -> onWriteId());
        factoryIdPanel.add(writeIdBtn);
        factoryIdPanel.setVisible(false);

        // Fault row
        JPanel faultRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        faultRow.add(new JLabel(Bundle.getMessage("RepLabelFaultRmk") + ":"));
        rmkStoerungSpinner = new JSpinner(new SpinnerNumberModel(101, 1, 65000, 1));
        faultRow.add(rmkStoerungSpinner);
        stoerungAktCheck = new JCheckBox(Bundle.getMessage("RepLabelFaultActive"));
        faultRow.add(stoerungAktCheck);
        stoerungStatusLabel = new JLabel("");
        stoerungStatusLabel.setOpaque(true);
        stoerungStatusLabel.setPreferredSize(new Dimension(60, 20));
        faultRow.add(stoerungStatusLabel);

        // Freeze row (StromSniffer only)
        freezePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        freezePanel.add(new JLabel(Bundle.getMessage("RepLabelFreezeRmk") + ":"));
        rmkAdresFreezSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 65000, 1));
        freezePanel.add(rmkAdresFreezSpinner);
        freezeCheck = new JCheckBox(Bundle.getMessage("RepLabelFreezeCheck"));
        freezeCheck.addActionListener(e -> onFreezeCheckChanged());
        freezePanel.add(freezeCheck);
        freezeStatusLabel = new JLabel("");
        freezeStatusLabel.setOpaque(true);
        freezeStatusLabel.setPreferredSize(new Dimension(80, 20));
        freezePanel.add(freezeStatusLabel);
        freezePanel.setVisible(isFreezeRowVisible());

        // Assemble north region
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(infoBar);
        northPanel.add(namePanel);
        northPanel.add(controlRow);
        northPanel.add(slavePanel);
        northPanel.add(sonderfunktionPanel);
        northPanel.add(factoryIdPanel);
        northPanel.add(faultRow);
        northPanel.add(freezePanel);
        add(northPanel, BorderLayout.NORTH);

        // Channel grid
        add(new JScrollPane(buildChannelGrid()), BorderLayout.CENTER);
    }

    private JPanel buildChannelGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBorder(BorderFactory.createTitledBorder(""));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;

        // Header row
        c.gridy = 0; c.gridx = 0;
        grid.add(new JLabel(""), c);
        for (int i = 0; i < 8; i++) {
            c.gridx = i + 1;
            grid.add(new JLabel(String.valueOf(i + 1), SwingConstants.CENTER), c);
        }

        // Feedback address (read-only)
        c.gridy = 1; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("RepLabelFeedbackAddress")), c);
        for (int i = 0; i < 8; i++) {
            inputNrLabels[i] = new JLabel("--");
            inputNrLabels[i].setPreferredSize(new Dimension(55, 20));
            c.gridx = i + 1;
            grid.add(inputNrLabels[i], c);
        }

        // Status
        c.gridy = 2; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("RepLabelStatus")), c);
        for (int i = 0; i < 8; i++) {
            statusLabels[i] = new JLabel("  ");
            statusLabels[i].setOpaque(true);
            statusLabels[i].setBackground(Color.LIGHT_GRAY);
            statusLabels[i].setPreferredSize(new Dimension(55, 20));
            c.gridx = i + 1;
            grid.add(statusLabels[i], c);
        }

        // On time (Einschaltzeit)
        c.gridy = 3; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("RepLabelOnTime")), c);
        for (int i = 0; i < 8; i++) {
            einSchaltzeitSpinners[i] = new JSpinner(new SpinnerNumberModel(30, 1, 6000, 1));
            einSchaltzeitSpinners[i].setPreferredSize(new Dimension(65, 22));
            c.gridx = i + 1;
            grid.add(einSchaltzeitSpinners[i], c);
        }

        // Off time (Ausschaltzeit)
        c.gridy = 4; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("RepLabelOffTime")), c);
        for (int i = 0; i < 8; i++) {
            ausSchaltzeitSpinners[i] = new JSpinner(new SpinnerNumberModel(120, 1, 6000, 1));
            ausSchaltzeitSpinners[i].setPreferredSize(new Dimension(65, 22));
            c.gridx = i + 1;
            grid.add(ausSchaltzeitSpinners[i], c);
        }

        return grid;
    }

    private void wireService(MarklinSystemConnectionMemo memo) {
        McanFrameSender frameSender = (memo != null && memo.getTrafficController() != null)
            ? frame -> memo.getTrafficController().sendMarklinMessage(new MarklinMessage(frame), null)
            : frame -> {};

        service = new ReporterParameterService(device, frameSender);
        service.addListener(new ReporterParameterService.ReporterListener() {
            @Override public void firmwareReceived(int hi, int lo, boolean showSonder, boolean showSlave) {
                fwLabel.setText(hi + "." + lo);
                sonderfunktionPanel.setVisible(showSonder);
                slavePanel.setVisible(showSlave);
            }
            @Override public void hardwareReceived(int hi, int lo, Integer chipType, String manualText) {
                String text = hi + "." + lo;
                if (chipType != null) text += "." + chipType;
                hwLabel.setText(text);
                if (manualText != null) {
                    anleitungField.setText(manualText);
                    anleitungField.setVisible(true);
                }
            }
            @Override public void voltageReceived(String text, boolean warn) {
                voltageLabel.setText(text);
                voltageLabel.setBackground(warn ? Color.RED : new Color(173, 216, 230));
            }
            @Override public void moduleNameReceived(String name) {
                modulnameField.setText(name);
            }
            @Override public void moduleFound(int rawId) {
                currentRawId = rawId;
                moduleAddressLabel.setText(String.valueOf(rawId + 128));
            }
            @Override public void factoryIdModeEntered() {
                factoryIdPanel.setVisible(true);
            }
            @Override public void inputNumbersUpdated(int channel, String rmkAddr) {
                if (channel >= 0 && channel < 8) inputNrLabels[channel].setText(rmkAddr);
            }
            @Override public void einschaltzeitReceived(int channel, int ms) {
                if (channel >= 1 && channel <= 8) einSchaltzeitSpinners[channel - 1].setValue(ms);
            }
            @Override public void ausschaltzeitReceived(int channel, int ms) {
                if (channel >= 1 && channel <= 8) ausSchaltzeitSpinners[channel - 1].setValue(ms);
            }
            @Override public void sonderfunktionReceived(int idx) {
                sonderfunktionCombo.setSelectedIndex(Math.min(idx, 2));
                sonderfunktionActive = (idx != 0);
                statusLabels[7].setVisible(!sonderfunktionActive);
            }
            @Override public void freezCheckReceived(boolean on) {
                freezeCheck.setSelected(on);
            }
            @Override public void freezAdresseReceived(int addr) {
                rmkAdresFreezSpinner.setValue(addr);
            }
            @Override public void freezStatusReceived(Boolean boosterOn) {
                if (boosterOn == null) {
                    freezeStatusLabel.setText("");
                    freezeStatusLabel.setBackground(Color.LIGHT_GRAY);
                } else if (boosterOn) {
                    freezeStatusLabel.setText(Bundle.getMessage("RepStatusBoosterOn"));
                    freezeStatusLabel.setBackground(Color.GREEN);
                } else {
                    freezeStatusLabel.setText(Bundle.getMessage("RepStatusBoosterOff"));
                    freezeStatusLabel.setBackground(Color.RED);
                }
            }
            @Override public void slaveAdresseReceived(int addr, boolean enabled) {
                slaveAdresseSpinner.setValue(addr);
                slveCS2Check.setSelected(enabled);
                slaveAdresseSpinner.setEnabled(enabled);
            }
            @Override public void stoerungAktReceived(boolean on) {
                stoerungAktCheck.setSelected(on);
            }
            @Override public void rmkStoerungReceived(int addr) {
                rmkStoerungSpinner.setValue(addr);
            }
            @Override public void stoerungStatusReceived(Boolean ok) {
                if (ok == null) {
                    stoerungStatusLabel.setText("");
                    stoerungStatusLabel.setBackground(Color.LIGHT_GRAY);
                } else if (ok) {
                    stoerungStatusLabel.setText(Bundle.getMessage("RepStatusOk"));
                    stoerungStatusLabel.setBackground(Color.GREEN);
                } else {
                    stoerungStatusLabel.setText(Bundle.getMessage("RepStatusFault"));
                    stoerungStatusLabel.setBackground(Color.RED);
                }
            }
            @Override public void occupancyReceived(int channel, Boolean occupied) {
                if (channel < 0 || channel >= 8) return;
                if (occupied == null) {
                    statusLabels[channel].setBackground(Color.LIGHT_GRAY);
                } else if (occupied) {
                    statusLabels[channel].setBackground(Color.RED);
                } else {
                    statusLabels[channel].setBackground(Color.GREEN);
                }
            }
        });

        McanDeviceIdentity identity = device.getIdentity();
        if (identity.isCdB()) {
            currentRawId = ((McanDeviceIdentity.CdB) identity).getModuleId();
            service.selectModule(currentRawId);
        }

        // Register for MCAN traffic to receive occupancy status frames
        if (memo != null && memo.getTrafficController() != null) {
            memo.getTrafficController().addMarklinListener(this);
        }
    }

    // ---- MarklinListener implementation ----

    @Override
    public void message(MarklinMessage m) {
        // outbound messages not relevant
    }

    @Override
    public void reply(MarklinReply r) {
        if (service != null) {
            service.handleStatusFrame(r);
        }
    }

    // ---- actions ----

    private void onSave() {
        ReporterParameterService.ReporterConfig config = new ReporterParameterService.ReporterConfig();
        config.modulname = modulnameField.getText();
        config.rmkStoerung = (Integer) rmkStoerungSpinner.getValue();
        config.stoerungAkt = stoerungAktCheck.isSelected();
        config.rmkAdresFreez = (Integer) rmkAdresFreezSpinner.getValue();
        config.slaveAdresse = slveCS2Check.isSelected() ? (Integer) slaveAdresseSpinner.getValue() : 0;
        config.sonderfunktionIdx = sonderfunktionCombo.getSelectedIndex();
        for (int i = 0; i < 8; i++) {
            config.einSchaltzeiten[i] = (Integer) einSchaltzeitSpinners[i].getValue();
            config.ausSchaltzeiten[i] = (Integer) ausSchaltzeitSpinners[i].getValue();
        }
        service.writeConfiguration(config);
    }

    private void onReset() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset module?", Bundle.getMessage("RepButtonReset"),
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            service.resetModule();
        }
    }

    private void onSelbstTest() {
        service.setSelbstTest(selbstTestCheck.isSelected());
    }

    private void onFreezeCheckChanged() {
        service.setFreezCheck(freezeCheck.isSelected());
    }

    private void onSlveCS2Changed() {
        slaveAdresseSpinner.setEnabled(slveCS2Check.isSelected());
    }

    private void onWriteId() {
        int newId = (Integer) newIdSpinner.getValue();
        service.writeNewModuleId(newId);
    }

    private void onWiringDiagram() {
        JOptionPane.showMessageDialog(this,
            "See product manual for wiring diagram.",
            Bundle.getMessage("RepButtonWiringDiagram"),
            JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("RepConfigTitle");
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.configure.ReporterConfigPanel";
    }

    @Override
    public void dispose() {
        if (memo != null && memo.getTrafficController() != null) {
            memo.getTrafficController().removeMarklinListener(this);
        }
        if (service != null) {
            service.deselectModule();
        }
        super.dispose();
    }
}

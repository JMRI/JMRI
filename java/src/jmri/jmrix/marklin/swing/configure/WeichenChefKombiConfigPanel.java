package jmri.jmrix.marklin.swing.configure;

import java.awt.*;
import javax.swing.*;
import jmri.jmrix.marklin.*;
import jmri.jmrix.marklin.mcan.address.CdBAddressCodec;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.jmrix.marklin.swing.MarklinPanel;

/**
 * Configuration panel for WeichenChef Kombi (CdB class 0x27) modules.
 * <p>
 * Ported from the standalone MainFrame.java tool. Communicates through
 * {@link WeichenChefKombiParameterService}. No COM-port, ENDE button or
 * scan controls  -  those live in McanDevicePanel.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class WeichenChefKombiConfigPanel extends MarklinPanel {

    private final McanDevice device;
    private WeichenChefKombiParameterService service;
    private CdBConfigListener cdBListener;
    private MarklinSystemConnectionMemo wireMemo;

    // Info area
    private JLabel fwLabel;
    private JLabel hwLabel;
    private JLabel voltageLabel;
    private JLabel loadingLabel;
    private JLabel errorLabel;

    // Factory-ID row (hidden unless factoryIdModeEntered)
    private JPanel factoryIdPanel;
    private JSpinner newIdSpinner;
    private JButton saveButton;

    private final JSpinner[] adresseSpinner = new JSpinner[5];
    private final JLabel[]   adresseHexLabel = new JLabel[5];
    private final JComboBox<String>[] formatCombo;
    private final JSpinner[] schaltzeitSpinner = new JSpinner[4];
    private final JCheckBox[] cdbCheckbox = new JCheckBox[4];
    private final JButton[] positionButton = new JButton[4];
    private final JButton[] rotButton = new JButton[4];
    private final JButton[] gruenButton = new JButton[4];

    private final JSpinner[] rmkSpinner = new JSpinner[8];
    private final JSpinner[] anschlussSpinner = new JSpinner[8];
    private JPanel anschlussRow;

    private int currentModuleType = 2;
    private int currentRawId;

    @SuppressWarnings("unchecked")
    public WeichenChefKombiConfigPanel(McanDevice device, MarklinSystemConnectionMemo memo) {
        super();
        this.device = device;
        formatCombo = new JComboBox[5];
        if (memo != null) {
            initComponents(memo);
        }
    }

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
        buildUI();
        wireService(memo);
    }

    private void buildUI() {
        setLayout(new BorderLayout(5, 5));

        // ---- top info bar ----
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        infoBar.add(new JLabel(Bundle.getMessage("WcKombiLabelFirmware") + ":"));
        fwLabel = new JLabel("--.-");
        infoBar.add(fwLabel);
        infoBar.add(new JLabel(Bundle.getMessage("WcKombiLabelHardware") + ":"));
        hwLabel = new JLabel("--.-");
        infoBar.add(hwLabel);
        infoBar.add(new JLabel(Bundle.getMessage("WcKombiLabelVoltage") + ":"));
        voltageLabel = new JLabel("--");
        voltageLabel.setOpaque(true);
        voltageLabel.setBackground(new Color(173, 216, 230));
        infoBar.add(voltageLabel);
        loadingLabel = new JLabel(Bundle.getMessage("WcKombiLabelLoading"));
        loadingLabel.setForeground(new Color(150, 100, 0));
        infoBar.add(loadingLabel);
        errorLabel = new JLabel(Bundle.getMessage("WcKombiLabelLoadFailed"));
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
        infoBar.add(errorLabel);

        // ---- factory ID row (hidden by default) ----
        factoryIdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JLabel newIdLabel = new JLabel(Bundle.getMessage("WcKombiLabelNewModuleId"));
        newIdLabel.setOpaque(true);
        newIdLabel.setBackground(new Color(255, 192, 128));
        factoryIdPanel.add(newIdLabel);
        newIdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        factoryIdPanel.add(newIdSpinner);
        JButton applyIdButton = new JButton(Bundle.getMessage("WcKombiButtonApplyId"));
        applyIdButton.setBackground(new Color(255, 192, 128));
        applyIdButton.addActionListener(e -> onApplyNewId());
        factoryIdPanel.add(applyIdButton);
        factoryIdPanel.setVisible(false);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(infoBar);
        northPanel.add(factoryIdPanel);
        add(northPanel, BorderLayout.NORTH);

        // ---- centre: output grid + sensor grid ----
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(buildOutputGrid());
        centre.add(Box.createVerticalStrut(8));
        centre.add(buildSensorGrid());
        add(new JScrollPane(centre), BorderLayout.CENTER);

        // ---- button bar ----
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        saveButton = new JButton(Bundle.getMessage("WcKombiButtonSave"));
        saveButton.addActionListener(e -> onSave());
        saveButton.setEnabled(false);
        buttonBar.add(saveButton);
        JButton resetButton = new JButton(Bundle.getMessage("WcKombiButtonReset"));
        resetButton.setBackground(new Color(255, 128, 128));
        resetButton.addActionListener(e -> onReset());
        buttonBar.add(resetButton);
        JButton wiringButton = new JButton(Bundle.getMessage("WcKombiButtonWiringDiagram"));
        wiringButton.addActionListener(e -> onWiringDiagram());
        buttonBar.add(wiringButton);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private JPanel buildOutputGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("WcKombiLabelOutput")));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;

        Color[] colColors = {
            new Color(220, 220, 220), new Color(192, 192, 192),
            new Color(220, 220, 220), new Color(192, 192, 192), new Color(220, 220, 220)
        };

        // Header row
        c.gridx = 0; c.gridy = 0;
        grid.add(new JLabel(""), c);
        for (int i = 0; i < 5; i++) {
            c.gridx = i + 1;
            JLabel h = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            h.setOpaque(true);
            h.setBackground(colColors[i]);
            h.setPreferredSize(new Dimension(80, 20));
            grid.add(h, c);
        }

        // Address row
        c.gridy = 1; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelMagnetAddress")), c);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            adresseSpinner[i] = new JSpinner(new SpinnerNumberModel(1, 1, 65534, 1));
            adresseSpinner[i].setPreferredSize(new Dimension(80, 22));
            adresseHexLabel[i] = new JLabel();
            adresseHexLabel[i].setForeground(new Color(100, 100, 100));
            adresseHexLabel[i].setFont(adresseHexLabel[i].getFont().deriveFont(10.0f));
            adresseHexLabel[i].setVisible(false);
            adresseSpinner[i].addChangeListener(e -> updateHexHint(idx));
            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setOpaque(false);
            cell.add(adresseSpinner[i]);
            cell.add(adresseHexLabel[i]);
            c.gridx = i + 1;
            grid.add(cell, c);
        }

        // Format row
        c.gridy = 2; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelFormat")), c);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            formatCombo[i] = new JComboBox<>(new String[]{"CAN", "DCC", "MM"});
            formatCombo[i].setPreferredSize(new Dimension(80, 22));
            formatCombo[i].addActionListener(e -> updateHexHint(idx));
            c.gridx = i + 1;
            grid.add(formatCombo[i], c);
        }

        // Switching time row (outputs 1-4 only)
        c.gridy = 3; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelSwitchTime")), c);
        for (int i = 0; i < 4; i++) {
            final int out = i + 1;
            schaltzeitSpinner[i] = new JSpinner(new SpinnerNumberModel(0, 0, 65534, 1));
            schaltzeitSpinner[i].setPreferredSize(new Dimension(80, 22));
            schaltzeitSpinner[i].addChangeListener(e -> warnSpecialSwitchTime(out,
                (Integer) schaltzeitSpinner[out - 1].getValue()));
            c.gridx = i + 1;
            grid.add(schaltzeitSpinner[i], c);
        }

        // CdB flag row
        c.gridy = 4; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelCdBMessage")), c);
        for (int i = 0; i < 4; i++) {
            cdbCheckbox[i] = new JCheckBox();
            c.gridx = i + 1;
            grid.add(cdbCheckbox[i], c);
        }

        // Position row
        c.gridy = 5; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelPosition")), c);
        for (int i = 0; i < 4; i++) {
            positionButton[i] = new JButton("?");
            positionButton[i].setEnabled(false);
            positionButton[i].setPreferredSize(new Dimension(80, 22));
            c.gridx = i + 1;
            grid.add(positionButton[i], c);
        }

        // Test buttons row (visible for type 2)
        c.gridy = 6; c.gridx = 0;
        JLabel testLabel = new JLabel("Test");
        grid.add(testLabel, c);
        for (int i = 0; i < 4; i++) {
            JPanel pair = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            final int idx = i;
            rotButton[i] = new JButton("rot");
            rotButton[i].setBackground(Color.RED);
            rotButton[i].setPreferredSize(new Dimension(36, 22));
            rotButton[i].addActionListener(e -> onTestOutput(idx, false));
            gruenButton[i] = new JButton("grün");
            gruenButton[i].setBackground(new Color(0, 192, 0));
            gruenButton[i].setPreferredSize(new Dimension(40, 22));
            gruenButton[i].addActionListener(e -> onTestOutput(idx, true));
            pair.add(rotButton[i]);
            pair.add(gruenButton[i]);
            c.gridx = i + 1;
            grid.add(pair, c);
        }

        return grid;
    }

    private JPanel buildSensorGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBorder(BorderFactory.createTitledBorder(
            Bundle.getMessage("WcKombiLabelFeedbackAddresses")));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;

        // Header
        c.gridy = 0; c.gridx = 0;
        grid.add(new JLabel(""), c);
        for (int i = 0; i < 8; i++) {
            c.gridx = i + 1;
            grid.add(new JLabel(String.valueOf(i + 1), SwingConstants.CENTER), c);
        }

        // RMK address row
        c.gridy = 1; c.gridx = 0;
        grid.add(new JLabel(Bundle.getMessage("WcKombiLabelFeedbackAddr")), c);
        for (int i = 0; i < 8; i++) {
            rmkSpinner[i] = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
            rmkSpinner[i].setPreferredSize(new Dimension(72, 22));
            c.gridx = i + 1;
            grid.add(rmkSpinner[i], c);
        }

        // Connection number row (type 3 only)
        anschlussRow = new JPanel(new GridBagLayout());
        GridBagConstraints ar = new GridBagConstraints();
        ar.insets = new Insets(2, 4, 2, 4);
        ar.gridy = 0; ar.gridx = 0;
        anschlussRow.add(new JLabel(Bundle.getMessage("WcKombiLabelConnectionNumber")), ar);
        for (int i = 0; i < 8; i++) {
            anschlussSpinner[i] = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            anschlussSpinner[i].setPreferredSize(new Dimension(72, 22));
            ar.gridx = i + 1;
            anschlussRow.add(anschlussSpinner[i], ar);
        }
        anschlussRow.setVisible(false);

        c.gridy = 2; c.gridx = 0;
        c.gridwidth = 9;
        grid.add(anschlussRow, c);

        return grid;
    }

    private void wireService(MarklinSystemConnectionMemo memo) {
        this.wireMemo = memo;
        McanFrameSender frameSender = (memo != null && memo.getTrafficController() != null)
            ? frame -> memo.getTrafficController().sendMarklinMessage(new MarklinMessage(frame), null)
            : frame -> {};

        service = new WeichenChefKombiParameterService(device, frameSender);

        if (memo != null && memo.getTrafficController() != null) {
            cdBListener = (cls, raw) -> service.handleCdBReply(cls, raw);
            memo.getTrafficController().getCdBFilter().addCdBConfigListener(cdBListener);
        }

        service.addListener(new WeichenChefKombiParameterService.WeichenChefKombiListener() {
            @Override
            public void firmwareReceived(int hi, int lo) {
                fwLabel.setText(hi + "." + lo);
            }
            @Override
            public void hardwareReceived(int hi, int lo, Integer chipType) {
                String text = hi + "." + lo;
                if (chipType != null) text += "." + chipType;
                hwLabel.setText(text);
            }
            @Override
            public void voltageReceived(WeichenChefKombiParameterService.VoltageResult r) {
                voltageLabel.setText(r.text);
                voltageLabel.setBackground(r.lowVoltageWarning ? Color.RED : new Color(173, 216, 230));
            }
            @Override
            public void moduleTypeReceived(int moduleType) {
                currentModuleType = moduleType;
                boolean type3 = (moduleType == 3);
                String[] type3Formats = {"DCC", "DCC ext."};
                String[] type2Formats = {"CAN", "DCC", "MM"};
                for (int i = 0; i < 5; i++) {
                    String selected = (String) formatCombo[i].getSelectedItem();
                    formatCombo[i].setModel(new DefaultComboBoxModel<>(
                        type3 ? type3Formats : type2Formats));
                    formatCombo[i].setSelectedItem(selected);
                }
                anschlussRow.setVisible(type3);
                for (int i = 0; i < 4; i++) {
                    rotButton[i].setVisible(!type3);
                    gruenButton[i].setVisible(!type3);
                }
                revalidate();
            }
            @Override
            public void addressReceived(int channel, String format, int address) {
                if (channel < 1 || channel > 5) return;
                adresseSpinner[channel - 1].setValue(address);
                formatCombo[channel - 1].setSelectedItem(format);
                updateHexHint(channel - 1);
            }
            @Override
            public void rmkReceived(int channel, int rmkAddr, int connNum) {
                if (channel < 1 || channel > 8) return;
                rmkSpinner[channel - 1].setValue(rmkAddr);
                if (currentModuleType == 3) anschlussSpinner[channel - 1].setValue(connNum);
            }
            @Override
            public void switchingTimeReceived(int channel, int timeMs) {
                if (channel < 1 || channel > 4) return;
                schaltzeitSpinner[channel - 1].setValue(timeMs);
            }
            @Override
            public void cdBFlagReceived(int channel, boolean enabled) {
                if (channel < 1 || channel > 4) return;
                cdbCheckbox[channel - 1].setSelected(enabled);
            }
            @Override
            public void positionReceived(int channel, int statusByte) {
                if (channel < 0 || channel >= 4) return;
                String text;
                Color bg;
                if (statusByte == 255) { text = Bundle.getMessage("WcKombiPositionFault"); bg = Color.RED; }
                else if (statusByte == 254) { text = Bundle.getMessage("WcKombiPositionNoActuator"); bg = Color.GRAY; }
                else if (statusByte == 1) { text = "grün"; bg = Color.GREEN; }
                else { text = "rot"; bg = Color.RED; }
                positionButton[channel].setText(text);
                positionButton[channel].setBackground(bg);
            }
            @Override
            public void factoryIdModeEntered() {
                factoryIdPanel.setVisible(true);
            }
            @Override
            public void moduleFound(int rawId) {
                currentRawId = rawId;
            }
            @Override
            public void configReadyReceived() {
                saveButton.setEnabled(true);
                loadingLabel.setVisible(false);
            }
            @Override
            public void configLoadFailedReceived() {
                loadingLabel.setVisible(false);
                errorLabel.setVisible(true);
            }
        });

        McanDeviceIdentity identity = device.getIdentity();
        if (identity.isCdB()) {
            currentRawId = ((McanDeviceIdentity.CdB) identity).getModuleId();
            service.selectModule(currentRawId);
        }
    }

    // ---- actions ----

    private void onSave() {
        WeichenChefKombiParameterService.WeichenChefKombiConfig config =
            new WeichenChefKombiParameterService.WeichenChefKombiConfig();
        config.moduleType = currentModuleType;
        for (int i = 0; i < 5; i++) {
            int addr = (Integer) adresseSpinner[i].getValue();
            String fmt = (String) formatCombo[i].getSelectedItem();
            config.addresses[i] = (currentModuleType == 3)
                ? CdBAddressCodec.INSTANCE.encodeType3(fmt, addr)
                : CdBAddressCodec.INSTANCE.encodeType2(fmt, addr);
            config.formats[i] = fmt;
        }
        for (int i = 0; i < 4; i++) {
            config.switchingTimes[i] = (Integer) schaltzeitSpinner[i].getValue();
            config.cdBFlags[i] = cdbCheckbox[i].isSelected();
        }
        for (int i = 0; i < 8; i++) {
            config.rmkAddresses[i] = (Integer) rmkSpinner[i].getValue();
            config.connectionNumbers[i] = (Integer) anschlussSpinner[i].getValue();
        }
        service.writeConfiguration(config);
    }

    private void onReset() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset module?", Bundle.getMessage("WcKombiButtonReset"),
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            service.resetModule();
        }
    }

    private void onApplyNewId() {
        int newId = (Integer) newIdSpinner.getValue();
        service.writeNewModuleId(newId);
        // Close the dialog  -  triggers dispose() via the window listener, which deselects.
        // Then re-discover so the module can respond at its new ID after rebooting.
        SwingUtilities.getWindowAncestor(this).dispose();
        if (wireMemo != null) {
            McanDeviceManager mgr = wireMemo.getMcanDeviceManager();
            if (mgr != null) {
                mgr.discoverCdBDevice(CdBDeviceClass.WEICHEN_CHEF_KOMBI);
            }
        }
    }

    private void onTestOutput(int idx, boolean gruen) {
        // Send a turnout command via standard MCAN (not CdB)  -  placeholder
        // (full implementation would use MarklinTurnout or send a direct accessory cmd)
    }

    private void onWiringDiagram() {
        JOptionPane.showMessageDialog(this,
            "See product manual for wiring diagram.",
            Bundle.getMessage("WcKombiButtonWiringDiagram"),
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateHexHint(int idx) {
        boolean isCan = "CAN".equals(formatCombo[idx].getSelectedItem());
        if (isCan) {
            int val = (Integer) adresseSpinner[idx].getValue();
            adresseHexLabel[idx].setText(String.format("0x%X", val));
            adresseHexLabel[idx].setVisible(true);
        } else {
            adresseHexLabel[idx].setVisible(false);
        }
    }

    private void warnSpecialSwitchTime(int outNum, int val) {
        if (val == 0) {
            JOptionPane.showMessageDialog(this,
                Bundle.getMessage("WcKombiWarnSwitchTime0", outNum),
                "WeichenChef Kombi", JOptionPane.INFORMATION_MESSAGE);
        } else if (val == 1) {
            JOptionPane.showMessageDialog(this,
                Bundle.getMessage("WcKombiWarnSwitchTime1", outNum),
                "WeichenChef Kombi", JOptionPane.INFORMATION_MESSAGE);
        } else if (val == 2) {
            JOptionPane.showMessageDialog(this,
                Bundle.getMessage("WcKombiWarnSwitchTime2", outNum),
                "WeichenChef Kombi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("WcKombiConfigTitle");
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.configure.WeichenChefKombiConfigPanel";
    }

    @Override
    public void dispose() {
        if (cdBListener != null && wireMemo != null && wireMemo.getTrafficController() != null) {
            wireMemo.getTrafficController().getCdBFilter().removeCdBConfigListener(cdBListener);
            cdBListener = null;
        }
        if (service != null) {
            service.deselectModule();
        }
        super.dispose();
    }
}

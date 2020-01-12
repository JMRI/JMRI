package jmri.jmrit.progsupport;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.implementation.AccessoryOpsModeProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JPanel to configure the ops programming mode.
 * <p>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModePane object can disconnect its listeners.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class ProgOpsModePane extends ProgModeSelector implements PropertyChangeListener, ActionListener {

    // GUI member declarations
    ButtonGroup modeGroup = new ButtonGroup();
    HashMap<ProgrammingMode, JRadioButton> buttonMap = new HashMap<>();
    JComboBox<AddressedProgrammerManager> progBox;
    ArrayList<JRadioButton> buttonPool = new ArrayList<>();
    // JTextField mAddrField = new JTextField(4);
    // use JSpinner for CV number input
    SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10239, 1); // 10239 is highest DCC Long Address documented by NMRA as per 2017
    JSpinner mAddrField = new JSpinner(model);
    int lowAddrLimit = 0;
    int highAddrLimit = 10239;
    int oldAddrValue = 3; // Default start value
    ButtonGroup addrGroup = new ButtonGroup();
    JRadioButton shortAddrButton = new JRadioButton(Bundle.getMessage("ShortAddress"));
    JRadioButton longAddrButton = new JRadioButton(Bundle.getMessage("LongAddress"));
    JCheckBox offsetAddrCheckBox = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));
    JLabel addressLabel = new JLabel(Bundle.getMessage("AddressLabel"));
    boolean oldLongAddr = false;
    boolean opsAccyMode = false;
    boolean oldOpsAccyMode = false;
    boolean opsSigMode = false;
    boolean oldOpsSigMode = false;
    boolean oldoffsetAddrCheckBox = false;
    transient volatile AddressedProgrammer programmer = null;
    transient volatile AccessoryOpsModeProgrammerFacade facadeProgrammer = null;

    /**
     * Get the selected programmer
     */
    @Override
    public Programmer getProgrammer() {
        log.debug("getProgrammer mLongAddrCheck.isSelected()={}, oldLongAddr={}, mAddrField.getValue()={}, oldAddrValue={}, opsAccyMode={}, oldOpsAccyMode={}, opsSigMode={}, oldOpsSigMode={}, oldoffsetAddrCheckBox={})",
                longAddrButton.isSelected(), oldLongAddr, mAddrField.getValue(), oldAddrValue, opsAccyMode, oldOpsAccyMode, opsSigMode, oldOpsSigMode, oldoffsetAddrCheckBox);
        if (longAddrButton.isSelected() == oldLongAddr
                && mAddrField.getValue().equals(oldAddrValue)
                && offsetAddrCheckBox.isSelected() == oldoffsetAddrCheckBox
                && opsAccyMode == oldOpsAccyMode
                && opsSigMode == oldOpsSigMode) {
            log.debug("getProgrammer hasn't changed");
            // hasn't changed
            if (opsAccyMode || opsSigMode) {
                return facadeProgrammer;
            } else {
                return programmer;
            }
        }

        // here values have changed, try to create a new one
        AddressedProgrammerManager pm = ((AddressedProgrammerManager) progBox.getSelectedItem());
        oldLongAddr = longAddrButton.isSelected();
        oldAddrValue = (Integer) mAddrField.getValue();
        oldOpsAccyMode = opsAccyMode;
        oldOpsSigMode = opsSigMode;
        oldoffsetAddrCheckBox = offsetAddrCheckBox.isSelected();
        setAddrParams();

        if (pm != null) {
            int address = 3;
            try {
                address = (Integer) mAddrField.getValue();
            } catch (java.lang.NumberFormatException e) {
                log.error("loco address \"{}\" not correct", mAddrField.getValue());
                programmer = null;
            }
            boolean longAddr = longAddrButton.isSelected();
            log.debug("ops programmer for address " + address
                    + ", long address " + longAddr);
            programmer = pm.getAddressedProgrammer(longAddr, address);
            log.debug("   programmer: {}", programmer);

            // whole point is to get mode...
            setProgrammerFromGui(programmer);
        } else {
            log.warn("request for ops mode programmer with no ProgrammerManager configured");
            programmer = null;
        }
        if (opsAccyMode) {
            log.debug("   getting AccessoryOpsModeProgrammerFacade");
            facadeProgrammer = new AccessoryOpsModeProgrammerFacade(programmer,
                    longAddrButton.isSelected() ? "accessory" : "decoder", 200, programmer);
            return facadeProgrammer;
        } else if (opsSigMode) {
            String addrType = offsetAddrCheckBox.isSelected() ? "signal" : "altsignal";
            log.debug("   getting AccessoryOpsModeProgrammerFacade {}", addrType);
            facadeProgrammer = new AccessoryOpsModeProgrammerFacade(programmer, addrType, 200, programmer);
            return facadeProgrammer;
        }
        return programmer;
    }

    /**
     * Are any of the modes selected?
     *
     * @return true is any button is selected
     */
    @Override
    public boolean isSelected() {
        for (JRadioButton button : buttonMap.values()) {
            if (button.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructor for the Programming settings pane.
     *
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgOpsModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * Constructor for the Programming settings pane.
     *
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     * @param group     A set of JButtons to display programming modes
     */
    public ProgOpsModePane(int direction, javax.swing.ButtonGroup group) {
        modeGroup = group;
        addrGroup.add(shortAddrButton);
        addrGroup.add(longAddrButton);

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // create the programmer display combo box
        java.util.Vector<AddressedProgrammerManager> v = new java.util.Vector<>();
        for (AddressedProgrammerManager pm : InstanceManager.getList(jmri.AddressedProgrammerManager.class)) {
            v.add(pm);
        }
        add(progBox = new JComboBox<>(v));
        // if only one, don't show
        if (progBox.getItemCount() < 2) {
            progBox.setVisible(false);
        }
        progBox.setSelectedItem(InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)); // set default
        progBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected();
            }
        });

        add(new JLabel(Bundle.getMessage("TitleProgramOnMain")));
        add(new JLabel(" "));
        add(shortAddrButton);
        add(longAddrButton);
        add(offsetAddrCheckBox);
        offsetAddrCheckBox.setToolTipText(Bundle.getMessage("DccOffsetTooltip"));
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.FlowLayout());
        panel.add(addressLabel);
        panel.add(mAddrField);
        mAddrField.setToolTipText(Bundle.getMessage("ToolTipEnterDecoderAddress"));
        add(panel);
        add(new JLabel(Bundle.getMessage("OpsModeLabel")));

//        mAddrField.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
        // new programmer selection
//                programmerSelected(); // in case it has valid address now
//            }
//        });
        shortAddrButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected(); // in case it has valid address now
            }
        });

        longAddrButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected(); // in case it has valid address now
            }
        });

        offsetAddrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected(); // in case it has valid address now
            }
        });

        shortAddrButton.setSelected(true);
        offsetAddrCheckBox.setSelected(false);
        offsetAddrCheckBox.setVisible(false);

        // and execute the setup for 1st time
        programmerSelected();
    }

    /**
     * Reload the interface with the new programmers.
     */
    void programmerSelected() {
        log.debug("programmerSelected starts with {} buttons", buttonPool.size());
        // hide buttons
        for (JRadioButton button : buttonPool) {
            button.setVisible(false);
        }

        // clear map
        buttonMap.clear();

        // require new programmer if possible
        oldAddrValue = -1;

        // configure buttons
        int index = 0;
        List<ProgrammingMode> modes = new ArrayList<>();
        if (getProgrammer() != null) {
            modes.addAll(programmer.getSupportedModes());
        } else {
            modes.addAll(((AddressedProgrammerManager) progBox.getSelectedItem()).getDefaultModes());
        }
        // add OPSACCBYTEMODE & OPSACCEXTBYTEMODE if possible
        if (modes.contains(ProgrammingMode.OPSBYTEMODE)) {
            if (!modes.contains(ProgrammingMode.OPSACCBYTEMODE)) {
                log.debug("   adding button for {} via AccessoryOpsModeProgrammerFacade", ProgrammingMode.OPSACCBYTEMODE);
                modes.add(ProgrammingMode.OPSACCBYTEMODE);
            }
            if (!modes.contains(ProgrammingMode.OPSACCEXTBYTEMODE)) {
                log.debug("   adding button for {} via AccessoryOpsModeProgrammerFacade", ProgrammingMode.OPSACCEXTBYTEMODE);
                modes.add(ProgrammingMode.OPSACCEXTBYTEMODE);
            }
        }
        log.debug("   has {} modes", modes.size());
        for (ProgrammingMode mode : modes) {
            JRadioButton button;
            // need a new button?
            if (index >= buttonPool.size()) {
                log.debug("   add button");
                button = new JRadioButton();
                buttonPool.add(button);
                modeGroup.add(button);
                button.addActionListener(this);
                add(button); // add to GUI
            }
            // configure next button in pool
            log.debug("   set for {}", mode.toString());
            button = buttonPool.get(index++);
            button.setVisible(true);
            modeGroup.add(button);
            button.setText(mode.toString());
            buttonMap.put(mode, button);
        }

        setGuiFromProgrammer();
    }

    /**
     * Listen to buttons for mode changes.
     *
     * @param e ActionEvent heard
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // find selected button
        log.debug("Selected button: {}", e.getActionCommand());
        for (ProgrammingMode mode : buttonMap.keySet()) {
            if (mode.toString().equals(e.getActionCommand())) {
                log.debug("      setting mode {} on {}", mode.toString(), getProgrammer());
                if (getProgrammer() != null) {
                    log.debug("getProgrammer() != null");
                    if (mode == ProgrammingMode.OPSACCBYTEMODE) {
                        log.debug("OPS ACCY was selected in actionPerformed");
                        opsAccyMode = true;
                        opsSigMode = false;
                    } else if (mode == ProgrammingMode.OPSACCEXTBYTEMODE) {
                        log.debug("OPS SIG was selected in actionPerformed");
                        opsAccyMode = false;
                        opsSigMode = true;
                    } else {
                        opsAccyMode = false;
                        opsSigMode = false;
                        getProgrammer().setMode(mode);
                    }
                }
                setAddrParams();
                return; // 1st match
            }
        }
    }

    /**
     * Change the programmer (mode).
     *
     * @param programmer The type of programmer (i.e. Byte Mode)
     */
    void setProgrammerFromGui(Programmer programmer
    ) {
        for (Map.Entry<ProgrammingMode, JRadioButton> entry : buttonMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                if (entry.getKey() == ProgrammingMode.OPSACCBYTEMODE) {
                    log.debug("OPS ACCY was selected in setProgrammerFromGui");
                    opsAccyMode = true;
                    opsSigMode = false;
                } else if (entry.getKey() == ProgrammingMode.OPSACCEXTBYTEMODE) {
                    log.debug("OPS SIG was selected in setProgrammerFromGui");
                    opsAccyMode = false;
                    opsSigMode = true;
                } else {
                    opsAccyMode = false;
                    opsSigMode = false;
                    getProgrammer().setMode(entry.getKey());
                }
            }
        }
    }

    /**
     * Listen to programmer for mode changes.
     *
     * @param e ActionEvent heard
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e
    ) {
        if ("Mode".equals(e.getPropertyName()) && getProgrammer().equals(e.getSource())) {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // only change mode if we have a selected mode, in case some other selector with shared group has the selection
                setGuiFromProgrammer();
            }
        }
    }

    /**
     * Change the selected mode in GUI when programmer is changed elsewhere.
     */
    void setGuiFromProgrammer() {
        if (getProgrammer() == null) {
            // no mode selected
            for (JRadioButton button : buttonPool) {
                button.setSelected(false);
            }
            return;
        }

        ProgrammingMode mode = getProgrammer().getMode();
        if (opsAccyMode) {
            mode = ProgrammingMode.OPSACCBYTEMODE;
        } else if (opsSigMode) {
            mode = ProgrammingMode.OPSACCEXTBYTEMODE;
        }
        JRadioButton button = buttonMap.get(mode);
        if (button == null) {
            log.error("setGuiFromProgrammer found mode \"{}\" that's not supported by the programmer", mode);
            return;
        }
        log.debug("  setting button for mode {}", mode);
        button.setSelected(true);
        setAddrParams();
    }

    /**
     * Set address limits and field names depending on address type.
     */
    void setAddrParams() {
        if (opsAccyMode) {
            shortAddrButton.setText(Bundle.getMessage("DecoderAddress"));
            shortAddrButton.setToolTipText(Bundle.getMessage("ToolTipDecoderAddress"));
            shortAddrButton.setVisible(true);
            longAddrButton.setText(Bundle.getMessage("AccessoryAddress"));
            longAddrButton.setToolTipText(Bundle.getMessage("ToolTipAccessoryAddress"));
            offsetAddrCheckBox.setVisible(false);
            addressLabel.setText(Bundle.getMessage("AddressLabel"));
            if (longAddrButton.isSelected()) {
                lowAddrLimit = 1;
                highAddrLimit = 2044;
            } else {
                lowAddrLimit = 1;
                highAddrLimit = 511;
            }
        } else if (opsSigMode) {
            shortAddrButton.setVisible(false);
            longAddrButton.setVisible(false);
            offsetAddrCheckBox.setVisible(true);
            addressLabel.setText(Bundle.getMessage("SignalAddressLabel"));
            lowAddrLimit = 1;
            highAddrLimit = 2044;
        } else {
            shortAddrButton.setText(Bundle.getMessage("ShortAddress"));
            shortAddrButton.setToolTipText(Bundle.getMessage("ToolTipShortAddress"));
            shortAddrButton.setVisible(true);
            longAddrButton.setText(Bundle.getMessage("LongAddress"));
            longAddrButton.setToolTipText(Bundle.getMessage("ToolTipLongAddress"));
            offsetAddrCheckBox.setVisible(false);
            addressLabel.setText(Bundle.getMessage("AddressLabel"));
            if (longAddrButton.isSelected()) {
                lowAddrLimit = 0;
                highAddrLimit = 10239;
            } else {
                lowAddrLimit = 1;
                highAddrLimit = 127;
            }
        }

        log.debug(
                "Setting lowAddrLimit={}, highAddrLimit={}", lowAddrLimit, highAddrLimit);
        model.setMinimum(lowAddrLimit);

        model.setMaximum(highAddrLimit);
        int address;

        try {
            address = (Integer) mAddrField.getValue();
        } catch (java.lang.NumberFormatException e) {
            log.debug("loco address \"{}\" not correct", mAddrField.getValue());
            return;
        }
        if (address < lowAddrLimit) {
            mAddrField.setValue(lowAddrLimit);
        } else if (address > highAddrLimit) {
            mAddrField.setValue(highAddrLimit);
        }
    }

// Free up memory from no longer needed stuff, disconnect if still connected.
    @Override
    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(ProgOpsModePane.class
            .getName());

}

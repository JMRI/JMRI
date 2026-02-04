package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusConsistManager;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Mode Switcher to switch modes between programmer and command station for simple
 * hardware with a single track output.
 *
 * @author Andrew Crosland Copyright (C) 2020, 2021
 */
public class SprogCbusSimpleModeSwitcherFrame extends SprogCbusModeSwitcherFrame {
    
    protected static final int PROG_MODE = 0;     // Original SPROG Programnmer mode
    protected static final int CMD_MODE = 1;      // Original SPROG Command Station mode

    private JRadioButton progModeButton;
    private JRadioButton cmdModeButton;

    public SprogCbusSimpleModeSwitcherFrame(CanSystemConnectionMemo memo) {
        super(memo, Bundle.getMessage("SprogCbusSimpleModeSwitcher"));
    }
    
    
    /**
     * Display radio buttons to select between Programmer mode (service mode
     * programming) and command station  mode (ops mode programming).
     * <p>
     * Only one mode may be selected at a time.
     * <p>
     * At least one mode must be enabled and the default is programmer mode if
     * all modes are deselected.
     * 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        if (initSetup()) {
            // Create selection buttons, add to exclusive group and set initial state from preferences
            progModeButton = new JRadioButton(Bundle.getMessage("ProgMode"));
            cmdModeButton = new JRadioButton(Bundle.getMessage("CmdMode"));
            ButtonGroup buttons = new ButtonGroup();
            buttons.add(progModeButton);
            buttons.add(cmdModeButton);
            
            // Get current preferences
            // It is expected that the saved preferences will usually match the hardware.
            if (pm.isGlobalProgrammerAvailable() && preferences.isGlobalProgrammerAvailable()) {
                // Programmer (service) mode
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(false);
                mode = PROG_MODE;
                _memo.setMultipleThrottles(false);
            } else if (pm.isAddressedModePossible() && preferences.isAddressedModePossible()) {
                // Command Station (ops, addressed) mode
                progModeButton.setSelected(false);
                cmdModeButton.setSelected(true);
                mode = CMD_MODE;
                _memo.setMultipleThrottles(true);
            } else {
                // Default to programmer (service) mode if inconsistent preference
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(false);
                mode = PROG_MODE;
                _memo.setMultipleThrottles(false);
            }
            // Reset hardware mode and preferences in case there was any inconsistency
            setHardwareMode(mode);
            preferences.setProgrammersAvailable(progModeButton.isSelected(), cmdModeButton.isSelected());

            // Handle Programmer mode button activity
            ActionListener listener = ae -> {
                CbusConsistManager cm = (CbusConsistManager)InstanceManager.getNullableDefault(jmri.ConsistManager.class);
                if (progModeButton.isSelected() && mode != PROG_MODE) {
                    // Switch to programmer mode
                    log.info("Setting Global Programmer Available");
                    pm.setGlobalProgrammerAvailable(true);
                    log.info("Setting Addressed Programmer Unavailable");
                    pm.setAddressedModePossible(false);
                    _memo.setMultipleThrottles(false);
                    showServiceModeWarningDialogue();
                    closeProgrammerWarningDialogue();
                    if (cm != null) {
                        cm.setEnabled(false);
                    }
                    mode = PROG_MODE;
                } else if (cmdModeButton.isSelected() && mode != CMD_MODE) {
                    // Switch to command station mode
                    log.info("Setting Global Programmer Unavailable");
                    pm.setGlobalProgrammerAvailable(false);
                    log.info("Setting Addressed Programmer Available");
                    pm.setAddressedModePossible(true);
                    _memo.setMultipleThrottles(true);
                    closeProgrammerWarningDialogue();
                    if (cm != null) {
                        cm.setEnabled(true);
                    }
                    mode = CMD_MODE;
                }
                setHardwareMode(mode);
                preferences.setProgrammersAvailable(progModeButton.isSelected(), cmdModeButton.isSelected());
            };

            progModeButton.addActionListener(listener);
            cmdModeButton.addActionListener(listener);
            modePane.add(progModeButton);
            modePane.add(cmdModeButton);

            panel.add(label, BorderLayout.NORTH);
            panel.add(modePane, BorderLayout.CENTER);
        }
        
        // add help menu to window
        setHelp();

        this.add(panel);
        ThreadingUtil.runOnGUI( () -> {
            pack();
            setVisible(true);
        });
    }

    private boolean _hideProgWarning = false;

    protected void closeProgrammerWarningDialogue(){
        if ((!java.awt.GraphicsEnvironment.isHeadless()) && (!_hideProgWarning)){
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                javax.swing.JCheckBox checkbox = new javax.swing.JCheckBox(Bundle.getMessage("HideFurtherWarnings"));
                java.awt.event.ActionListener progPopUpCheckBox = (java.awt.event.ActionEvent evt) -> hideProgWarning(checkbox.isSelected());
                checkbox.addActionListener(progPopUpCheckBox);
                Object[] params = {Bundle.getMessage("ProgWarning"), checkbox};
                JmriJOptionPane.showMessageDialogNonModal(null, params,
                    Bundle.getMessage("switchMode"),
                    JmriJOptionPane.WARNING_MESSAGE, null);
            });
        }
    }

    /**
     * Receive notification from a mode switcher dialogue to close programmer when
     * switching modes. This so buttons correctly reflect available operations.
     * False by default to show notifications
     *
     * @param hide set True to hide notifications, else False.
     */
    public void hideProgWarning(boolean hide){
        _hideProgWarning = hide;
    }

    private boolean _hideProgModeWarning = false;

    protected void showServiceModeWarningDialogue(){
        if ((!java.awt.GraphicsEnvironment.isHeadless()) && (!_hideProgModeWarning)){
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                javax.swing.JCheckBox checkbox = new javax.swing.JCheckBox(Bundle.getMessage("HideFurtherWarnings"));
                Object[] params = {Bundle.getMessage("ProgModeWarning"), checkbox};
                java.awt.event.ActionListener progPopUpCheckBox = (java.awt.event.ActionEvent evt) -> hideProgModeWarning(checkbox.isSelected());
                checkbox.addActionListener(progPopUpCheckBox);
                JmriJOptionPane.showMessageDialogNonModal(null,params,
                    Bundle.getMessage("switchToProgMode"),
                    JmriJOptionPane.WARNING_MESSAGE, null);
            });
        }
    }

    /**
     * Receive notification from a mode switcher dialogue to display warning
     * message about service mode programminf.
     * False by default to show notifications
     *
     * @param hide set True to hide notifications, else False.
     */
    public void hideProgModeWarning(boolean hide){
        _hideProgModeWarning = hide;
    }

    /**
     * Define help menu for this window.
     */
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.can.cbus.swing.modeswitcher.SprogCbusSimpleModeSwitcherFrame", true); // NOI18N
    }

    
    /**
     * disconnect from the CBUS
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogCbusSimpleModeSwitcherFrame.class);
    
}

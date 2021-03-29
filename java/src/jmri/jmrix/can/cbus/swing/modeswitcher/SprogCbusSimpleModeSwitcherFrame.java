package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

import jmri.jmrix.can.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                if (progModeButton.isSelected() && mode != PROG_MODE) {
                    // Switch to programmer mode
                    log.info("Setting Global Programmer Available");
                    pm.setGlobalProgrammerAvailable(true);
                    pm.setAddressedModePossible(false);
                    _memo.setMultipleThrottles(false);
                    showServiceModeWarningDialogue();
                    closeProgrammerWarningDialogue();
                    mode = PROG_MODE;
                } else if (cmdModeButton.isSelected() && mode != CMD_MODE) {
                    // Switch to command station mode
                    log.info("Setting Global Programmer Unavailable");
                    pm.setGlobalProgrammerAvailable(false);
                    pm.setAddressedModePossible(true);
                    _memo.setMultipleThrottles(true);
                    closeProgrammerWarningDialogue();
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
        pack();
        setVisible(true);
    }

    private boolean _hideProgWarning = false;

    protected void closeProgrammerWarningDialogue(){
        if ((!java.awt.GraphicsEnvironment.isHeadless()) && (!_hideProgWarning)){
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                javax.swing.JCheckBox checkbox = new javax.swing.JCheckBox(Bundle.getMessage("HideFurtherWarnings"));
                Object[] params = {Bundle.getMessage("ProgWarning"), checkbox};
                javax.swing.JOptionPane pane = new javax.swing.JOptionPane(params);
                pane.setMessageType(javax.swing.JOptionPane.WARNING_MESSAGE);
                JDialog dialog = pane.createDialog(null, Bundle.getMessage("switchMode"));
                dialog.setModal(false);
                dialog.setVisible(true);
                dialog.requestFocus();
                dialog.toFront();
                java.awt.event.ActionListener progPopUpCheckBox = (java.awt.event.ActionEvent evt) -> hideProgWarning(checkbox.isSelected());
                checkbox.addActionListener(progPopUpCheckBox);
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
                javax.swing.JOptionPane pane = new javax.swing.JOptionPane(params);
                pane.setMessageType(javax.swing.JOptionPane.WARNING_MESSAGE);
                JDialog dialog = pane.createDialog(null, Bundle.getMessage("switchToProgMode"));
                dialog.setModal(false);
                dialog.setVisible(true);
                dialog.requestFocus();
                dialog.toFront();
                java.awt.event.ActionListener progPopUpCheckBox = (java.awt.event.ActionEvent evt) -> hideProgModeWarning(checkbox.isSelected());
                checkbox.addActionListener(progPopUpCheckBox);
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

    
    private final static Logger log = LoggerFactory.getLogger(SprogCbusSimpleModeSwitcherFrame.class);
    
}

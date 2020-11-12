package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import jmri.jmrix.can.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mode Switcher to switch modes between programmer and command station for simple
 * hardware with a single track output.
 *
 * @author Andrew Crosland Copyright (C) 2020
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
            mode = PROG_MODE;
            if (pm.isGlobalProgrammerAvailable() && preferences.isGlobalProgrammerAvailable()) {
                // Programmer mode
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(false);
            } else if (pm.isAddressedModePossible() && preferences.isAddressedModePossible()) {
                // Command Station mode
                progModeButton.setSelected(false);
                cmdModeButton.setSelected(true);
                mode = CMD_MODE;
            } else {
                // Default to service mode if inconsistent preference
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(false);
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
                    mode = PROG_MODE;
                } else if (cmdModeButton.isSelected() && mode != CMD_MODE) {
                    // Switch to command station mode
                    log.info("Setting Global Programmer Unavailable");
                    pm.setGlobalProgrammerAvailable(false);
                    pm.setAddressedModePossible(true);
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

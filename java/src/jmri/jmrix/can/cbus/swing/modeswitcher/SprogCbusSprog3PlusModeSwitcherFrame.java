package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

import jmri.jmrix.can.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mode Switcher to switch programming track mode.
 *
 * @author Andrew Crosland Copyright (C) 2020
 */
public class SprogCbusSprog3PlusModeSwitcherFrame extends SprogCbusModeSwitcherFrame {
    
    public static final int PROG_OFF_MODE = 0; // Prog track off when not programming
    public static final int PROG_ON_MODE = 1;  // Prog track follows main when not programming
    public static final int PROG_AR_MODE = 2;  // Prog track is auto-reverse power district, no programming on progtrack

    private JRadioButton progOffButton;
    private JRadioButton progOnButton;
    private JRadioButton progArButton;
    
    public SprogCbusSprog3PlusModeSwitcherFrame(CanSystemConnectionMemo memo) {
        super(memo, Bundle.getMessage("SprogCbusPlusModeSwitcher"));
    }
    
    
    /**
     * Display radio buttons to select programming track mode).
     * <p>
     * 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        if (initSetup()) {
            // Create selection buttons and set initial state
            progOffButton = new JRadioButton(Bundle.getMessage("ProgOffMode"));
            progOnButton = new JRadioButton(Bundle.getMessage("ProgOnMode"));
            progArButton = new JRadioButton(Bundle.getMessage("ProgArMode"));
            ButtonGroup buttons = new ButtonGroup();
            buttons.add(progOffButton);
            buttons.add(progOnButton);
            buttons.add(progArButton);
            
            // Get current preferences
            // It is expected that the saved preferences will usually match the hardware.
            mode = PROG_OFF_MODE;
            if (preferences.getProgTrackMode() == PROG_OFF_MODE) {
                progOffButton.setSelected(true);
                pm.setGlobalProgrammerAvailable(true);
            } else if (preferences.getProgTrackMode() == PROG_ON_MODE) {
                progOnButton.setSelected(true);
                mode = PROG_ON_MODE;
                pm.setGlobalProgrammerAvailable(true);
            } else if (preferences.getProgTrackMode() == PROG_AR_MODE) {
                progArButton.setSelected(true);
                mode = PROG_AR_MODE;
                pm.setGlobalProgrammerAvailable(false);
            } else {
                // Default if inconsistent preference
                progOffButton.setSelected(true);
                pm.setGlobalProgrammerAvailable(true);
            }
            // Reset hardware mode and preferences in case there was any inconsistency
            setHardwareMode(mode);
            preferences.setProgTrackMode(mode);

            // Handle Programming track mode button activity
            ActionListener listener = ae -> {
                if (progOnButton.isSelected()) {
                    log.info("Setting prog track on when not programming");
                    mode = PROG_ON_MODE;
                    setHardwareMode(mode);
                    pm.setGlobalProgrammerAvailable(true);
                } else if (progArButton.isSelected()) {
                    log.info("Setting prog track to auto-reverse");
                    mode = PROG_AR_MODE;
                    setHardwareMode(mode);
                    pm.setGlobalProgrammerAvailable(false);
                } else {
                    log.info("Setting prog track off when not programming");
                    mode = PROG_OFF_MODE;
                    setHardwareMode(mode);
                    pm.setGlobalProgrammerAvailable(true);
                }
                preferences.setProgTrackMode(mode);
            };

            progOffButton.addActionListener(listener);
            progOnButton.addActionListener(listener);
            progArButton.addActionListener(listener);
            modePane.add(progOffButton);
            modePane.add(progOnButton);
            modePane.add(progArButton);

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
        addHelpMenu("package.jmri.jmrix.can.cbus.swing.modeswitcher.SprogCbusSprog3PlusModeSwitcherFrame", true); // NOI18N
    }

    
    /**
     * disconnect from the CBUS
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    
    private final static Logger log = LoggerFactory.getLogger(SprogCbusSprog3PlusModeSwitcherFrame.class);
    
}

package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mode Switcher to switch modes between programmer and command station.
 *
 * No matter what I tried, I cannot get the label text to wrap when added to a
 * CanNamedPane, so this is implemented as a JmriJFrame.
 * 
 * @author Andrew Crosland Copyright (C) 2020
 */
public class SprogCbusModeSwitcherPane extends JmriJFrame {
    
    CbusDccProgrammerManager pm = null;
    
    JRadioButton progModeButton;
    JRadioButton cmdModeButton;

    private CbusPreferences preferences;

    public SprogCbusModeSwitcherPane() {
        super();
    }
    
    
    /**
     * Display radio buttons to select between Programmer mode (service mode
     * programming) and command station  mode (ops mode programming).
     * <p>
     * It is possible to enable both modes, but care must be exercised not to
     * use the service mode programmer when connected to a layout with multiple
     * decoders present.
     * <p>
     * At least one mode must be enabled and the default is programmer mode if
     * all modes are deselected.
     * 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        
        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        
        JLabel label = new JLabel();
        // Wrap  in html to get wrapping when added to border layout
        label.setText("<html>"+Bundle.getMessage("HardwareModeLabel")+"</html>");
        
        // Mode selector
        JPanel modePane = new JPanel();
        modePane.setLayout(new BoxLayout(modePane, BoxLayout.Y_AXIS));
        modePane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("HardwareMode")));

        // Create selection buttons and set initial state
        progModeButton = new JRadioButton(Bundle.getMessage("ProgMode"));
        cmdModeButton = new JRadioButton(Bundle.getMessage("CmdMode"));
        if (pm.isGlobalProgrammerAvailable()) {
            progModeButton.setSelected(true);
        } else {
            progModeButton.setSelected(false);
        }
        if (pm.isAddressedModePossible()) {
            cmdModeButton.setSelected(true);
        } else {
            cmdModeButton.setSelected(false);
        }
        
        // Handle Programmer mode button activity
        ActionListener setProgMode = ae -> {
            if (progModeButton.isSelected()) {
                // Enable service mode programmer
                log.info("Setting Global Programmer Available");
                pm.setGlobalProgrammerAvailable(true);
            } else if (cmdModeButton.isSelected()) {
                // Only disable service mode if ops mode active
                log.info("Setting Global Programmer Unavailable");
                pm.setGlobalProgrammerAvailable(false);
            } else {
                // Service mode is the default if all are deselected - reselect it
                log.info("Cannot de-select programmer mode as only mode");
                progModeButton.setSelected(true);
            }
            preferences.setProgrammersAvailable(progModeButton.isSelected(), cmdModeButton.isSelected());
        };
        
        // Handle command station mode button activity
        ActionListener setCmdMode = ae -> {
            if (cmdModeButton.isSelected()) {
                // Enable ops mode programmer
                log.info("Setting Addressed Programmer Available");
                pm.setAddressedModePossible(true);
            } else {
                // Disable ops mode programmer
                log.info("Setting Addressed Programmer Unavailable");
                pm.setAddressedModePossible(false);
                if (!progModeButton.isSelected()) {
                    // Re-enable service mode if all are deselected
                    log.info("No current programmer, setting Global Programmer Available");
                    pm.setGlobalProgrammerAvailable(true);
                    progModeButton.setSelected(true);
                }
            }
            preferences.setProgrammersAvailable(progModeButton.isSelected(), cmdModeButton.isSelected());
        };
        
        progModeButton.addActionListener(setProgMode);
        cmdModeButton.addActionListener(setCmdMode);
        modePane.add(progModeButton);
        modePane.add(cmdModeButton);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(modePane, BorderLayout.CENTER);
        
        this.add(panel);
        pack();
        setVisible(true);
    }
    
    private final static Logger log = LoggerFactory.getLogger(SprogCbusModeSwitcherPane.class);
    
}

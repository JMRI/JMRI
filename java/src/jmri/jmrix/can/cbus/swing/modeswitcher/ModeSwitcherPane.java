package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.AddressedProgrammerManager;
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
public class ModeSwitcherPane extends JmriJFrame {
    
    CbusDccProgrammerManager pm = null;
    
    JRadioButton progModeButton;
    JRadioButton cmdModeButton;

    private CbusPreferences preferences;

    public ModeSwitcherPane() {
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

        progModeButton = new JRadioButton(Bundle.getMessage("ProgMode"));
        cmdModeButton = new JRadioButton(Bundle.getMessage("CmdMode"));
        switch (preferences.getHardwareMode()) {
            case BOTH:
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(true);
                log.info("Hardware mode from preferences now BOTH");
                break;
            case COMMANDSTATION:
                progModeButton.setSelected(false);
                cmdModeButton.setSelected(true);
                log.info("Hardware mode from preferences now COMMANDSTATION"); 
                break;
            default:    // Programmer
                progModeButton.setSelected(true);
                cmdModeButton.setSelected(false);
                log.info("Hardware mode from preferences now PROGRAMMER");
                break;
        }
        
        // Handle Programmer mode button activity
        ActionListener setProgMode = ae -> {
            if (progModeButton.isSelected()) {
                // Enable service mode programmer
                log.debug("Firing property change on {}", InstanceManager.getListPropertyName(GlobalProgrammerManager.class));
                pm.setGlobalProgrammerAvailable(true);
                InstanceManager.firePropertyChange(InstanceManager.getListPropertyName(GlobalProgrammerManager.class), false, true);
            } else if (cmdModeButton.isSelected()) {
                // Only disable service mode if ops mode active
                log.debug("Firing property change on {}", InstanceManager.getListPropertyName(GlobalProgrammerManager.class));
                pm.setGlobalProgrammerAvailable(false);
                InstanceManager.firePropertyChange(InstanceManager.getListPropertyName(GlobalProgrammerManager.class), true, false);
            } else {
                // Service mode is the default if all are deselected - reselect it
                log.debug("Cannot de-select programmer mode");
                progModeButton.setSelected(true);
            }
            writeMode();
        };
        
        // Handle command station mode button activity
        ActionListener setCmdMode = ae -> {
            if (cmdModeButton.isSelected()) {
                // Enable ops mode programmer
                log.debug("Firing property change on {}", InstanceManager.getListPropertyName(AddressedProgrammerManager.class));
                pm.setAddressedModePossible(true);
                InstanceManager.firePropertyChange(InstanceManager.getListPropertyName(AddressedProgrammerManager.class), false, true);
            } else {
                // Disable ops mode programmer
                log.debug("Firing property change on {}", InstanceManager.getListPropertyName(AddressedProgrammerManager.class));
                pm.setAddressedModePossible(false);
                InstanceManager.firePropertyChange(InstanceManager.getListPropertyName(AddressedProgrammerManager.class), true, false);
                if (!progModeButton.isSelected()) {
                    // Re-enable service mode if all are deselected
                    log.debug("Firing property change on {}", InstanceManager.getListPropertyName(GlobalProgrammerManager.class));
                    pm.setGlobalProgrammerAvailable(true);
                    InstanceManager.firePropertyChange(InstanceManager.getListPropertyName(GlobalProgrammerManager.class), false, true);
                    progModeButton.setSelected(true);
                }
            }
            writeMode();
        };
        
        progModeButton.addActionListener(setProgMode);
        cmdModeButton.addActionListener(setCmdMode);
        modePane.add(progModeButton);
        modePane.add(cmdModeButton);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(modePane, BorderLayout.CENTER);
        
        this.add(panel);

        setVisible(true);
    }
    
    
    /**
     * Write the current mode to the preferences
     */
    private void writeMode() {
        if (progModeButton.isSelected() && cmdModeButton.isSelected()) {
            preferences.setHardwareMode(CbusPreferences.HardwareMode.BOTH);
            log.info("Hardware mode now BOTH");
        } else if (cmdModeButton.isSelected()) {
            preferences.setHardwareMode(CbusPreferences.HardwareMode.COMMANDSTATION);
            log.info("Hardware mode now COMMANDSTATION");
        } else {
            preferences.setHardwareMode(CbusPreferences.HardwareMode.PROGRAMMER);
            log.info("Hardware mode now PROGRAMMER");
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ModeSwitcherPane.class);
    
}

package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.ProgrammerManager.ProgrammerType;
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
public class CbusModeSwitcherPane extends JmriJFrame {
    
    CbusDccProgrammerManager cdpm = null;
    GlobalProgrammerManager gpm = null;
    
    JRadioButton progModeButton;
    JRadioButton cmdModeButton;

    private CbusPreferences preferences;

    public CbusModeSwitcherPane() {
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
        gpm = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        
        JLabel label = new JLabel();
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!(gpm instanceof CbusDccProgrammerManager)) {
            // We don't have a CBUS programmer
            label.setText("<html>"+Bundle.getMessage("NoCBUSProgrammer")+"</html>");
            panel.add(label, BorderLayout.NORTH);
        } else {
            cdpm = (CbusDccProgrammerManager)gpm;
            
            // Wrap  in html to get wrapping when added to border layout
            label.setText("<html>"+Bundle.getMessage("HardwareModeLabel")+"</html>");

            // Mode selector
            JPanel modePane = new JPanel();
            modePane.setLayout(new BoxLayout(modePane, BoxLayout.Y_AXIS));
            modePane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("HardwareMode")));

            progModeButton = new JRadioButton(Bundle.getMessage("ProgMode"));
            cmdModeButton = new JRadioButton(Bundle.getMessage("CmdMode"));
            switch (preferences.getProgrammerType()) {
                case BOTH:
                    progModeButton.setSelected(true);
                    cmdModeButton.setSelected(true);
                    log.info("Hardware mode from preferences now BOTH");
                    break;
                case ADDRESSED: // command station with addressed (ops mode) programmer
                    progModeButton.setSelected(false);
                    cmdModeButton.setSelected(true);
                    log.info("Programmer from preferences now ADDRESSED"); 
                    break;
                default:    // Programmer with global (service mode) programmer
                    progModeButton.setSelected(true);
                    cmdModeButton.setSelected(false);
                    log.info("Programmer type from preferences now GLOBAL");
                    break;
            }

            // Handle Programmer mode button activity
            ActionListener setMode = (ActionEvent ae) -> {
                if ((!progModeButton.isSelected() && !cmdModeButton.isSelected())
                        || (progModeButton.isSelected() && !cmdModeButton.isSelected())) {
                    // Default or switch to programmer mode
                    progModeButton.setSelected(true);
                    log.info("System now in programmer mode with global (service mode) programmer");
                    cdpm.setProgrammerType(ProgrammerType.GLOBAL);
                } else if (!progModeButton.isSelected() && cmdModeButton.isSelected()) {
                    // Switch to command staton mode
                    log.info("System now in command station mode with addressed (ops mode) programmer");
                    cdpm.setProgrammerType(ProgrammerType.ADDRESSED);
                } else {
                    // Switch to both
                    log.debug("System now in universal mode with global and addressed programmers");
                    cdpm.setProgrammerType(ProgrammerType.BOTH);
                }
                preferences.setProgrammerType(cdpm.getProgrammerType());
            };

            progModeButton.addActionListener(setMode);
            cmdModeButton.addActionListener(setMode);
            modePane.add(progModeButton);
            modePane.add(cmdModeButton);

            panel.add(label, BorderLayout.NORTH);
            panel.add(modePane, BorderLayout.CENTER);
        }
        
        this.add(panel);

        setVisible(true);
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(CbusModeSwitcherPane.class);
    
}

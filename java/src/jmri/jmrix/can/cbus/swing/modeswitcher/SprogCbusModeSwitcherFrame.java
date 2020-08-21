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
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mode Switcher to switch modes between programmer and command station.
 *
 * @author Andrew Crosland Copyright (C) 2020
 */
public class SprogCbusModeSwitcherFrame extends JmriJFrame 
        implements CanListener {
    
    private static final int PROP_CMD_STATION = 5;
    private static final int PROG_MODE = 0;
    private static final int CMD_MODE = 1;
    
    private CbusPreferences preferences;
    private CbusDccProgrammerManager pm = null;
    private CanSystemConnectionMemo _memo = null;
    private TrafficController tc;
    private CbusSend send;

    JRadioButton progModeButton;
    JRadioButton cmdModeButton;

    int csNode;

    public SprogCbusModeSwitcherFrame(CanSystemConnectionMemo memo) {
        super();
        _memo = memo;
        
        // connect to the CanInterface
        tc = _memo.getTrafficController();
        addTc(tc);
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
        JPanel panel = new JPanel(new BorderLayout());
        
        if (pm == null) {
            // Wrap  in html to get wrapping when added to border layout
            label.setText("<html>"+Bundle.getMessage("NoCbusProgrammer")+"</html>");
            panel.add(label, BorderLayout.NORTH);
        } else {
            csNode = 65534;
            CbusNodeTableDataModel cs =  jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
            if (cs != null) {
                CbusNode csnode = cs.getCsByNum(0);
                if (csnode != null) {
                    csNode = csnode.getNodeNumber();
                }
            } else {
                log.info("Unable to fetch Master Command Station from Node Manager");
            }
            send = new CbusSend(_memo);
            
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
                    setHardwareMode();
                } else if (cmdModeButton.isSelected()) {
                    // Only disable service mode if ops mode active
                    log.info("Setting Global Programmer Unavailable");
                    pm.setGlobalProgrammerAvailable(false);
                    setHardwareMode();
                } else {
                    // Service mode is the default if all are deselected
                    // Reselect it - nothing has changed
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
                    setHardwareMode();
                } else {
                    // Disable ops mode programmer
                    log.info("Setting Addressed Programmer Unavailable");
                    pm.setAddressedModePossible(false);
                    setHardwareMode();
                    if (!progModeButton.isSelected()) {
                        // Re-enable service mode if all are deselected
                        log.info("No current programmer, setting Global Programmer Available");
                        pm.setGlobalProgrammerAvailable(true);
                        progModeButton.setSelected(true);
                        setHardwareMode();
                    }
                }
                preferences.setProgrammersAvailable(progModeButton.isSelected(), cmdModeButton.isSelected());
            };

            progModeButton.addActionListener(setProgMode);
            cmdModeButton.addActionListener(setCmdMode);
            modePane.add(progModeButton);
            modePane.add(cmdModeButton);

            panel.add(label, BorderLayout.NORTH);
            panel.add(modePane, BorderLayout.CENTER);
        }
        
        this.add(panel);
        pack();
        setVisible(true);
    }
    
    
    /**
     * Set the hardware operating mode to programmer mode if only service mode is
     * selected, otherwise set it to command station mode
     */
    private void setHardwareMode() {
        if (progModeButton.isSelected() && !cmdModeButton.isSelected()) {
            send.nVSET(csNode, PROP_CMD_STATION, PROG_MODE);
        } else {
            send.nVSET(csNode, PROP_CMD_STATION, CMD_MODE);
        }
    }
    
    
    /**
     * Process outgoing CAN messages
     * 
     * {@inheritDoc} 
     */
    @Override
    public void message(CanMessage m) {
    }
    
    
    /**
     * Processes incoming CAN replies
     * <p>
     *
     * {@inheritDoc} 
     */
    @Override
    public void reply(CanReply r) {
        
        if ( r.isRtr() ) {
            return;
        }
        
        if (!r.isExtended() ) {
            log.debug("Standard Reply {}", r);
            
        }
    }
    
    
    /**
     * disconnect from the CBUS
     */
    @Override
    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }

    
    private final static Logger log = LoggerFactory.getLogger(SprogCbusModeSwitcherFrame.class);
    
}

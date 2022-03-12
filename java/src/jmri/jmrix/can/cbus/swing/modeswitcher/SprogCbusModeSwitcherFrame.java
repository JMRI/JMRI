package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.ConfigurationManager.ProgModeSwitch;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for CBUS SPROG Mode Switcher .
 *
 * @author Andrew Crosland Copyright (C) 2020
 */
public class SprogCbusModeSwitcherFrame extends JmriJFrame 
        implements CanListener {
    
    protected static final int PROP_CMD_STATION = 5;
    
    protected CbusPreferences preferences;
    protected CbusDccProgrammerManager pm = null;
    protected CanSystemConnectionMemo _memo = null;
    protected ProgModeSwitch _pms;
    protected TrafficController tc;
    protected CbusSend send;
    
    protected JLabel label = null;
    protected JPanel panel = null;
    protected JPanel modePane = null;

    protected int mode;

    int csNode;

    public SprogCbusModeSwitcherFrame(CanSystemConnectionMemo memo, String s) {
        super(s);
        _memo = memo;
        _pms = memo.getProgModeSwitch();
        
        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);

        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        
        // connect to the CanInterface
        tc = _memo.getTrafficController();
        addTc(tc);
    }
    
    
    protected boolean initSetup()  {
        label = new JLabel();
        panel = new JPanel(new BorderLayout());
        
        if (pm == null) {
            // Wrap  in html to get wrapping when added to border layout
            label.setText("<html>"+Bundle.getMessage("NoCbusProgrammer")+"</html>");
            panel.add(label, BorderLayout.NORTH);
            return false;
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
            modePane = new JPanel();
            modePane.setLayout(new BoxLayout(modePane, BoxLayout.Y_AXIS));
            modePane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("HardwareMode")));
            
            return true;
        }
    }
    

    /**
     * Switch the hardware to the requested operating mode
     * 
     * @param mode mode requested
     */
    protected void setHardwareMode(int mode) {
        send.nVSET(csNode, PROP_CMD_STATION, mode);
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

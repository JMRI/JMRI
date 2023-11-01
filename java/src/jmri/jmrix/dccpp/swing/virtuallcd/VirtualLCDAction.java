/**
 * Swing action to create and register a DisplayFrame
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */
package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.*;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

public class VirtualLCDAction extends DCCppSystemConnectionAction {

    public VirtualLCDAction(String s, jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        super(s, memo);
    }

    public VirtualLCDAction(jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        this(Bundle.getMessage("VirtualLCDFrameTitle"), memo);
    }

    public VirtualLCDAction() {
        this(InstanceManager.getDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo == null) {
            log.error("connection memo was null!");
            return;
        }

        // create a VirtualLCDFrame
        VirtualLCDFrame f = new VirtualLCDFrame(memo);
        try {
            DCCppTrafficController tc = memo.getDCCppTrafficController();
            tc.addDCCppListener(DCCppInterface.CS_INFO, f);
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception",ex);
        }
        f.setVisible(true);
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDAction.class);
}




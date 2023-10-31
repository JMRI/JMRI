/**
 * Swing action to create and register a DisplayFrame
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */
package jmri.jmrix.dccpp.swing.lcd;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.*;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

public class DisplayAction extends DCCppSystemConnectionAction {

    public DisplayAction(String s, jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        super(s, memo);
    }

    public DisplayAction(jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        this("Display DCC-EX Status", memo);
    }

    public DisplayAction() {
        this(InstanceManager.getDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo == null) {
            log.error("connection memo was null!");
            return;
        }

        // create a DisplayFrame
        DisplayFrame f = new DisplayFrame(memo);
        try {
            DCCppTrafficController tc = memo.getDCCppTrafficController();
            tc.addDCCppListener(DCCppInterface.ALL, f);  // TODO: This should be restricted to the proper selection class
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception",ex);
        }
        f.setVisible(true);
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DisplayAction.class);
}




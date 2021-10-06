/**
 * PacketGenAction.java
 *
 * Swing action to create and register a XpressNet PacketGenFrame
 * object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author      Mark Underwood Copyright (C) 2015
 */
package jmri.jmrix.dccpp.swing.packetgen;

import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

public class PacketGenAction extends DCCppSystemConnectionAction {

    public PacketGenAction(String s, jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        super(s, memo);
    }

    public PacketGenAction(jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        this("Generate DCC++ message", memo);
    }

    public PacketGenAction() {
        this(InstanceManager.getDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
        // create a PacketGenFrame
        PacketGenFrame f = new PacketGenFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}",ex);
        }
        f.setVisible(true);
    }
    private static final Logger log = LoggerFactory.getLogger(PacketGenAction.class);
}




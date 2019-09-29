/**
 * PacketGenAction.java
 *
 * Description: Swing action to create and register a XpressNet PacketGenFrame
 * object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author      Mark Underwood Copyright (C) 2015
 */
package jmri.jmrix.dccpp.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGenAction extends AbstractAction {

       jmri.jmrix.dccpp.DCCppSystemConnectionMemo _memo = null;

    public PacketGenAction(String s, jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        this("Generate DCC++ message", memo);
    }

       @Override
    public void actionPerformed(ActionEvent e) {
        // create a PacketGenFrame
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}",ex);
        }
        f.setVisible(true);

        // connect to the TrafficController
        f.connect(_memo.getDCCppTrafficController());
    }
    private static final Logger log = LoggerFactory.getLogger(PacketGenAction.class);
}




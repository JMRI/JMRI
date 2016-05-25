/**
 * PacketGenAction.java
 *
 * Description:	Swing action to create and register a IEEE802154 PacketGenFrame
 * object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
package jmri.jmrix.ieee802154.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8964241715144652712L;
    jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo _memo = null;

    public PacketGenAction(String s, jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {
        this("Send IEEE 802.15.4 Message", memo);
    }

    public PacketGenAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo.class).get(0);
    }

    public PacketGenAction() {
        this("Send IEEE 802.15.4 Message");
    }

    public void actionPerformed(ActionEvent e) {
        // create a PacketGenFrame
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);

        // connect to the TrafficController
        f.connect(_memo.getTrafficController());
    }
    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class.getName());
}


/* @(#)LocoGenAction.java */

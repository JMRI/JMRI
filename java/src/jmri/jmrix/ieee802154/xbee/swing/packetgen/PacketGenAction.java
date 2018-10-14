/**
 * Swing action to create and register a IEEE802154 PacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGenAction extends AbstractAction {

    jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo _memo = null;

    public PacketGenAction(String s, XBeeConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo memo) {
        this(Bundle.getMessage("SendCommandTitle"), memo);
    }

    public PacketGenAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo.class).get(0);
    }

    public PacketGenAction() {
        this(Bundle.getMessage("SendXbeeCommandTitle"));
    }

    @Override
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
        f.connect((XBeeTrafficController) _memo.getTrafficController());
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}

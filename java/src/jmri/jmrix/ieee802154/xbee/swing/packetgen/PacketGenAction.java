/**
 * Swing action to create and register a IEEE802154 PacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.jmrix.ieee802154.xbee.swing.XBeeSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGenAction extends XBeeSystemConnectionAction {

    public PacketGenAction(String s, XBeeConnectionMemo memo) {
        super(s, memo);
    }

    public PacketGenAction(XBeeConnectionMemo memo) {
        this(Bundle.getMessage("SendCommandTitle"), memo);
    }

    public PacketGenAction(String s) {
        this(InstanceManager.getNullableDefault(XBeeConnectionMemo.class));
    }

    public PacketGenAction() {
        this(Bundle.getMessage("SendXbeeCommandTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        XBeeConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            // create a PacketGenFrame
            PacketGenFrame f = new PacketGenFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.error("Exception: {}", ex.toString());
            }
            f.setVisible(true);

            // connect to the TrafficController
            f.connect((XBeeTrafficController) memo.getTrafficController());
        } else {
            log.error("No connection, so not performing action {}", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}

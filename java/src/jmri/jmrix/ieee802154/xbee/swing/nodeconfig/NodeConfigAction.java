package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class NodeConfigAction extends jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigAction {

    private jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo xcm = null;

    public NodeConfigAction(String s, jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo cm) {
        super(s, cm);
        if (cm == null) {
            // find the first registered memo.
            try {
                xcm = jmri.InstanceManager.
                        getList(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo.class).get(0);
            } catch (java.lang.NullPointerException|java.lang.IndexOutOfBoundsException e) {
                // no memo is registered, is this the first time the
                // connection has been configured?
                log.debug("No XBee System Connection Memo available");
            }
        } else {
            xcm = cm;
        }
    }

    public NodeConfigAction() {
        this(Bundle.getMessage("ConfigureXbeeTitle"), null);
    }

    public NodeConfigAction(String s) {
        this(s, null);
    }

    public NodeConfigAction(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo cm) {
        this(Bundle.getMessage("ConfigureXbeeTitle"), cm);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame((jmri.jmrix.ieee802154.xbee.XBeeTrafficController) xcm.getTrafficController());
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class.getName());

}

package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NodeConfigAction extends AbstractAction {

    private jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo icm = null;

    public NodeConfigAction(String s, jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo cm) {
        super(s);
        if (cm == null) {
            try {
                // find the first registered memo.
                icm = jmri.InstanceManager.
                        getList(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo.class).get(0);
            } catch (java.lang.NullPointerException|java.lang.IndexOutOfBoundsException e) {
                // no memo exists, are we configuring this for the first time?
                log.debug("No IEEE 802.15.4 System Connection Memo available");
            }
        } else {
            icm = cm;
        }
    }

    public NodeConfigAction() {
        this("Configure IEEE802154 Nodes", null);
    }

    public NodeConfigAction(String s) {
        this(s, null);
    }

    public NodeConfigAction(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo cm) {
        this("Configure IEEE802154 Nodes", cm);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(icm.getTrafficController());
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class);

}

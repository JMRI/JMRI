// NodeTableAction.java
package jmri.jmrix.grapevine.nodetable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeTableFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 * @version	$Revision$
 */
public class NodeTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8663753655086906561L;

    public NodeTableAction(String s) {
        super(s);
    }

    public NodeTableAction() {
        this("Configure Grapevine Nodes");
    }

    public void actionPerformed(ActionEvent e) {
        NodeTableFrame f = new NodeTableFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(NodeTableAction.class.getName());
}


/* @(#)NodeTableAction.java */

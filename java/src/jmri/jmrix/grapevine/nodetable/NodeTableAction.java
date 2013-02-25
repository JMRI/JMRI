// NodeTableAction.java

package jmri.jmrix.grapevine.nodetable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeTableFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2006, 2008
 * @version	$Revision$
 */
public class NodeTableAction extends AbstractAction {

	public NodeTableAction(String s) { super(s);}

    public NodeTableAction() {
        this("Configure Grapevine Nodes");
    }

    public void actionPerformed(ActionEvent e) {
        NodeTableFrame f = new NodeTableFrame();
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setLocation(100,30);
        f.setVisible(true);
    }
   static Logger log = LoggerFactory.getLogger(NodeTableAction.class.getName());
}


/* @(#)NodeTableAction.java */

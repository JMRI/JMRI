// NodeConfigAction.java

package jmri.jmrix.oaktree.nodeconfig;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeConfigFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2006
 * @version	$Revision$
 */
public class NodeConfigAction extends AbstractAction {

	public NodeConfigAction(String s) { super(s);}

    public NodeConfigAction() {
        this("Configure Oak Tree Systems Nodes");
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame();
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setLocation(100,30);
        f.setVisible(true);
    }
   static Logger log = Logger.getLogger(NodeConfigAction.class.getName());
}


/* @(#)NodeConfigAction.java */

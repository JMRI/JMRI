// NodeConfigAction.java

package jmri.jmrix.cmri.serial.nodeconfig;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeConfigFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.2 $
 */
public class NodeConfigAction extends AbstractAction {

	public NodeConfigAction(String s) { super(s);}

    public NodeConfigAction() {
        this("Configure C/MRI Nodes");
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
        f.show();
    }
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NodeConfigAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */

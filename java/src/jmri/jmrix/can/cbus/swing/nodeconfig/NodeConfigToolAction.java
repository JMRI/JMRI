// NodeConfigToolAction.java

package jmri.jmrix.can.cbus.swing.nodeconfig;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.util.ResourceBundle;

/**
 * Create and register NodeConfigFrame
 * 
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision$
 * @since 2.3.1
 */
public class NodeConfigToolAction extends AbstractAction {

    public NodeConfigToolAction(String s) {
        super(s);
    }
    
    public NodeConfigToolAction() {
        this(ResourceBundle
                .getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle")
                        .getString("MenuItemNodeConfigTool"));
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigToolFrame f = new NodeConfigToolFrame();
        f.setVisible(true);
    }
}

/* @(#)NodeConfigToolAction.java */

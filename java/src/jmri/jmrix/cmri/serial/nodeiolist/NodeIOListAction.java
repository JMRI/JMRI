// NodeIOListAction.java

package jmri.jmrix.cmri.serial.nodeiolist;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			ListFrame object
 *
 * @author   Dave Duchamp  Copyright (C) 2006
 * @author   Chuck Catania Copyright (C) 2014
 * @version	$Revision: 17977 $
 */
public class NodeIOListAction extends AbstractAction {

    public NodeIOListAction(String s) { super(s);}

    public NodeIOListAction() {
        this("CMRInet Bit Assignments");
    }

    public void actionPerformed(ActionEvent e) {
        NodeIOListFrame f = new NodeIOListFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NodeIOListAction.class.getName());
}

/* @(#)ListAction.java */

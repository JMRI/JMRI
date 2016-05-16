// NodeConfigurationMgr.java

package jmri.jmrix.cmri.serial.nodeconfigmanager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * NodeTableAction object
 *
 * @author	Chuck Catania    Copyright (C) 2014
 * @version	$Revision: 17977 $
 */

public class NodeConfigManagerAction extends AbstractAction {

    public NodeConfigManagerAction(String s) { super(s);}

    public NodeConfigManagerAction() {
        this("CMRInet Node Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigManagerFrame f = new NodeConfigManagerFrame();
        try {
              f.initComponents();
            }
        catch (Exception ex) {
            log.error("NodeConfigManagerAction Exception-C2: "+ex.toString());
            }
        f.setLocation(20,40);
        f.setVisible(true);
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NodeConfigManagerFrame.class.getName());
}


/* @(#)NodeConfigurationMgr.java */

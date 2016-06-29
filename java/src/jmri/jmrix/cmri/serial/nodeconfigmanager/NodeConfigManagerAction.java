package jmri.jmrix.cmri.serial.nodeconfigmanager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a
 * NodeTableAction object
 *
 * @author	Chuck Catania    Copyright (C) 2014
 */

public class NodeConfigManagerAction extends AbstractAction {

    CMRISystemConnectionMemo _memo = null;

    public NodeConfigManagerAction(String s,CMRISystemConnectionMemo memo){ 
       super(s);
       _memo = memo;
    }

    public NodeConfigManagerAction(CMRISystemConnectionMemo memo) {
        this("CMRInet Node Configuration Manager",memo);
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigManagerFrame f = new NodeConfigManagerFrame(_memo);
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

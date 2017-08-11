// NodeIOListAction.java

package jmri.jmrix.cmri.serial.nodeiolist;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a Node IO ListFrame object
 *
 * @author   Dave Duchamp  Copyright (C) 2006
 * @author   Chuck Catania Copyright (C) 2014
 */
public class NodeIOListAction extends AbstractAction {
    CMRISystemConnectionMemo _memo = null;

    public NodeIOListAction(String s,CMRISystemConnectionMemo memo) { 
        super(s);
    _memo = memo;}

    public NodeIOListAction(CMRISystemConnectionMemo memo) {
        this("C/MRI Node Bit Assignments",memo);
    }

    public void actionPerformed(ActionEvent e) {
        NodeIOListFrame f = new NodeIOListFrame(_memo);
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

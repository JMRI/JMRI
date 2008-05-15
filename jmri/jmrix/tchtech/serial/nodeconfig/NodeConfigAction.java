/*
 * NodeConfigAction.java
 *
 * Created on August 18, 2007, 8:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.nodeconfig;

/**
 *
 * @author tim
 */
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NodeConfigFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.1 $
 */
public class NodeConfigAction extends AbstractAction {

	public NodeConfigAction(String s) { super(s);}

    public NodeConfigAction() {
        this("Provison Node Interface Cards");
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame();
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setLocation(200,75);
        f.setVisible(true);
    }
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NodeConfigAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */

// SerialDriverAction.java

package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Swing action to create and register a SerialDriverFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public class SerialDriverAction extends AbstractAction  {
    
    public SerialDriverAction(String s) { super(s);}
    
    public void actionPerformed(ActionEvent e) {
        // create a SerialDriverFrame
        SerialDriverFrame f = new SerialDriverFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("starting SerialDriverFrame caught exception: "+ex.toString());
        }
        f.setVisible(true);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAction.class.getName());
    
}

/* @(#)SerialDriverAction.java */

// SerialDriverAction.java

package jmri.jmrix.acela.serialdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a SerialDriverFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.1 $
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on Mrc example, modified to establish Acela support. 
 */

public class SerialDriverAction extends AbstractAction  {
    
    public SerialDriverAction(String s) { 
        super(s);
    }
    
    public void actionPerformed(ActionEvent e) {
	// create a SerialDriverFrame
	SerialDriverFrame f = new SerialDriverFrame();
	try {
            f.initComponents();
	} catch (Exception ex) {
            log.error("starting SerialDriverFrame caught exception: "+ex.toString());
	}
	f.setVisible(true);		
    };
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAction.class.getName());
}

/* @(#)SerialDriverAction.java */
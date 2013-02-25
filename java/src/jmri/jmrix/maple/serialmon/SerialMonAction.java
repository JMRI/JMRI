// SerialMonAction.java

package jmri.jmrix.maple.serialmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SerialMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public class SerialMonAction 			extends AbstractAction {

	public SerialMonAction(String s) { super(s);}

    public SerialMonAction() {
        this("Maple monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		SerialMonFrame f = new SerialMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialMonAction starting SerialMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static Logger log = LoggerFactory.getLogger(SerialMonAction.class.getName());

}


/* @(#)SerialMonAction.java */

// SerialMonAction.java

package jmri.jmrix.powerline.swing.serialmon;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Swing action to create and register a
 *       			SerialMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version			$Revision$
 */

@Deprecated
public class SerialMonAction 			extends AbstractAction {

	public SerialMonAction(String s, SerialTrafficController tc) {
		super(s);
		this.tc = tc;
	}
	
	SerialTrafficController tc = null;

    public SerialMonAction(SerialTrafficController tc) {
        this("Powerline Device Monitor", tc);
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		SerialMonFrame f = new SerialMonFrame(tc);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialMonAction starting SerialMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static Logger log = Logger.getLogger(SerialMonAction.class.getName());

}


/* @(#)SerialMonAction.java */

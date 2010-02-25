/**
 * LocoMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			LocoMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet.locomon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoMonAction 			extends AbstractAction {

	public LocoMonAction(String s, LnTrafficController tc) { 
	    super(s);
	    this.tc = tc;
	}

    private LocoMonAction() {
        //this("LocoNet monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
                log.debug("starting LocoMon frame creation");
		LocoMonFrame f = new LocoMonFrame(tc);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("LocoMonAction starting LocoMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

    LnTrafficController tc;
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoMonAction.class.getName());

}


/* @(#)LocoMonAction.java */

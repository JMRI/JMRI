/**
 * RpsMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			RpsMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2006
 * @version
 */

package jmri.jmrix.rps.rpsmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class RpsMonAction 			extends AbstractAction {

	public RpsMonAction(String s) { super(s);}

    public RpsMonAction() {
        this("RPS Monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a LocoMonFrame
                log.debug("starting frame creation");
		RpsMonFrame f = new RpsMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("starting frame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsMonAction.class.getName());

}


/* @(#)RpsMonAction.java */

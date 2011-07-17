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
		RpsMonFrame f = new RpsMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			}
		f.setVisible(true);

	}

}


/* @(#)RpsMonAction.java */

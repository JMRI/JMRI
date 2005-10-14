// BoosterProgAction.java

package jmri.jmrix.nce.boosterprog;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * BoosterProgFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision: 1.2 $
 */
public class BoosterProgAction extends AbstractAction {
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.nce.boosterprog.BoosterProgBundle");

    public BoosterProgAction(String s) {
	super(s);

	// disable ourself if there is no programmer manager
        if (jmri.InstanceManager.programmerManagerInstance()==null) {
            setEnabled(false);
        }
    }

    public BoosterProgAction() {
        this(res.getString("TitleBoosterProg"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a BoosterProgFrame
        BoosterProgFrame f = new BoosterProgFrame();
        f.setVisible(true);
    }
}

/* @(#)BoosterProgAction.java */

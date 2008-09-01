// TrainsTableAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * PowerPanelFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @author 	Daniel Boudreau Copyright (C) 2008
 * @version         $Revision: 1.1 $
 */
public class TrainsTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    public TrainsTableAction(String s) {
	super(s);
    }

    public TrainsTableAction() {
        this(rb.getString("TitleTrainsTable"));
    }
    TrainsTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
    	if (f == null || !f.isVisible()){
    		f = new TrainsTableFrame();
    		f.setVisible(true);
    	}
    	f.setExtendedState(f.NORMAL);
    }
}

/* @(#)TrainsTableAction.java */

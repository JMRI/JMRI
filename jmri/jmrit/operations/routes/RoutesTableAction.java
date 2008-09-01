// RoutesTableAction.java

package jmri.jmrit.operations.routes;

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
public class RoutesTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

    public RoutesTableAction(String s) {
	super(s);
    }

    public RoutesTableAction() {
        this(rb.getString("TitleRoutesTable"));
    }

    RoutesTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
    	if (f == null || !f.isVisible()){
    		f = new RoutesTableFrame();
    		f.setVisible(true);
    	}
    	f.setExtendedState(f.NORMAL);
    }
}

/* @(#)RoutesTableAction.java */

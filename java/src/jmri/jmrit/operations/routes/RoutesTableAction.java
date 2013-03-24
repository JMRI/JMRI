// RoutesTableAction.java

package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * RoutesTableFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @author 	Daniel Boudreau Copyright (C) 2008
 * @version         $Revision$
 */
public class RoutesTableAction extends AbstractAction {

    public RoutesTableAction(String s) {
    	super(s);
    }

    public RoutesTableAction() {
    	this(Bundle.getMessage("MenuRoutes"));
    }

    static RoutesTableFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
    	if (f == null || !f.isVisible()){
    		f = new RoutesTableFrame();
     	}
    	f.setExtendedState(Frame.NORMAL);
    	f.setVisible(true);
    }
}

/* @(#)RoutesTableAction.java */

// RouteCopyAction.java

package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RouteCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */
public class RouteCopyAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

    public RouteCopyAction(String s) {
    	super(s);
    }

    RouteCopyFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
    	if (f == null || !f.isVisible()){
    		f = new RouteCopyFrame();
    	}
    	f.setExtendedState(f.NORMAL);
    	f.setVisible(true);
    }
}

/* @(#)RouteCopyAction.java */

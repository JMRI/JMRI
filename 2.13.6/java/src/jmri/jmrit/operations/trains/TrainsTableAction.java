// TrainsTableAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class TrainsTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    public TrainsTableAction(String s) {
    	super(s);
    }

    static TrainsTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a train table frame
    	if (f == null || !f.isVisible()){
    		f = new TrainsTableFrame();
    	}
    	f.setExtendedState(Frame.NORMAL);
    	f.setVisible(true);
    }
}

/* @(#)TrainsTableAction.java */

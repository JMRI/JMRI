// ModifyLocationsAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LocationsByCarTypeFrame object.
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class ModifyLocationsAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

    public ModifyLocationsAction(String s) {
    	super(s);
    }

    LocationsByCarTypeFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a frame
    	if (f == null || !f.isVisible()){
    		f = new LocationsByCarTypeFrame();
    		f.initComponents("");
     	}
    	f.setExtendedState(Frame.NORMAL);
   		f.setVisible(true);
    }
}

/* @(#)ModifyLocationsAction.java */

// EditCarTypeAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.cars.CarAttributeEditFrame;
import jmri.jmrit.operations.rollingstock.cars.CarEditFrame;

/**
 * Swing action to create and register a LocationCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class EditCarTypeAction extends AbstractAction {
    
    public EditCarTypeAction() {
    	super(Bundle.getMessage("MenuItemEditCarType"));
    }

    CarAttributeEditFrame f = null;
    
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
    	if (f == null || !f.isVisible()){
    		f = new CarAttributeEditFrame();
    	}
    	f.initComponents(CarEditFrame.TYPE, null);
    	f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)EditCarTypeAction.java */

// SetTrainIconPositionAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create a SetPhysicalLocation dialog.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2011
 * @version $Revision$
 */
public class SetPhysicalLocationAction extends AbstractAction {

    public SetPhysicalLocationAction(String s) {
    	super(s);
    }

    SetPhysicalLocationFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
    	if (f == null || !f.isVisible()){
    		f = new SetPhysicalLocationFrame();
    	}
    	f.setExtendedState(Frame.NORMAL);
    }
}

/* @(#)SetPhysicalLocationAction.java */

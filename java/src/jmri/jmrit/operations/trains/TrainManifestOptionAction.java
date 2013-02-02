//TrainManifestOptionAction.java

package jmri.jmrit.operations.trains;

import org.apache.log4j.Logger;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Swing action to load the train manifest options frame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainManifestOptionAction extends AbstractAction {
	
	TrainEditFrame frame = null;

    public TrainManifestOptionAction(String s, TrainEditFrame frame) {
    	super(s);
    	this.frame = frame;
    }

    TrainManifestOptionFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new TrainManifestOptionFrame();
    		f.initComponents(frame);
    	}
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	
    }
    
	static Logger log = org.apache.log4j.Logger
	.getLogger(TrainManifestOptionAction.class.getName());
}

/* @(#)TrainManifestOptionAction.java */

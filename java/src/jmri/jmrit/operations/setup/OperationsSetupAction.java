// OperationsSetupAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a OperationsSetupFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class OperationsSetupAction extends AbstractAction {

    
    public OperationsSetupAction(String s) {
    	super(s);
    }

    public OperationsSetupAction() {
    	this(Bundle.getMessage("MenuSetup"));	// NOI18N
    }

    static OperationsSetupFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new OperationsSetupFrame();
    		f.initComponents();
    	}
        f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)OperationsSetupAction.java */

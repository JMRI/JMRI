// PrintMoreOptionAction.java

package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Swing action to load the print options.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision: 21656 $
 */
public class PrintMoreOptionAction extends AbstractAction {
	
    public PrintMoreOptionAction() {
    	this (Bundle.getMessage("TitlePrintMoreOptions"));
    }
	
	public PrintMoreOptionAction(String s) {
    	super(s);
    }

    PrintMoreOptionFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new PrintMoreOptionFrame();
    		f.initComponents();
    	}
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(PrintMoreOptionAction.class.getName());
}

/* @(#)PrintMoreOptionAction.java */

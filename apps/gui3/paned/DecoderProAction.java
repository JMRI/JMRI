// DecoderProAction.java

 package apps.gui3.paned;

import javax.swing.*;
import java.awt.event.ActionEvent;

import jmri.util.swing.*;

/**
 * Action to produce a new, standalone DecoderPro window.
 *
 * Ignores WindowInterface.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.1 $
 */
 
public class DecoderProAction extends jmri.util.swing.JmriAbstractAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public DecoderProAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public DecoderProAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
       
    public void actionPerformed(ActionEvent e) {
        jmri.util.swing.multipane.MultiPaneWindow mainFrame 
            = new jmri.util.swing.multipane.MultiPaneWindow("DecoderPro", "apps/decoderpro");
        mainFrame.setSize(mainFrame.getMaximumSize());
        mainFrame.setVisible(true);
    }
    
    // never invoked, because we overrode actionPerformed above
    public void dispose() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}

/* @(#)PanelProAction.java */

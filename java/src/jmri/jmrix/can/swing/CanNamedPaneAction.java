// CbusNamedPaneAction.java

package jmri.jmrix.can.swing;

import org.apache.log4j.Logger;
import javax.swing.*;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.swing.*;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author		Bob Jacobsen Copyright (C) 2012
 * @version		$Revision: 17977 $
 */
 
public class CanNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public CanNamedPaneAction(String s, WindowInterface wi, String paneClass, CanSystemConnectionMemo memo) {
    	super(s, wi, paneClass);
    	this.memo = memo;
    }
    
 	public CanNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, CanSystemConnectionMemo memo) {
    	super(s, i, wi, paneClass);
    	this.memo = memo;
    }
    
    CanSystemConnectionMemo memo;
    
    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) return null;
        
        try {
            ((CanPanelInterface)p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: "+paneClass+" due to:"+ex);
            ex.printStackTrace();
        }      
        
        return p;
    }

    static Logger log = Logger.getLogger(CanNamedPaneAction.class.getName());
}

/* @(#)CbusNamedPaneAction.java */

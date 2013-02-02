// TamsNamedPaneAction.java

package jmri.jmrix.tams.swing;

import org.apache.log4j.Logger;
import javax.swing.*;

import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.util.swing.*;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version		$Revision: 17977 $
 */
 
public class TamsNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public TamsNamedPaneAction(String s, WindowInterface wi, String paneClass, TamsSystemConnectionMemo memo) {
    	super(s, wi, paneClass);
    	this.memo = memo;
    }
    
 	public TamsNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, TamsSystemConnectionMemo memo) {
    	super(s, i, wi, paneClass);
    	this.memo = memo;
    }
    
    TamsSystemConnectionMemo memo;
    
    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) return null;
        
        try {
            ((TamsPanelInterface)p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: "+paneClass+" due to:"+ex);
            ex.printStackTrace();
        }      
        
        return p;
    }

    static Logger log = Logger.getLogger(TamsNamedPaneAction.class.getName());
}

/* @(#)TamsNamedPaneAction.java */

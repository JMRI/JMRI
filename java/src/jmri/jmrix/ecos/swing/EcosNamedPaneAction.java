// EcosNamedPaneAction.java

package jmri.jmrix.ecos.swing;

import org.apache.log4j.Logger;
import javax.swing.*;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.swing.*;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision$
 */
 
public class EcosNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public EcosNamedPaneAction(String s, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
    	super(s, wi, paneClass);
    	this.memo = memo;
    }
    
 	public EcosNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
    	super(s, i, wi, paneClass);
    	this.memo = memo;
    }
    
    EcosSystemConnectionMemo memo;
    
    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) return null;
        
        try {
            ((EcosPanelInterface)p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: "+paneClass+" due to:"+ex);
            ex.printStackTrace();
        }      
        
        return p;
    }

    static Logger log = Logger.getLogger(EcosNamedPaneAction.class.getName());
}

/* @(#)EcosNamedPaneAction.java */

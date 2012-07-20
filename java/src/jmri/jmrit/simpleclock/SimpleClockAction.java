// SimpleClockAction.java

 package jmri.jmrit.simpleclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *  SimpleClockFrame object
 *
 * @author			Dave Duchamp    Copyright (C) 2004
 * @version			$Revision$
 */
 
public class SimpleClockAction extends AbstractAction {

 	public SimpleClockAction(String s) {
    	super(s);
     }
 	public SimpleClockAction() {
    	super("Fast Clock Setup");
     }
     
     public void actionPerformed(ActionEvent e) {
         
        SimpleClockFrame f = new SimpleClockFrame();
        try {
            f.initComponents();
        } catch (Exception E) {
            log.error("Exception in Simple Clock: "+e);
        }
        f.setVisible(true);
     }
     
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleClockAction.class.getName());
}

/* @(#)SimpleClockAction.java */

// SimpleClockAction.java

 package jmri.jmrit.simpleclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *  SimpleClockFrame object
 *
 * @author			Dave Duchamp    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
 
 public class SimpleClockAction extends AbstractAction {

 	public SimpleClockAction(String s) {
    	super(s);
     }
     
     public void actionPerformed(ActionEvent e) {
         
         SimpleClockFrame f = new SimpleClockFrame();
         if ( f.initComponents() ) {
            f.show();
        }
     }
     
 }

/* @(#)SimpleClockAction.java */

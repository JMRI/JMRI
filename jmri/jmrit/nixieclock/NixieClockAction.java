// NixieClockAction.java

 package jmri.jmrit.nixieclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.*;

/**
 * Swing action to create and register a
 *  NixieClockFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */

 public class NixieClockAction extends AbstractAction {

 	public NixieClockAction(String s) {
    	super(s);
     }

     public void actionPerformed(ActionEvent e) {

         NixieClockFrame f = new NixieClockFrame();
         f.show();

     }

 }

/* @(#)NixieClockAction.java */

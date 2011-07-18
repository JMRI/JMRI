// LcdClockAction.java

 package jmri.jmrit.lcdclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *  LcdClockFrame object
 *
 * @author			Ken Cameron    Copyright (C) 2007
 * @version			$Revision$
 * 
 * This was a direct steal form the Nixie clock code, ver 1.5.
 * Thank you Bob Jacobsen.
 */

 public class LcdClockAction extends AbstractAction {
	 
	 public LcdClockAction() {
         this("LCD Clock");
     }

 	public LcdClockAction(String s) {
    	super(s);
     }

     public void actionPerformed(ActionEvent e) {

         LcdClockFrame f = new LcdClockFrame();
         f.setVisible(true);

     }

 }

/* @(#)LcdClockAction.java */

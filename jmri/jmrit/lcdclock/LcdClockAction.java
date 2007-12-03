// LcdClockAction.java

 package jmri.jmrit.lcdclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *  LcdClockFrame object
 *
 * @author			Ken Cameron    Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */

 public class LcdClockAction extends AbstractAction {

 	public LcdClockAction(String s) {
    	super(s);
     }

     public void actionPerformed(ActionEvent e) {

         LcdClockFrame f = new LcdClockFrame();
         f.setVisible(true);

     }

 }

/* @(#)LcdClockAction.java */

//SprogIIUpdateAction.java

package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogIIUpdateFrame object
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */

public class SprogIIUpdateAction 	extends SprogUpdateAction {

  public SprogIIUpdateAction(String s) { super(s);}

  public void actionPerformed(ActionEvent e) {
    // create a SprogIIUpdateFrame
    SprogIIUpdateFrame f = new SprogIIUpdateFrame();
    try {
      f.initComponents();
    }
    catch (Exception ex) {
      log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: "+ex.toString());
    }
    f.show();
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogIIUpdateAction.class.getName());

}


/* @(#)SprogIIUpdateAction.java */

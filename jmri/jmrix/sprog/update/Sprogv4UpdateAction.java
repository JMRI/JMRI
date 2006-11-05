//Sprogv4UpdateAction.java

package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SprogIIUpdateFrame object
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */

public class Sprogv4UpdateAction 	extends SprogUpdateAction {

  public Sprogv4UpdateAction(String s) { super(s);}

  public void actionPerformed(ActionEvent e) {
    // create a SprogUpdateFrame
    Sprogv4UpdateFrame f = new Sprogv4UpdateFrame();
    try {
      f.initComponents();
    }
    catch (Exception ex) {
      log.warn("Sprogv4UpdateAction starting Sprogv4UpdateFrame: Exception: "+ex.toString());
    }
    f.setVisible(true);
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Sprogv4UpdateAction.class.getName());

}


/* @(#)Sprogv4UpdateAction.java */

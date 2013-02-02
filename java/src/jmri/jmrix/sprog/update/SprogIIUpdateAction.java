//SprogIIUpdateAction.java

package jmri.jmrix.sprog.update;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Swing action to create and register a
 *       			SprogIIUpdateFrame object
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision$
 */

public class SprogIIUpdateAction 	extends SprogUpdateAction {

  public SprogIIUpdateAction(String s) { super(s);}

  public void actionPerformed(ActionEvent e) {
      Object[] options = {"Cancel", "Update"};
      if (1 == JOptionPane.showOptionDialog(null, 
              "In order to proceed with a SPROG II firmware update" +
              "You must have a valid .hex firmware update file\n" +
              "Are you certain you want to update the SPROG II firmware?",
              "SPROG II Firmware Update", JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
          // create a SprogIIUpdateFrame
          SprogIIUpdateFrame f = new SprogIIUpdateFrame();
          try {
              f.initComponents();
          } catch (Exception ex) {
              log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: "+ex.toString());
          }
          f.setVisible(true);
      }
  }
  
  static Logger log = Logger.getLogger(SprogIIUpdateAction.class.getName());

}


/* @(#)SprogIIUpdateAction.java */

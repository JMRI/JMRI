//SprogUpdateAction.java

package jmri.jmrix.sprog.update;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogIIUpdateFrame object
 *
 * @author			Andrew crosland    Copyright (C) 2004
 * @version			$Revision$
 */

public class SprogUpdateAction 	extends AbstractAction {

  public SprogUpdateAction(String s) { super(s);}

  public void actionPerformed(ActionEvent e) {
  }

  static Logger log = Logger.getLogger(SprogUpdateAction.class.getName());

}


/* @(#)SprogUpdateAction.java */

// AnalogClock.Action.java

package jmri.jmrit.analogclock;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.*;
/**
 * Swing action to create and register a
 *  AnalogClockFrame object
 * Copied from code for NixieClockAction by Bob Jacobsen
 *
 * @author			Dennis Miller    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */



public class AnalogClockAction extends AbstractAction {
  public AnalogClockAction(String s) {
    super(s);
  }
  public void actionPerformed(ActionEvent e) {

        AnalogClockFrame f = new AnalogClockFrame();
        f.show();

    }

}
// SpeedometerAction.java

package jmri.jmrit.speedometer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SpeedometerFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

public class SpeedometerAction 			extends AbstractAction {
    
    public SpeedometerAction(String s) {
        super(s);
    }
    
    public void actionPerformed(ActionEvent e) {
        
        // create a SimpleProgFrame
        SpeedometerFrame f = new SpeedometerFrame();
        f.show();
        
    }
    
}

/* @(#)SpeedometerAction.java */

// SpeedometerAction.java

package jmri.jmrit.speedometer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SpeedometerFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */

public class SpeedometerAction 			extends AbstractAction {

    public SpeedometerAction(String s) {
        super(s);

	// disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null) {
            setEnabled(false);
        }
    }

    public SpeedometerAction() {
        this("Speedometer");
    }

    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SpeedometerFrame f = new SpeedometerFrame();
        f.setVisible(true);

    }

}

/* @(#)SpeedometerAction.java */

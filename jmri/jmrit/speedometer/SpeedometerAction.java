// SpeedometerAction.java

package jmri.jmrit.speedometer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SpeedometerFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */

public class SpeedometerAction 			extends AbstractAction {

    public SpeedometerAction(String s) {
        super(s);

	// disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .sensorManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }

    public SpeedometerAction() {
        this("Speedometer");
    }

    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SpeedometerFrame f = new SpeedometerFrame();
        f.show();

    }

}

/* @(#)SpeedometerAction.java */

// AmpMeterAction.java
package jmri.jmrit.ampmeter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AmpMeterFrame object
 *
 * @author	Ken Cameron Copyright (C) 2007
 * @author	Mark Underwood Copyright (C) 2007
 * @version	$Revision$
 *
 * This was a direct steal form the LCDClock code by Ken Cameron,
 * which was a direct steal from the Nixie clock code, ver 1.5. 
 * Thank you Bob Jacobsen and Ken Cameron.
 */
public class AmpMeterAction extends AbstractAction {

    public AmpMeterAction() {
        this("Track Current Meter");
    }

    public AmpMeterAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        AmpMeterFrame f = new AmpMeterFrame();
        f.setVisible(true);

    }

}

/* @(#)AmpMeterAction.java */

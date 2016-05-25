// CrrSection9A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 9A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection9A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 9A");
        inputs = new NamedBean[]{tu[14], tu[15], bo[1], si[60]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu14 = (tu[14].getKnownState() == Sensor.ACTIVE);
        boolean tu15 = (tu[15].getKnownState() == Sensor.ACTIVE);

        boolean bo1 = (bo[1].getKnownState() == Sensor.ACTIVE);

        boolean si60 = (si[60].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo1 || !tu14 || tu15) {
            value = RED;
        }

        if (value == GREEN && si60) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection9A.java */

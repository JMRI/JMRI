// CrrSection7B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 7B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection7B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 7B");
        inputs = new NamedBean[]{bo[3], si[6]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo3 = (bo[3].getKnownState() == Sensor.ACTIVE);
        boolean si6 = (si[6].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo3) {
            value = RED;
        }

        if (value == GREEN && si6) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection7B.java */

// CrrSection2B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 2B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection2B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 2B");
        inputs = new NamedBean[]{tu[12], bo[4], si[21]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4 = (bo[4].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = (tu[12].getKnownState() == Sensor.ACTIVE);
        boolean si21 = (si[21].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo4 || tu12) {
            value = RED;
        }

        if (value == GREEN && si21) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection2B.java */

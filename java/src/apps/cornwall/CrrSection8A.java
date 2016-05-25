// CrrSection8A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 8A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection8A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 8A");
        inputs = new NamedBean[]{tu[2], tu[6], bo[5], bo[11], bo[12], si[75], si[78]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu2 = (tu[2].getKnownState() == Sensor.ACTIVE);
        boolean tu6 = (tu[6].getKnownState() == Sensor.ACTIVE);

        boolean bo5 = (bo[5].getKnownState() == Sensor.ACTIVE);
        boolean bo11 = (bo[11].getKnownState() == Sensor.ACTIVE);
        boolean bo12 = (bo[12].getKnownState() == Sensor.ACTIVE);

        boolean si75 = (si[75].getCommandedState() == THROWN);
        boolean si78 = (si[78].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu2 || bo5) {
            value = RED;
        } else if (!tu6 && bo11) {
            value = RED;
        } else if (tu6 && bo12) {
            value = RED;
        }

        if (value == GREEN && !tu6 && si75) {
            value = YELLOW;
        } else if (value == GREEN && tu6 && si78) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection8A.java */

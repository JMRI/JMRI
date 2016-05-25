// CrrSection12B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 12B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection12B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 12B");
        inputs = new NamedBean[]{tu[2], tu[6], bo[5], bo[13], bo[14], si[39], si[42]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu2 = (tu[2].getKnownState() == Sensor.ACTIVE);
        boolean tu6 = (tu[6].getKnownState() == Sensor.ACTIVE);

        boolean bo5 = (bo[5].getKnownState() == Sensor.ACTIVE);
        boolean bo13 = (bo[13].getKnownState() == Sensor.ACTIVE);
        boolean bo14 = (bo[14].getKnownState() == Sensor.ACTIVE);

        boolean si39 = (si[39].getCommandedState() == THROWN);
        boolean si42 = (si[42].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu6 || bo5) {
            value = RED;
        } else if (!tu2 && bo13) {
            value = RED;
        } else if (tu2 && bo14) {
            value = RED;
        }

        if (value == GREEN && !tu2 && si39) {
            value = YELLOW;
        } else if (value == GREEN && tu2 && si42) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection12B.java */

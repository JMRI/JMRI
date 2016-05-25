// CrrSection14A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 14A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection14A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 14A");
        inputs = new NamedBean[]{tu[7], tu[8], tu[9], bo[11], bo[12], bo[15], si[69], si[72]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu7 = (tu[7].getKnownState() == Sensor.ACTIVE);
        boolean tu8 = (tu[8].getKnownState() == Sensor.ACTIVE);
        boolean tu9 = (tu[9].getKnownState() == Sensor.ACTIVE);

        boolean bo11 = (bo[11].getKnownState() == Sensor.ACTIVE);
        boolean bo12 = (bo[12].getKnownState() == Sensor.ACTIVE);
        boolean bo15 = (bo[15].getKnownState() == Sensor.ACTIVE);

        boolean si69 = (si[69].getCommandedState() == THROWN);
        boolean si72 = (si[72].getCommandedState() == THROWN);

        int value = GREEN;
        if (tu9 || bo15) {
            value = RED;
        } else if (tu7 && bo12) {
            value = RED;
        } else if (!tu7 && bo11) {
            value = RED;
        } else if (tu8) {
            value = FLASHYELLOW;
        }

        if (value == GREEN && !tu9 && !tu7 && si69) {
            value = YELLOW;
        } else if (value == GREEN && !tu9 && tu7 && si72) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection14A.java */

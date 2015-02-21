// CrrSection1B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 1B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection1B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 1B");
        inputs = new NamedBean[]{tu[1], tu[3], tu[12], bo[4], bo[16], si[21], si[57]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4 = (bo[4].getKnownState() == Sensor.ACTIVE);
        boolean bo16 = (bo[16].getKnownState() == Sensor.ACTIVE);
        boolean tu1 = (tu[1].getKnownState() == Sensor.ACTIVE);
        boolean tu3 = (tu[3].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = (tu[12].getKnownState() == Sensor.ACTIVE);
        boolean si21 = (si[21].getCommandedState() == THROWN);
        boolean si57 = (si[57].getCommandedState() == THROWN);

        int value = GREEN;
        if ((tu1)
                || (!tu1 && !tu3 && (bo4 || !tu12))
                || (!tu1 && tu3 && bo16)) {
            value = RED;
        }

        if ((value == GREEN && !tu3 && si21)
                || (value == GREEN && tu3 && si57)) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection1B.java */

// CrrSection19B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 19B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas, but the logic has been changed due to renaming of tu20,
 * tu21, tu22
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection19B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 19B");
        inputs = new NamedBean[]{tu[19], tu[20], tu[25], bo[26], bo[30], si[96]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu19 = (tu[19].getKnownState() == Sensor.ACTIVE);
        boolean tu20 = (tu[20].getKnownState() == Sensor.ACTIVE);
        boolean tu25 = (tu[25].getKnownState() == Sensor.ACTIVE);

        boolean bo26 = (bo[26].getKnownState() == Sensor.ACTIVE);
        boolean bo30 = (bo[30].getKnownState() == Sensor.ACTIVE);

        boolean si96 = (si[96].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu25 || bo30 || tu20 || bo26 || !tu19) {
            value = RED;
        }

        if (value == GREEN && si96) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection19B.java */

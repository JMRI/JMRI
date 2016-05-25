// CrrSection15A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 15A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection15A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 15A");
        inputs = new NamedBean[]{tu[17], tu[18], tu[19], bo[24], bo[25], bo[26], bo[34],
            si[99], si[102], si[105]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu17 = (tu[17].getKnownState() == Sensor.ACTIVE);
        boolean tu18 = (tu[18].getKnownState() == Sensor.ACTIVE);
        boolean tu19 = (tu[19].getKnownState() == Sensor.ACTIVE);

        boolean bo24 = (bo[24].getKnownState() == Sensor.ACTIVE);
        boolean bo25 = (bo[25].getKnownState() == Sensor.ACTIVE);
        boolean bo26 = (bo[26].getKnownState() == Sensor.ACTIVE);
        boolean bo34 = (bo[34].getKnownState() == Sensor.ACTIVE);

        boolean si99 = (si[99].getCommandedState() == THROWN);
        boolean si102 = (si[102].getCommandedState() == THROWN);
        boolean si105 = (si[105].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu17 && tu18) {
            value = RED;
        } else if (!tu17 && bo34) {
            value = RED;
        } else if (!tu17 && !tu18 && bo24) {
            value = RED;
        } else if (tu17 && !tu19 && tu18 && bo24) {
            value = RED;
        } else if (tu17 && !tu19 && !tu18 && bo25) {
            value = RED;
        } else if (tu17 && tu19 && bo26) {
            value = RED;
        }

        if (value == GREEN && !tu17 && si99) {
            value = YELLOW;
        } else if (value == GREEN && tu17 && tu18 && !tu19 && si99) {
            value = YELLOW;
        } else if (value == GREEN && tu17 && !tu18 && !tu19 && si102) {
            value = YELLOW;
        } else if (value == GREEN && tu19 && si105) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection15A.java */

// CrrSection13A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 13A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection13A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 13A");
        inputs = new NamedBean[]{tu[7], tu[8], tu[9], bo[15], bo[22], bo[23], si[87]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu7 = (tu[7].getKnownState() == Sensor.ACTIVE);
        boolean tu8 = (tu[8].getKnownState() == Sensor.ACTIVE);
        boolean tu9 = (tu[9].getKnownState() == Sensor.ACTIVE);

        boolean bo15 = (bo[15].getKnownState() == Sensor.ACTIVE);
        boolean bo22 = (bo[22].getKnownState() == Sensor.ACTIVE);
        boolean bo23 = (bo[23].getKnownState() == Sensor.ACTIVE);

        boolean si87 = (si[87].getCommandedState() == THROWN);
        boolean si90 = (si[90].getCommandedState() == THROWN);

        int value = GREEN;
        if (tu7 || bo15 || tu8) {
            value = RED;
        } else if (!tu9 && bo22) {
            value = RED;
        } else if (tu9 && bo23) {
            value = RED;
        }

        if (value == GREEN && !tu9 && si87) {
            value = YELLOW;
        } else if (value == GREEN && tu9 && si90) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection13A.java */

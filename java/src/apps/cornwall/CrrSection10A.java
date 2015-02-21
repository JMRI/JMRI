// CrrSection10A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 10A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection10A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 10A");
        inputs = new NamedBean[]{tu[14], bo[1], bo[20], bo[21], si[60]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu14 = (tu[14].getKnownState() == Sensor.ACTIVE);
        boolean bo1 = (bo[1].getKnownState() == Sensor.ACTIVE);
        boolean bo20 = (bo[20].getKnownState() == Sensor.ACTIVE);
        boolean bo21 = (bo[21].getKnownState() == Sensor.ACTIVE);
        boolean si60 = (si[60].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo1 || tu14) {
            value = RED;
        } else if (bo20 && bo21) {
            value = RED;
        }

        if (value == GREEN && si60) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection10A.java */

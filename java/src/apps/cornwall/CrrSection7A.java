// CrrSection7A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 7A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection7A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 7A");
        inputs = new NamedBean[]{bo[2], si[3]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo2 = (bo[2].getKnownState() == Sensor.ACTIVE);
        boolean si3 = (si[3].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo2) {
            value = RED;
        }

        if (value == GREEN && si3) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection7A.java */

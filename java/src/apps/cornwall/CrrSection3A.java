// CrrSection3A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 3A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection3A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 3A");
        inputs = new NamedBean[]{tu[13], bo[19], si[54]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo19 = (bo[19].getKnownState() == Sensor.ACTIVE);

        boolean tu13 = (tu[13].getKnownState() == Sensor.ACTIVE);

        boolean si54 = (si[54].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo19 || tu13) {
            value = RED;
        }

        if (value == GREEN && si54) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection3A.java */

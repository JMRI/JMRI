// CrrSection16B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 16B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection16B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 16B");
        inputs = new NamedBean[]{tu[17], bo[22], bo[23], si[81], si[84]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu17 = (tu[17].getKnownState() == Sensor.ACTIVE);
        boolean bo22 = (bo[22].getKnownState() == Sensor.ACTIVE);
        boolean bo23 = (bo[23].getKnownState() == Sensor.ACTIVE);
        boolean si81 = (si[81].getCommandedState() == THROWN);
        boolean si84 = (si[84].getCommandedState() == THROWN);

        int value = GREEN;
        if (tu17 && bo22) {
            value = RED;
        } else if (!tu17 && bo23) {
            value = RED;
        }

        if (value == GREEN && tu17 && si81) {
            value = YELLOW;
        } else if (value == GREEN && !tu17 && si84) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection16B.java */

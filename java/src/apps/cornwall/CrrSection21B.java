// CrrSection21B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 21B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection21B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 21B");
        inputs = new NamedBean[]{tu[11], tu[26], bo[7], bo[8], bo[10],
            gate, si[27], si[30]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu11 = (tu[11].getKnownState() == Sensor.ACTIVE);
        boolean tu26 = (tu[26].getKnownState() == Sensor.ACTIVE);

        boolean bo7 = (bo[7].getKnownState() == Sensor.ACTIVE);
        boolean bo8 = (bo[8].getKnownState() == Sensor.ACTIVE);
        boolean bo10 = (bo[10].getKnownState() == Sensor.ACTIVE);

        boolean si27 = (si[27].getCommandedState() == THROWN);
        boolean si30 = (si[30].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu26 || bo10) {
            value = RED;
        } else if (!tu11 && bo7) {
            value = RED;
        } else if (tu11 && bo8) {
            value = RED;
        } else if (gate.getKnownState() != ACTIVE) {
            value = RED;
        }

        if (value == GREEN && !tu11 && si30) {
            value = YELLOW;
        }
        if (value == GREEN && tu11 && si27) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection21B.java */

// CrrSection3B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 3B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection3B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 3B");
        inputs = new NamedBean[]{bo[2], bo[3], bo[13], bo[14], bo[18], tu[1], tu[3], tu[12],
            si[45], si[48], si[51]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo2 = (bo[2].getKnownState() == Sensor.ACTIVE);
        boolean bo3 = (bo[3].getKnownState() == Sensor.ACTIVE);
        //boolean bo13 = ( bo[13].getKnownState() == Sensor.ACTIVE);
        boolean bo14 = (bo[14].getKnownState() == Sensor.ACTIVE);
        boolean bo18 = (bo[18].getKnownState() == Sensor.ACTIVE);

        boolean tu1 = (tu[1].getKnownState() == Sensor.ACTIVE);
        boolean tu3 = (tu[3].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = (tu[12].getKnownState() == Sensor.ACTIVE);

        boolean si45 = (si[45].getCommandedState() == THROWN);
        boolean si48 = (si[48].getCommandedState() == THROWN);
        boolean si51 = (si[51].getCommandedState() == THROWN);

        int value = GREEN;
        if ((tu12 && tu3)
                || (!tu1 && tu12 && (bo3 || bo14))
                || (!tu12 && bo18)
                || (tu1 && tu12 && (bo2 || bo14))) {
            value = RED;
        }

        if (value == GREEN && si51 && !tu12) {
            value = YELLOW;
        } else if (value == GREEN && si45 && tu12 && !tu1) {
            value = YELLOW;
        } else if (value == GREEN && si48 && tu12 && tu1) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection3B.java */

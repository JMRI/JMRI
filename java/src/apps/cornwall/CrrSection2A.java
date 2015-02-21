// CrrSection2A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 2A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection2A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 2A");
        inputs = new NamedBean[]{tu[1], tu[3], bo[2], bo[3], bo[13], bo[14], si[45], si[48]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu1 = (tu[1].getKnownState() == Sensor.ACTIVE);
        boolean tu3 = (tu[3].getKnownState() == Sensor.ACTIVE);
        boolean bo2 = (bo[2].getKnownState() == Sensor.ACTIVE);
        boolean bo3 = (bo[3].getKnownState() == Sensor.ACTIVE);
        boolean bo13 = (bo[13].getKnownState() == Sensor.ACTIVE);
        boolean bo14 = (bo[14].getKnownState() == Sensor.ACTIVE);
        boolean si45 = (si[45].getCommandedState() == THROWN);
        boolean si48 = (si[48].getCommandedState() == THROWN);

        int value = GREEN;
        if ((!tu3 && (bo2 || tu1 || bo13))
                || (tu3 && !tu1 && (bo3 || bo14))
                || (tu3 && tu1 && (bo2 || bo13))) {
            value = RED;
        }

        if ((value == GREEN && !tu3 && si48)
                || (value == GREEN && tu3 && !tu1 && si45)
                || (value == GREEN && tu3 && tu1 && si48)) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection2A.java */

// CrrSection10B.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 10B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection10B extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 10B");
        inputs = new NamedBean[]{tu[16], bo[20], bo[21]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo20 = (bo[20].getKnownState() == Sensor.ACTIVE);
        boolean bo21 = (bo[21].getKnownState() == Sensor.ACTIVE);
        boolean tu16 = (tu[16].getKnownState() == Sensor.ACTIVE);
        //boolean si21 = ( si[21].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo20 && !tu16) {
            value = RED;
        } else if (bo21 && tu16) {
            value = RED;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection10B.java */

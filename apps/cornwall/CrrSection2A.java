// CrrSection2A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 2A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection2A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 2A");
        sensors = new Sensor[]{ tu[1], bo[2], bo[13] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu1  = ( tu[ 1].getKnownState() == Sensor.ACTIVE);
        boolean bo2  = ( bo[ 2].getKnownState() == Sensor.ACTIVE);
        boolean bo13 = ( bo[13].getKnownState() == Sensor.ACTIVE);

        int value = RED;
        if ( bo2 || tu1 || bo13 ) {
            value = RED;
        } else {
            value = GREEN;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection2A.java */

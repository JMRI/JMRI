// CrrSection2B.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 2B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection2B extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 2B");
        sensors = new Sensor[]{ tu[12], bo[4] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4  = ( bo[ 4].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = ( tu[12].getKnownState() == Sensor.ACTIVE);

        int value = RED;
        if ( bo4 || tu12 ) {
            value = RED;
        } else {
            value = GREEN;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection2B.java */

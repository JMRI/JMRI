// CrrSection3B.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 3B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection3B extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 3B");
        sensors = new Sensor[]{ bo[2], bo[3], bo[13], bo[14], bo[18], tu[1], tu[12] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo2  = ( bo[ 2].getKnownState() == Sensor.ACTIVE);
        boolean bo3  = ( bo[ 3].getKnownState() == Sensor.ACTIVE);
        boolean bo13 = ( bo[13].getKnownState() == Sensor.ACTIVE);
        boolean bo14 = ( bo[14].getKnownState() == Sensor.ACTIVE);
        boolean bo18 = ( bo[18].getKnownState() == Sensor.ACTIVE);
        boolean tu1  = ( tu[ 1].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = ( tu[12].getKnownState() == Sensor.ACTIVE);

        int value = RED;
        if (    ( !tu1 && tu12 && ( bo3 || bo14) )
             || ( !tu12 && bo18 )
             || ( tu1 && tu12 && (bo2 || bo14) )
            ) {
            value = RED;
        } else {
            value = GREEN;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection3B.java */

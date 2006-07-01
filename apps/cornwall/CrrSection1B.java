// CrrSection1B.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 1B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.3 $
 */
public class CrrSection1B extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 1B");
        inputs = new NamedBean[]{ tu[1], tu[12], bo[4], si[21] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4  = ( bo[ 4].getKnownState() == Sensor.ACTIVE);
        boolean tu1  = ( tu[ 1].getKnownState() == Sensor.ACTIVE);
        boolean tu12 = ( tu[12].getKnownState() == Sensor.ACTIVE);
        boolean si21 = ( si[21].getCommandedState() == THROWN);

        int value = GREEN;
        if (
                ( tu1)
             || ( !tu1 && bo4 )
             || ( !tu1 && !tu12 )
            )
            value = RED;

        if (value==GREEN && si21)
            value = YELLOW;

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection1B.java */

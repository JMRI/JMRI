// CrrSection19A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 19A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection19A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 19A");
        inputs = new NamedBean[]{ tu[18], tu[22], tu[25], bo[26], bo[30], si[96] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu18 = ( tu[18].getKnownState() == Sensor.ACTIVE);
        boolean tu22 = ( tu[22].getKnownState() == Sensor.ACTIVE);
        boolean tu25 = ( tu[25].getKnownState() == Sensor.ACTIVE);

        boolean bo26 = ( bo[26].getKnownState() == Sensor.ACTIVE);
        boolean bo30 = ( bo[30].getKnownState() == Sensor.ACTIVE);

        boolean si96 = ( si[96].getCommandedState() == THROWN);

        int value = GREEN;
        if ( tu25 || bo30 || bo26 || tu22 || tu18 )
            value = RED;

        if (value == GREEN && si96)
            value = YELLOW;

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection19A.java */

// CrrSection18A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 18A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection18A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 18A");
        inputs = new NamedBean[]{ tu[18], tu[23], bo[24], bo[27], si[93], si[96] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu18  = ( tu[18].getKnownState() == Sensor.ACTIVE);
        boolean tu23  = ( tu[23].getKnownState() == Sensor.ACTIVE);

        boolean bo24 = ( bo[24].getKnownState() == Sensor.ACTIVE);
        boolean bo27 = ( bo[27].getKnownState() == Sensor.ACTIVE);

        boolean si93 = ( si[93].getCommandedState() == THROWN);
        boolean si96 = ( si[96].getCommandedState() == THROWN);

        int value = GREEN;
        if ( tu23 || bo27 || bo24 )
            value = RED;

        if (value == GREEN && !tu18 && si93)
            value = YELLOW;
        if (value == GREEN && tu18 && si96)
            value = YELLOW;

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection18A.java */

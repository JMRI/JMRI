// CrrSection17B.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 17B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection17B extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 17B");
        inputs = new NamedBean[]{ tu[20], tu[21], tu[23], bo[27], bo[28], bo[29], si[120], si[123] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu20 = ( tu[20].getKnownState() == Sensor.ACTIVE);
        boolean tu21 = ( tu[21].getKnownState() == Sensor.ACTIVE);
        boolean tu23 = ( tu[23].getKnownState() == Sensor.ACTIVE);

        boolean bo27 = ( bo[27].getKnownState() == Sensor.ACTIVE);
        boolean bo28 = ( bo[28].getKnownState() == Sensor.ACTIVE);
        boolean bo29 = ( bo[29].getKnownState() == Sensor.ACTIVE);

        boolean si120 = ( si[120].getCommandedState() == THROWN);
        boolean si123 = ( si[123].getCommandedState() == THROWN);

        int value = GREEN;
        if ( !tu21 || bo27 || !tu20 )
            value = RED;
        else if ( tu23 && bo29 )
            value = RED;
        else if ( !tu23 && bo28 )
            value = RED;

        if (value == GREEN && !tu23 && si120 )
            value = YELLOW;
        if (value == GREEN && tu23 && si123 )
            value = YELLOW;

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection17B.java */

// CrrSection1A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 1A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection1A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 1A");
        sensors = new Sensor[]{ tu[1], tu[12], bo[4], bo[16] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4  = ( bo[ 4].getKnownState() == ACTIVE);
        boolean bo16 = ( bo[16].getKnownState() == ACTIVE);
        boolean tu1  = ( tu[ 1].getKnownState() == ACTIVE);
        boolean tu12 = ( tu[12].getKnownState() == ACTIVE);

        // section 1a
        int value = RED;
        if (
                ( bo4 && tu1 && tu12)
             || ( bo16 && !tu1 )
             || ( tu1 && !tu12 )
            ) {
            value = RED;
        } else {
            value = GREEN;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection1A.java */

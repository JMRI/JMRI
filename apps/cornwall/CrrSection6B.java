// CrrSection6B.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 6B of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */
public class CrrSection6B extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 6B");
        inputs = new NamedBean[]{ tu[11],tu[24],tu[26],
                                bo[9], bo[10], bo[28], bo[29], bo[31], bo[32],
                                gate, si[108], si[111] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo9  = ( bo[ 9].getKnownState() == Sensor.ACTIVE);
        boolean bo10 = ( bo[10].getKnownState() == Sensor.ACTIVE);
        boolean bo28 = ( bo[28].getKnownState() == Sensor.ACTIVE);
        boolean bo29 = ( bo[29].getKnownState() == Sensor.ACTIVE);
        boolean bo31 = ( bo[31].getKnownState() == Sensor.ACTIVE);
        boolean bo32 = ( bo[32].getKnownState() == Sensor.ACTIVE);

        boolean tu11 = ( tu[11].getKnownState() == Sensor.ACTIVE);
        boolean tu24 = ( tu[24].getKnownState() == Sensor.ACTIVE);
        boolean tu26 = ( tu[26].getKnownState() == Sensor.ACTIVE);

        boolean si108 = ( si[111].getCommandedState() == THROWN);
        boolean si111 = ( si[108].getCommandedState() == THROWN);

        int value = RED;
        if (
               ( tu11 && bo9 )
            || ( tu11 && !tu24 && bo28  )   // is the bo28/bo29 selection backwards here?
            || ( tu11 && tu24 && bo29  )
            || ( !tu11 && bo10 )
            || ( !tu11 && !tu26 && bo31  )
            || ( !tu11 && tu26 && bo32  )
            || (! (gate.getKnownState() == ACTIVE))
           ){
            value = RED;
        } else {
            value = GREEN;
        }

        if (value == GREEN && !tu24 && si108)
            value = YELLOW;
        else if (value == GREEN && tu24 && si111)
            value = YELLOW;

        sig.setAppearance(value);
    }
}

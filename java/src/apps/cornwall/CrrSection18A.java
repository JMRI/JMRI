// CrrSection18A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 18A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas, but the logic has been changed due to renaming of tu20,
 * tu21, tu22. Includes additional routes.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection18A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 18A");
        inputs = new NamedBean[]{tu[18], tu[19], tu[20], tu[21], tu[22], tu[23],
            bo[24], bo[25], bo[26], bo[27], bo[34],
            si[93], si[96]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu18 = (tu[18].getKnownState() == Sensor.ACTIVE);
        boolean tu19 = (tu[19].getKnownState() == Sensor.ACTIVE);
        boolean tu20 = (tu[20].getKnownState() == Sensor.ACTIVE);
        boolean tu21 = (tu[21].getKnownState() == Sensor.ACTIVE);
        boolean tu22 = (tu[22].getKnownState() == Sensor.ACTIVE);
        boolean tu23 = (tu[23].getKnownState() == Sensor.ACTIVE);

        boolean bo24 = (bo[24].getKnownState() == Sensor.ACTIVE);
        boolean bo25 = (bo[25].getKnownState() == Sensor.ACTIVE);
        boolean bo26 = (bo[26].getKnownState() == Sensor.ACTIVE);
        boolean bo27 = (bo[27].getKnownState() == Sensor.ACTIVE);
        boolean bo34 = (bo[34].getKnownState() == Sensor.ACTIVE);

        boolean si93 = (si[93].getCommandedState() == THROWN);
        boolean si96 = (si[96].getCommandedState() == THROWN);

        int value = GREEN;
        if (tu23) {
            value = RED;
        } else if (bo27) {
            value = RED;
        } else if (!tu22 && bo24) {
            value = RED;
        } else if (!tu22 && !tu18 && bo34) {
            value = RED;
        } else if (!tu22 && tu18 && !tu19) {
            value = RED;
        } else if (tu22 && !tu21 && bo25) {
            value = RED;
        } else if (tu22 && !tu21 && tu18) {
            value = RED;
        } else if (tu22 && !tu21 && tu19) {
            value = RED;
        } else if (tu22 && tu21 && !tu20) {
            value = RED;
        } else if (tu22 && tu21 && tu20 && bo26) {
            value = RED;
        } else if (tu22 && tu21 && tu20 && !tu19) {
            value = RED;
        }

        if (value == GREEN && !tu22 && !tu18 && si93) {
            value = YELLOW;
        }
        if (value == GREEN && !tu22 && tu18 && si96) {
            value = YELLOW;
        }
        if (value == GREEN && tu22 && si96) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection18A.java */

// CrrSection17C.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 17C of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas, but the logic has been changed due to renaming of tu20,
 * tu21, tu22
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection17C extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 17C");
        inputs = new NamedBean[]{tu[20], tu[21], tu[22], tu[23], tu[25],
            bo[27], bo[28], bo[29], bo[30], bo[31], bo[32],
            si[120], si[123], si[126], si[129]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu20 = (tu[20].getKnownState() == Sensor.ACTIVE);
        boolean tu21 = (tu[21].getKnownState() == Sensor.ACTIVE);
        boolean tu22 = (tu[22].getKnownState() == Sensor.ACTIVE);
        boolean tu23 = (tu[23].getKnownState() == Sensor.ACTIVE);
        boolean tu25 = (tu[25].getKnownState() == Sensor.ACTIVE);

        boolean bo27 = (bo[27].getKnownState() == Sensor.ACTIVE);
        boolean bo28 = (bo[28].getKnownState() == Sensor.ACTIVE);
        boolean bo29 = (bo[29].getKnownState() == Sensor.ACTIVE);
        boolean bo30 = (bo[30].getKnownState() == Sensor.ACTIVE);
        boolean bo31 = (bo[31].getKnownState() == Sensor.ACTIVE);
        boolean bo32 = (bo[32].getKnownState() == Sensor.ACTIVE);

        boolean si120 = (si[120].getCommandedState() == THROWN);
        boolean si123 = (si[123].getCommandedState() == THROWN);
        boolean si126 = (si[126].getCommandedState() == THROWN);
        boolean si129 = (si[129].getCommandedState() == THROWN);

        int value = GREEN;
        if (!tu20 && bo30) {
            value = RED;
        } else if (!tu20 && !bo30 && tu25 && bo32) {
            value = RED;
        } else if (!tu20 && !bo30 && !tu25 && bo31) {
            value = RED;
        } else if (tu20 && !tu21) {
            value = RED;
        } else if (tu20 && !tu22) {
            value = RED;
        } else if (tu20 && bo27) {
            value = RED;
        } else if (tu20 && tu23 && bo29) {
            value = RED;
        } else if (tu20 && !tu23 && bo28) {
            value = RED;
        }

        if (value == GREEN && tu20 && tu23 && si123) {
            value = YELLOW;
        }
        if (value == GREEN && tu20 && !tu23 && si120) {
            value = YELLOW;
        }
        if (value == GREEN && !tu20 && tu25 && si129) {
            value = YELLOW;
        }
        if (value == GREEN && !tu20 && !tu25 && si126) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection17C.java */

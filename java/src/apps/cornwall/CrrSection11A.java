// CrrSection11A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 11A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection11A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 11A");
        inputs = new NamedBean[]{tu[13], tu[14], tu[15], tu[16],
            bo[1], bo[16], bo[17], bo[18], bo[19],
            si[9], si[12], si[24]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu13 = (tu[13].getKnownState() == Sensor.ACTIVE);
        boolean tu14 = (tu[14].getKnownState() == Sensor.ACTIVE);
        boolean tu15 = (tu[15].getKnownState() == Sensor.ACTIVE);
        boolean tu16 = (tu[16].getKnownState() == Sensor.ACTIVE);

        boolean bo1 = (bo[1].getKnownState() == Sensor.ACTIVE);
        boolean bo16 = (bo[16].getKnownState() == Sensor.ACTIVE);
        boolean bo17 = (bo[17].getKnownState() == Sensor.ACTIVE);
        boolean bo18 = (bo[18].getKnownState() == Sensor.ACTIVE);
        boolean bo19 = (bo[19].getKnownState() == Sensor.ACTIVE);

        boolean si9 = (si[9].getCommandedState() == THROWN);
        boolean si12 = (si[12].getCommandedState() == THROWN);
        boolean si24 = (si[24].getCommandedState() == THROWN);

        int value = GREEN;
        if (bo1 || tu16) {
            value = RED;
        } else if (!tu14 && bo16) {
            value = RED;
        } else if (tu14 && !tu15 && bo18) {
            value = RED;
        } else if (tu14 && tu15 && bo19) {
            value = RED;
        } else if (tu14 && tu15 && !tu13 && bo17) {
            value = RED;
        }

        if (value == GREEN && !tu14 && si9) {
            value = YELLOW;
        } else if (value == GREEN && tu14 && !tu15 && si12) {
            value = YELLOW;
        } else if (value == GREEN && tu14 && tu15 && tu13 && si24) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection11A.java */

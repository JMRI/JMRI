// CrrSection4A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 4A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection4A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 4A");
        inputs = new NamedBean[]{tu[4], tu[5], tu[10], bo[6], bo[7], bo[8], si[33], si[36]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo6 = (bo[6].getKnownState() == Sensor.ACTIVE);
        boolean bo7 = (bo[7].getKnownState() == Sensor.ACTIVE);
        boolean bo8 = (bo[8].getKnownState() == Sensor.ACTIVE);

        boolean tu4 = (tu[4].getKnownState() == Sensor.ACTIVE);
        boolean tu5 = (tu[5].getKnownState() == Sensor.ACTIVE);
        boolean tu10 = (tu[10].getKnownState() == Sensor.ACTIVE);

        boolean si33 = (si[33].getCommandedState() == THROWN);
        boolean si36 = (si[36].getCommandedState() == THROWN);

        int value = GREEN;
        if ((tu4 || bo6)
                || (bo7 && bo8)
                || (bo8 && !tu5)
                || (tu10 && tu5)
                || (!tu10 && tu5 && bo7)) {
            value = RED;
        }

        if (value == GREEN && tu5 && si33) {
            value = YELLOW;
        } else if (value == GREEN && !tu5 && si36) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection4A.java */

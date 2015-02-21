// CrrSection16A.java
package apps.cornwall;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Automate section 16A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class CrrSection16A extends CrrSection {

    void defineIO() {
        sig = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 16A");
        inputs = new NamedBean[]{tu[17], bo[22], si[81]};
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu17 = (tu[17].getKnownState() == Sensor.ACTIVE);
        boolean bo22 = (bo[22].getKnownState() == Sensor.ACTIVE);
        boolean si81 = (si[81].getCommandedState() == THROWN);

        int value = GREEN;
        if (tu17 || bo22) {
            value = RED;
        }

        if (value == GREEN && si81) {
            value = YELLOW;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection16A.java */

// BlockBossLogic.java

package jmri.jmrit.blockboss;

import jmri.*;
import jmri.jmrit.automat.*;
import java.awt.event.ActionEvent;
import java.util.*;
import com.sun.java.util.collections.*;

/**
 * Represents the "simple signal" logic for one signal; provides
 * some collection services.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class BlockBossLogic extends Siglet {

    public void setOutputSignal(String name) {
        driveSignal = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
    }

    public void setSensor(String name) {
        watchSensor = InstanceManager.sensorManagerInstance().getSensor(name);
    }

    public void setTurnout(String name, int state) {
        watchTurnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
    }

    SignalHead driveSignal = null;
    Sensor watchSensor = null;
    Turnout watchTurnout = null;

    /**
     * Define the siglet's input and output.
     */
    public void defineIO() {
        if (watchTurnout!=null && watchSensor != null) {
            inputs = new NamedBean[]{watchSensor, watchTurnout};
        } else if (watchTurnout==null && watchSensor != null) {
            inputs = new NamedBean[]{watchSensor};
        } else if (watchTurnout!=null && watchSensor == null) {
            inputs = new NamedBean[]{watchTurnout};
        } else {
            log.error("Can't leave sensor and turnout undefined");
        }
        outputs = new NamedBean[]{driveSignal};
    }

    /**
     * Recompute new output state
     * and apply it.
     */
    public void setOutput() {
        int appearance = SignalHead.GREEN;
        if (watchSensor!=null && watchSensor.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchSensor!=null && watchSensor.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;

        ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    /**
     * Force update of the siglet's IO and restart
     */

     public void restart() {
        super.stop();
        super.start();
    }

     static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());

}

/* @(#)BlockBossLogic.java */

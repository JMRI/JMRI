// BlockBossLogic.java

package jmri.jmrit.blockboss;

import jmri.*;
import jmri.jmrit.automat.*;
import java.awt.event.ActionEvent;
import com.sun.java.util.collections.*;
import java.util.Hashtable;

/**
 * Represents the "simple signal" logic for one signal; provides
 * some collection services.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */

public class BlockBossLogic extends Siglet {

    /**
     * Create a default object, without contents
     */
    public BlockBossLogic() {
    }

    public BlockBossLogic(String name) {
        this.name = name;
        driveSignal = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
    }

    public void setSensor(String name) {
        watchSensor = InstanceManager.sensorManagerInstance().getSensor(name);
    }

    public void setTurnout(String name, int state) {
        watchTurnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
    }

    String name;
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
        System.out.println("got it "+watchSensor.getKnownState());
        int appearance = SignalHead.GREEN;
        if (watchSensor!=null && watchSensor.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchSensor!=null && watchSensor.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;

        System.out.println("Appear "+appearance);
        ((SignalHead)outputs[0]).setAppearance(appearance);
    }


    static Hashtable map = null;

    private static void setup() {
        if (map == null) {
            map = new Hashtable();
            InstanceManager.configureManagerInstance().registerConfig(new BlockBossLogic());
        }
    }

    public void retain() {
        map.put(name, this);
    }

    /**
     * Return the BlockBossLogic item governing a specific signal.
     * @param signal
     * @return never null
     */
    static BlockBossLogic getStoppedObject(String signal) {
        BlockBossLogic b;
        setup(); // ensure we've been registered
        if (map.contains(signal)) {
            b = (BlockBossLogic)map.get(signal);
            b.stop();
            map.remove(b);
        } else {
            b = new BlockBossLogic(signal);
        }
        return b;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());

}

/* @(#)BlockBossLogic.java */

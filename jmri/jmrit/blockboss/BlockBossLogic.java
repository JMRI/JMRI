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
 * @version     $Revision: 1.3 $
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
        watchTurnoutState = state;
    }

    public void setSignal(String name, boolean useFlash) {
        protectSignal = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        protectWithFlashing = useFlash;
    }

    String name;
    SignalHead driveSignal = null;
    Sensor watchSensor = null;
    Turnout watchTurnout = null;
    int watchTurnoutState = -1;
    SignalHead protectSignal = null;
    boolean protectWithFlashing = false;

    /**
     * Define the siglet's input and output.
     */
    public void defineIO() {
        NamedBean[] tempArray = new NamedBean[10];
        int n = 0;

        if (watchTurnout!=null ) {
            tempArray[n]= watchTurnout;
            n++;
        }
        if (watchSensor != null) {
            tempArray[n]= watchSensor;
            n++;
        }

        if (protectSignal != null) {
            tempArray[n]= protectSignal;
            n++;
        }

        // copy temp to definitive inputs
        inputs = new NamedBean[n];
        for (int i = 0; i< inputs.length; i++)
            inputs[i] = tempArray[i];

        outputs = new NamedBean[]{driveSignal};
    }

    /**
     * Recompute new output state
     * and apply it.
     */
    public void setOutput() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();

        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && protectSignal.getAppearance()==SignalHead.FLASHYELLOW)
            appearance = SignalHead.YELLOW;
        if (protectSignal.getAppearance()==SignalHead.RED)
            if (protectWithFlashing)
                appearance = SignalHead.FLASHYELLOW;
            else
                appearance = SignalHead.YELLOW;

        // check for red overriding yellow or green
        if (watchSensor!=null && watchSensor.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchTurnout!=null && watchTurnout.getKnownState() == watchTurnoutState)
            appearance = SignalHead.RED;

        // show result if changed
        System.out.println("Appear "+appearance);
        if (appearance != oldAppearance)
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

// BlockBossLogic.java

package jmri.jmrit.blockboss;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.automat.Siglet;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Represents the "simple signal" logic for one signal; provides
 * some collection services.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.6 $
 */

public class BlockBossLogic extends Siglet {

    /**
     * Create a default object, without contents
     */
    public BlockBossLogic() {
    }

    public BlockBossLogic(String name) {
        this.name = name;
        driveSignal = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (driveSignal == null) log.warn("Signal "+name+" was not found!");
    }

    /**
     * The "driven signal" is controlled by this
     * element.
     * @return system name of the driven signal
     */
    public String getDrivenSignal() {
        return driveSignal.getSystemName();
    }

    public void setSensor(String name) {
        if (name == null || name.equals("")) {
            watchSensor = null;
            return;
        }
        watchSensor = InstanceManager.sensorManagerInstance().getSensor(name);
        if (watchSensor == null) log.warn("Sensor "+name+" was not found!");
    }

    /**
     * Return the system name of the sensor being monitored
     * @return system name; null if no sensor configured
     */
    public String getSensor() {
        if (watchSensor == null) return null;
        return watchSensor.getSystemName();
    }

    public void setTurnout(String name, int state) {
        if (name == null || name.equals("")) {
            watchTurnout = null;
            return;
        }
        watchTurnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
        if (watchTurnout == null) log.warn("Turnout "+name+" was not found!");
        watchTurnoutState = state;
    }

    /**
     * Return the system name of the turnout being monitored
     * @return system name; null if no turnout configured
     */
    public String getTurnout() {
        if (watchTurnout == null) return null;
        return watchTurnout.getSystemName();
    }
    public int getTurnoutState() {
        return watchTurnoutState;
    }
    public void setWatchedSignal(String name, boolean useFlash) {
        if (name == null || name.equals("")) {
            protectSignal = null;
            return;
        }
        protectSignal = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (protectSignal == null) log.warn("Signal "+name+" was not found!");
        protectWithFlashing = useFlash;
    }
    /**
     * Return the system name of the turnout being monitored
     * @return system name; null if no turnout configured
     */
    public String getWatchedSignal() {
        if (protectSignal == null) return null;
        return protectSignal.getSystemName();
    }

    public boolean getUseFlash() {
        return protectWithFlashing;
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
        if (protectSignal!=null && protectWithFlashing && protectSignal.getAppearance()==SignalHead.FLASHYELLOW)
            appearance = SignalHead.YELLOW;
        if (protectSignal!=null && protectSignal.getAppearance()==SignalHead.RED)
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
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }


    static Hashtable map = null;

    public static Enumeration entries() {
        return map.elements();
    }

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
     * Return the BlockBossLogic item governing a specific signal,
     * having removed it from use.
     * @param signal
     * @return never null
     */
    public static BlockBossLogic getStoppedObject(String signal) {
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
    /**
     * Return the BlockBossLogic item governing a specific signal.
     * <P>
     * Unlike {@link BlockBossLogic#getStoppedObject(String signal)}
     * this does not remove the object from being used.
     * @param signal system name
     * @return never null
     */
    public static BlockBossLogic getExisting(String signal) {
        BlockBossLogic b;
        setup(); // ensure we've been registered
        if (map.containsKey(signal)) {
            b = (BlockBossLogic)map.get(signal);
        } else {
            b = new BlockBossLogic(signal);
        }
        return b;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());

}

/* @(#)BlockBossLogic.java */

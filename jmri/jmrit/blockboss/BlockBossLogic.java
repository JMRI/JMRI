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
 * <P>
 * There are four situations that this logic can handle:
 * <OL>
 * <LI>SIMPLEBLOCK - a simple block, without a turnout.
 * <LI>TRAILINGMAIN - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is closed.
 * <LI>TRAILINGDIVERGING - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is thrown.
 * <LI>FACING - This signal protects a facing point turnout, which
 * may therefore have two next signals for the closed and thrown
 * states of the turnout.
 * </OL><P>
 * Note that these four possibilities logically require that certain
 * information be configured consistently; e.g. not specifying a turnout
 * in TRAILINGMAIN doesn't make any sense.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.8 $
 */

public class BlockBossLogic extends Siglet {

    static public final int SINGLEBLOCK = 1;
    static public final int TRAILINGMAIN = 2;
    static public final int TRAILINGDIVERGING = 3;
    static public final int FACING = 4;

    int mode = 0;

    /**
     * Create a default object, without contents.
     */
    public BlockBossLogic() {
    }

    /**
     * Create an object to drive a specific signal.
     * @param name System or user name of the driven signal.
     */
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

    public void setSensor1(String name) {
        if (name == null || name.equals("")) {
            watchSensor1 = null;
            return;
        }
        watchSensor1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor1 == null) log.warn("Sensor1 "+name+" was not found!");
    }

    public void setSensor2(String name) {
        if (name == null || name.equals("")) {
            watchSensor2 = null;
            return;
        }
        watchSensor2 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor2 == null) log.warn("Sensor2 "+name+" was not found!");
    }

    public void setSensor3(String name) {
        if (name == null || name.equals("")) {
            watchSensor3 = null;
            return;
        }
        watchSensor3 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor3 == null) log.warn("Sensor3 "+name+" was not found!");
    }

    public void setSensor4(String name) {
        if (name == null || name.equals("")) {
            watchSensor4 = null;
            return;
        }
        watchSensor4 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor4 == null) log.warn("Sensor4 "+name+" was not found!");
    }

    /**
     * Return the system name of the sensor being monitored
     * @return system name; null if no sensor configured
     */
    public String getSensor1() {
        if (watchSensor1 == null) return null;
        return watchSensor1.getSystemName();
    }
    public String getSensor2() {
        if (watchSensor2 == null) return null;
        return watchSensor2.getSystemName();
    }
    public String getSensor3() {
        if (watchSensor3 == null) return null;
        return watchSensor3.getSystemName();
    }
    public String getSensor4() {
        if (watchSensor4 == null) return null;
        return watchSensor4.getSystemName();
    }

    public void setTurnout(String name) {
        if (name == null || name.equals("")) {
            watchTurnout = null;
            return;
        }
        watchTurnout = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        if (watchTurnout == null) log.warn("Turnout "+name+" was not found!");
    }

    /**
     * Return the system name of the turnout being monitored
     * @return system name; null if no turnout configured
     */
    public String getTurnout() {
        if (watchTurnout == null) return null;
        return watchTurnout.getSystemName();
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public int getMode() {
        return mode;
    }
    public void setWatchedSignal1(String name, boolean useFlash) {
        if (name == null || name.equals("")) {
            watchedSignal1 = null;
            return;
        }
        watchedSignal1 = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal1 == null) log.warn("Signal "+name+" was not found!");
        protectWithFlashing = useFlash;
    }
    /**
     * Return the system name of the primary signal being monitored
     * @return system name; null if no primary signal configured
     */
    public String getWatchedSignal1() {
        if (watchedSignal1 == null) return null;
        return watchedSignal1.getSystemName();
    }

    public boolean getUseFlash() {
        return protectWithFlashing;
    }

    public void setWatchedSignal2(String name) {
        if (name == null || name.equals("")) {
            watchedSignal2 = null;
            return;
        }
        watchedSignal2 = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal2 == null) log.warn("Signal "+name+" was not found!");
    }
    /**
     * Return the system name of the secondary signal being monitored
     * @return system name; null if no secondary signal configured
     */
    public String getWatchedSignal2() {
        if (watchedSignal2 == null) return null;
        return watchedSignal2.getSystemName();
    }

    String name;
    SignalHead driveSignal = null;
    Sensor watchSensor1 = null;
    Sensor watchSensor2 = null;
    Sensor watchSensor3 = null;
    Sensor watchSensor4 = null;
    Turnout watchTurnout = null;
    SignalHead watchedSignal1 = null;
    SignalHead watchedSignal2 = null;
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
        if (watchSensor1 != null) {
            tempArray[n]= watchSensor1;
            n++;
        }
        if (watchSensor2 != null) {
            tempArray[n]= watchSensor2;
            n++;
        }
        if (watchSensor3 != null) {
            tempArray[n]= watchSensor3;
            n++;
        }
        if (watchSensor4 != null) {
            tempArray[n]= watchSensor4;
            n++;
        }

        if (watchedSignal1 != null) {
            tempArray[n]= watchedSignal1;
            n++;
        }
        if (watchedSignal2 != null) {
            tempArray[n]= watchedSignal2;
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

        switch (mode) {
        case SINGLEBLOCK:
            doSingleBlock();
            break;
        case TRAILINGMAIN:
            doTrailingMain();
            break;
        case TRAILINGDIVERGING:
            doTrailingDiverging();
            break;
        case FACING:
            doFacing();
            break;
        default:
            log.error("Unexpected mode: "+mode);
        }
    }

    void doSingleBlock() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (watchedSignal1!=null && protectWithFlashing && watchedSignal1.getAppearance()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (watchedSignal1!=null && watchedSignal1.getAppearance()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;

        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    void doTrailingMain() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (watchedSignal1!=null && protectWithFlashing && watchedSignal1.getAppearance()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (watchedSignal1!=null && watchedSignal1.getAppearance()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.CLOSED)
            appearance = SignalHead.RED;

        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    void doTrailingDiverging() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (watchedSignal1!=null && protectWithFlashing && watchedSignal1.getAppearance()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (watchedSignal1!=null && watchedSignal1.getAppearance()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.THROWN)
            appearance = SignalHead.RED;

        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    void doFacing() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();

        // find signal to watch
        SignalHead s = watchedSignal1;
        if (watchTurnout!=null && watchTurnout.getKnownState() == Turnout.THROWN )
            s = watchedSignal2;

        // check for yellow, flashing yellow overriding green
        if (s!=null && protectWithFlashing && s.getAppearance()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (s!=null && s.getAppearance()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) appearance = SignalHead.RED;

        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    static Hashtable umap = null;
    static Hashtable smap = null;

    public static Enumeration entries() {
        return smap.elements();
    }

    private static void setup() {
        if (smap == null) {
            smap = new Hashtable();
            umap = new Hashtable();
            InstanceManager.configureManagerInstance().registerConfig(new BlockBossLogic());
        }
    }

    public void retain() {
        smap.put(driveSignal.getSystemName(), this);
        if (driveSignal.getUserName()!=null)
            umap.put(driveSignal.getUserName(), this);
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
        if (smap.contains(signal)) {
            b = (BlockBossLogic)smap.get(signal);
            b.stop();
            smap.remove(b);
            if (b.driveSignal.getUserName()!=null)
                umap.remove(b.driveSignal.getUserName());
            return b;
        } else if (umap.contains(signal)) {
            b = (BlockBossLogic)umap.get(signal);
            b.stop();
            umap.remove(b);
            smap.remove(b.driveSignal.getSystemName());
            return b;

        } else {
            b = new BlockBossLogic(signal);
            return b;
        }

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
        if (smap.containsKey(signal)) {
            b = (BlockBossLogic)smap.get(signal);
        } else if (umap.containsKey(signal)) {
            b = (BlockBossLogic)umap.get(signal);
        } else {
            b = new BlockBossLogic(signal);
        }
        return b;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());
}

/* @(#)BlockBossLogic.java */

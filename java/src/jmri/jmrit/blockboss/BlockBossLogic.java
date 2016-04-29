package jmri.jmrit.blockboss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.automat.Siglet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drives the "simple signal" logic for one signal.
 * <P>
 * Signals "protect" by telling the engineer about the conditions ahead. The
 * engineer controls the speed of the train based on what the signals show, and
 * the signals in turn react to whether the track ahead is occupied, what
 * signals further down the line show, etc.
 * <P>
 * There are four situations that this logic can handle:
 * <OL>
 * <LI>SINGLEBLOCK - A simple block, without a turnout.
 * <P>
 * In this case, there is only a single set of sensors and a single next signal
 * to protect.
 * <LI>TRAILINGMAIN - This signal is protecting a trailing point turnout, which
 * can only be passed when the turnout is closed. It can also be used for the
 * upper head of a two head signal on the facing end of the turnout.
 * <P>
 * In this case, the signal is forced red if the specified turnout is THROWN.
 * When the turnout is CLOSED, there is a single set of sensors and next
 * signal(s) to protect.
 * <LI>TRAILINGDIVERGING - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is thrown. It can also be used for
 * the lower head of a two head signal on the facing end of the turnout.
 * <P>
 * In this case, the signal is forced red if the specified turnout is CLOSED.
 * When the turnout is THROWN, there is a single set of sensors and next
 * signal(s) to protect.
 * <LI>FACING - This single head signal protects a facing point turnout, which
 * may therefore have two next signals and two sets of next sensors for the
 * closed and thrown states of the turnout.
 * <P>
 * If the turnout is THROWN, one set of sensors and next signal(s) is protected.
 * If the turnout is CLOSED, another set of sensors and next signal(s) is
 * protected.
 * </OL><P>
 * Note that these four possibilities logically require that certain information
 * be configured consistently; e.g. not specifying a turnout in TRAILINGMAIN
 * doesn't make any sense. That's not enforced explicitly, but violating it can
 * result in confusing behavior.
 *
 * <P>
 * The protected sensors should cover the track to the next signal. If any of
 * the protected sensors show ACTIVE, the signal will be dropped to red.
 * Normally, the protected sensors cover the occupancy of the track to the next
 * signal. In this case, the signal will show red to prevent trains from
 * entering an occupied stretch of track (often called a "block"). But the
 * actual source of the sensors can be anything useful, for example a
 * microswitch on a local turnout, etc.
 * <P>
 * There are several varients to how a next signal is protected. In the simplest
 * form, the controlled signal provides a warning to the engineer of what the
 * signal being protected will show when it becomes visible:
 * <UL>
 * <LI>If the next signal is red, the engineer needs to be told to slow down;
 * this signal will be set to yellow.
 * <LI>If the next signal is green, the engineer can proceed at track speed;
 * this signal will be set to green.
 * </UL>
 * If the next signal is yellow, there are two possible varients that can be
 * configured:
 * <UL>
 * <LI>For the common "three-aspect" signaling system, an engineer doesn't need
 * any warning before a yellow signal. In this case, this signal is set to green
 * when the protected signal is yellow.
 * <LI>For lines where track speed is very fast or braking distances are very
 * long, it can be useful to give engineers warning that the next signal is
 * yellow (and the one after that is red) so that slowing the train can start
 * early. Usually flashing yellow preceeds the yellow signal, and the system is
 * called "four-aspect" signaling.
 * </UL>
 *
 * <P>
 * In some cases, you want a signal to show <i>exactly</I> what the next signal
 * shows, instead of one speed faster. E.g. if the (protected) next signal is
 * red, this one should be red, instead of yellow. In this case, this signal is
 * called a "distant signal", as it provides a "distant" view of the protected
 * signal's appearance. Note that when in this mode, this signal still protects
 * the interveneing track, etc.
 * <P>
 * The "hold" unbound parameter can be used to set this logic to show red,
 * regardless of input. That's intended for use with CTC logic, etc.
 * <P>
 * "Approach lit" signaling sets the signal head to dark (off) unless the
 * specified sensor(s) are ACTIVE. Normally, those sensors are in front of
 * (before) the signal head. The signal heads then only light when a train is
 * approaching. This is used to preserve bulbs and batteries (and sometimes to
 * reduce engineer workload) on prototype railroads, but is uncommon on model
 * railroads; once the layout owner has gone to the trouble and expense of
 * installing signals, he usually wants them lit up.
 * <P>
 * Two signal heads can be protected. For example, if the next signal has two
 * heads to control travel onto a main track or siding, then both heads should
 * be provided here. The <i>faster</i> signal aspect will control the appearance
 * of this head. For example, if the next signal is showing a green head and a
 * red head, this signal will be green, because the train will be able to
 * proceed at track speed when it reaches that next signal (along the track with
 * the green signal).
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2005
 *
 * Revisions to add facing point sensors, approach lighting, and check box to
 * limit speed. Dick Bronosn (RJB) 2006
 */
public class BlockBossLogic extends Siglet implements java.beans.VetoableChangeListener {

    static public final int SINGLEBLOCK = 1;
    static public final int TRAILINGMAIN = 2;
    static public final int TRAILINGDIVERGING = 3;
    static public final int FACING = 4;

    int mode = 0;

    /**
     * Create a default object, without contents.
     */
    public BlockBossLogic() {
        jmri.InstanceManager.signalHeadManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }

    /**
     * Create an object to drive a specific signal.
     *
     * @param name System or user name of the driven signal.
     */
    public BlockBossLogic(String name) {
        super(name + rb.getString("_BlockBossLogic"));
        this.name = name;
        if (log.isTraceEnabled()) {
            log.trace("Create BBL " + name);
        }
        jmri.InstanceManager.signalHeadManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        driveSignal = nbhm.getNamedBeanHandle(name, InstanceManager.signalHeadManagerInstance().getSignalHead(name));
        if (driveSignal.getBean() == null) {
            log.warn(rb.getString("Signal_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * The "driven signal" is controlled by this element.
     *
     * @return system name of the driven signal
     */
    public String getDrivenSignal() {
        if (driveSignal != null) {
            return driveSignal.getName();
        }
        return "Unknown";
    }

    public NamedBeanHandle<SignalHead> getDrivenSignalNamedBean() {
        return driveSignal;
    }

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    protected static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.blockboss.BlockBossBundle");

    public void setSensor1(String name) {
        if (name == null || name.equals("")) {
            watchSensor1 = null;
            return;
        }
        watchSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchSensor1.getBean() == null) {
            log.warn(rb.getString("Sensor1_") + name + rb.getString("_was_not_found!"));
        }
    }

    public void setSensor2(String name) {
        if (name == null || name.equals("")) {
            watchSensor2 = null;
            return;
        }
        watchSensor2 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchSensor2.getBean() == null) {
            log.warn(rb.getString("Sensor2_") + name + rb.getString("_was_not_found!"));
        }
    }

    public void setSensor3(String name) {
        if (name == null || name.equals("")) {
            watchSensor3 = null;
            return;
        }
        watchSensor3 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchSensor3.getBean() == null) {
            log.warn(rb.getString("Sensor3_") + name + rb.getString("_was_not_found!"));
        }
    }

    public void setSensor4(String name) {
        if (name == null || name.equals("")) {
            watchSensor4 = null;
            return;
        }
        watchSensor4 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchSensor4.getBean() == null) {
            log.warn(rb.getString("Sensor4_") + name + rb.getString("_was_not_found!"));
        }
    }

    public void setSensor5(String name) {
        if (name == null || name.equals("")) {
            watchSensor5 = null;
            return;
        }
        watchSensor5 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchSensor5.getBean() == null) {
            log.warn(rb.getString("Sensor5_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * Return the system name of the sensor being monitored
     *
     * @return system name; null if no sensor configured
     */
    public String getSensor1() {
        if (watchSensor1 == null) {
            return null;
        }
        return watchSensor1.getName();
    }

    public String getSensor2() {
        if (watchSensor2 == null) {
            return null;
        }
        return watchSensor2.getName();
    }

    public String getSensor3() {
        if (watchSensor3 == null) {
            return null;
        }
        return watchSensor3.getName();
    }

    public String getSensor4() {
        if (watchSensor4 == null) {
            return null;
        }
        return watchSensor4.getName();
    }

    public String getSensor5() {
        if (watchSensor5 == null) {
            return null;
        }
        return watchSensor5.getName();
    }

    public void setTurnout(String name) {
        if (name == null || name.equals("")) {
            watchTurnout = null;
            return;
        }
        watchTurnout = nbhm.getNamedBeanHandle(name, InstanceManager.turnoutManagerInstance().provideTurnout(name));
        if (watchTurnout.getBean() == null) {
            log.warn(rb.getString("Turnout_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * Return the system name of the turnout being monitored
     *
     * @return system name; null if no turnout configured
     */
    public String getTurnout() {
        if (watchTurnout == null) {
            return null;
        }
        return watchTurnout.getName();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    String comment;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public void setWatchedSignal1(String name, boolean useFlash) {
        if (name == null || name.equals("")) {
            watchedSignal1 = null;
            return;
        }
        watchedSignal1 = nbhm.getNamedBeanHandle(name, InstanceManager.signalHeadManagerInstance().getSignalHead(name));
        if (watchedSignal1.getBean() == null) {
            log.warn(rb.getString("Signal_") + name + rb.getString("_was_not_found!"));
        }
        protectWithFlashing = useFlash;
    }

    /**
     * Return the system name of the signal being monitored for first route
     *
     * @return system name; null if no primary signal configured
     */
    public String getWatchedSignal1() {
        if (watchedSignal1 == null) {
            return null;
        }
        return watchedSignal1.getName();
    }

    public void setWatchedSignal1Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSignal1Alt = null;
            return;
        }
        watchedSignal1Alt = nbhm.getNamedBeanHandle(name, InstanceManager.signalHeadManagerInstance().getSignalHead(name));
        if (watchedSignal1Alt.getBean() == null) {
            log.warn(rb.getString("Signal_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * Return the system name of the alternate signal being monitored for first
     * route
     *
     * @return system name; null if no signal configured
     */
    public String getWatchedSignal1Alt() {
        if (watchedSignal1Alt == null) {
            return null;
        }
        return watchedSignal1Alt.getName();
    }

    public void setWatchedSignal2(String name) {
        if (name == null || name.equals("")) {
            watchedSignal2 = null;
            return;
        }
        watchedSignal2 = nbhm.getNamedBeanHandle(name, InstanceManager.signalHeadManagerInstance().getSignalHead(name));
        if (watchedSignal2.getBean() == null) {
            log.warn(rb.getString("Signal_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * Return the system name of the signal being monitored for the 2nd route
     *
     * @return system name; null if no signal configured
     */
    public String getWatchedSignal2() {
        if (watchedSignal2 == null) {
            return null;
        }
        return watchedSignal2.getName();
    }

    public void setWatchedSignal2Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSignal2Alt = null;
            return;
        }
        watchedSignal2Alt = nbhm.getNamedBeanHandle(name, InstanceManager.signalHeadManagerInstance().getSignalHead(name));
        if (watchedSignal2Alt.getBean() == null) {
            log.warn(rb.getString("Signal_") + name + rb.getString("_was_not_found!"));
        }
    }

    /**
     * Return the system name of the secondary signal being monitored for the
     * 2nd route
     *
     * @return system name; null if no secondary signal configured
     */
    public String getWatchedSignal2Alt() {
        if (watchedSignal2Alt == null) {
            return null;
        }
        return watchedSignal2Alt.getName();
    }

    public void setWatchedSensor1(String name) {
        if (name == null || name.equals("")) {
            watchedSensor1 = null;
            return;
        }
        watchedSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchedSensor1.getBean() == null) {
            log.warn(rb.getString("Sensor1_") + name + rb.getString("_was_not_found!"));
        }

    }

    /**
     * Return the original name of the sensor being monitored
     *
     * @return original name; null if no sensor configured
     */
    public String getWatchedSensor1() {
        if (watchedSensor1 == null) {
            return null;
        }
        return watchedSensor1.getName();
    }

    public void setWatchedSensor1Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSensor1Alt = null;
            return;
        }
        watchedSensor1Alt = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchedSensor1Alt.getBean() == null) {
            log.warn(rb.getString("Sensor1Alt_") + name + rb.getString("_was_not_found!"));
        }

    }

    /**
     * Return the system name of the sensor being monitored
     *
     * @return system name; null if no sensor configured
     */
    public String getWatchedSensor1Alt() {
        if (watchedSensor1Alt == null) {
            return null;
        }
        return watchedSensor1Alt.getName();
    }

    public void setWatchedSensor2(String name) {
        if (name == null || name.equals("")) {
            watchedSensor2 = null;
            return;
        }
        watchedSensor2 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchedSensor2.getBean() == null) {
            log.warn(rb.getString("Sensor2_") + name + rb.getString("_was_not_found!"));
        }

    }

    /**
     * Return the system name of the sensor being monitored
     *
     * @return system name; null if no sensor configured
     */
    public String getWatchedSensor2() {
        if (watchedSensor2 == null) {
            return null;
        }
        return watchedSensor2.getName();
    }

    public void setWatchedSensor2Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSensor2Alt = null;
            return;
        }
        watchedSensor2Alt = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (watchedSensor2Alt.getBean() == null) {
            log.warn(rb.getString("Sensor2Alt_") + name + rb.getString("_was_not_found!"));
        }

    }

    /**
     * Return the system name of the sensor being monitored
     *
     * @return system name; null if no sensor configured
     */
    public String getWatchedSensor2Alt() {
        if (watchedSensor2Alt == null) {
            return null;
        }
        return watchedSensor2Alt.getName();
    }

    public void setLimitSpeed1(boolean d) {
        limitSpeed1 = d;
    }

    public boolean getLimitSpeed1() {
        return limitSpeed1;
    }

    public void setLimitSpeed2(boolean d) {
        limitSpeed2 = d;
    }

    public boolean getLimitSpeed2() {
        return limitSpeed2;
    }

    public boolean getUseFlash() {
        return protectWithFlashing;
    }

    public void setDistantSignal(boolean d) {
        distantSignal = d;
    }

    public boolean getDistantSignal() {
        return distantSignal;
    }

    boolean mHold = false;

    /**
     * Provide the current value of the "hold" parameter. If true, the output is
     * forced to a RED "stop" aspect. This allows CTC and other higher-level
     * functions to control permission to enter this section of track.
     */
    public boolean getHold() {
        return mHold;
    }
    /*
     * Set the current value of the "hold" parameter.
     * If true, the output is forced to a RED "stop" aspect.
     * This allows CTC and other higher-level functions to 
     * control permission to enter this section of track.
     */

    public void setHold(boolean m) {
        mHold = m;
        setOutput();  // to invoke the new state
    }

    String name;
    NamedBeanHandle<SignalHead> driveSignal = null;
    NamedBeanHandle<Sensor> watchSensor1 = null;
    NamedBeanHandle<Sensor> watchSensor2 = null;
    NamedBeanHandle<Sensor> watchSensor3 = null;
    NamedBeanHandle<Sensor> watchSensor4 = null;
    NamedBeanHandle<Sensor> watchSensor5 = null;
    NamedBeanHandle<Turnout> watchTurnout = null;
    NamedBeanHandle<SignalHead> watchedSignal1 = null;
    NamedBeanHandle<SignalHead> watchedSignal1Alt = null;
    NamedBeanHandle<SignalHead> watchedSignal2 = null;
    NamedBeanHandle<SignalHead> watchedSignal2Alt = null;
    NamedBeanHandle<Sensor> watchedSensor1 = null;
    NamedBeanHandle<Sensor> watchedSensor1Alt = null;
    NamedBeanHandle<Sensor> watchedSensor2 = null;
    NamedBeanHandle<Sensor> watchedSensor2Alt = null;
    NamedBeanHandle<Sensor> approachSensor1 = null;

    boolean limitSpeed1 = false;
    boolean limitSpeed2 = false;
    boolean protectWithFlashing = false;
    boolean distantSignal = false;

    public void setApproachSensor1(String name) {
        if (name == null || name.equals("")) {
            approachSensor1 = null;
            return;
        }
        approachSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (approachSensor1.getBean() == null) {
            log.warn(rb.getString("Approach_Sensor1_") + name + rb.getString("_was_not_found!"));
        }

    }

    /**
     * Return the system name of the sensor being monitored
     *
     * @return system name; null if no sensor configured
     */
    public String getApproachSensor1() {
        if (approachSensor1 == null) {
            return null;
        }
        return approachSensor1.getName();
    }

    /**
     * Define the siglet's input and output.
     */
    public void defineIO() {
        NamedBean[] tempArray = new NamedBean[10];
        int n = 0;

        if (watchTurnout != null) {
            tempArray[n] = watchTurnout.getBean();
            n++;
        }
        if (watchSensor1 != null) {
            tempArray[n] = watchSensor1.getBean();
            n++;
        }
        if (watchSensor2 != null) {
            tempArray[n] = watchSensor2.getBean();
            n++;
        }
        if (watchSensor3 != null) {
            tempArray[n] = watchSensor3.getBean();
            n++;
        }
        if (watchSensor4 != null) {
            tempArray[n] = watchSensor4.getBean();
            n++;
        }
        if (watchSensor5 != null) {
            tempArray[n] = watchSensor5.getBean();
            n++;
        }
        if (watchedSignal1 != null) {
            tempArray[n] = watchedSignal1.getBean();
            n++;
        }
        if (watchedSignal1Alt != null) {
            tempArray[n] = watchedSignal1Alt.getBean();
            n++;
        }
        if (watchedSignal2 != null) {
            tempArray[n] = watchedSignal2.getBean();
            n++;
        }
        if (watchedSignal2Alt != null) {
            tempArray[n] = watchedSignal2Alt.getBean();
            n++;
        }
        if (watchedSensor1 != null) {
            tempArray[n] = watchedSensor1.getBean();
            n++;
        }
        if (watchedSensor1Alt != null) {
            tempArray[n] = watchedSensor1Alt.getBean();
            n++;
        }
        if (watchedSensor2 != null) {
            tempArray[n] = watchedSensor2.getBean();
            n++;
        }
        if (watchedSensor2Alt != null) {
            tempArray[n] = watchedSensor2Alt.getBean();
            n++;
        }
        if (approachSensor1 != null) {
            tempArray[n] = approachSensor1.getBean();
            n++;
        }

        // copy temp to definitive inputs
        inputs = new NamedBean[n];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = tempArray[i];
        }

        outputs = new NamedBean[]{driveSignal.getBean()};

        // also need to act if the _signal's_ "held"
        // parameter changes, but we don't want to 
        // act if the signals appearance changes (to 
        // avoid a loop, or avoid somebody changing appearance
        // manually and having it instantly recomputed & changed back
        driveSignal.getBean().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(rb.getString("Held"))) {
                    setOutput();
                }
            }
        }, driveSignal.getName(), "BlockBossLogic:" + name);
    }

    /**
     * Recompute new output state and apply it.
     */
    public void setOutput() {
        if (log.isTraceEnabled()) {
            log.trace("setOutput for " + name);
        }
        // make sure init is complete
        if ((outputs == null) || (outputs[0] == null)) {
            return;
        }

        // if "hold" is true, must show red
        if (getHold()) {
            ((SignalHead) outputs[0]).setAppearance(SignalHead.RED);
            if (log.isDebugEnabled()) {
                log.debug("setOutput red due to held for " + name);
            }
            return;
        }

        // otherwise, process algorithm
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
                log.error(rb.getString("Unexpected_mode:_") + mode + "_Signal_" + getDrivenSignal());
        }
    }

    int fastestColor1() {
        int result = SignalHead.RED;
        // special case:  GREEN if no next signal
        if (watchedSignal1 == null && watchedSignal1Alt == null) {
            result = SignalHead.GREEN;
        }

        int val = result;
        if (watchedSignal1 != null) {
            val = watchedSignal1.getBean().getAppearance();
        }
        if (watchedSignal1 != null && watchedSignal1.getBean().getHeld()) {
            val = SignalHead.RED;  // if Held, act as if Red
        }
        int valAlt = result;
        if (watchedSignal1Alt != null) {
            valAlt = watchedSignal1Alt.getBean().getAppearance();
        }
        if (watchedSignal1Alt != null && watchedSignal1Alt.getBean().getHeld()) {
            valAlt = SignalHead.RED; // if Held, act as if Red
        }
        return fasterOf(val, valAlt);
    }

    int fastestColor2() {
        int result = SignalHead.RED;
        // special case:  GREEN if no next signal
        if (watchedSignal2 == null && watchedSignal2Alt == null) {
            result = SignalHead.GREEN;
        }

        int val = result;
        if (watchedSignal2 != null) {
            val = watchedSignal2.getBean().getAppearance();
        }
        if (watchedSignal2 != null && watchedSignal2.getBean().getHeld()) {
            val = SignalHead.RED;
        }

        int valAlt = result;
        if (watchedSignal2Alt != null) {
            valAlt = watchedSignal2Alt.getBean().getAppearance();
        }
        if (watchedSignal2Alt != null && watchedSignal2Alt.getBean().getHeld()) {
            valAlt = SignalHead.RED;
        }

        return fasterOf(val, valAlt);
    }

    /**
     * Given two {@link SignalHead} color constants, returns the one
     * corresponding to the slower speed.
     */
    static int slowerOf(int a, int b) {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.min(a, b);
    }

    /**
     * Given two {@link SignalHead} color constants, returns the one
     * corresponding to the faster speed.
     */
    static int fasterOf(int a, int b) {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.max(a, b);
    }

    void doSingleBlock() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead) outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1() == SignalHead.YELLOW) {
            appearance = SignalHead.FLASHYELLOW;
        }
        if (fastestColor1() == SignalHead.RED || fastestColor1() == SignalHead.FLASHRED) {
            appearance = SignalHead.YELLOW;
        }

        // if distant signal, show exactly what the home signal does
        if (distantSignal) {
            appearance = fastestColor1();
        }

        // if limited speed and green, reduce to yellow
        if (limitSpeed1) {
            appearance = slowerOf(appearance, SignalHead.YELLOW);
        }

        // check for red overriding yellow or green
        if (watchSensor1 != null && watchSensor1.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor2 != null && watchSensor2.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor3 != null && watchSensor3.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor4 != null && watchSensor4.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor5 != null && watchSensor5.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            if (log.isDebugEnabled()) {
                log.debug("Change appearance of " + name + " to " + appearance);
            }
        }
    }

    void doTrailingMain() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead) outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1() == SignalHead.YELLOW) {
            appearance = SignalHead.FLASHYELLOW;
        }
        if (fastestColor1() == SignalHead.RED || fastestColor1() == SignalHead.FLASHRED) {
            appearance = SignalHead.YELLOW;
        }

        // if distant signal, show exactly what the home signal does
        if (distantSignal) {
            appearance = fastestColor1();
        }

        // if limited speed and green, reduce to yellow
        if (limitSpeed1) {
            appearance = slowerOf(appearance, SignalHead.YELLOW);
        }

        // check for red overriding yellow or green
        if (watchSensor1 != null && watchSensor1.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor2 != null && watchSensor2.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor3 != null && watchSensor3.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor4 != null && watchSensor4.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor5 != null && watchSensor5.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }

        if (watchTurnout != null && watchTurnout.getBean().getKnownState() != Turnout.CLOSED) {
            appearance = SignalHead.RED;
        }
        if (watchTurnout != null && watchTurnout.getBean().getCommandedState() != Turnout.CLOSED) {
            appearance = SignalHead.RED;
        }

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            if (log.isDebugEnabled()) {
                log.debug("Change appearance of " + name + " to " + appearance);
            }
        }
    }

    void doTrailingDiverging() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead) outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1() == SignalHead.YELLOW) {
            appearance = SignalHead.FLASHYELLOW;
        }
        if (fastestColor1() == SignalHead.RED || fastestColor1() == SignalHead.FLASHRED) {
            appearance = SignalHead.YELLOW;
        }

        // if distant signal, show exactly what the home signal does
        if (distantSignal) {
            appearance = fastestColor1();
        }

        // if limited speed and green, reduce to yellow
        if (limitSpeed2) {
            appearance = slowerOf(appearance, SignalHead.YELLOW);
        }

        // check for red overriding yellow or green
        if (watchSensor1 != null && watchSensor1.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor2 != null && watchSensor2.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor3 != null && watchSensor3.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor4 != null && watchSensor4.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor5 != null && watchSensor5.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }

        if (watchTurnout != null && watchTurnout.getBean().getKnownState() != Turnout.THROWN) {
            appearance = SignalHead.RED;
        }
        if (watchTurnout != null && watchTurnout.getBean().getCommandedState() != Turnout.THROWN) {
            appearance = SignalHead.RED;
        }

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            if (log.isDebugEnabled()) {
                log.debug("Change appearance of " + name + " to " + appearance);
            }
        }
    }

    void doFacing() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead) outputs[0]).getAppearance();

        // find downstream appearance, being pessimistic if we're not sure of the state
        int s = SignalHead.GREEN;
        if (watchTurnout != null && watchTurnout.getBean().getKnownState() != Turnout.THROWN) {
            s = slowerOf(s, fastestColor1());
        }
        if (watchTurnout != null && watchTurnout.getBean().getKnownState() != Turnout.CLOSED) {
            s = slowerOf(s, fastestColor2());
        }

        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && s == SignalHead.YELLOW) {
            appearance = SignalHead.FLASHYELLOW;
        }
        if (s == SignalHead.RED || s == SignalHead.FLASHRED) {
            appearance = SignalHead.YELLOW;
        }
        // if distant signal, show exactly what the home signal does
        if (distantSignal) {
            appearance = s;
        }

        // if limited speed and green or flashing yellow, reduce to yellow
        if (watchTurnout != null && limitSpeed1 && watchTurnout.getBean().getKnownState() != Turnout.THROWN) {
            appearance = slowerOf(appearance, SignalHead.YELLOW);
        }

        if (watchTurnout != null && limitSpeed2 && watchTurnout.getBean().getKnownState() != Turnout.CLOSED) {
            appearance = slowerOf(appearance, SignalHead.YELLOW);
        }

        // check for red overriding yellow or green
        if (watchSensor1 != null && watchSensor1.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor2 != null && watchSensor2.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor3 != null && watchSensor3.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor4 != null && watchSensor4.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }
        if (watchSensor5 != null && watchSensor5.getBean().getKnownState() != Sensor.INACTIVE) {
            appearance = SignalHead.RED;
        }

        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.CLOSED)
                && ((watchedSensor1 != null && watchedSensor1.getBean().getKnownState() != Sensor.INACTIVE))) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.CLOSED) && ((watchedSensor1Alt != null && watchedSensor1Alt.getBean().getKnownState() != Sensor.INACTIVE))) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.THROWN) && ((watchedSensor2 != null && watchedSensor2.getBean().getKnownState() != Sensor.INACTIVE))) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.THROWN) && ((watchedSensor2Alt != null && watchedSensor2Alt.getBean().getKnownState() != Sensor.INACTIVE))) {
            appearance = SignalHead.RED;
        }

        // check if turnout in motion, if so force red
        if (watchTurnout != null && (watchTurnout.getBean().getKnownState() != watchTurnout.getBean().getCommandedState())) {
            appearance = SignalHead.RED;
        }
        if (watchTurnout != null && (watchTurnout.getBean().getKnownState() != Turnout.THROWN) && (watchTurnout.getBean().getKnownState() != Turnout.CLOSED)) // checking for other states
        {
            appearance = SignalHead.RED;
        }

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
        }
    }

    /**
     * Handle the approach lighting logic for all modes
     */
    void doApproach() {
        if (approachSensor1 != null && approachSensor1.getBean().getKnownState() == Sensor.INACTIVE) {
            // should not be lit
            if (driveSignal.getBean().getLit()) {
                driveSignal.getBean().setLit(false);
            }
        } else {
            // should be lit
            if (!driveSignal.getBean().getLit()) {
                driveSignal.getBean().setLit(true);
            }
        }
        return;
    }

    static ArrayList<BlockBossLogic> bblList = null;

    public static Enumeration<BlockBossLogic> entries() {
        setup(); // ensure we've been registered
        return Collections.enumeration(bblList);
    }

    private static void setup() {
        if (bblList == null) {
            bblList = new ArrayList<BlockBossLogic>();
            InstanceManager.configureManagerInstance().registerConfig(new BlockBossLogic(), jmri.Manager.BLOCKBOSS);
        }
    }

    /**
     * Ensure that this BlockBossLogic object is available for later retrieval
     */
    public void retain() {
        bblList.add(this);
    }

    /**
     * Return the BlockBossLogic item governing a specific signal, having
     * removed it from use.
     *
     * @param signal
     * @return never null
     */
    public static BlockBossLogic getStoppedObject(String signal) {
        return getStoppedObject(InstanceManager.signalHeadManagerInstance().getSignalHead(signal));
    }

    /**
     * Return the BlockBossLogic item governing a specific signal, having
     * removed it from use.
     *
     * @param sh
     * @return never null
     */
    public static BlockBossLogic getStoppedObject(SignalHead sh) {
        BlockBossLogic b = null;
        setup();

        for (BlockBossLogic bbl : bblList) {
            if (bbl.getDrivenSignalNamedBean().getBean() == sh) {
                b = bbl;
                break;
            }
        }

        if (b != null) {
            // found an existing one, remove it from the map and stop its thread
            bblList.remove(b);
            b.stop();
            return b;
        } else {
            // no existing one, create a new one
            return new BlockBossLogic(sh.getDisplayName());
        }
    }

    /**
     * Return the BlockBossLogic item governing a specific signal.
     * <P>
     * Unlike {@link BlockBossLogic#getStoppedObject(String signal)} this does
     * not remove the object from being used.
     *
     * @param signal system name
     * @return never null
     */
    public static BlockBossLogic getExisting(String signal) {
        return getExisting(InstanceManager.signalHeadManagerInstance().getSignalHead(signal));
    }

    public static BlockBossLogic getExisting(SignalHead sh) {
        setup();

        for (BlockBossLogic bbl : bblList) {
            if (bbl.getDrivenSignalNamedBean().getBean() == sh) {
                return bbl;
            }
        }

        return (new BlockBossLogic(sh.getDisplayName()));
    }

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("InUseBlockBossHeader", getDrivenSignal())); //IN18N
            boolean found = false;

            if (nb instanceof SignalHead) {
                if (getDrivenSignalNamedBean() != null && getDrivenSignalNamedBean().getBean().equals(nb)) {
                    message.append("<br><b>This SSL will be deleted</b>");
                    throw new java.beans.PropertyVetoException(message.toString(), evt);
                }
                if ((watchedSignal1 != null && watchedSignal1.getBean().equals(nb))
                        || (watchedSignal1Alt != null && watchedSignal1Alt.getBean().equals(nb))
                        || (watchedSignal2 != null && watchedSignal2.getBean().equals(nb))
                        || (watchedSignal2Alt != null && watchedSignal2Alt.getBean().equals(nb))) {
                    message.append("<ul>");
                    message.append(Bundle.getMessage("InUseWatchedSignal"));
                    message.append("</ul>");
                    found = true;
                }

            } else if (nb instanceof Turnout) {
                if (watchTurnout != null && watchTurnout.getBean().equals(nb)) {
                    found = true;
                    message.append("<ul>");
                    message.append(Bundle.getMessage("InUseWatchedTurnout"));
                    message.append("</ul>");
                }
            } else if (nb instanceof Sensor) {
                message.append("<ul>");
                if ((watchSensor1 != null && watchSensor1.getBean().equals(nb))
                        || (watchSensor2 != null && watchSensor2.getBean().equals(nb))
                        || (watchSensor3 != null && watchSensor3.getBean().equals(nb))
                        || (watchSensor4 != null && watchSensor4.getBean().equals(nb))
                        || (watchSensor5 != null && watchSensor5.getBean().equals(nb))) {
                    message.append("<li>");
                    message.append(Bundle.getMessage("InUseWatchedSensor"));
                    message.append("</li>");
                    found = true;
                }
                if ((watchedSensor1 != null && watchedSensor1.getBean().equals(nb))
                        || (watchedSensor2 != null && watchedSensor2.getBean().equals(nb))
                        || (watchedSensor1Alt != null && watchedSensor1Alt.getBean().equals(nb))
                        || (watchedSensor2Alt != null && watchedSensor2Alt.getBean().equals(nb))) {
                    message.append("<li>");
                    message.append(Bundle.getMessage("InUseWatchedSensor"));
                    message.append("</li>");
                    found = true;

                }
                if (approachSensor1 != null && approachSensor1.getBean().equals(nb)) {
                    found = true;
                    message.append("<li>");
                    message.append(Bundle.getMessage("InUseApproachSensor"));
                    message.append("</li>");
                }

                message.append("</ul>");
            }
            if (found) {
                message.append(Bundle.getMessage("InUseBlockBossFooter")); //IN18N
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { //IN18N
            if (nb instanceof SignalHead) {
                if (getDrivenSignalNamedBean() != null && getDrivenSignalNamedBean().getBean().equals(nb)) {
                    stop();
                    bblList.remove(this);
                }
                if (watchedSignal1 != null && watchedSignal1.getBean().equals(nb)) {
                    stop();
                    setWatchedSignal1(null, false);
                    start();
                }
                if (watchedSignal1Alt != null && watchedSignal1Alt.getBean().equals(nb)) {
                    stop();
                    setWatchedSignal1Alt(null);
                    start();
                }
                if (watchedSignal2 != null && watchedSignal2.getBean().equals(nb)) {
                    stop();
                    setWatchedSignal2(null);
                    start();
                }
                if (watchedSignal2Alt != null && watchedSignal2Alt.getBean().equals(nb)) {
                    stop();
                    setWatchedSignal2Alt(null);
                    start();
                }
            } else if (nb instanceof Turnout) {
                if (watchTurnout != null && watchTurnout.getBean().equals(nb)) {
                    stop();
                    setTurnout(null);
                    start();
                }
            } else if (nb instanceof Sensor) {
                if (watchSensor1 != null && watchSensor1.getBean().equals(nb)) {
                    stop();
                    setSensor1(null);
                    start();
                }
                if (watchSensor2 != null && watchSensor2.getBean().equals(nb)) {
                    stop();
                    setSensor2(null);
                    start();
                }
                if (watchSensor3 != null && watchSensor3.getBean().equals(nb)) {
                    stop();
                    setSensor3(null);
                    start();
                }
                if (watchSensor4 != null && watchSensor4.getBean().equals(nb)) {
                    stop();
                    setSensor4(null);
                    start();
                }
                if (watchSensor5 != null && watchSensor5.getBean().equals(nb)) {
                    stop();
                    setSensor5(null);
                    start();
                }
                if (watchedSensor1 != null && watchedSensor1.getBean().equals(nb)) {
                    stop();
                    setWatchedSensor1(null);
                    start();
                }
                if (watchedSensor2 != null && watchedSensor2.getBean().equals(nb)) {
                    stop();
                    setWatchedSensor2(null);
                    start();
                }
                if (watchedSensor1Alt != null && watchedSensor1Alt.getBean().equals(nb)) {
                    stop();
                    setWatchedSensor1Alt(null);
                    start();
                }
                if (watchedSensor2Alt != null && watchedSensor2Alt.getBean().equals(nb)) {
                    stop();
                    setWatchedSensor2Alt(null);
                    start();
                }
                if (approachSensor1 != null && approachSensor1.getBean().equals(nb)) {
                    stop();
                    setApproachSensor1(null);
                    start();
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BlockBossLogic.class.getName());
}

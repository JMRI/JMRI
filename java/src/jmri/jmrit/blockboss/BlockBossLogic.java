package jmri.jmrit.blockboss;

import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanUsageReport;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.automat.Siglet;

/**
 * Drives the "simple signal" logic for one signal.
 * <p>
 * Signals "protect" by telling the engineer about the conditions ahead. The
 * engineer controls the speed of the train based on what the signals show, and
 * the signals in turn react to whether the track ahead is occupied, what
 * signals further down the line show, etc.
 * <p>
 * There are four situations that this logic can handle:
 * <ol>
 * <li>SINGLEBLOCK - A simple block, without a turnout.
 * <p>
 * In this case, there is only a single set of sensors and a single next signal
 * to protect.
 * <li>TRAILINGMAIN - This signal is protecting a trailing point turnout, which
 * can only be passed when the turnout is closed. It can also be used for the
 * upper head of a two head signal on the facing end of the turnout.
 * <p>
 * In this case, the signal is forced red if the specified turnout is THROWN.
 * When the turnout is CLOSED, there is a single set of sensors and next
 * signal(s) to protect.
 * <li>TRAILINGDIVERGING - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is thrown. It can also be used for
 * the lower head of a two head signal on the facing end of the turnout.
 * <p>
 * In this case, the signal is forced red if the specified turnout is CLOSED.
 * When the turnout is THROWN, there is a single set of sensors and next
 * signal(s) to protect.
 * <li>FACING - This single head signal protects a facing point turnout, which
 * may therefore have two next signals and two sets of next sensors for the
 * closed and thrown states of the turnout.
 * <p>
 * If the turnout is THROWN, one set of sensors and next signal(s) is protected.
 * If the turnout is CLOSED, another set of sensors and next signal(s) is
 * protected.
 * </ol>
 * <p>
 * Note that these four possibilities logically require that certain information
 * be configured consistently; e.g. not specifying a turnout in TRAILINGMAIN
 * doesn't make any sense. That's not enforced explicitly, but violating it can
 * result in confusing behavior.
 *
 * <p>
 * The protected sensors should cover the track to the next signal. If any of
 * the protected sensors show ACTIVE, the signal will be dropped to red.
 * Normally, the protected sensors cover the occupancy of the track to the next
 * signal. In this case, the signal will show red to prevent trains from
 * entering an occupied stretch of track (often called a "block"). But the
 * actual source of the sensors can be anything useful, for example a
 * microswitch on a local turnout, etc.
 * <p>
 * There are several variants to how a next signal is protected. In the simplest
 * form, the controlled signal provides a warning to the engineer of what the
 * signal being protected will show when it becomes visible:
 * <ul>
 * <li>If the next signal is red, the engineer needs to be told to slow down;
 * this signal will be set to yellow.
 * <li>If the next signal is green, the engineer can proceed at track speed;
 * this signal will be set to green.
 * </ul>
 * If the next signal is yellow, there are two possible variants that can be
 * configured:
 * <ul>
 * <li>For the common "three-aspect" signaling system, an engineer doesn't need
 * any warning before a yellow signal. In this case, this signal is set to green
 * when the protected signal is yellow.
 * <li>For lines where track speed is very fast or braking distances are very
 * long, it can be useful to give engineers warning that the next signal is
 * yellow (and the one after that is red) so that slowing the train can start
 * early. Usually flashing yellow preceeds the yellow signal, and the system is
 * called "four-aspect" signaling.
 * </ul>
 *
 * <p>
 * In some cases, you want a signal to show <i>exactly</I> what the next signal
 * shows, instead of one speed faster. E.g. if the (protected) next signal is
 * red, this one should be red, instead of yellow. In this case, this signal is
 * called a "distant signal", as it provides a "distant" view of the protected
 * signal heads's appearance. Note that when in this mode, this signal still protects
 * the interveneing track, etc.
 * <p>
 * The "hold" unbound parameter can be used to set this logic to show red,
 * regardless of input. That's intended for use with CTC logic, etc.
 * <p>
 * "Approach lit" signaling sets the signal head to dark (off) unless the
 * specified sensor(s) are ACTIVE. Normally, those sensors are in front of
 * (before) the signal head. The signal heads then only light when a train is
 * approaching. This is used to preserve bulbs and batteries (and sometimes to
 * reduce engineer workload) on prototype railroads, but is uncommon on model
 * railroads; once the layout owner has gone to the trouble and expense of
 * installing signals, he usually wants them lit up.
 * <p>
 * Two signal heads can be protected. For example, if the next signal has two
 * heads to control travel onto a main track or siding, then both heads should
 * be provided here. The <i>faster</i> signal aspect will control the appearance
 * of this head. For example, if the next signal is showing a green head and a
 * red head, this signal will be green, because the train will be able to
 * proceed at track speed when it reaches that next signal (along the track with
 * the green signal).
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 * @author Dick Bronson 2006 Revisions to add facing point sensors, approach lighting
 * and check box to limit speed.
 */
public class BlockBossLogic extends Siglet implements java.beans.VetoableChangeListener {

    public static final int SINGLEBLOCK = 1;
    public static final int TRAILINGMAIN = 2;
    public static final int TRAILINGDIVERGING = 3;
    public static final int FACING = 4;
    private static final String BEAN_X_NOT_FOUND = "BeanXNotFound";
    private static final String BEAN_NAME_SIGNAL_HEAD = "BeanNameSignalHead";
    private static final String BEAN_NAME_SENSOR = "BeanNameSensor";

    private int mode = 0;

    /**
     * Create an object to drive a specific signal head.
     *
     * @param name System or user name of the driven signal head, which must exist
     */
    public BlockBossLogic(@Nonnull String name) {
        super(name + Bundle.getMessage("_BlockBossLogic"));
        java.util.Objects.requireNonNull(name, "BlockBossLogic name cannot be null");
        this.name = name;
        log.trace("Create BBL {}", name);

        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        SignalHead driveHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (driveHead == null) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SIGNAL_HEAD), name));
            throw new IllegalArgumentException("SignalHead \"" + name + "\" does not exist");
        }
        driveSignal = nbhm.getNamedBeanHandle(name, driveHead);
        java.util.Objects.requireNonNull(driveSignal, "driveSignal should not have been null");
    }

    /**
     * The "driven signal" is controlled by this element.
     *
     * @return system name of the driven signal head
     */
    public @Nonnull String getDrivenSignal() {
        java.util.Objects.requireNonNull(driveSignal, "driveSignal should not have been null");
        String retVal = driveSignal.getName();
        java.util.Objects.requireNonNull(retVal, "driveSignal system name should not have been null");
        return retVal;
    }

    public @Nonnull NamedBeanHandle<SignalHead> getDrivenSignalNamedBean() {
        java.util.Objects.requireNonNull(driveSignal, "driveSignal should have been null");
        return driveSignal;
    }

    private final jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    public void setSensor1(String name) {
        if (name == null || name.equals("")) {
            watchSensor1 = null;
            return;
        }
        try {
            watchSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "1", name));
        }
    }

    public void setSensor2(String name) {
        if (name == null || name.equals("")) {
            watchSensor2 = null;
            return;
        }
        try {
            watchSensor2 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "2", name));
        }
    }

    public void setSensor3(String name) {
        if (name == null || name.equals("")) {
            watchSensor3 = null;
            return;
        }
        try {
            watchSensor3 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "3", name));
        }
    }

    public void setSensor4(String name) {
        if (name == null || name.equals("")) {
            watchSensor4 = null;
            return;
        }
        try {
            watchSensor4 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "4", name));
        }
    }

    public void setSensor5(String name) {
        if (name == null || name.equals("")) {
            watchSensor5 = null;
            return;
        }
        try {
            watchSensor5 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "5", name));
        }
    }

    /**
     * Get the system name of the sensors 1-5 being monitored.
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
        try {
            watchTurnout = nbhm.getNamedBeanHandle(name, InstanceManager.turnoutManagerInstance().provideTurnout(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage("BeanNameTurnout"), name));
        }
    }

    /**
     * Get the system name of the turnout being monitored.
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

    private String comment;

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
        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (head != null) {
            watchedSignal1 = nbhm.getNamedBeanHandle(name, head);
        } else {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SIGNAL_HEAD), name));
            watchedSignal1 = null;
        }
        protectWithFlashing = useFlash;
    }

    /**
     * Get the system name of the signal head being monitored for first route.
     *
     * @return system name; null if no primary signal head is configured
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
        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (head != null) {
            watchedSignal1Alt = nbhm.getNamedBeanHandle(name, head);
        } else {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SIGNAL_HEAD), name));
            watchedSignal1Alt = null;
        }
    }

    /**
     * Get the system name of the alternate signal head being monitored for first
     * route.
     *
     * @return system name; null if no signal head is configured
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
        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (head != null) {
            watchedSignal2 = nbhm.getNamedBeanHandle(name, head);
        } else {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SIGNAL_HEAD), name));
            watchedSignal2 = null;
        }
    }

    /**
     * Get the system name of the signal head being monitored for the 2nd route.
     *
     * @return system name; null if no signal head is configured
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
        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (head != null) {
            watchedSignal2Alt = nbhm.getNamedBeanHandle(name, head);
        } else {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SIGNAL_HEAD), name));
            watchedSignal2Alt = null;
        }
    }

    /**
     * Get the system name of the secondary signal head being monitored for the
     * 2nd route.
     *
     * @return system name; null if no secondary signal head is configured
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
        try {
            watchedSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "1", name));
            watchedSensor1 = null;
        }
    }

    /**
     * Get the original name of the sensor1 being monitored.
     *
     * @return original name; null if no sensor is configured
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
        try {
            watchedSensor1Alt = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "1Alt", name));
            watchedSensor1Alt = null;
        }
    }

    /**
     * Get the system name of the sensor1Alt being monitored.
     *
     * @return system name; null if no sensor is configured
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
        try {
            watchedSensor2 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "2", name));
            watchedSensor2 = null;
        }
    }

    /**
     * Get the system name of the sensor2 being monitored.
     *
     * @return system name; null if no sensor is configured
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
        try {
            watchedSensor2Alt = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        } catch (IllegalArgumentException ex) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage(BEAN_NAME_SENSOR) + "2Alt", name));
            watchedSensor2Alt = null;
        }
    }

    /**
     * Get the system name of the sensor2Alt being monitored.
     *
     * @return system name; null if no sensor is configured
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

    public void setRestrictingSpeed1(boolean d) {
        restrictingSpeed1 = d;
    }

    public boolean getRestrictingSpeed1() {
        return restrictingSpeed1;
    }

    public void setLimitSpeed2(boolean d) {
        limitSpeed2 = d;
    }

    public boolean getLimitSpeed2() {
        return limitSpeed2;
    }

    public void setRestrictingSpeed2(boolean d) {
        restrictingSpeed2 = d;
    }

    public boolean getRestrictingSpeed2() {
        return restrictingSpeed2;
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

    private boolean mHold = false;

    /**
     * Provide the current value of the "hold" parameter.
     * <p>
     * If true, the output is forced to a RED "stop" appearance.
     * This allows CTC and other higher-level functions to control
     * permission to enter this section of track.
     *
     * @return true if this Logic currently is Held
     */
    private boolean getHold() {
        return mHold;
    }

    /**
     * Set the current value of the "hold" parameter.
     * If true, the output is forced to a RED "stop" appearance.
     * This allows CTC and other higher-level functions to
     * control permission to enter this section of track.
     *
     * @param m true to set Logic to Held
     */
    public void setHold(boolean m) {
        mHold = m;
        setOutput();  // to invoke the new state
    }

    private final String name;

    @Nonnull NamedBeanHandle<SignalHead> driveSignal;

    private NamedBeanHandle<Sensor> watchSensor1 = null;
    private NamedBeanHandle<Sensor> watchSensor2 = null;
    private NamedBeanHandle<Sensor> watchSensor3 = null;
    private NamedBeanHandle<Sensor> watchSensor4 = null;
    private NamedBeanHandle<Sensor> watchSensor5 = null;
    private NamedBeanHandle<Turnout> watchTurnout = null;
    private NamedBeanHandle<SignalHead> watchedSignal1 = null;
    private NamedBeanHandle<SignalHead> watchedSignal1Alt = null;
    private NamedBeanHandle<SignalHead> watchedSignal2 = null;
    private NamedBeanHandle<SignalHead> watchedSignal2Alt = null;
    private NamedBeanHandle<Sensor> watchedSensor1 = null;
    private NamedBeanHandle<Sensor> watchedSensor1Alt = null;
    private NamedBeanHandle<Sensor> watchedSensor2 = null;
    private NamedBeanHandle<Sensor> watchedSensor2Alt = null;
    private NamedBeanHandle<Sensor> approachSensor1 = null;

    private boolean limitSpeed1 = false;
    private boolean restrictingSpeed1 = false;
    private boolean limitSpeed2 = false;
    private boolean restrictingSpeed2 = false;
    private boolean protectWithFlashing = false;
    private boolean distantSignal = false;

    public void setApproachSensor1(String name) {
        if (name == null || name.equals("")) {
            approachSensor1 = null;
            return;
        }
        approachSensor1 = nbhm.getNamedBeanHandle(name, InstanceManager.sensorManagerInstance().provideSensor(name));
        if (approachSensor1.getBean() == null) {
            log.warn(Bundle.getMessage(BEAN_X_NOT_FOUND, Bundle.getMessage("Approach_Sensor1_"), name));
        }
    }

    /**
     * Get the system name of the sensor being monitored.
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
    @Override
    public void defineIO() {
        List<NamedBean> namedBeanList = new ArrayList<>();

        addBeanToListIfItExists(namedBeanList,watchTurnout);
        addBeanToListIfItExists(namedBeanList,watchSensor1);
        addBeanToListIfItExists(namedBeanList,watchSensor2);
        addBeanToListIfItExists(namedBeanList,watchSensor3);
        addBeanToListIfItExists(namedBeanList,watchSensor4);
        addBeanToListIfItExists(namedBeanList,watchSensor5);
        addBeanToListIfItExists(namedBeanList,watchedSignal1);
        addBeanToListIfItExists(namedBeanList,watchedSignal1Alt);
        addBeanToListIfItExists(namedBeanList,watchedSignal2);
        addBeanToListIfItExists(namedBeanList,watchedSignal2Alt);
        addBeanToListIfItExists(namedBeanList,watchedSensor1);
        addBeanToListIfItExists(namedBeanList,watchedSensor1Alt);
        addBeanToListIfItExists(namedBeanList,watchedSensor2);
        addBeanToListIfItExists(namedBeanList,watchedSensor2Alt);
        addBeanToListIfItExists(namedBeanList,approachSensor1);

        // copy temp to definitive inputs
        inputs = namedBeanList.toArray(new NamedBean[1]);

        outputs = new NamedBean[]{driveSignal.getBean()};

        // also need to act if the _signal's_ "held"
        // parameter changes, but we don't want to
        // act if the signals appearance changes (to
        // avoid a loop, or avoid somebody changing appearance
        // manually and having it instantly recomputed & changed back
        driveSignal.getBean().addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals(Bundle.getMessage("Held"))) {
                setOutput();
            }
        }, driveSignal.getName(), "BlockBossLogic:" + name);
    }

    private void addBeanToListIfItExists(List<NamedBean> namedBeanList, NamedBeanHandle<?> namedBeanHandle) {
        if (namedBeanHandle != null) {
            namedBeanList.add(namedBeanHandle.getBean());
        }
    }

    /**
     * Recompute new output state and apply it.
     */
    @Override
    public void setOutput() {

        log.trace("setOutput for {}", name);

        // make sure init is complete
        if ((outputs == null) || (outputs[0] == null)) {
            return;
        }

        // if "hold" is true, must show red
        if (getHold()) {
            ((SignalHead) outputs[0]).setAppearance(SignalHead.RED);
            log.debug("setOutput red due to held for {}", name);
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
                log.error("{}{}_Signal_{}", Bundle.getMessage("UnexpectedMode"), mode, getDrivenSignal());
        }
    }

    private int fastestColor1() {
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

    private int fastestColor2() {
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
     * Given two {@link SignalHead} color constants, return the one
     * corresponding to the slower speed.
     *
     * @param a color constant 1 to compare with
     * @param b color constant 2
     * @return the lowest of the two values entered
     */
    private static int slowerOf(int a, int b) {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.min(a, b);
    }

    /**
     * Given two {@link SignalHead} color constants, return the one
     * corresponding to the faster speed.
     *
     * @param a color constant 1 to compare with
     * @param b color constant 2
     * @return the highest of the two values entered
     */
    private static int fasterOf(int a, int b) {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.max(a, b);
    }

    private void doSingleBlock() {
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

        // if restricting, limit to flashing red
        if (restrictingSpeed1) {
            appearance = slowerOf(appearance, SignalHead.FLASHRED);
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

        // check if signal if held, forcing a red appearance by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            log.debug("Change appearance of {} to: {}", name, appearance);
        }
    }

    private void doTrailingMain() {
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
        // if restricting, limit to flashing red
        if (restrictingSpeed1) {
            appearance = slowerOf(appearance, SignalHead.FLASHRED);
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

        // check if signal if held, forcing a red appearance by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            log.debug("Change appearance of {} to:{}", name, appearance);
        }
    }

    private void doTrailingDiverging() {
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
        // if restricting, limit to flashing red
        if (restrictingSpeed2) {
            appearance = slowerOf(appearance, SignalHead.FLASHRED);
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

        // check if signal if held, forcing a red appearance by this calculation
        if (((SignalHead) outputs[0]).getHeld()) {
            appearance = SignalHead.RED;
        }

        // handle approach lighting
        doApproach();

        // show result if changed
        if (appearance != oldAppearance) {
            ((SignalHead) outputs[0]).setAppearance(appearance);
            if (log.isDebugEnabled()) {
                log.debug("Change appearance of {} to: {}", name, appearance);
            }
        }
    }

    private void doFacing() {
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
        // if restricting, limit to flashing red
        if (watchTurnout != null && restrictingSpeed1 && watchTurnout.getBean().getKnownState() != Turnout.THROWN) {
            appearance = slowerOf(appearance, SignalHead.FLASHRED);
        }
        if (watchTurnout != null && restrictingSpeed2 && watchTurnout.getBean().getKnownState() != Turnout.CLOSED) {
            appearance = slowerOf(appearance, SignalHead.FLASHRED);
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
                && (watchedSensor1 != null && watchedSensor1.getBean().getKnownState() != Sensor.INACTIVE)) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.CLOSED) &&
                (watchedSensor1Alt != null && watchedSensor1Alt.getBean().getKnownState() != Sensor.INACTIVE)) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.THROWN) &&
                (watchedSensor2 != null && watchedSensor2.getBean().getKnownState() != Sensor.INACTIVE)) {
            appearance = SignalHead.RED;
        }
        if ((watchTurnout != null && watchTurnout.getBean().getKnownState() == Turnout.THROWN) &&
                (watchedSensor2Alt != null && watchedSensor2Alt.getBean().getKnownState() != Sensor.INACTIVE)) {
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

        // check if signal if held, forcing a red appearance by this calculation
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
     * Handle the approach lighting logic for all modes.
     */
    private void doApproach() {
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
    }

    /**
     * @return an enumeration of the collection of BlockBossLogic objects.
     * @deprecated Since 4.21.1 use {@link BlockBossLogicProvider#provideAll()} instead.
     */
    @Deprecated
    public static Enumeration<BlockBossLogic> entries() {
        return Collections.enumeration(InstanceManager.getDefault(BlockBossLogicProvider.class).provideAll());
    }

    /**
     * Ensure that this BlockBossLogic object is available for later retrieval.
     * @deprecated Since 4.21.1 use {@link BlockBossLogicProvider#register(BlockBossLogic)} instead.
     */
    @Deprecated
    public void retain() {
        InstanceManager.getDefault(BlockBossLogicProvider.class).register(this);
    }

    /**
     * Get the BlockBossLogic item governing a specific signal head by its name,
     * having removed it from use.
     *
     * @param signal name of the signal head object
     * @return never null
     */
    @Nonnull
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
                        justification="enforced dynamically, too hard to prove statically")
    public static BlockBossLogic getStoppedObject(String signal) {
        // As a static requirement, the signal head must exist, but
        // we can't express that statically.  We test it dynamically.
        SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signal);
        java.util.Objects.requireNonNull(sh, "signal head must exist");
        return getStoppedObject(sh);
    }

    /**
     * Get the BlockBossLogic item governing a specific signal head, having
     * removed it from use.
     *
     * @param sh signal head object
     * @return never null
     */
    @Nonnull
    public static BlockBossLogic getStoppedObject(@Nonnull SignalHead sh) {
        BlockBossLogic b = InstanceManager.getDefault(BlockBossLogicProvider.class).provide(sh);
        b.stop();
        return b;
    }

    /**
     * Get the BlockBossLogic item governing a specific signal head located from its name.
     * <p>
     * Unlike {@link BlockBossLogic#getStoppedObject(String signal)} this does
     * not remove the object from being used.
     *
     * @param signal SignalHead system or user name
     * @return never null - creates new object if none exists
     * @deprecated Since 4.21.1 use {@link BlockBossLogicProvider#provide(String)} instead.
     */
    @Nonnull
    @Deprecated
    public static BlockBossLogic getExisting(@Nonnull String signal) {
        return InstanceManager.getDefault(BlockBossLogicProvider.class).provide(signal);
    }

    /**
     * Get the BlockBossLogic item governing a specific signal head object.
     * <p>
     * Unlike {@link BlockBossLogic#getStoppedObject(String signal)} this does
     * not remove the object from being used.
     *
     * @param sh Existing SignalHead object
     * @return never null - creates new object if none exists
     * @deprecated Since 4.21.1 use {@link BlockBossLogicProvider#provide(SignalHead)} instead.
     */
    @Nonnull
    @Deprecated
    public static BlockBossLogic getExisting(@Nonnull SignalHead sh) {
        return InstanceManager.getDefault(BlockBossLogicProvider.class).provide(sh);
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            processCanDelete(evt, nb);
        } else if ("DoDelete".equals(evt.getPropertyName())) { // NOI18N
            processDoDelete(nb);
        }
    }

    private void processDoDelete(NamedBean nb) {
        if (nb instanceof SignalHead) {
            deleteSignalHead(nb);
        } else if (nb instanceof Turnout) {
            deleteTurnout(nb);
        } else if (nb instanceof Sensor) {
            deleteSensor(nb);
        }
    }

    private void deleteSensor(NamedBean nb) {
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

    private void deleteTurnout(NamedBean nb) {
        if (watchTurnout != null && watchTurnout.getBean().equals(nb)) {
            stop();
            setTurnout(null);
            start();
        }
    }

    private void deleteSignalHead(NamedBean nb) {
        if (nb.equals(getDrivenSignalNamedBean().getBean())) {
            stop();

            InstanceManager.getDefault(BlockBossLogicProvider.class).remove(this);
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
    }

    private void processCanDelete(PropertyChangeEvent evt, NamedBean nb) throws java.beans.PropertyVetoException {
        log.debug("name: {} got {} from {}", name, evt, evt.getSource());

        StringBuilder message = new StringBuilder();
        message.append(Bundle.getMessage("InUseBlockBossHeader", getDrivenSignal()));

        boolean found = false;

        if (nb instanceof SignalHead) {
            found = canDeleteSignalHead(evt, nb, message, found);
        } else if (nb instanceof Turnout) {
            found = canDeleteTurnout(nb, message, found);
        } else if (nb instanceof Sensor) {
            found = canDeleteSensor(nb, message, found);
        }
        if (found) {
            message.append(Bundle.getMessage("InUseBlockBossFooter")); // NOI18N
            throw new java.beans.PropertyVetoException(message.toString(), evt);
        }
    }

    private boolean canDeleteSensor(NamedBean nb, StringBuilder message, boolean found) {
        message.append("<ul>");
        if ((watchSensor1 != null && watchSensor1.getBean().equals(nb))
                || (watchSensor2 != null && watchSensor2.getBean().equals(nb))
                || (watchSensor3 != null && watchSensor3.getBean().equals(nb))
                || (watchSensor4 != null && watchSensor4.getBean().equals(nb))
                || (watchSensor5 != null && watchSensor5.getBean().equals(nb))) {
            addMessageToHtmlList(message, "<li>", "InUseWatchedSensor", "</li>");
            found = true;
        }
        if ((watchedSensor1 != null && watchedSensor1.getBean().equals(nb))
                || (watchedSensor2 != null && watchedSensor2.getBean().equals(nb))
                || (watchedSensor1Alt != null && watchedSensor1Alt.getBean().equals(nb))
                || (watchedSensor2Alt != null && watchedSensor2Alt.getBean().equals(nb))) {
            addMessageToHtmlList(message, "<li>", "InUseWatchedSensor", "</li>");
            found = true;

        }
        if (approachSensor1 != null && approachSensor1.getBean().equals(nb)) {
            found = true;
            addMessageToHtmlList(message, "<li>", "InUseApproachSensor", "</li>");
        }

        message.append("</ul>");
        return found;
    }

    private boolean canDeleteTurnout(NamedBean nb, StringBuilder message, boolean found) {
        if (watchTurnout != null && watchTurnout.getBean().equals(nb)) {
            found = true;
            addMessageToHtmlList(message, "<ul>", "InUseWatchedTurnout", "</ul>");
        }
        return found;
    }

    private boolean canDeleteSignalHead(PropertyChangeEvent evt, NamedBean nb, StringBuilder message, boolean found) throws java.beans.PropertyVetoException {
        if (nb.equals(getDrivenSignalNamedBean().getBean())) {
            message.append("<br><b>").append(Bundle.getMessage("InUseThisSslWillBeDeleted")).append("</b>");
            throw new java.beans.PropertyVetoException(message.toString(), evt);
        }
        if ((watchedSignal1 != null && watchedSignal1.getBean().equals(nb))
                || (watchedSignal1Alt != null && watchedSignal1Alt.getBean().equals(nb))
                || (watchedSignal2 != null && watchedSignal2.getBean().equals(nb))
                || (watchedSignal2Alt != null && watchedSignal2Alt.getBean().equals(nb))) {
            addMessageToHtmlList(message, "<ul>", "InUseWatchedSignal", "</ul>");
            found = true;
        }
        return found;
    }

    private void addMessageToHtmlList(StringBuilder message, String s, String inUseWatchedSignal, String s2) {
        message.append(s);
        message.append(Bundle.getMessage(inUseWatchedSignal));
        message.append(s2);
    }

    /**
     * Stop() all existing objects and clear the list.
     * <p>
     * Intended to be only used during testing.
     * @deprecated Since 4.21.1 use {@link BlockBossLogicProvider#dispose()} instead.
     */
    @Deprecated
    public static void stopAllAndClear() {
        InstanceManager.getDefault(BlockBossLogicProvider.class).dispose();
    }

    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        SignalHead head = driveSignal.getBean();
        if (bean != null) {
            if (watchSensor1 != null && bean.equals(getDrivenSignalNamedBean().getBean())) {
                report.add(new NamedBeanUsageReport("SSLSignal", head));  // NOI18N
            }
            if (watchSensor1 != null && bean.equals(watchSensor1.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensor1", head));  // NOI18N
            }
            if (watchSensor2 != null && bean.equals(watchSensor2.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensor2", head));  // NOI18N
            }
            if (watchSensor3 != null && bean.equals(watchSensor3.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensor3", head));  // NOI18N
            }
            if (watchSensor4 != null && bean.equals(watchSensor4.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensor4", head));  // NOI18N
            }
            if (watchSensor5 != null && bean.equals(watchSensor5.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensor5", head));  // NOI18N
            }
            if (watchTurnout != null && bean.equals(watchTurnout.getBean())) {
                report.add(new NamedBeanUsageReport("SSLTurnout", head));  // NOI18N
            }
            if (watchedSignal1 != null && bean.equals(watchedSignal1.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSignal1", head));  // NOI18N
            }
            if (watchedSignal1Alt != null && bean.equals(watchedSignal1Alt.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSignal1Alt", head));  // NOI18N
            }
            if (watchedSignal2 != null && bean.equals(watchedSignal2.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSignal2", head));  // NOI18N
            }
            if (watchedSignal2Alt != null && bean.equals(watchedSignal2Alt.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSignal2Alt", head));  // NOI18N
            }
            if (watchedSensor1 != null && bean.equals(watchedSensor1.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensorWatched1", head));  // NOI18N
            }
            if (watchedSensor1Alt != null && bean.equals(watchedSensor1Alt.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensorWatched1Alt", head));  // NOI18N
            }
            if (watchedSensor2 != null && bean.equals(watchedSensor2.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensorWatched2", head));  // NOI18N
            }
            if (watchedSensor2Alt != null && bean.equals(watchedSensor2Alt.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensorWatched2Alt", head));  // NOI18N
            }
            if (approachSensor1 != null && bean.equals(approachSensor1.getBean())) {
                report.add(new NamedBeanUsageReport("SSLSensorApproach", head));  // NOI18N
            }
        }
        return report;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockBossLogic.class);

}

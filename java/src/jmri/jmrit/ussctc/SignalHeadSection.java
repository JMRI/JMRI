package jmri.jmrit.ussctc;

import java.beans.*;
import java.util.*;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.*;
import jmri.util.*;

/**
 * Drive a signal section on a USS CTC panel.
 * Implements {@link Section} for both the field and CTC machine parts.
 * <p>
 * Based on the Signal interface.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017, 2021
 * TODO: Update state diagram
 */
public class SignalHeadSection implements Section<CodeGroupThreeBits, CodeGroupThreeBits> {

    /**
     *  Anonymous object only for testing
     */
    SignalHeadSection() {
        this.station = new Station("1", null, new CodeButton("IS1","IT1"));
    }

    static final int DEFAULT_RUN_TIME_LENGTH = 30000;

    /**
     * Create and configure.
     *
     * Accepts user or system names.
     *
     * @param rightHeads  Set of Signals to release when rightward travel allowed
     * @param leftHeads  Set of Signals to release when leftward travel allowed
     * @param leftIndicator  Turnout name for leftward indicator
     * @param stopIndicator  Turnout name for stop indicator
     * @param rightIndicator  Turnout name for rightward indicator
     * @param leftInput Sensor name for rightward side of lever on panel
     * @param rightInput Sensor name for leftward side of lever on panel
     * @param station Station to which this Section belongs
     */
    public SignalHeadSection(List<String> rightHeads, List<String> leftHeads,
                             String leftIndicator, String stopIndicator, String rightIndicator,
                             String leftInput, String rightInput,
                             Station station) {

        this.station = station;

        timeMemory = InstanceManager.getDefault(MemoryManager.class).getMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"TIME"); // NOI18N
        if (timeMemory == null) {
            timeMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"TIME"); // NOI18N
            timeMemory.setValue(Integer.valueOf(DEFAULT_RUN_TIME_LENGTH));
        }

        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);

        hRightHeads = new ArrayDeque<>();
        for (String s : rightHeads) {
            SignalHead sh = shm.getSignalHead(s);
            if (sh != null) {
                hRightHeads.add(hm.getNamedBeanHandle(s,sh));
            } else {
                log.debug("Signal {} for SignalHeadSection wasn't found", s); // NOI18N
            }
        }

        hLeftHeads = new ArrayDeque<>();
        for (String s : leftHeads) {
            SignalHead sh = shm.getSignalHead(s);
            if (sh != null) {
                hLeftHeads.add(hm.getNamedBeanHandle(s,sh));
            } else {
                log.debug("Signal {} for SignalHeadSection wasn't found", s); // NOI18N
            }
        }

        timeLogSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("IS"+Constants.commonNamePrefix
                                            +"SIGNALSECTION:"+station.getName()+":RUNNINGTIME"
                                            +Constants.commonNameSuffix);
        timeLogSensor.setCommandedState(Sensor.INACTIVE);

        hLeftIndicator = hm.getNamedBeanHandle(leftIndicator, tm.provideTurnout(leftIndicator));
        hStopIndicator = hm.getNamedBeanHandle(stopIndicator, tm.provideTurnout(stopIndicator));
        hRightIndicator = hm.getNamedBeanHandle(rightIndicator, tm.provideTurnout(rightIndicator));

        hLeftInput = hm.getNamedBeanHandle(leftInput, sm.provideSensor(leftInput));
        hRightInput = hm.getNamedBeanHandle(rightInput, sm.provideSensor(rightInput));

        // initialize lamps to follow layout state to STOP
        tm.provideTurnout(leftIndicator).setCommandedState(Turnout.CLOSED);
        tm.provideTurnout(stopIndicator).setCommandedState(Turnout.THROWN);
        tm.provideTurnout(rightIndicator).setCommandedState(Turnout.CLOSED);
        // hold everything
        setListHeldState(hRightHeads, true);
        setListHeldState(hLeftHeads, true);

        // add listeners
        for (NamedBeanHandle<Signal> b : hRightHeads) {
            b.getBean().addPropertyChangeListener(
                (java.beans.PropertyChangeEvent e) -> {
                    jmri.util.ThreadingUtil.runOnLayoutEventually( ()->{
                        layoutSignalHeadChanged(e);
                    });
                }
            );
        }
        for (NamedBeanHandle<Signal> b : hLeftHeads) {
            b.getBean().addPropertyChangeListener(
                (java.beans.PropertyChangeEvent e) -> {
                    jmri.util.ThreadingUtil.runOnLayoutEventually( ()->{
                        layoutSignalHeadChanged(e);
                    });
                }
            );
        }
    }

    Memory timeMemory = null;

    Sensor timeLogSensor;

    ArrayDeque<NamedBeanHandle<Signal>> hRightHeads;
    ArrayDeque<NamedBeanHandle<Signal>> hLeftHeads;

    NamedBeanHandle<Turnout> hLeftIndicator;
    NamedBeanHandle<Turnout> hStopIndicator;
    NamedBeanHandle<Turnout> hRightIndicator;

    NamedBeanHandle<Sensor> hLeftInput;
    NamedBeanHandle<Sensor> hRightInput;

    // coding used locally to ensure consistency
    public static final CodeGroupThreeBits CODE_LEFT = CodeGroupThreeBits.Triple100;
    public static final CodeGroupThreeBits CODE_STOP = CodeGroupThreeBits.Triple010;
    public static final CodeGroupThreeBits CODE_RIGHT = CodeGroupThreeBits.Triple001;
    public static final CodeGroupThreeBits CODE_OFF = CodeGroupThreeBits.Triple000;

    // States to track changes at the Code Machine end
    enum Machine {
        SET_LEFT,
        SET_STOP,
        SET_RIGHT
    }
    Machine machine = Machine.SET_STOP;

    CodeGroupThreeBits lastIndication = CODE_STOP;
    void setLastIndication(CodeGroupThreeBits v) {
        log.trace("lastIndication goes from {} to {}", lastIndication, v);
        CodeGroupThreeBits old = lastIndication;
        lastIndication = v;
        firePropertyChange("LastIndication", old, lastIndication); // NOI18N
    }
    CodeGroupThreeBits getLastIndication() { return lastIndication; }

    boolean timeRunning = false;

    public boolean isRunningTime() { return timeRunning; }

    Station station;
    @Override
    public Station getStation() { return station;}
    @Override
    public String getName() { return "SH for "+hStopIndicator.getBean().getDisplayName(); }

    List<Lock> rightwardLocks;
    List<Lock> leftwardLocks;
    public void addRightwardLocks(List<Lock> locks) { this.rightwardLocks = locks; }
    public void addLeftwardLocks(List<Lock> locks) { this.leftwardLocks = locks; }

    /**
     * Start of sending code operation:
     * <ul>
     * <li>Set indicators off if a change has been requested
     * <li>Provide values to send over line
     * </ul>
     * @return code line value to transmit from machine to field
     */
    @Override
    public CodeGroupThreeBits codeSendStart() {
        // are we setting to stop, which might start running time?
        // check for setting to stop while machine has been cleared to left or right
        if (    (hRightInput.getBean().getKnownState()==Sensor.ACTIVE &&
                    hLeftIndicator.getBean().getKnownState() == Turnout.THROWN )
             ||
                (hLeftInput.getBean().getKnownState()==Sensor.ACTIVE &&
                    hRightIndicator.getBean().getKnownState() == Turnout.THROWN )
             ||
                (hLeftInput.getBean().getKnownState()!=Sensor.ACTIVE && hRightInput.getBean().getKnownState()!=Sensor.ACTIVE &&
                    ( hRightIndicator.getBean().getKnownState() == Turnout.THROWN || hLeftIndicator.getBean().getKnownState() == Turnout.THROWN) )
            ) {

            // setting to stop, have to start running time
            startRunningTime();
        }

        // Set the indicators based on current and requested state
        if ( !timeRunning && (
                  ( machine==Machine.SET_LEFT && hLeftInput.getBean().getKnownState()==Sensor.ACTIVE)
                || ( machine==Machine.SET_RIGHT && hRightInput.getBean().getKnownState()==Sensor.ACTIVE)
                || ( machine==Machine.SET_STOP && hRightInput.getBean().getKnownState()!=Sensor.ACTIVE && hLeftInput.getBean().getKnownState()!=Sensor.ACTIVE) )
                ) {
            log.debug("No signal change required, states aligned"); // NOI18N
        } else {
            log.debug("Signal change requested"); // NOI18N
            // have to turn off
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        }

        // return the settings to send
        CodeGroupThreeBits retval;
        if (timeRunning) {
            machine = Machine.SET_STOP;
            retval = CODE_STOP;
        } else if (hLeftInput.getBean().getKnownState()==Sensor.ACTIVE) {
            machine = Machine.SET_LEFT;
            retval = CODE_LEFT;
        } else if (hRightInput.getBean().getKnownState()==Sensor.ACTIVE) {
            machine = Machine.SET_RIGHT;
            retval = CODE_RIGHT;
        } else {
            machine = Machine.SET_STOP;
            retval = CODE_STOP;
        }
        log.debug("codeSendStart returns {}", retval);

        // A model thought -  if setting stop, hold signals immediately
        // instead of waiting for code cycle.  Model railroads move fast...
        //if (retval == CODE_STOP) {
        //    setListHeldState(hRightHeads, true);
        //    setListHeldState(hLeftHeads, true);
        //}

        return retval;
    }

    void startRunningTime() {
            if (timeRunning) {
                log.error("Attempt to start running time while it is already running",
                        LoggingUtil.shortenStacktrace(new Exception("traceback")));
                return;
            }
            timeRunning = true;
            timeLogSensor.setCommandedState(Sensor.ACTIVE);
            jmri.util.ThreadingUtil.runOnLayoutDelayed(  ()->{
                    log.debug("End running time: Station {}", station.getName()); // NOI18N
                    Lock.signalLockLogger.setStatus(this, "End running time: Station "+station.getName());
                    timeLogSensor.setCommandedState(Sensor.INACTIVE);
                    if (!timeRunning) log.warn("Running time timer ended while not marked as running time");
                    timeRunning = false;
                    station.requestIndicationStart();
                } ,
                (int)timeMemory.getValue());

           Lock.signalLockLogger.setStatus(this, "Running time: Station "+station.getName());
    }

    public static int MOVEMENT_DELAY = 5000;

    boolean deferIndication = false; // when set, don't indicate on layout change
                                     // because something else will ensure it later

    /**
     * Code arrives in field. Sets the signals on the layout.
     */
    @Override
    public void codeValueDelivered(CodeGroupThreeBits value) {
        // @TODO add lock checking here; this is part of vital logic implementation

        // Set signals. While doing that, remember command as indication, so that the
        // following signal change won't drive an _immediate_ indication cycle.
        // Also, always go via stop...
        CodeGroupThreeBits  currentIndication = getCurrentIndication();
        log.debug("codeValueDelivered sets value {} current: {} last: {}", value, currentIndication, lastIndication);

        if (value == CODE_LEFT && Lock.checkLocksClear(leftwardLocks, Lock.signalLockLogger)) {
            // setLastIndication(CODE_STOP);
            // setListHeldState(hRightHeads, true);
            // setListHeldState(hLeftHeads, true);
            setLastIndication(CODE_LEFT);
            log.debug("Layout signals set LEFT"); // NOI18N
            setListHeldState(hLeftHeads, false);
            setListHeldState(hRightHeads, true);
        } else if (value == CODE_RIGHT && Lock.checkLocksClear(rightwardLocks, Lock.signalLockLogger)) {
            // lastIndication = CODE_STOP;
            // setListHeldState(hRightHeads, true);
            // setListHeldState(hLeftHeads, true);
            setLastIndication(CODE_RIGHT);
            log.debug("Layout signals set RIGHT"); // NOI18N
            setListHeldState(hRightHeads, false);
            setListHeldState(hLeftHeads, true);
        } else if (value == CODE_STOP) {
            setLastIndication(CODE_STOP);
            log.debug("Layout signals set STOP"); // NOI18N
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        } else {
            // RIGHT or LEFT but locks not clear
            Lock.signalLockLogger.setStatus(this,
                "Force stop: left clear "+Lock.checkLocksClear(leftwardLocks, Lock.signalLockLogger)
                 +", right clear "+Lock.checkLocksClear(rightwardLocks, Lock.signalLockLogger));
            setLastIndication(CODE_STOP);
            log.debug("Layout signals set STOP due to locks"); // NOI18N
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }

        // start the timer for the signals to change
        if (currentIndication != lastIndication) {
            log.debug("codeValueDelivered started timer for return indication"); // NOI18N
            jmri.util.TimerUtil.schedule(new TimerTask() { // NOI18N
                @Override
                public void run() {
                    jmri.util.ThreadingUtil.runOnLayout( ()->{
                        log.debug("end of movement delay from codeValueDelivered");
                        station.requestIndicationStart();
                    } );
                }
            }, MOVEMENT_DELAY);
        }
        log.debug("End codeValueDelivered");
    }

    protected void setListHeldState(Iterable<NamedBeanHandle<Signal>> list, boolean state) {
        for (NamedBeanHandle<Signal> handle : list) {
            if (handle.getBean().getHeld() != state) handle.getBean().setHeld(state);
        }
    }

    @Override
    public String toString() {
        StringBuffer retVal = new StringBuffer("SignalHeadSection "); // NOI18N

        retVal.append(" state: "+machine); // NOI18N
        retVal.append(" time: "+isRunningTime()); // NOI18N
        retVal.append(" defer: "+deferIndication); // NOI18N

        for (NamedBeanHandle<Signal> handle : hRightHeads) {
            retVal.append("\n  \"").append(handle.getName()).append("\" "); // NOI18N
            retVal.append(" held: "+handle.getBean().getHeld()+" "); // NOI18N
            retVal.append(" clear: "+handle.getBean().isCleared()+" "); // NOI18N
            retVal.append(" stop: "+handle.getBean().isAtStop()); // NOI18N
        }
        for (NamedBeanHandle<Signal> handle : hLeftHeads) {
            retVal.append("\n  \"").append(handle.getName()).append("\" "); // NOI18N
            retVal.append(" held: "+handle.getBean().getHeld()+" "); // NOI18N
            retVal.append(" clear: "+handle.getBean().isCleared()+" "); // NOI18N
            retVal.append(" stop: "+handle.getBean().isAtStop()); // NOI18N
        }

        return retVal.toString();
    }

    /**
     * Provide state that's returned from field to machine via indication.
     */
    @Override
    public CodeGroupThreeBits indicationStart() {
        // check for signal drop unexpectedly, other automatic clears
        // is done in getCurrentIndication()
        CodeGroupThreeBits retval = getCurrentIndication();
        log.debug("indicationStart with {}; last indication was {}", retval, lastIndication); // NOI18N

        // TODO: anti-fleeting done always, need call-on logic


        setLastIndication(retval);
        return retval;
    }

    /**
     * Clear is defined as showing above Restricting.
     * We implement that as not Held, not RED, not Restricting.
     * @param handle signal bean handle.
     * @return true if clear.
     */
    public boolean headShowsClear(NamedBeanHandle<Signal> handle) {
        return !handle.getBean().getHeld() && handle.getBean().isCleared();
        }

    /**
     * "Restricting" means that a signal is showing FLASHRED
     * @param handle signal bean handle.
     * @return true if showing restricting.
     */
    public boolean headShowsRestricting(NamedBeanHandle<Signal> handle) {
        return handle.getBean().isShowingRestricting();
    }

    /**
     * Work out current indication from layout status.
     * @return code group.
     */
    public CodeGroupThreeBits getCurrentIndication() {
        log.trace("Start getCurrentIndication with lastIndication {}", lastIndication, LoggingUtil.shortenStacktrace(new Exception("traceback")));
        boolean leftClear = false;
        boolean leftHeld = false;
        boolean leftRestricting = false;
        for (NamedBeanHandle<Signal> handle : hLeftHeads) {
            if (headShowsClear(handle)) leftClear = true;
            if (handle.getBean().getHeld()) leftHeld = true;
            if (headShowsRestricting(handle)) leftRestricting = true;
        }
        boolean rightClear = false;
        boolean rightHeld = false;
        boolean rightRestricting = false;
        for (NamedBeanHandle<Signal> handle : hRightHeads) {
            if (headShowsClear(handle)) rightClear = true;
            if (handle.getBean().getHeld()) rightHeld = true;
            if (headShowsRestricting(handle)) rightRestricting = true;
        }
        log.trace(" found  leftClear {},  leftHeld {},  leftRestricting {}, lastIndication {}", leftClear, leftHeld, leftRestricting, lastIndication); // NOI18N
        log.trace("       rightClear {}, rightHeld {}, rightRestricting {}", rightClear, rightHeld, rightRestricting); // NOI18N
        if (leftClear && rightClear) log.error("Found both left and right clear: {}", this); // NOI18N
        if (leftClear && rightRestricting) log.warn("Found left clear and right at restricting: {}", this); // NOI18N
        if (leftRestricting && rightClear) log.warn("Found left at restricting and right clear: {}", this); // NOI18N


        CodeGroupThreeBits retval;

        // Restricting cases show OFF
        if (leftRestricting || rightRestricting) {
            Lock.signalLockLogger.setStatus(this, "Force off due to restricting");
            retval = CODE_OFF;
        } else if ((!leftClear) && (!rightClear)) {
            // check for a signal dropping while cleared
            if (lastIndication != CODE_STOP) {
                log.debug("CurrentIndication stop due to right and left not clear with "+lastIndication);
                Lock.signalLockLogger.setStatus(this, "Show stop due to right and left not clear");
            } else {
                Lock.signalLockLogger.clear();
            }
            retval = CODE_STOP;
        } else if ((!leftClear) && rightClear && (lastIndication == CODE_RIGHT  )) {
            Lock.signalLockLogger.clear();
            retval = CODE_RIGHT;
        } else if (leftClear && (!rightClear) && (lastIndication == CODE_LEFT)) {
            Lock.signalLockLogger.clear();
            retval = CODE_LEFT;
        } else {
            log.debug("Not individually cleared, set OFF");
            if (!rightClear) Lock.signalLockLogger.setStatus(this, "Force stop due to right not clear");
            else if (!leftClear) Lock.signalLockLogger.setStatus(this, "Force stop due to left not clear");
            else Lock.signalLockLogger.setStatus(this, "Force stop due to vital settings");
            retval = CODE_OFF;
        }
        log.trace("End getCurrentIndication returns {}", retval);
        return retval;
    }

    /**
     * Process values received from the field unit.
     */
    @Override
    public void indicationComplete(CodeGroupThreeBits value) {
        log.debug("indicationComplete sets from {} in state {}", value, machine); // NOI18N
        if (timeRunning) {
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        } else switch (value) {
            case Triple100: // CODE_LEFT
                hLeftIndicator.getBean().setCommandedState(Turnout.THROWN);
                hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
                break;
            case Triple010: // CODE_STOP
                hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hStopIndicator.getBean().setCommandedState(Turnout.THROWN);
                hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
                break;
            case Triple001: // CODE_RIGHT
                hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hRightIndicator.getBean().setCommandedState(Turnout.THROWN);
                break;
            case Triple000: // CODE_OFF
                hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED); // all off
                hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
                break;
            default:
                log.error("Got code not recognized: {}", value);
                hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
                hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
                break;
        }
    }

    void layoutSignalHeadChanged(java.beans.PropertyChangeEvent e) {
        CodeGroupThreeBits current = getCurrentIndication();
        // as a modeling thought, if we're dropping to stop, set held right now
        log.debug("layoutSignalHeadChanged with last: {} current: {} defer: {}, driving update", lastIndication, current, deferIndication);
        if (current == CODE_STOP && current != lastIndication && ! deferIndication ) {
            deferIndication = true;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            deferIndication = false;
        }

        // if there was a change, need to send indication back to central
        if (current != lastIndication && ! deferIndication) {
            log.debug("  SignalHead change sends changed Indication last: {} current: {} defer: {}, driving update", lastIndication, current, deferIndication);
            station.requestIndicationStart();
        } else {
            log.debug("  SignalHead change without change in Indication");
        }
        log.debug("end of layoutSignalHeadChanged");
    }

    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadSection.class);
}

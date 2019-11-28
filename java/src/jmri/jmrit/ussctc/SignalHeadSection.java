package jmri.jmrit.ussctc;

import java.beans.*;
import java.util.*;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.*;

/**
 * Drive a signal section on a USS CTC panel.
 * Implements {@link Section} for both the field and CTC machine parts.
 * <p>
 * Based on the Signal interface.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 * TODO: Update state diagram
 */
public class SignalHeadSection implements Section<CodeGroupThreeBits, CodeGroupThreeBits> {

    /**
     *  Anonymous object only for testing
     */
    SignalHeadSection() {}
    
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

        logMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"LOG"); // NOI18N
        log.debug("log memory name is {}", logMemory.getSystemName());
        
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
                (java.beans.PropertyChangeEvent e) -> {layoutSignalHeadChanged(e);}
            );
        }
        for (NamedBeanHandle<Signal> b : hLeftHeads) {
            b.getBean().addPropertyChangeListener(
                (java.beans.PropertyChangeEvent e) -> {layoutSignalHeadChanged(e);}
            );
        }
    }
    
    Memory timeMemory = null;
    Memory logMemory = null;
    
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
    Machine machine;

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
            timeRunning = true;
            jmri.util.ThreadingUtil.runOnLayoutDelayed(  ()->{ 
                    log.debug("End running time"); // NOI18N
                    logMemory.setValue("");
                    timeRunning = false;
                    station.requestIndicationStart();
                } ,
                (int)timeMemory.getValue());
            
            log.debug("starting to run time");
            logMemory.setValue("Running time: Station "+station.getName());
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
        if (retval == CODE_STOP) {
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }
        
        return retval;
    }

    public static int MOVEMENT_DELAY = 5000;

    boolean deferIndication = false; // when set, don't indicate on layout change
                                     // because something else will ensure it later

    /**
     * Code arrives in field. Sets the signals on the layout.
     */
    @Override
    public void codeValueDelivered(CodeGroupThreeBits value) {
        log.debug("codeValueDelivered sets value {}", value);
        // @TODO add lock checking here; this is part of vital logic implementation
        
        // Set signals. While doing that, remember command as indication, so that the
        // following signal change won't drive an _immediate_ indication cycle.
        // Also, always go via stop...
        CodeGroupThreeBits  currentIndication = getCurrentIndication();
        if (value == CODE_LEFT && Lock.checkLocksClear(leftwardLocks)) {
            lastIndication = CODE_STOP;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            lastIndication = CODE_LEFT;
            log.debug("Layout signals set LEFT"); // NOI18N
            setListHeldState(hLeftHeads, false);
        } else if (value == CODE_RIGHT && Lock.checkLocksClear(rightwardLocks)) {
            lastIndication = CODE_STOP;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            lastIndication = CODE_RIGHT;
            log.debug("Layout signals set RIGHT"); // NOI18N
            setListHeldState(hRightHeads, false);
        } else {
            lastIndication = CODE_STOP;
            log.debug("Layout signals set STOP"); // NOI18N
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }
        
        // start the timer for the signals to change
        if (currentIndication != lastIndication) {
            log.debug("codeValueDelivered started timer for return indication"); // NOI18N
            jmri.util.TimerUtil.schedule(new TimerTask() { // NOI18N
                @Override
                public void run() {
                    jmri.util.ThreadingUtil.runOnGUI( ()->{
                        log.debug("end of movement delay from codeValueDelivered");
                        station.requestIndicationStart();
                    } );
                }
            }, MOVEMENT_DELAY);
        }
    }

    protected void setListHeldState(Iterable<NamedBeanHandle<Signal>> list, boolean state) {
        for (NamedBeanHandle<Signal> handle : list) {
            if (handle.getBean().getHeld() != state) handle.getBean().setHeld(state);
        }
    }
    
    @Override
    public String toString() {
        StringBuffer retVal = new StringBuffer("SignalHeadSection ["); // NOI18N
        boolean first;
        first = true;
        for (NamedBeanHandle<Signal> handle : hRightHeads) {
            if (!first) retVal.append(", "); // NOI18N
            first = false;
            retVal.append("\"").append(handle.getName()).append("\""); // NOI18N
        }
        retVal.append("],["); // NOI18N
        first = true;
        for (NamedBeanHandle<Signal> handle : hLeftHeads) {
            if (!first) retVal.append(", "); // NOI18N
            first = false;
            retVal.append("\"").append(handle.getName()).append("\""); // NOI18N
        }        
        retVal.append("]"); // NOI18N
        return retVal.toString();
    }
    
    /**
     * Provide state that's returned from field to machine via indication.
     */
    @Override
    public CodeGroupThreeBits indicationStart() {
        CodeGroupThreeBits retval = getCurrentIndication();
        log.debug("indicationStart with {}; last indication was {}", retval, lastIndication); // NOI18N
        
        // TODO: anti-fleeting done always, need call-on logic
        
        // set Held right away
        if (retval == CODE_STOP && lastIndication != CODE_STOP) {
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }
        
        lastIndication = retval;
        return retval;
    }
    
    /**
     * Clear is defined as showing above Restricting.
     * We implement that as not Held, not RED, not Restricting.
     */
    public boolean headShowsClear(NamedBeanHandle<Signal> handle) { 
        return !handle.getBean().getHeld() && handle.getBean().isCleared();
        }
    
    /**
     * "Restricting" means that a signal is showing FLASHRED
     */
    public boolean headShowsRestricting(NamedBeanHandle<Signal> handle) { 
        return handle.getBean().isShowingRestricting();
    }
    
    /**
     * Work out current indication from layout status
     */
    public CodeGroupThreeBits getCurrentIndication() {
        boolean leftClear = false;
        boolean leftRestricting = false;
        for (NamedBeanHandle<Signal> handle : hLeftHeads) {
            if (headShowsClear(handle)) leftClear = true;
            if (headShowsRestricting(handle)) leftRestricting = true;
        }
        boolean rightClear = false;
        boolean rightRestricting = false;
        for (NamedBeanHandle<Signal> handle : hRightHeads) {
            if (headShowsClear(handle)) rightClear = true;
            if (headShowsRestricting(handle)) rightRestricting = true;
        }
        log.debug("    found leftClear {}, leftRestricting {}, rightClear {}, rightRestricting {}", leftClear, leftRestricting, rightClear, rightRestricting); // NOI18N
        if (leftClear && rightClear) log.error("Found both left and right clear: {}", this); // NOI18N
        if (leftClear && rightRestricting) log.warn("Found left clear and right at restricting: {}", this); // NOI18N
        if (leftRestricting && rightClear) log.warn("Found left at restricting and right clear: {}", this); // NOI18N

        
        CodeGroupThreeBits retval;

        // Restricting cases show OFF
        if (leftRestricting || rightRestricting) {      
            retval = CODE_OFF;
        } else if ((!leftClear) && (!rightClear)) {
            retval = CODE_STOP;
        } else if ((!leftClear) && rightClear && (lastIndication == CODE_RIGHT  )) {
            retval = CODE_RIGHT;
        } else if (leftClear && (!rightClear) && (lastIndication == CODE_LEFT)) {
            retval = CODE_LEFT;
        } else {
            log.debug("not individually cleared, set OFF");
            retval = CODE_OFF;
        }
        return retval;
    }

    CodeGroupThreeBits lastIndication = CODE_OFF;
    void setLastIndication(CodeGroupThreeBits v) { 
        CodeGroupThreeBits old = lastIndication;
        lastIndication = v;
        firePropertyChange("LastIndication", old, lastIndication); // NOI18N
    }
    CodeGroupThreeBits getLastIndication() { return lastIndication; }

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
        if (current == CODE_STOP && current != lastIndication && ! deferIndication ) {
            deferIndication = true;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            deferIndication = false;
        }

        // if there was a change, need to send indication back to central
        if (current != lastIndication && ! deferIndication) {
            log.debug("  SignalHead change resulted in changed Indication, driving update");
            station.requestIndicationStart();
        } else {
            log.debug("  SignalHead change without change in Indication");
        }
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

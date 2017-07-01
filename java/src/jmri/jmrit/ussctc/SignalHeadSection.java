package jmri.jmrit.ussctc;

import jmri.*;
import java.util.*;

/**
 * Drive a signal section on a USS CTC panel.
 * Implements {@link Section} for both the field and CTC machine parts.
 * <p>
 * Based on SignalHead signals for now.
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
     * @param rightHeads  Set of SignalHeads to release when rightward travel allowed
     * @param leftHeads  Set of SignalHeads to release when leftward travel allowed
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
        
        logMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"LOG");
        log.debug("log memory name is {}", logMemory.getSystemName());
        
        timeMemory = InstanceManager.getDefault(MemoryManager.class).getMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"TIME");
        if (timeMemory == null) {
            timeMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(
                        Constants.commonNamePrefix+"SIGNALHEADSECTION"+Constants.commonNameSuffix+"TIME");
            timeMemory.setValue(new Integer(DEFAULT_RUN_TIME_LENGTH));
        }

        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);
        
        hRightHeads = new ArrayList<>();
        for (String s : rightHeads) hRightHeads.add(hm.getNamedBeanHandle(s, shm.getSignalHead(s)));

        hLeftHeads = new ArrayList<>();
        for (String s : leftHeads) hLeftHeads.add(hm.getNamedBeanHandle(s, shm.getSignalHead(s)));
        
        hLeftIndicator = hm.getNamedBeanHandle(leftIndicator, tm.provideTurnout(leftIndicator));
        hStopIndicator = hm.getNamedBeanHandle(stopIndicator, tm.provideTurnout(stopIndicator));
        hRightIndicator = hm.getNamedBeanHandle(rightIndicator, tm.provideTurnout(rightIndicator));

        hLeftInput = hm.getNamedBeanHandle(leftInput, sm.provideSensor(leftInput));
        hRightInput = hm.getNamedBeanHandle(rightInput, sm.provideSensor(rightInput));
        
        this.station = station;
        
        // initialize lamps to follow layout state to all off - you don't know anything
        tm.provideTurnout(leftIndicator).setCommandedState(Turnout.CLOSED);
        tm.provideTurnout(stopIndicator).setCommandedState(Turnout.CLOSED);
        tm.provideTurnout(rightIndicator).setCommandedState(Turnout.CLOSED);
        // hold everything
        setListHeldState(hRightHeads, true);
        setListHeldState(hLeftHeads, true);
        
        // add listeners
        for (String s : leftHeads) 
            shm.getSignalHead(s).addPropertyChangeListener(
                (java.beans.PropertyChangeEvent e) -> {layoutSignalHeadChanged(e);}
            );
        for (String s : rightHeads) 
            shm.getSignalHead(s).addPropertyChangeListener(
                (java.beans.PropertyChangeEvent e) -> {layoutSignalHeadChanged(e);}
            );

    }
    
    Memory timeMemory = null;
    Memory logMemory = null;
    
    ArrayList<NamedBeanHandle<SignalHead>> hRightHeads;
    ArrayList<NamedBeanHandle<SignalHead>> hLeftHeads;

    NamedBeanHandle<Turnout> hLeftIndicator;
    NamedBeanHandle<Turnout> hStopIndicator;
    NamedBeanHandle<Turnout> hRightIndicator;

    NamedBeanHandle<Sensor> hLeftInput;
    NamedBeanHandle<Sensor> hRightInput;
        
    // coding used locally to ensure consistency
    private final CodeGroupThreeBits CODE_LEFT = CodeGroupThreeBits.Triple100;
    private final CodeGroupThreeBits CODE_STOP = CodeGroupThreeBits.Triple010;
    private final CodeGroupThreeBits CODE_RIGHT = CodeGroupThreeBits.Triple001;
    private final CodeGroupThreeBits CODE_OFF = CodeGroupThreeBits.Triple000;
    
    // States to track changes at the Code Machine end
    enum Machine {
        SET_LEFT,
        SET_STOP,
        SET_RIGHT
    }
    Machine machine;

    boolean timeRunning = false;
    
    Station station;
    
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
                    log.debug("End running time");
                    logMemory.setValue("");
                    timeRunning = false;
                    station.requestIndicationStart();
                } ,
                (int)timeMemory.getValue());
            
            log.debug("starting to run time");
            logMemory.setValue("Running time");
        }
    
        // Set the indicators based on current and requested state
        if ( !timeRunning && (
                  ( machine==Machine.SET_LEFT && hLeftInput.getBean().getKnownState()==Sensor.ACTIVE)
                || ( machine==Machine.SET_RIGHT && hRightInput.getBean().getKnownState()==Sensor.ACTIVE) 
                || ( machine==Machine.SET_STOP && hRightInput.getBean().getKnownState()!=Sensor.ACTIVE && hLeftInput.getBean().getKnownState()!=Sensor.ACTIVE) )
                ) {
            log.debug("No signal change required, states aligned");
        } else {
            log.debug("Signal change requested");
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
    
    /**
     * Code arrives in field. Sets the signals on the layout.
     */
    public void codeValueDelivered(CodeGroupThreeBits value) {
        log.debug("codeValueDelivered sets value {}", value);
        // @TODO add lock checking here; this is part of vital logic implementation
        
        // Set signals. While doing that, remember command as indication, so that the
        // following signal change won't drive an _immediate_ indication cycle.
        // Also, always go via stop...
        CodeGroupThreeBits  currentIndication = getCurrentIndication();
        if (value == CODE_LEFT) {
            lastIndication = CODE_STOP;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            log.debug("Layout signals set LEFT");
            lastIndication = CODE_LEFT;
            setListHeldState(hLeftHeads, false);
        } else if (value == CODE_RIGHT) {
            lastIndication = CODE_STOP;
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
            lastIndication = CODE_RIGHT;
            log.debug("Layout signals set RIGHT");
            setListHeldState(hRightHeads, false);
        } else {
            lastIndication = CODE_STOP;
            log.debug("Layout signals set STOP");
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }
        
        // start the timer for the signals to change
        if (currentIndication != lastIndication) {
            log.debug("codeValueDelivered started timer for return indication");
            new Timer().schedule(new TimerTask() {
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

    protected void setListHeldState(ArrayList<NamedBeanHandle<SignalHead>> list, boolean state) {
        for (NamedBeanHandle<SignalHead> handle : list) {
            if (handle.getBean().getHeld() != state) handle.getBean().setHeld(state);
        }
    }
    
    
    /**
     * Provide state that's returned from field to machine via indication.
     */
    public CodeGroupThreeBits indicationStart() {
        CodeGroupThreeBits retval = getCurrentIndication();
        log.debug("indicationStart with {}; last indication was {}", retval, lastIndication);
        
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
     * Work out current indication from layout status
     */
    public CodeGroupThreeBits getCurrentIndication() {     
        boolean leftStopped = true;
        for (NamedBeanHandle<SignalHead> handle : hLeftHeads) {
            if ((!handle.getBean().getHeld()) && handle.getBean().getAppearance()!=SignalHead.RED) leftStopped = false;
        }
        boolean rightStopped = true;
        for (NamedBeanHandle<SignalHead> handle : hRightHeads) {
            if ((!handle.getBean().getHeld()) && handle.getBean().getAppearance()!=SignalHead.RED) rightStopped = false;
        }
        log.debug("    found leftStopped {}, rightStopped {}", leftStopped, rightStopped);
        if (!leftStopped && !rightStopped) log.error("Found both left and right not at stop");

        
        CodeGroupThreeBits retval;
        
        if (leftStopped && rightStopped) {
            retval = CODE_STOP;
        } else if (!rightStopped && leftStopped) {
            retval = CODE_RIGHT;
        } else if (!leftStopped && rightStopped) {
            retval = CODE_LEFT;
        } else 
            retval = CODE_OFF;
            
        return retval;
    }

    CodeGroupThreeBits lastIndication = CODE_OFF;
    
    /**
     * Process values received from the field unit.
     */
    public void indicationComplete(CodeGroupThreeBits value) {
        log.debug("indicationComplete sets from {} in state {}", value, machine);
        if (timeRunning) {
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        } else if (value == CODE_LEFT) {
            hLeftIndicator.getBean().setCommandedState(Turnout.THROWN);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        } else if (value == CODE_STOP) {
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.THROWN);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        } else if (value == CODE_RIGHT) {
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.THROWN);
        } else {
            log.error("Got code not recognized: {}", value);
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        }
    } 

    void layoutSignalHeadChanged(java.beans.PropertyChangeEvent e) {
        if (getCurrentIndication() != lastIndication) {
            log.debug("  SignalHead change resulted in changed Indication, driving update");
            station.requestIndicationStart();
        } else {
            log.debug("  SignalHead change without change in Indication");
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadSection.class.getName());
}

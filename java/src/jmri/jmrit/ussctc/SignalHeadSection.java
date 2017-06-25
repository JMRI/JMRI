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
public class SignalHeadSection implements Section {

    /**
     *  Anonymous object only for testing
     */
    SignalHeadSection() {}
    
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
     * @param codeline common CodeLine for this machine panel
     */
    public SignalHeadSection(List<String> rightHeads, List<String> leftHeads, 
                             String leftIndicator, String stopIndicator, String rightIndicator, 
                             String leftInput, String rightInput,
                             CodeLine codeline) {
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
        
        this.codeline = codeline;
        
        // initialize lamps to follow layout state to all off - you don't know anything
        //tm.provideTurnout(leftIndicator).setCommandedState(Turnout.CLOSED);
        //tm.provideTurnout(stopIndicator).setCommandedState(Turnout.CLOSED);
        //tm.provideTurnout(rightIndicator).setCommandedState(Turnout.CLOSED);
        
        // TODO - add a listener for changes?  See layoutTurnoutChanged at end
    }

    CodeLine codeline;
    Station station;
    
    public void addStation(Station station) { this.station = station; }
    
    ArrayList<NamedBeanHandle<SignalHead>> hRightHeads;
    ArrayList<NamedBeanHandle<SignalHead>> hLeftHeads;

    NamedBeanHandle<Turnout> hLeftIndicator;
    NamedBeanHandle<Turnout> hStopIndicator;
    NamedBeanHandle<Turnout> hRightIndicator;

    NamedBeanHandle<Sensor> hLeftInput;
    NamedBeanHandle<Sensor> hRightInput;
        
    // coding used locally to ensure consistency
    private final Station.Value CODE_LEFT = Station.Value.Triple100;
    private final Station.Value CODE_STOP = Station.Value.Triple010;
    private final Station.Value CODE_RIGHT = Station.Value.Triple001;
    private final Station.Value CODE_OFF = Station.Value.Triple000;
    
    // States to track changes
    enum State {
        REQUEST_LEFT,
        REQUEST_STOP,
        REQUEST_RIGHT
    }
    State state;

    /**
     * Start of sending code operation:
     * <ul>
     * <li>Set indicators
     * <li>Provide values to send over line
     * </ul>
     * @return code line value to transmit
     */
    @Override
    public Station.Value codeSendStart() {
        // Set the indicators based on current and requested state
        if (   ( state==State.REQUEST_LEFT && hLeftInput.getBean().getKnownState()==Sensor.ACTIVE)
            || ( state==State.REQUEST_RIGHT && hRightInput.getBean().getKnownState()==Sensor.ACTIVE) ) {
            log.debug("No signal change requested");
        } else {
            log.debug("Signal change requested");
            // have to turn off
            hLeftIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hStopIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hRightIndicator.getBean().setCommandedState(Turnout.CLOSED);
        }
        
        // return the settings to send
        if (hLeftInput.getBean().getKnownState()==Sensor.ACTIVE) {
            state = State.REQUEST_LEFT;
            return CODE_LEFT;
        } else if (hRightInput.getBean().getKnownState()==Sensor.ACTIVE) {
            state = State.REQUEST_RIGHT;
            return CODE_RIGHT;
        } else {
            state = State.REQUEST_STOP;
            return CODE_STOP;
        }
    }

    public static int MOVEMENT_DELAY = 5000;
    
    /**
     * Notification that code has been sent. Sets the signals on the layout.
     */
    public void codeValueDelivered(Station.Value value) {
        // @TODO add lock checking here; this is part of vital logic implementation
        
        // read and set signals
        if (value == CODE_LEFT) {
            log.debug("Layout signals set LEFT");
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, false);
        } else if (value == CODE_RIGHT) {
            log.debug("Layout signals set RIGHT");
            setListHeldState(hRightHeads, false);
            setListHeldState(hLeftHeads, true);
        } else {
            log.debug("Layout signals set STOP");
            setListHeldState(hRightHeads, true);
            setListHeldState(hLeftHeads, true);
        }
        
        // start the timer for the signals to change
        new Timer().schedule(new TimerTask() { // turn that off
            @Override
            public void run() {
                jmri.util.ThreadingUtil.runOnGUI( ()->{
                    log.debug("end of movement delay");
                    station.requestIndicationStart();
                } );
            }
        }, MOVEMENT_DELAY);
        
    }

    protected void setListHeldState(ArrayList<NamedBeanHandle<SignalHead>> list, boolean state) {
        for (NamedBeanHandle<SignalHead> handle : list)
            handle.getBean().setHeld(state);
    }
    
    /**
     * Provide state that's returned from field to machine via indication.
     */
    public Station.Value indicationStart() {
        boolean leftStopped = true;
        for (NamedBeanHandle<SignalHead> handle : hLeftHeads)
            if (handle.getBean().getAppearance()!=SignalHead.RED) leftStopped = false;

        boolean rightStopped = true;
        for (NamedBeanHandle<SignalHead> handle : hRightHeads)
            if (handle.getBean().getAppearance()!=SignalHead.RED) rightStopped = false;

        log.debug("found leftStopped {}, rightStopped {}", leftStopped, rightStopped);
        if (!leftStopped && !rightStopped) log.error("Found both left and right not at stop");

        
        if (!leftStopped) {
            return CODE_LEFT;
        } else if (!rightStopped) {
            return CODE_RIGHT;
        } else 
            return CODE_STOP;
    }

    /**
     * Process values received from the field unit.
     */
    public void indicationComplete(Station.Value value) {
        log.debug("Indication sets from {}", value, new Exception("traceback"));
        if (value == CODE_LEFT) {
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

    void layoutTurnoutChanged(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && !e.getNewValue().equals(e.getOldValue()) )
            station.requestIndicationStart();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadSection.class.getName());
}

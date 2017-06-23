package jmri.jmrit.ussctc;

import jmri.*;
import java.util.*;

/**
 * Drive a single Turnout section on a USS CTC panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TurnoutSection implements Section {

    /**
     *  Anonymous object only for testing
     */
    TurnoutSection() {}
    
    /**
     * Create and configure 
     *
     * @param layoutTO  Name for turnout on railroad
     * @param normalIndicator  Turnout name for normal (left) indicator light on panel
     * @param reversedIndicator Turnout name for reversed (right) indicator light on panel
     * @param normalInput Sensor name for normal (left) side of switch on panel
     * @param reversedInput Sensor name for reversed (right) side of switch on panel
     * @param code common CodeLine for this machine panel
     */
    public TurnoutSection(String layoutTO, String normalIndicator, String reversedIndicator, String normalInput, String reversedInput, CodeLine codeline) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        hLayoutTO = hm.getNamedBeanHandle(layoutTO, tm.provideTurnout(layoutTO));

        hNormalIndicator = hm.getNamedBeanHandle(normalIndicator, tm.provideTurnout(normalIndicator));
        hReversedIndicator = hm.getNamedBeanHandle(reversedIndicator, tm.provideTurnout(reversedIndicator));

        hNormalInput = hm.getNamedBeanHandle(normalInput, sm.provideSensor(normalInput));
        hReversedInput = hm.getNamedBeanHandle(reversedInput, sm.provideSensor(reversedInput));
        
        this.codeline = codeline;
        
        // initialize lamps to follow layout state
        if (tm.provideTurnout(layoutTO).getKnownState()==Turnout.THROWN) {
            hNormalIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hReversedIndicator.getBean().setCommandedState(Turnout.THROWN);
            state = State.SHOWING_REVERSED;
        } else if (tm.provideTurnout(layoutTO).getKnownState()==Turnout.CLOSED) {
            hNormalIndicator.getBean().setCommandedState(Turnout.THROWN);
            hReversedIndicator.getBean().setCommandedState(Turnout.CLOSED);
            state = State.SHOWING_NORMAL;
        } else {
            hNormalIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hReversedIndicator.getBean().setCommandedState(Turnout.CLOSED);
            state = State.DARK_UNABLE;
        }
        
        tm.provideTurnout(layoutTO).addPropertyChangeListener((java.beans.PropertyChangeEvent e) -> {layoutTurnoutChanged(e);});
    }

    enum State {
        SHOWING_NORMAL,
        SHOWING_REVERSED,
        /**
         * Command has gone to layout, no verification of move has come back yet; both indicators OFF
         */
        DARK_WAITING_REPLY,
        /**
         * A lock has forbidden move; both indicators OFF
         */
        DARK_UNABLE
        
    }
    
    CodeLine codeline;
    Station station;
    
    public void addStation(Station station) { this.station = station; }
    
    NamedBeanHandle<Turnout> hLayoutTO;

    NamedBeanHandle<Turnout> hNormalIndicator;
    NamedBeanHandle<Turnout> hReversedIndicator;

    NamedBeanHandle<Sensor> hNormalInput;
    NamedBeanHandle<Sensor> hReversedInput;
    
    State state = State.DARK_UNABLE;
    
    // coding used locally to ensure consistency
    private final Station.Value CODE_CLOSED = Station.Value.Double10;
    private final Station.Value CODE_THROWN = Station.Value.Double01;
    private final Station.Value CODE_NEITHER = Station.Value.Double00;
    
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
        if (   (state == State.SHOWING_NORMAL && hNormalInput.getBean().getKnownState()==Sensor.ACTIVE)
            || (state == State.SHOWING_REVERSED && hReversedInput.getBean().getKnownState()==Sensor.ACTIVE) ) {
            log.debug("No turnout change requested");
        } else {
            log.debug("Turnout change requested");
            // have to turn off
            hNormalIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hReversedIndicator.getBean().setCommandedState(Turnout.CLOSED);
            state = State.DARK_WAITING_REPLY;
        }
        
        // return the settings to send
        if (hNormalInput.getBean().getKnownState()==Sensor.ACTIVE) return CODE_CLOSED;
        if (hReversedInput.getBean().getKnownState()==Sensor.ACTIVE) return CODE_THROWN;
        return CODE_NEITHER;
    }

    public static int MOVEMENT_DELAY = 5000;
    
    /**
     * Notification that code has been sent. Sets the turnout on the layout.
     */
    public void codeValueDelivered(Station.Value value) {
        // @TODO add lock checking here; this is part of vital logic implementation
        
        // read and set turnout
        if (value == CODE_CLOSED && hLayoutTO.getBean().getCommandedState() != Turnout.CLOSED) {
            hLayoutTO.getBean().setCommandedState(Turnout.CLOSED);
            log.debug("Layout turnout set THROWN");
        } else if (value == CODE_THROWN && hLayoutTO.getBean().getCommandedState() != Turnout.THROWN) {
            hLayoutTO.getBean().setCommandedState(Turnout.THROWN);
            log.debug("Layout turnout set THROWN");
        } else log.debug("Layout turnout not set for {} is {}", value, hLayoutTO.getBean().getCommandedState());
        
        // start the timer for the turnout to move
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

    /**
     * Just sets the code lamp active. Rest of action is on indicationComplete
     */
    public Station.Value indicationStart() {
        if (hLayoutTO.getBean().getKnownState() == Turnout.CLOSED) {
            return CODE_CLOSED;
        } else if (hLayoutTO.getBean().getKnownState() == Turnout.THROWN) {
            return CODE_THROWN;
        } else 
            return CODE_NEITHER;
    }

    /**
     * Provide value to the field unit
     */
    public void indicationComplete(Station.Value value) {
        log.debug("Indication sets from {}", value, new Exception("traceback"));
        if (value == CODE_CLOSED) {
            hNormalIndicator.getBean().setCommandedState(Turnout.THROWN);
            hReversedIndicator.getBean().setCommandedState(Turnout.CLOSED);
            state = State.SHOWING_NORMAL;
        } else if (value == CODE_THROWN) {
            hNormalIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hReversedIndicator.getBean().setCommandedState(Turnout.THROWN);
            state = State.SHOWING_REVERSED;
        } else if (value == CODE_NEITHER) {
            hNormalIndicator.getBean().setCommandedState(Turnout.CLOSED);
            hReversedIndicator.getBean().setCommandedState(Turnout.CLOSED);
            state = State.DARK_UNABLE;
        } else log.error("Got code not recognized: {}", value);
    } 

    void layoutTurnoutChanged(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && !e.getNewValue().equals(e.getOldValue()) )
            station.requestIndicationStart();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSection.class.getName());
}

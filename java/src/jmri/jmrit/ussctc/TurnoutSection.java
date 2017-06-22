package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive a single Turnout section on a USS CTC panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TurnoutSection {

    /**
     * Nobody can build anonymous object
     */
    private TurnoutSection() {}
    
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
    public TurnoutSection(String layoutTO, String normalIndicator, String reversedIndicator, String normalInput, String reversedInput) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        hLayoutTO = hm.getNamedBeanHandle(layoutTO, tm.provideTurnout(layoutTO));

        hNormalIndicator = hm.getNamedBeanHandle(normalIndicator, tm.provideTurnout(normalIndicator));
        hReversedIndicator = hm.getNamedBeanHandle(reversedIndicator, tm.provideTurnout(reversedIndicator));

        hNormalInput = hm.getNamedBeanHandle(normalInput, sm.provideSensor(normalInput));
        hReversedInput = hm.getNamedBeanHandle(reversedInput, sm.provideSensor(reversedInput));
        
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
    
    CodeLine code;
    
    NamedBeanHandle<Turnout> hLayoutTO;

    NamedBeanHandle<Turnout> hNormalIndicator;
    NamedBeanHandle<Turnout> hReversedIndicator;

    NamedBeanHandle<Sensor> hNormalInput;
    NamedBeanHandle<Sensor> hReversedInput;
    
    State state = State.DARK_UNABLE;
    
    void codeSendStart() {
        // Is the lever inconsistent with the current state?
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
    }

    /**
     * Check lock(s)
     * @return true if operation (currently) permitted by locks
     */
    boolean codeSendOK() {
        return true;
    }

    void codeSendComplete () {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSection.class.getName());
}

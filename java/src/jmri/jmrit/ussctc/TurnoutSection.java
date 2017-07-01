package jmri.jmrit.ussctc;

import jmri.*;
import java.util.*;

/**
 * Drive a single Turnout section on a USS CTC panel.
 * Implements {@link Section} for both the field and CTC machine parts. 
 * <a href="doc-files/TurnoutSection-ClassDiagram.png"><img src="doc-files/TurnoutSection-ClassDiagram.png" alt="UML Class diagram" height="50%" width="50%"></a>
 * The two parts
 * are implemented as separate {@link FieldSection} and {@link CentralSection}
 * static inner classes to ensure they're functionally separate, connected only
 * by the code they exchange. They're combined in this single class
 * to make sure they work together.
 *
 * <p>
 * The state diagram for the central section is presented in three parts to make it more useful:
 *<ul>
 * <li>Initialization
 *      <a href="doc-files/TurnoutSection-Central-Init-StateDiagram.png"><img src="doc-files/TurnoutSection-Central-Init-StateDiagram.png" alt="UML State diagram" height="33%" width="33%"></a>
 * <li>Handline code button presses
 *      <a href="doc-files/TurnoutSection-Central-Code-StateDiagram.png"><img src="doc-files/TurnoutSection-Central-Code-StateDiagram.png" alt="UML State diagram" height="33%" width="33%"></a>
 * <li>Receiving indications
 *      <a href="doc-files/TurnoutSection-Central-Indication-StateDiagram.png"><img src="doc-files/TurnoutSection-Central-Indication-StateDiagram.png" alt="UML State diagram" height="33%" width="33%"></a>
 * </ul>
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 * TODO - add field state diagram
 */
/*
 * @startuml jmri/jmrit/ussctc/doc-files/TurnoutSection-ClassDiagram.png
 * FieldSection <|-- Section
 * CentralSection <|-- Section
 * Section <|-- TurnoutSection
 * FieldSection <|-- TurnoutFieldSection
 * CentralSection <|-- TurnoutCentralSection
 * TurnoutSection *.. TurnoutCentralSection
 * TurnoutSection *.. TurnoutFieldSection
 * 'note A TurnoutSection object comprises itself, plus\ncontained CentralSection and FieldSection objects
 @end
 */
/*
 * @startuml jmri/jmrit/ussctc/doc-files/TurnoutSection-Central-Init-StateDiagram.png
 * state "Showing 10 Normal" as ShowN
 * state "Showing 01 Reversed" as ShowR
 * state "Showing 00 Off" as ShowOff
 * [*] --> ShowN : CLOSED at startup
 * [*] --> ShowR : THROWN at startup
 * [*] --> ShowOff : Unknown at startup
 @end
 */
/*
 * @startuml jmri/jmrit/ussctc/doc-files/TurnoutSection-Central-Code-StateDiagram.png
 * state "Showing 10 Normal" as ShowN
 * state "Showing 01 Reversed" as ShowR
 * state "Showing 00 Off" as ShowOff
 *
 * note bottom of ShowOff : Pressing code results in lights off\nif lamps and lever don't match
 *
 * ShowR --> ShowOff : Lever at Normal\nand Code pressed
 * ShowR --> ShowR : Lever at Reversed\nand Code pressed
 * ShowN --> ShowN: Lever at Normal\nand Code pressed
 * ShowN --> ShowOff : Lever at Reversed\nand Code pressed
 @end
 */
/*
 * @startuml jmri/jmrit/ussctc/doc-files/TurnoutSection-Central-Indication-StateDiagram.png
 * state "Showing 10 Normal" as ShowN
 * state "Showing 01 Reversed" as ShowR
 * state "Showing 00 Off" as ShowOff
 *
 * ShowOff --> ShowN : Indication 10 received
 * ShowN --> ShowN : Indication 10 received
 * ShowR --> ShowN : Indication 10 received
 *
 * ShowOff --> ShowR : Indication 01 received
 * ShowN --> ShowR : Indication 01 received
 * ShowR --> ShowR : Indication 01 received
 *
 * ShowOff --> ShowOff: Indication 00 received
 * ShowR --> ShowOff: Indication 00 received
 * ShowN --> ShowOff: Indication 00 received 
 *
 * note left of ShowOff : Indications always drive the display
 @end
 */
public class TurnoutSection implements Section<CodeGroupTwoBits, CodeGroupTwoBits> {

    /**
     *  Anonymous object only for testing
     */
    TurnoutSection() {}
    
    TurnoutFieldSection field;
    TurnoutCentralSection central;
    
    /**
     * Create and configure.
     *
     * Accepts user or system names.
     *
     * @param layoutTO  Name for turnout on railroad
     * @param normalIndicator  Turnout name for normal (left) indicator light on panel
     * @param reversedIndicator Turnout name for reversed (right) indicator light on panel
     * @param normalInput Sensor name for normal (left) side of switch on panel
     * @param reversedInput Sensor name for reversed (right) side of switch on panel
     * @param station Station to which this Section belongs
     */
    public TurnoutSection(String layoutTO, String normalIndicator, String reversedIndicator, String normalInput, String reversedInput, Station station) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        central = new TurnoutCentralSection(normalIndicator, reversedIndicator, normalInput, reversedInput);
        central.addStation(station);

        field = new TurnoutFieldSection(layoutTO);
        field.addStation(station);

        // initialize lamps to follow layout state
        if (tm.provideTurnout(layoutTO).getKnownState()==Turnout.THROWN) {
            tm.provideTurnout(normalIndicator).setCommandedState(Turnout.CLOSED);
            tm.provideTurnout(reversedIndicator).setCommandedState(Turnout.THROWN);
            central.state = State.SHOWING_REVERSED;
        } else if (tm.provideTurnout(layoutTO).getKnownState()==Turnout.CLOSED) {
            tm.provideTurnout(normalIndicator).setCommandedState(Turnout.THROWN);
            tm.provideTurnout(reversedIndicator).setCommandedState(Turnout.CLOSED);
            central.state = State.SHOWING_NORMAL;
        } else {
            tm.provideTurnout(normalIndicator).setCommandedState(Turnout.CLOSED);
            tm.provideTurnout(reversedIndicator).setCommandedState(Turnout.CLOSED);
            central.state = State.DARK_UNABLE;
        }
        
    }

    // TODO - make sure state is properly implemented throughout for locking. Are these the right DARK states?
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
    
    public void addLocks(List<Lock> locks) { 
        central.addLocks(locks);
        field.addLocks(locks); 
    }
    
    // coding used locally to ensure consistency
    static final CodeGroupTwoBits CODE_CLOSED = CodeGroupTwoBits.Double10;
    static final CodeGroupTwoBits CODE_THROWN = CodeGroupTwoBits.Double01;
    static final CodeGroupTwoBits CODE_NEITHER = CodeGroupTwoBits.Double00;
    
    @Override
    public CodeGroupTwoBits codeSendStart() { return central.codeSendStart(); }
    @Override
    public void codeValueDelivered(CodeGroupTwoBits value) { field.codeValueDelivered(value); }   
    @Override
    public CodeGroupTwoBits indicationStart() { return field.indicationStart(); }
    @Override
    public void indicationComplete(CodeGroupTwoBits value) { central.indicationComplete(value); }   


    
    static class TurnoutCentralSection implements CentralSection<CodeGroupTwoBits, CodeGroupTwoBits>  {
        public TurnoutCentralSection(String normalIndicator, String reversedIndicator, String normalInput, String reversedInput) {
            NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
            TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
            hNormalIndicator = hm.getNamedBeanHandle(normalIndicator, tm.provideTurnout(normalIndicator));
            hReversedIndicator = hm.getNamedBeanHandle(reversedIndicator, tm.provideTurnout(reversedIndicator));

            hNormalInput = hm.getNamedBeanHandle(normalInput, sm.provideSensor(normalInput));
            hReversedInput = hm.getNamedBeanHandle(reversedInput, sm.provideSensor(reversedInput));
        }
        
        State state = State.DARK_UNABLE;
    
        NamedBeanHandle<Turnout> hNormalIndicator;
        NamedBeanHandle<Turnout> hReversedIndicator;

        NamedBeanHandle<Sensor> hNormalInput;
        NamedBeanHandle<Sensor> hReversedInput;

        List<Lock> locks;
        public void addLocks(List<Lock> locks) { this.locks = locks; }

        Station station;
        public void addStation(Station station) { this.station = station; }

        /**
         * Start of sending code operation:
         * <ul>
         * <li>Set indicators
         * <li>Provide values to send over line
         * </ul>
         * @return code line value to transmit
         */
        @Override
        public CodeGroupTwoBits codeSendStart() {
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

        /**
         * Process values received from the field unit.
         */
        @Override
        public void indicationComplete(CodeGroupTwoBits value) {
            log.debug("Indication sets from {}", value);
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
    }
    
    static class TurnoutFieldSection implements FieldSection<CodeGroupTwoBits, CodeGroupTwoBits>  {
        CodeGroupTwoBits lastCodeValue = CODE_NEITHER;
    
        public TurnoutFieldSection(String layoutTO) {
            NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
            TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
            hLayoutTO = hm.getNamedBeanHandle(layoutTO, tm.provideTurnout(layoutTO));
        
            tm.provideTurnout(layoutTO).addPropertyChangeListener((java.beans.PropertyChangeEvent e) -> {layoutTurnoutChanged(e);});
        }

        NamedBeanHandle<Turnout> hLayoutTO;

        List<Lock> locks;
        public void addLocks(List<Lock> locks) { this.locks = locks; }

        Station station;
        public void addStation(Station station) { this.station = station; }

        /**
         * Notification that code has arrived in the field. Sets the turnout on the layout.
         */
        @Override
        public void codeValueDelivered(CodeGroupTwoBits value) {
            lastCodeValue = value;
        
            // Check locks
            boolean permitted = true;
            if (locks != null) {
                for (Lock lock : locks) {
                    if ( ! lock.isLockClear()) permitted = false;
                }
            }
            log.debug(" Lock check found permitted = {}", permitted);
        
            if (permitted) {
                // Set turnout as commanded, skipping redundant operations
                if (value == CODE_CLOSED && hLayoutTO.getBean().getCommandedState() != Turnout.CLOSED) {
                    hLayoutTO.getBean().setCommandedState(Turnout.CLOSED);
                    log.debug("Layout turnout set CLOSED");
                } else if (value == CODE_THROWN && hLayoutTO.getBean().getCommandedState() != Turnout.THROWN) {
                    hLayoutTO.getBean().setCommandedState(Turnout.THROWN);
                    log.debug("Layout turnout set THROWN");
                } else {
                    log.debug("Layout turnout already set for {} as {}", value, hLayoutTO.getBean().getCommandedState());
                    // Usually, indication will come back when turnout feedback (defined elsewhere) triggers
                    // from motion run above
                    // But we have to handle the case of re-commanding back to the current turnout state
                    if ( (value == CODE_CLOSED && hLayoutTO.getBean().getCommandedState() == Turnout.CLOSED)
                            || (value == CODE_THROWN && hLayoutTO.getBean().getCommandedState() == Turnout.THROWN) ) {
                    
                        log.debug("    Start indication due to aligned with last request");
                        jmri.util.ThreadingUtil.runOnLayoutEventually( ()->{ station.requestIndicationStart(); } );
                
                    }
                }
            } else {
                log.debug("No turnout operation due to not permitted by lock: {}", value);
                // Usually, indication will come back when turnout feedback (defined elsewhere) triggers
                // from motion run above
                // But we have to handle the case of re-commanding back to the current turnout state
                if ( (value == CODE_CLOSED && hLayoutTO.getBean().getKnownState() == Turnout.CLOSED)
                        || (value == CODE_THROWN && hLayoutTO.getBean().getKnownState() == Turnout.THROWN) ) {
                
                    log.debug("    Start indication due to aligned with last request");
                    jmri.util.ThreadingUtil.runOnLayoutEventually( ()->{ station.requestIndicationStart(); } );
                
                }
            }
        }

        /**
         * Provide state that's returned from field to machine via indication.
         */
        @Override
        public CodeGroupTwoBits indicationStart() {
            if (hLayoutTO.getBean().getKnownState() == Turnout.CLOSED && lastCodeValue == CODE_CLOSED ) {
                return CODE_CLOSED;
            } else if (hLayoutTO.getBean().getKnownState() == Turnout.THROWN  && lastCodeValue == CODE_THROWN) {
                return CODE_THROWN;
            } else 
                return CODE_NEITHER;
        }

        void layoutTurnoutChanged(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState") && !e.getNewValue().equals(e.getOldValue()) ) {
                log.debug("Turnout changed from {} to {}, so requestIndicationStart", e.getOldValue(), e.getNewValue());
                // Always send an indication if there's a change in the turnout
                station.requestIndicationStart();
            }
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSection.class.getName());
}

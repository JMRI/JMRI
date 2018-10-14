package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive a single Track Circuit section on a USS CTC panel.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 * TODO - add field state diagram
 */

public class TrackCircuitSection implements Section<CodeGroupNoBits, CodeGroupOneBit> {

    /**
     *  Anonymous object only for testing
     */
    TrackCircuitSection() {}
    
    /**
     * Create and configure.
     *
     * Accepts user or system names.
     *
     * @param inputSensor  Sensor for occupancy on layout
     * @param panelOutput  Turnout drives lamp on panel
     * @param station Station to which this Section belongs
     * @param bell  Bell driver (can be null)
     */
    public TrackCircuitSection(String inputSensor, String panelOutput, Station station, Bell bell) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        hInputSensor = hm.getNamedBeanHandle(inputSensor, sm.provideSensor(inputSensor));
        hPanelOutput = hm.getNamedBeanHandle(panelOutput, tm.provideTurnout(panelOutput));
        this.station = station;
        this.bell = bell;

        // align at start
        indicationComplete(indicationStart());
        
        // attach listeners for future changes
        sm.provideSensor(inputSensor).addPropertyChangeListener((java.beans.PropertyChangeEvent e) -> {layoutTurnoutChanged(e);});
    }

    /**
     * Create and configure.
     * <p>
     * Accepts user or system names. 
     *
     * @param inputSensor  Sensor for input from central CTC machine
     * @param panelOutput  Turnout name for maintainer call on layout
     * @param station      Station to which this Section belongs
     */
    public TrackCircuitSection(String inputSensor, String panelOutput, Station station) {
        this(inputSensor, panelOutput, station, null);
    }
    
    NamedBeanHandle<Sensor> hInputSensor;
    NamedBeanHandle<Turnout> hPanelOutput;
    
    Bell bell;

    Station station;
    @Override
    public Station getStation() { return station; }
    @Override
    public String getName() { return "TC for "+hInputSensor.getBean().getDisplayName(); }

     /**
     * Start of sending code operation.
      *
     * @return code line value to transmit
     */
    @Override
    public CodeGroupNoBits codeSendStart() {
        return CodeGroupNoBits.None;
    }

    /**
     * Process values received from the field unit.
     */
    @Override
    public void indicationComplete(CodeGroupOneBit value) {
        if ( value == CodeGroupOneBit.Single1 && hPanelOutput.getBean().getCommandedState()!=Turnout.THROWN ) {
            hPanelOutput.getBean().setCommandedState(Turnout.THROWN);
            if (bell != null) bell.ring();
        } else if (value == CodeGroupOneBit.Single0 && hPanelOutput.getBean().getCommandedState()!=Turnout.CLOSED ) {
            hPanelOutput.getBean().setCommandedState(Turnout.CLOSED);
            if (bell != null) bell.ring();
        }
    } 

    /**
     * Notification that code has arrived in the field. Sets the turnout on the layout.
     */
    @Override
    public void codeValueDelivered(CodeGroupNoBits value) {
    }

    /**
     * Provide state that's returned from field to machine via indication.
     */
    @Override
    public CodeGroupOneBit indicationStart() {
        // Everything _except_ a clean INACTIVE shows active
        if (hInputSensor.getBean().getState()!=Sensor.INACTIVE) return CodeGroupOneBit.Single1;
        return CodeGroupOneBit.Single0;
    }

    void layoutTurnoutChanged(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && !e.getNewValue().equals(e.getOldValue()) ) {
            log.debug("Sensor changed from {} to {}, so requestIndicationStart", e.getOldValue(), e.getNewValue());
            // Always send an indication if there's a change in the turnout
            station.requestIndicationStart();
        }
    }
     
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackCircuitSection.class);

}

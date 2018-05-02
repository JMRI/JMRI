package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive a single Maintainer Call section on a USS CTC panel.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 * TODO - add field state diagram
 */

public class MaintainerCallSection implements Section<CodeGroupOneBit, CodeGroupNoBits> {

    /**
     *  Anonymous object only for testing
     */
    MaintainerCallSection() {}
    
    
    /**
     * Create and configure.
     *
     * Accepts user or system names.
     *
     * @param inputSensor  Sensor for input from central CTC machine
     * @param layoutOutput  Turnout name for maintainer call on layout
     * @param station Station to which this Section belongs
     */
    public MaintainerCallSection(String inputSensor, String layoutOutput, Station station) {
        this.station = station;

        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        hInputSensor = hm.getNamedBeanHandle(inputSensor, sm.provideSensor(inputSensor));
        hLayoutOutput = hm.getNamedBeanHandle(layoutOutput, tm.provideTurnout(layoutOutput));
        
        // aligns at start
        codeValueDelivered(codeSendStart());
    }

    NamedBeanHandle<Sensor> hInputSensor;
    NamedBeanHandle<Turnout> hLayoutOutput;
    
    Station station;
    @Override
    public Station getStation() { return station; }
    @Override
    public String getName() { return "MC for "+hLayoutOutput.getBean().getDisplayName(); }
 
     /**
     * Start of sending code operation.
     * @return code line value to transmit
     */
    @Override
    public CodeGroupOneBit codeSendStart() {
        log.debug("codeSendStart with Sensor state == {}", hInputSensor.getBean().getKnownState());
        // return the settings to send
        if (hInputSensor.getBean().getKnownState()==Sensor.ACTIVE) return CodeGroupOneBit.Single1;
        return CodeGroupOneBit.Single0;
    }

    /**
     * Process values received from the field unit.
     */
    @Override
    public void indicationComplete(CodeGroupNoBits value) {
    } 

    /**
     * Notification that code has arrived in the field. Sets the turnout on the layout.
     */
    @Override
    public void codeValueDelivered(CodeGroupOneBit value) {
        log.debug("codeValueDelivered({}) with Turnout state == {}", value, hLayoutOutput.getBean().getCommandedState());
        // Set out as commanded, skipping redundant operations
        if (value == CodeGroupOneBit.Single0 && hLayoutOutput.getBean().getCommandedState() != Turnout.CLOSED) {
            hLayoutOutput.getBean().setCommandedState(Turnout.CLOSED);
            log.debug("Layout MC output set CLOSED");
        } else if (value == CodeGroupOneBit.Single1 && hLayoutOutput.getBean().getCommandedState() != Turnout.THROWN) {
            hLayoutOutput.getBean().setCommandedState(Turnout.THROWN);
            log.debug("Layout MC output set THROWN");
        }
    }

    /**
     * Provide state that's returned from field to machine via indication.
     */
    @Override
    public CodeGroupNoBits indicationStart() {
        return CodeGroupNoBits.None;
    }

     
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MaintainerCallSection.class);
}

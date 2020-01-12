package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive the interactions of a code button and code light on the panel.
 * <p>
 * Primary interactions are with the common {@link CodeLine} and
 * specific {@link Station} object.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class CodeButton {
    
    /**
     * Create and configure 
     *
     * @param buttonSensor  Name for Sensor that shows button press
     * @param panelIndicator  Name of Turnout that lights panel indicator
     */
    public CodeButton(String buttonSensor, String panelIndicator) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        hButtonSensor = hm.getNamedBeanHandle(buttonSensor, sm.provideSensor(buttonSensor));
        hPanelIndicator = hm.getNamedBeanHandle(panelIndicator, tm.provideTurnout(panelIndicator));
                
        sm.provideSensor(buttonSensor).addPropertyChangeListener((java.beans.PropertyChangeEvent e) -> {layoutSensorChanged(e);});
    }

    /**
     * Configure the Station connection for this CodeButton.
     * <p>
     * Note that {@link Station} normally invokes this automatically
     * as part of its construction
     *
     * @param station A Station instance for this panel
     * @return This CodeButton object to permit call linking
     */
    CodeButton addStation(Station station) {
        this.station = station;
        return this;
    }
    
    Station station;
    
    NamedBeanHandle<Sensor> hButtonSensor;
    NamedBeanHandle<Turnout> hPanelIndicator;
    
    void layoutSensorChanged(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && e.getNewValue().equals(Sensor.ACTIVE) && !e.getOldValue().equals(Sensor.ACTIVE))
            codeButtonPressed();
    }
    
    void codeButtonPressed() {
        log.debug("Code button pressed");
        log.debug("Code light on - codeButtonPressed");
        hPanelIndicator.getBean().setCommandedState(Turnout.THROWN);
        station.codeSendRequest();
    }
    
    /**
     * Code sequence done, turn off code light
     */
    void codeValueDelivered() {
        log.debug("Code light off - codeValueDelivered");
        hPanelIndicator.getBean().setCommandedState(Turnout.CLOSED);
    }
    
    /**
     * Indication sequence starting, turn on code light
     */
    void indicationStart() {
        log.debug("Code light on - indicationStart");
        hPanelIndicator.getBean().setCommandedState(Turnout.THROWN);
    }

    /**
     * Indication sequence done, turn off code light
     */
    void indicationComplete() {
        log.debug("Code light off - indicationComplete");
        hPanelIndicator.getBean().setCommandedState(Turnout.CLOSED);
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeButton.class);
}

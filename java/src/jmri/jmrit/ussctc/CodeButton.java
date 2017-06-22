package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive the interactions of a code button and code light on the panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class CodeButton {

    /**
     * Nobody can build anonymous object
     */
    private CodeButton() {}
    
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
     * Configure the CodeLine connection for this CodeButton
     * @param code Common CodeLine instance for this panel
     * @return This CodeButton object to permit call linking
     */
    public CodeButton setColumn(Column column) {
        this.column = column;
        return this;
    }
    
    Column column;
    
    NamedBeanHandle<Sensor> hButtonSensor;
    NamedBeanHandle<Turnout> hPanelIndicator;
    
    void layoutSensorChanged(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && e.getNewValue().equals(Sensor.ACTIVE) && !e.getOldValue().equals(Sensor.ACTIVE))
            codeButtonPressed();
    }
    
    void codeButtonPressed() {
        log.debug("Code button {} pressed", column.name);
        column.requestSendCode();
    }
    
    void codeSendStart() {
    }

    void codeSendComplete () {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeLine.class.getName());
}

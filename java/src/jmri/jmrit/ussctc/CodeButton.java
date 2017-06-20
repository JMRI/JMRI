package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive the interactions of a code button and code light on the panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@net.jcip.annotations.Immutable
public class CodeButton {

    /**
     * Nobody can build anonymous object
     */
    private CodeButton() {}
    
    /**
     * Create and configure 
     *
     * @param layoutSensor  Name for Sensor that shows button press
     * @param panelIndicator  Name of Turnout that lights panel indicator
     * @param code Common CodeLine instance for this panel
     */
    public CodeButton(String layoutSensor, String panelIndicator, CodeLine code) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        hLayoutSensor = hm.getNamedBeanHandle(layoutSensor, sm.provideSensor(layoutSensor));
        hPanelIndicator = hm.getNamedBeanHandle(panelIndicator, tm.provideTurnout(panelIndicator));
        
        this.code = code;
    }

    CodeLine code;
    
    NamedBeanHandle<Sensor> hLayoutSensor;
    NamedBeanHandle<Turnout> hPanelIndicator;
    
    void codeSendStart() {
    }

    void codeSendComplete () {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeLine.class.getName());
}

package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive a single Turnout section on a USS CTC panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@net.jcip.annotations.Immutable
public class TurnoutSection {

    /**
     * Nobody can build anonymous object
     */
    private TurnoutSection() {}
    
    /**
     * Create and configure 
     *
     * @param layoutTO  Name for turnout on railroad
     * @param leftIndicator  Turnout name for left indicator light on panel
     * @param rightIndicator Turnout name for right indicator light on panel
     * @param leftInput Sensor name for left side of switch on panel
     * @param rightInput Sensor name for right side of switch on panel
     * @param code common CodeLine for this machine panel
     */
    public TurnoutSection(String layoutTO, String leftIndicator, String rightIndicator, String leftInput, String rightInput, CodeLine code) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        hLayoutTO = hm.getNamedBeanHandle(layoutTO, tm.provideTurnout(layoutTO));

        hLeftIndicator = hm.getNamedBeanHandle(leftIndicator, tm.provideTurnout(leftIndicator));
        hRightIndicator = hm.getNamedBeanHandle(rightIndicator, tm.provideTurnout(rightIndicator));

        hLeftInput = hm.getNamedBeanHandle(leftInput, sm.provideSensor(leftInput));
        hRightInput = hm.getNamedBeanHandle(rightInput, sm.provideSensor(rightInput));
        
        this.code = code;
    }

    CodeLine code;
    
    NamedBeanHandle<Turnout> hLayoutTO;

    NamedBeanHandle<Turnout> hLeftIndicator;
    NamedBeanHandle<Turnout> hRightIndicator;

    NamedBeanHandle<Sensor> hLeftInput;
    NamedBeanHandle<Sensor> hRightInput;
    
    void codeSendStart() {
    }

    void codeSendComplete () {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSection.class.getName());
}

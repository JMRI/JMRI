package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Derive a CTC machine bell via a Turnout output.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class VetoedBell implements Bell {

    public VetoedBell(String veto, Bell bell) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SensorManager tm = InstanceManager.getDefault(SensorManager.class);
        
        hVeto = hm.getNamedBeanHandle(veto, tm.provideSensor(veto));
        this.bell = bell;
    }

    NamedBeanHandle<Sensor> hVeto;
    Bell bell;
        
    @Override
    public void ring() {
        if (hVeto.getBean().getKnownState() != Sensor.ACTIVE) {
            bell.ring();
        }
    }

}

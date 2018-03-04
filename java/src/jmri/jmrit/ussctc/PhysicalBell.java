package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Derive a CTC machine bell via a Turnout output.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class PhysicalBell implements Bell {

    public PhysicalBell(String output) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        
        hOutput = hm.getNamedBeanHandle(output, tm.provideTurnout(output));
    }

    NamedBeanHandle<Turnout> hOutput;

    public static int STROKE_DELAY = 250;
    
    @Override
    public void ring() {
        hOutput.getBean().setCommandedState(Turnout.THROWN);
        jmri.util.ThreadingUtil.runOnLayoutDelayed(
            ()->{ hOutput.getBean().setCommandedState(Turnout.CLOSED); },
            STROKE_DELAY);
    }

}

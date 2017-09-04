package jmri.jmrit.ussctc;

import jmri.*;


/**
 * Lock if a turnout isn't in the desired state.
 * <p>
 * Can be used to e.g. lock when a call-on (turnout) is set
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TurnoutLock implements Lock {

    /**
     * @param name System or user name of turnout to monitor
     * @param okValue If this value isn't present, the operation is locked out
     */
    public TurnoutLock(String name, int okValue) {
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(name);
        turnout = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name,t);
        this.value = okValue;      
    }

    NamedBeanHandle<Turnout> turnout; 
    int value;
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear() {
        InstanceManager.getDefault(MemoryManager.class).provideMemory(logMemoryName).setValue("");
        if (turnout.getBean().getKnownState() != value) {
            InstanceManager.getDefault(MemoryManager.class).provideMemory(logMemoryName)
                .setValue("Locked due to turnout setting: "+turnout.getBean().getDisplayName());
            return false;
        }
        return true;
    }
    
}

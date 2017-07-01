package jmri.jmrit.ussctc;

import jmri.*;

import java.util.*;

/**
 * Lock if any of the SignalHeads controlling traffic over a turnout are not at stop.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class RouteLock implements Lock {

    public RouteLock(List<NamedBeanHandle<SignalHead>> list) {
        this.list = list;
    }
    
    public RouteLock(String[] array) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SignalHeadManager sm = InstanceManager.getDefault(SignalHeadManager.class);

        list = new ArrayList<>();
        for (String s : array) list.add(hm.getNamedBeanHandle(s, sm.getSignalHead(s)));
    }

    public RouteLock(String head) {
        this(new String[]{head});
    }

    List<NamedBeanHandle<SignalHead>> list; 
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear() {
        for (NamedBeanHandle<SignalHead> handle : list) {
            if (handle.getBean().getState() != SignalHead.RED) return false;
        }
        return true;
    }
    
}

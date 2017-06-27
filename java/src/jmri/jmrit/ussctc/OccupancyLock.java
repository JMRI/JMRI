package jmri.jmrit.ussctc;

import jmri.*;

import java.util.*;

/**
 * Lock if any of a list of sensors isn't INACTIVE.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class OccupancyLock implements Lock {

    public OccupancyLock(List<NamedBeanHandle<Sensor>> list) {
        this.list = list;
    }
    
    List<NamedBeanHandle<Sensor>> list; 
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear() {
        for (NamedBeanHandle<Sensor> handle : list) {
            if (handle.getBean().getState() != Sensor.INACTIVE) return false;
        }
        return true;
    }
    
}

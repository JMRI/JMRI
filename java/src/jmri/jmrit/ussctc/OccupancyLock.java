package jmri.jmrit.ussctc;

import java.util.*;
import jmri.*;

/**
 * Lock if any of a list of sensors isn't INACTIVE.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class OccupancyLock implements Lock {

    public OccupancyLock(List<NamedBeanHandle<Sensor>> list) {
        this.list = list;
    }
    
    public OccupancyLock(String[] array) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        list = new ArrayList<>();
        for (String s : array) list.add(hm.getNamedBeanHandle(s, sm.provideSensor(s)));
    }

    public OccupancyLock(String sensor) {
        this(new String[]{sensor});
    }

    List<NamedBeanHandle<Sensor>> list; 
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    @Override
    public boolean isLockClear() {
        for (NamedBeanHandle<Sensor> handle : list) {
            if (handle.getBean().getState() != Sensor.INACTIVE) {
                lockLogger.setStatus(this, "Locked due to occupancy: "+handle.getBean().getDisplayName());
                return false;
            }
        }
        lockLogger.setStatus(this, "");
        return true;
    }
    
}

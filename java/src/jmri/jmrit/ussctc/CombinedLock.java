package jmri.jmrit.ussctc;


import java.util.*;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Combines multiple locks into one with an AND operation.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@API(status = MAINTAINED)
public class CombinedLock implements Lock {

    public CombinedLock(List<Lock> list) {
        this.list = list;
    }
    
    List<Lock> list; 
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    @Override
    public boolean isLockClear() {
        for (Lock lock : list)
            if (!lock.isLockClear()) return false;
        return true;
    }
    
}

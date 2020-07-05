package jmri.jmrit.ctc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * Just a packet of fields:
 * 
 */
@API(status = MAINTAINED)
public class TrafficLockingInfo {
    public boolean      _mReturnStatus;     // JUST a return status, no relation to:
    public LockedRoute  _mLockedRoute;      // The locked route object or null if not available.
    
    public TrafficLockingInfo(boolean returnStatus) {
        _mReturnStatus = returnStatus;      // Whatever the user wants.
        _mLockedRoute = null;               // Assume none allocated.
    }
}

package jmri.jmrix.loconet.logixng;

import java.util.List;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Common methods
 */
public class Common {

    /**
     * Do we have a LocoNet connection?
     * @return true if we have LocoNet, false otherwise
     */
    public static boolean hasLocoNet() {
        List<LocoNetSystemConnectionMemo> list = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        
        // We have at least one LocoNet connection if the list is not empty
        return !list.isEmpty();
    }
    
}

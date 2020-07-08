package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket;

/**
 * Default implementation of the Female Digital Action socket
 */
public final class DefaultFemaleRootSocket
        extends DefaultFemaleDigitalActionSocket
        implements FemaleRootSocket {


    public DefaultFemaleRootSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    @Override
    public boolean isCompatible(MaleSocket socket) {
        if (! super.isCompatible(socket)) return false;
        
        for (List<Class<? extends Base>> list : getConnectableClasses().values()) {
            for (Class<? extends Base> clazz : list) {
                if (socket.getObject().getClass() == clazz) return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
        
        for (Map.Entry<Category, List<Map.Entry<Class<? extends Base>, Boolean>>> entry
                : InstanceManager.getDefault(ConditionalNG_Manager.class).getRootClasses().entrySet()) {
            
            List<Class<? extends Base>> list = new ArrayList<>();
            for (Map.Entry<Class<? extends Base>, Boolean> subEntry : entry.getValue()) {
                if (subEntry.getValue()) list.add(subEntry.getKey());
            }
            
            if (!list.isEmpty()) {
                map.put(entry.getKey(), list);
            }
        }
        
        return map;
    }

}

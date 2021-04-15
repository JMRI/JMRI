package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.jmrit.logixng.FemaleSocketManager;
import jmri.jmrit.logixng.FemaleSocketFactory;

/**
 *
 * @author daniel
 */
public class DefaultFemaleSocketManager implements FemaleSocketManager {

    private static final Map<String, SocketType> femaleSocketTypes = new HashMap<>();
    
    public DefaultFemaleSocketManager() {
        for (FemaleSocketFactory actionFactory : ServiceLoader.load(FemaleSocketFactory.class)) {
            femaleSocketTypes.put(actionFactory.getFemaleSocketType().getName(), actionFactory.getFemaleSocketType());
        }
    }
    
    @Override
    public Map<String, SocketType> getSocketTypes() {
        return Collections.unmodifiableMap(femaleSocketTypes);
    }
    
    @Override
    public SocketType getSocketTypeByType(String type) {
        return femaleSocketTypes.get(type);
    }
    
}

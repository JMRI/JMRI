package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.jmrit.logixng.*;

/**
 * Default implementation of FemaleAnySocket
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class DefaultFemaleAnySocket extends AbstractFemaleSocket implements FemaleAnySocket {

    public DefaultFemaleAnySocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    @Override
    public void disposeMe() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isCompatible(MaleSocket socket) {
        return true;
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return new HashMap<>();
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultAnySocket_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultAnySocket_Long", getName());
    }
    
}

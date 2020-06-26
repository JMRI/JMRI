package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.*;

/**
 * Class providing the basic logic of the ErrorNotifier interface.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class DefaultErrorNotifier implements ErrorNotifier {

    public DefaultErrorNotifier() {
    }
    
    @Override
    public boolean notifyError(Base object, String msg, Exception e) {
        log.error(msg);
        return false;
    }
    
    @Override
    public String getName() {
        return Bundle.getMessage("DefaultErrorNotifierName");
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultErrorNotifier.class);
    
}

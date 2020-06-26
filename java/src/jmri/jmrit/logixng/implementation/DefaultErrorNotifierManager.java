package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import jmri.util.Log4JUtil;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing the basic logic of the LogixNG_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultErrorNotifierManager implements ErrorNotifierManager {

    List<ErrorNotifierHolder> _errorNotifiers = new ArrayList<>();
    
    
    public DefaultErrorNotifierManager() {
    }
    
    @Override
    public void notifyError(Base object, String msg, Exception e) {
        // Uncomment this when the DefaultErrorNotifier class is written.
//        if (_errorNotifiers.isEmpty()) {
//            log.warn("No error notifier is registred");
//        }
        for (ErrorNotifierHolder errorNotifierHolder : _errorNotifiers) {
            if (errorNotifierHolder._state == State.READY) {
                if (errorNotifierHolder._errorNotifier.notifyError(object, msg, e)) {
                    errorNotifierHolder._state = State.WAITING;
                }
            }
        }
    }
    
    @Override
    public void registerErrorNotifier(ErrorNotifier errorNotifier) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public void unregisterErrorNotifier(ErrorNotifier errorNotifier) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public void clearErrorNotifiers() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public List<ErrorNotifier> getErrorNotifiers() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public void responseOK(ErrorNotifier errorNotifier) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public void responseMute(ErrorNotifier errorNotifier) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    
    
    private static class ErrorNotifierHolder {
        final ErrorNotifier _errorNotifier;
        State _state = State.READY;
        
        ErrorNotifierHolder(ErrorNotifier errorNotifier) {
            _errorNotifier = errorNotifier;
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(DefaultErrorNotifierManager.class);
}

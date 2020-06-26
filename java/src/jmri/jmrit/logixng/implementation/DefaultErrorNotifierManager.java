package jmri.jmrit.logixng.implementation;

import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jmri.jmrit.logixng.*;

/**
 * Class providing the basic logic of the ErrorNotifierManager interface.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class DefaultErrorNotifierManager implements ErrorNotifierManager {

    Map<ErrorNotifier, ErrorNotifierHolder> _errorNotifiers = new ConcurrentHashMap<>();
    
    
    public DefaultErrorNotifierManager() {
        ErrorNotifier errorNotifier = new DefaultErrorNotifier();
        _errorNotifiers.put(errorNotifier, new ErrorNotifierHolder(errorNotifier));
    }
    
    @Override
    public void notifyError(Base object, String msg, Exception e) {
        // Uncomment this when the DefaultErrorNotifier class is written.
        if (_errorNotifiers.isEmpty()) {
            log.warn("No error notifier is registred");
        }
        for (ErrorNotifierHolder errorNotifierHolder : _errorNotifiers.values()) {
            if (errorNotifierHolder._state == State.READY) {
                if (errorNotifierHolder._errorNotifier.notifyError(object, msg, e)) {
                    errorNotifierHolder._state = State.WAITING;
                }
            }
        }
    }
    
    @Override
    public void registerErrorNotifier(ErrorNotifier errorNotifier) {
        _errorNotifiers.put(errorNotifier, new ErrorNotifierHolder(errorNotifier));
    }
    
    @Override
    public void unregisterErrorNotifier(ErrorNotifier errorNotifier) {
        _errorNotifiers.remove(errorNotifier);
    }
    
    @Override
    public void clearErrorNotifiers() {
        _errorNotifiers.clear();
    }
    
    @Override
    public Set<ErrorNotifier> getErrorNotifiers() {
        return Collections.unmodifiableSet(_errorNotifiers.keySet());
    }
    
    @Override
    public void responseOK(ErrorNotifier errorNotifier) {
        _errorNotifiers.get(errorNotifier)._state = State.READY;
    }
    
    @Override
    public void responseMute(ErrorNotifier errorNotifier) {
        _errorNotifiers.get(errorNotifier)._state = State.MUTED;
    }
    
    
    
    private static class ErrorNotifierHolder {
        final ErrorNotifier _errorNotifier;
        State _state = State.READY;
        
        ErrorNotifierHolder(ErrorNotifier errorNotifier) {
            _errorNotifier = errorNotifier;
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultErrorNotifierManager.class);
}

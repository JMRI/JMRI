package jmri.implementation;

import java.util.*;

import jmri.Meter;

/**
 * Handles updates of meters. Several meters may share the update task.
 *
 * @author Mark Underwood    (C) 2015
 * @author Daniel Bergqvist  (C) 2020
 */
public abstract class MeterUpdateTask {
    
    Map<Meter, Boolean> meters = new HashMap<>();
    boolean _enabled = false;
    private UpdateTask _intervalTask = null;
    private final int _sleepInterval;
    
    public MeterUpdateTask() {
       _sleepInterval = 10000;
    }
    
    public MeterUpdateTask(int interval) {
       _sleepInterval = interval;
    }
    
    public void addMeter(Meter m) {
        meters.put(m, false);
    }
    
    public void removeMeter(Meter m) {
        meters.remove(m);
    }
    
    protected void enable() {
        if(_intervalTask != null) {
            _intervalTask.enable();
        }
    }
    
    public void enable(Meter m) {
        if (!meters.containsKey(m)) {
            throw new IllegalArgumentException("Meter is not registered");
        }
        
        if (!meters.get(m)) {
            meters.put(m, true);
            if (!_enabled) {
                _enabled = true;
                enable();
            }
        }
    }
    
    protected void disable() {
        if(_intervalTask != null) {
            _intervalTask.disable();
        }
    }
    
    public void disable(Meter m) {
        if (!meters.containsKey(m)) return;
        
        if (meters.get(m)) {
            meters.put(m, false);
            if (_enabled) {
                // Is there any more meters that are active?
                boolean found = false;
                for (Boolean b : meters.values()) {
                    found |= b;
                }
                if (! found) {
                    _enabled = false;
                    disable();
                }
            }
        }
    }
    
    public void initTimer() {
        if(_intervalTask != null) {
           _intervalTask.cancel();
           _intervalTask = null;
        }
        if(_sleepInterval < 0){
           return; // don't start or restart the timer.
        }
        _intervalTask = new UpdateTask();
        // At some point this will be dynamic intervals...
        log.debug("Starting Meter Timer");
        jmri.util.TimerUtil.scheduleAtFixedRate(_intervalTask,
                _sleepInterval, _sleepInterval);
    }
    
    public abstract void requestUpdateFromLayout();
    
    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     * @param m the meter that is calling dispose
     */
    public void dispose(Meter m){
        removeMeter(m);
        if (meters.isEmpty() && (_intervalTask != null)) {
           _intervalTask.cancel();
           _intervalTask = null;
        }
    }
    
    
    // Timer task for periodic updates...
    private class UpdateTask extends TimerTask {

        private boolean _isEnabled = false;

        public UpdateTask() {
            super();
        }

        public void enable() {
            _isEnabled = true;
        }

        public void disable() {
            _isEnabled = false;
        }

        @Override
        public void run() {
            if (_isEnabled) {
                log.debug("Timer Pop");
                requestUpdateFromLayout();
            }
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterUpdateTask.class);
}

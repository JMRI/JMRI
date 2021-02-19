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
    private int _initialInterval; //in ms
    private int _sleepInterval;   //in ms
    
    /* default values for both sleepIntervals */
    public MeterUpdateTask() {
        _sleepInterval = 10000;
        _initialInterval = 10000;
    }
    
    /* if only one interval passed, set both to same */
    public MeterUpdateTask(int interval) {
        _initialInterval = interval;
        _sleepInterval = interval;
     }
     
    public MeterUpdateTask(int initialInterval, int sleepInterval) {
        _initialInterval = initialInterval;
        _sleepInterval = sleepInterval;
     }
     
    public void addMeter(Meter m) {
        meters.put(m, false);
    }
    
    public void removeMeter(Meter m) {
        meters.remove(m);
    }
    
    public void enable() {
        if(_intervalTask != null) {
            _intervalTask.enable();
        } else {
            log.debug("_intervalTask is null, enable() ignored");
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
           log.debug("_sleepInterval {} less than zero, initTimer() ignored");           
           return; // don't start or restart the timer.
        }
        _intervalTask = new UpdateTask();
        // At some point this will be dynamic intervals...
        log.debug("Starting Meter Timer for {}ms, {}ms", _initialInterval, _sleepInterval);
        jmri.util.TimerUtil.scheduleAtFixedRate(_intervalTask,
                _initialInterval, _sleepInterval);
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
                log.debug("UpdateTask requesting update from layout");
                requestUpdateFromLayout();
            } else { 
                log.debug("UpdateTask not enabled, run() ignored");
            }
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterUpdateTask.class);
}

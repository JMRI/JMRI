/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.loconet;
import java.util.*;

/**
 * Handles updates of meters. Several meters may share the update task.
 *
 * @author B. Milhaupt     (C) 2020
 */
public class LnMeterInitTask {

    boolean _enabled = false;
    private UpdateTask _intervalTask = null;
    private final int _sleepInterval;
    private final LnTrafficController tc;

    public LnMeterInitTask(LnTrafficController tc) {
       this(tc, 100);
    }

    public LnMeterInitTask(LnTrafficController tc, int interval) {
        this.tc = tc;
       _sleepInterval = interval;
    }

    protected void enable() {
        if (!_enabled) {
            _enabled = true;
            if(_intervalTask != null) {
                _intervalTask.enable();
            }
        }
    }

    protected void disable() {
        if(_intervalTask != null) {
            _intervalTask.disable();
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
        log.debug("Starting Initialization Timer");
        jmri.util.TimerUtil.scheduleAtFixedRate(_intervalTask,
                _sleepInterval, _sleepInterval);
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose(){
        if ((_intervalTask != null) && (_intervalTask._isEnabled)) {
           _intervalTask.cancel();
        }
        if (_intervalTask != null) {
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
            if (_isEnabled && tc.status()) {
                log.debug("Timer triggered.");
                tc.sendLocoNetMessage(new LocoNetMessage(
                        new int[] {LnConstants.OPC_RQ_SL_DATA, 0x01, 0x79, 0x00}));
                disable();
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnMeterInitTask.class);
}

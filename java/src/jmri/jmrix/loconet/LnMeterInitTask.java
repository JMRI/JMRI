package jmri.jmrix.loconet;
import java.util.TimerTask;

/**
 * Provides for LocoNet "Meters" discovery query at connection start-up.
 * 
 * This class specifically deals with issues sometimes seen
 * at JMRI LocoNet connection start-up.
 *
 * @author B. Milhaupt     (C) 2020
 */
public class LnMeterInitTask {

    boolean _enabled = false;
    private UpdateTask _intervalTask = null;
    private final int _sleepInterval;
    private final LnTrafficController tc;

    /**
     * Create a task to perform an initial query of LocoNet for devices
     * which provide data for JMRI Meters.
     * 
     * @param tc Traffic Controller used when sending query
     * @param interval - delay between checks of connection's readiness
     */
    public LnMeterInitTask(LnTrafficController tc, int interval) {
        this.tc = tc;
       _sleepInterval = interval;
    }

    /**
     * Enable the task to begin
     */
    protected void enable() {
        if (!_enabled) {
            _enabled = true;
            if(_intervalTask != null) {
                _intervalTask.enable(true);
            }
        }
    }

    /**
     * Cancel the task (if it is not already canceled)
     */
    protected void disable() {
        if(_intervalTask != null) {
            _intervalTask.enable(false);
        }
    }

    /**
     * Initializes timer for send of meters query.
     *
     * Cancels any existing task.  Checks delay and
     * exits if delay is negative.  Establishes a
     * new task only if delay is greater than 0.
     */
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
        if ((_intervalTask != null) && (_intervalTask.isEnabled())) {
           _intervalTask.enable(false);
        }
        if (_intervalTask != null) {
            _intervalTask = null;
        }
    }

    /**
     * Timer task for periodic updates
     *
     * Task to check status of the LocoNet connection, and, when it is
     * ready, send a LocoNet query message.
     */
    private class UpdateTask extends TimerTask {

        private boolean _updateTaskIsEnabled;

        public UpdateTask() {
            super();
            this._updateTaskIsEnabled = false;
        }

        /**
         * Enable or disable the update task
         * @param val true to enable, false to disable
         */
        public void enable(boolean val) {
            if (!val) {
                cancel();
            }
            _updateTaskIsEnabled = val;
        }

        /**
         * get the enable/disable state of the update task
         * @return true if enabled, else false
         */
        public boolean isEnabled() {
            return _updateTaskIsEnabled;
        }

        @Override
        public void run() {
            if (!_updateTaskIsEnabled) {
                log.debug("LnMeter initialization timer finds task not enabled.");
                return;
            } else if (!tc.status()) {
                log.debug("LnMeter initialization timer finds connection not ready.");
                return;
            }
            log.debug("LnMeter initialization timer is sending query.");
            tc.sendLocoNetMessage(new LocoNetMessage(
                    new int[] {LnConstants.OPC_RQ_SL_DATA, 0x79, 0x01, 0x00}));
            disable();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnMeterInitTask.class);
}

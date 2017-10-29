package jmri.jmrix.dccpp;

import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import jmri.MultiMeter;
import jmri.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the DCC++ Base Station
 *
 * @author Mark Underwood (C) 2015
 */
public class DCCppMultiMeter extends Bean implements MultiMeter, DCCppListener {

    private float current_float = 0.0f;
    private float voltage_float = 0.0f;

    //private boolean is_enabled = false;
    private UpdateTask intervalTask = null;
    private Timer intervalTimer = null;

    private DCCppTrafficController tc = null;

    public DCCppMultiMeter(DCCppSystemConnectionMemo memo) {
        tc = memo.getDCCppTrafficController();

        // TODO: For now this is OK since the traffic controller
        // ignores filters and sends out all updates, but
        // at some point this will have to be customized.
        tc.addDCCppListener(DCCppInterface.THROTTLE, this);

        //is_enabled = false;
        initTimer();

        log.debug("DCCppMultiMeter constructor called");

    }

    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    @Override
    public void message(DCCppReply r) {
        log.debug("DCCppMultiMeter received reply: {}", r.toString());
        if (r.isCurrentReply()) {
            setCurrent((r.getCurrentInt() * 1.0f) / (DCCppConstants.MAX_CURRENT * 1.0f));
        }

    }

    @Override
    public void message(DCCppMessage m) {
    }

    protected void initTimer() {
        intervalTask = new UpdateTask(this);
        intervalTimer = new Timer();
        // At some point this will be dynamic intervals...
        log.debug("Starting Meter Timer");
        intervalTimer.scheduleAtFixedRate(intervalTask,
                DCCppConstants.METER_INTERVAL_MS,
                DCCppConstants.METER_INTERVAL_MS);
    }

    // Timer task for periodic updates...
    private class UpdateTask extends TimerTask {

        private DCCppMultiMeter parent = null;
        private boolean is_enabled = false;

        public UpdateTask(DCCppMultiMeter p) {
            super();
            parent = p;
        }

        //public void setInterval(int i) { sleep_interval = i; }
        //public int interval() { return(sleep_interval); }
        public void enable() {
            is_enabled = true;
        }

        public void disable() {
            is_enabled = false;
        }

        @Override
        public void run() {
            try {
                if (is_enabled) {
                    //log.debug("Timer Pop");
                    tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), parent);
                }
                Thread.sleep(DCCppConstants.METER_INTERVAL_MS);
            } catch (InterruptedException e) {
                log.error("Error running timer update task! {}", e);
            }
        }
    }

    // MultiMeter Interface Methods
    @Override
    public void enable() {
        log.debug("Enabling meter.");
        intervalTask.enable();
    }

    @Override
    public void disable() {
        log.debug("Disabling meter.");
        intervalTask.disable();
    }

    @Override
    public void setCurrent(float c) {
        float old = current_float;
        current_float = c;
        this.firePropertyChange(CURRENT, old, c);
    }

    @Override
    public void updateCurrent(float c) {
        setCurrent(c);
    }

    @Override
    public float getCurrent() {
        return current_float;
    }

    @Override
    public void setVoltage(float v) {
        voltage_float = v;
    }

    @Override
    public void updateVoltage(float v) {
        setVoltage(v);
    }

    @Override
    public float getVoltage() {
        return voltage_float;
    }

    @Override
    public void initializeHardwareMeter() {
        // Connect to the hardware.
    }

    @Override
    public String getHardwareMeterName() {
        return ("DCC++");
    }

    @Override
    public boolean hasCurrent() {
        return true;
    }

    @Override
    public boolean hasVoltage() {
        return false;
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
    }

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    @Override
    public synchronized void addDataUpdateListener(PropertyChangeListener l) {
        this.addPropertyChangeListener(CURRENT, l);
    }

    /**
     * Remove a request for call-back when the minutes place of the time
     * changes.
     */
    @Override
    public synchronized void removeDataUpdateListener(PropertyChangeListener l) {
        this.removePropertyChangeListener(CURRENT, l);
    }

    /**
     * Get the list of minute change listeners.
     */
    @Override
    public PropertyChangeListener[] getDataUpdateListeners() {
        return this.getPropertyChangeListeners(CURRENT);
    }

    /**
     *
     * @param p   the property
     * @param old the old value
     * @param n   the new value
     * @deprecated use
     * {@link #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)}
     * instead
     */
    @Deprecated
    protected void fireDataUpdate(String p, Object old, Object n) {
        this.firePropertyChange(p, old, n);
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message {}, {} retries available.", msg.toString(), msg.getRetries());
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeter.class);

}

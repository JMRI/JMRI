package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import jmri.MultiMeter;
import jmri.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for current meter objects.
 *
 * @author Mark Underwood (C) 2015
 */
abstract public class AbstractMultiMeter extends Bean implements MultiMeter {

    protected float current_float = 0.0f;
    protected float voltage_float = 0.0f;

    //private boolean is_enabled = false;
    private UpdateTask intervalTask = null;
    private Timer intervalTimer = null;
    private int sleepInterval = 10000; // default to 10 second sleep interval.

    public AbstractMultiMeter(int interval){
       sleepInterval = interval;
    }

    protected void initTimer() {
        if(intervalTimer!=null) {
           intervalTimer.cancel();
           intervalTask = null;
           intervalTimer = null;
        }
        intervalTask = new UpdateTask();
        intervalTimer = new Timer("MultiMeter Update Timer");
        // At some point this will be dynamic intervals...
        log.debug("Starting Meter Timer");
        intervalTimer.scheduleAtFixedRate(intervalTask,
                sleepInterval, sleepInterval);
    }

    /**
     * Request an update from the layout.  Triggered by a timer.
     */
    abstract protected void requestUpdateFromLayout();

    // Timer task for periodic updates...
    private class UpdateTask extends TimerTask {

        private boolean is_enabled = false;

        public UpdateTask() {
            super();
        }

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
                    log.debug("Timer Pop");
                    requestUpdateFromLayout();
                }
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                log.error("Error running timer update task! {}", e.getMessage());
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
    @Deprecated
    public void updateCurrent(float c) {
        setCurrent(c);
    }

    @Override
    public float getCurrent() {
        return current_float;
    }

    @Override
    public void setVoltage(float v) {
        float old = voltage_float;
        voltage_float = v;
        this.firePropertyChange(VOLTAGE, old, v);
    }

    @Override
    @Deprecated
    public void updateVoltage(float v) {
        setVoltage(v);
    }

    @Override
    public float getVoltage() {
        return voltage_float;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public synchronized void addDataUpdateListener(PropertyChangeListener l) {
        this.addPropertyChangeListener(CURRENT, l);
        this.addPropertyChangeListener(VOLTAGE, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public synchronized void removeDataUpdateListener(PropertyChangeListener l) {
        this.removePropertyChangeListener(CURRENT, l);
        this.removePropertyChangeListener(VOLTAGE, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public PropertyChangeListener[] getDataUpdateListeners() {
        return this.getPropertyChangeListeners(CURRENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
        if(intervalTimer!=null) {
           intervalTimer.cancel();
           intervalTask = null;
           intervalTimer = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMultiMeter.class);

}

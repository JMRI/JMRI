package jmri.implementation;

import java.beans.PropertyChangeListener;
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
    protected CurrentUnits currentUnits = CurrentUnits.CURRENT_UNITS_PERCENTAGE;

    //private boolean is_enabled = false;
    private UpdateTask intervalTask = null;
    private int sleepInterval = 10000; // default to 10 second sleep interval.

    public AbstractMultiMeter(int interval){
       sleepInterval = interval;
    }

    protected void initTimer() {
        if(intervalTask!=null) {
           intervalTask.cancel();
           intervalTask = null;
        }
        if(sleepInterval <0){
           return; // don't start or restart the timer.
        }
        intervalTask = new UpdateTask();
        // At some point this will be dynamic intervals...
        log.debug("Starting Meter Timer");
        jmri.util.TimerUtil.scheduleAtFixedRate(intervalTask,
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
            if (is_enabled) {
                log.debug("Timer Pop");
                requestUpdateFromLayout();
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
    public float getCurrent() {
        return current_float;
    }

    @Override
    public CurrentUnits getCurrentUnits() {
        return currentUnits;
    }

    @Override
    public void setVoltage(float v) {
        float old = voltage_float;
        voltage_float = v;
        this.firePropertyChange(VOLTAGE, old, v);
    }

    @Override
    public float getVoltage() {
        return voltage_float;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
        if(intervalTask!=null) {
           intervalTask.cancel();
           intervalTask = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMultiMeter.class);

}

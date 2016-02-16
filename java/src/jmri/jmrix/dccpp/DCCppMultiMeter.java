// DCCppMultiMeter.java
package jmri.jmrix.dccpp;

import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import jmri.MultiMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from the DCC++ Base Station
 *
 * @author	Mark Underwood (C) 2015
 * @version	$Revision$
 */

public class DCCppMultiMeter implements MultiMeter, DCCppListener {

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

	

        if (log.isDebugEnabled()) {
            log.debug("DCCppMultiMeter constructor called");
        }

    }

    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    public void message(DCCppReply r) {
	if (log.isDebugEnabled()) {
	    log.debug("DCCppMultiMeter received reply: {}", r.toString());
	}
	if (r.isCurrentReply()) {
	    int current_int = r.getCurrentInt();
	    current_float = (current_int * 1.0f) / (DCCppConstants.MAX_CURRENT * 1.0f);
	    //log.debug("Current Update: new={}", current_int);
	    // Broadcast the update
	    fireDataUpdate("MultiMeterCurrent", null, null);
	}

    }

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
	private int sleep_interval = DCCppConstants.METER_INTERVAL_MS;
	private DCCppMultiMeter parent = null;
	private boolean is_enabled = false;

	public UpdateTask(DCCppMultiMeter p) {
	    super();
	    parent = p;
	}

	//public void setInterval(int i) { sleep_interval = i; }

	//public int interval() { return(sleep_interval); }
	
	public void enable() { is_enabled = true; }
	public void disable() { is_enabled = false; }

	@Override
	public void run() {
	    try {
		if (is_enabled) {
		    //log.debug("Timer Pop");
		    tc.sendDCCppMessage(DCCppMessage.makeReadTrackCurrentMsg(), parent);
		}
		Thread.sleep(sleep_interval);
	    } catch (Exception e) {
		log.error("Error running timer update task! {}", e);
	    }
	}
    }

    // MultiMeter Interface Methods
    
    public void enable() {
	log.debug("Enabling meter.");
	intervalTask.enable();
    }
    
    public void disable() {
	log.debug("Disabling meter.");
	intervalTask.disable();
    }
    
    public void updateCurrent(float c) {
	current_float = c;
    }

    public float getCurrent() {
	return current_float;
    }

    public void updateVoltage(float v) {
	voltage_float = v;
    }
   
    public float getVoltage() {
	return voltage_float;
    }

    public void initializeHardwareMeter() {
	// Connect to the hardware.
    }

    public String getHardwareMeterName() {
	return("DCC++");
    }

    public boolean hasCurrent() {
	return true;
    }

    public boolean hasVoltage() {
	return false;
    }


    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose() { 
    }

    // Property Change Support Functions

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    java.beans.PropertyChangeSupport data_updates = new java.beans.PropertyChangeSupport(this);

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public synchronized void addDataUpdateListener(PropertyChangeListener l) {
	data_updates.addPropertyChangeListener(l);
    }

    /**
     * Remove a request for call-back when the minutes place of the time
     * changes.
     */
    public synchronized void removeDataUpdateListener(PropertyChangeListener l) {
	data_updates.removePropertyChangeListener(l);
    }

    /**
     * Get the list of minute change listeners.
     */
    public PropertyChangeListener[] getDataUpdateListeners() {
        return data_updates.getPropertyChangeListeners();
    }

    protected void fireDataUpdate(String p, Object old, Object n) {
        data_updates.firePropertyChange(p, old, n);
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString() + " , " + msg.getRetries() + " retries available.");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeter.class.getName());


}

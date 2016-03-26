package jmri.jmrix.dccpp;

import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a DCC++
 * connection.
 *
 * @author Paul Bender (C) 2002-2010
 * @author Giorgio Terdina (C) 2007
 * @author Mark Underwood (C) 2015
 * @version $Revision$
 *
 * Based on XNetThrottle by Paul Bender and Giorgio Terdina
 */
public class DCCppThrottle extends AbstractThrottle implements DCCppListener {

    protected boolean isAvailable;  // Flag  stating if the throttle is in 
    // use or not.
    protected java.util.TimerTask statusTask; // Timer Task used to 
    // periodically get 
    // current status of the 
    // throttle when throttle 
    // not available.
    protected static final int statTimeoutValue = 1000; // Interval to check the 
    protected DCCppTrafficController tc = null;

    // status of the throttle
    protected static final int THROTTLEIDLE = 0;  // Idle Throttle
    protected static final int THROTTLESTATSENT = 1;  // Sent Status request
    protected static final int THROTTLESPEEDSENT = 2;  // Sent speed/dir command to locomotive
    protected static final int THROTTLEFUNCSENT = 4;   // Sent a function command to locomotive.

    public int requestState = THROTTLEIDLE;

    protected int address;

    /**
     * Constructor
     */
    public DCCppThrottle(DCCppSystemConnectionMemo memo, DCCppTrafficController controller) {
        super(memo);
        tc = controller;
        requestList = new LinkedBlockingQueue<RequestMessage>();
        if (log.isDebugEnabled()) {
            log.debug("DCCppThrottle constructor");
        }
    }

    /**
     * Constructor
     */
    public DCCppThrottle(DCCppSystemConnectionMemo memo, LocoAddress address, DCCppTrafficController controller) {
        super(memo);
        this.tc = controller;
        this.setDccAddress(((DccLocoAddress) address).getNumber());
        this.speedIncrement = SPEED_STEP_128_INCREMENT;
        this.speedStepMode = DccThrottle.SpeedStepMode128;
        //       this.isForward=true;
        setIsAvailable(false);

        f0Momentary = f1Momentary = f2Momentary = f3Momentary = f4Momentary
                = f5Momentary = f6Momentary = f7Momentary = f8Momentary = f9Momentary
                = f10Momentary = f11Momentary = f12Momentary = false;

        requestList = new LinkedBlockingQueue<RequestMessage>();
        //sendStatusInformationRequest();
        if (log.isDebugEnabled()) {
            log.debug("DCCppThrottle constructor called for address " + address);
        }
    }

    /*
     *  Set the traffic controller used with this throttle
     */
    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    /**
     * Get the Register Number for this Throttle's assigned address
     */
    int getRegisterNum() {
	return(tc.getCommandStation().getRegisterNum(this.getDccAddress()));
    }

    /**
     * Send the DCC++  message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
	log.debug("sendFunctionGroup1(): f0 {} f1 {} f2 {} f3 {} f4{}",
		  f0, f1, f2, f3, f4);
        DCCppMessage msg = DCCppMessage.makeFunctionGroup1OpsMsg(this.getDccAddress(),
                f0, f1, f2, f3, f4);
	log.debug("sendFunctionGroup1(): Message: {}", msg.toString());
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup2OpsMsg(this.getDccAddress(),
                f5, f6, f7, f8);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the state of functions F9, F10, F11,
     * F12
     */
    @Override
    protected void sendFunctionGroup3() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup3OpsMsg(this.getDccAddress(),
                f9, f10, f11, f12);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup4OpsMsg(this.getDccAddress(),
                f13, f14, f15, f16, f17, f18, f19, f20);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
	log.debug("sendFunctionGroup5(): f21 {} f22 {} f23 {} f24 {} f25 {} f26 {} f27 {} f28 {}",
		  f21, f22, f23, f24, f25, f26, f27, f28);
        DCCppMessage msg = DCCppMessage.makeFunctionGroup5OpsMsg(this.getDccAddress(),
                f21, f22, f23, f24, f25, f26, f27, f28);
	log.debug("sendFunctionGroup5(): Message: {}", msg.toString());
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4
     */
    protected void sendMomentaryFunctionGroup1() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup1SetMomMsg(this.getDccAddress(),
                f0Momentary, f1Momentary, f2Momentary, f3Momentary, f4Momentary);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the momentary state of functions F5,
     * F6, F7, F8
     */
    protected void sendMomentaryFunctionGroup2() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup2SetMomMsg(this.getDccAddress(),
                f5Momentary, f6Momentary, f7Momentary, f8Momentary);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the momentary state of functions F9,
     * F10, F11, F12
     */
    protected void sendMomentaryFunctionGroup3() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup2SetMomMsg(this.getDccAddress(),
                f9Momentary, f10Momentary, f11Momentary, f12Momentary);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the momentary state of functions F13,
     * F14, F15, F16 F17 F18 F19 F20
     */
    protected void sendMomentaryFunctionGroup4() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup4SetMomMsg(this.getDccAddress(),
                f13Momentary, f14Momentary, f15Momentary, f16Momentary,
                f17Momentary, f18Momentary, f19Momentary, f20Momentary);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /**
     * Send the DCC++ message to set the momentary state of functions F21,
     * F22, F23, F24 F25 F26 F27 F28
     */
    protected void sendMomentaryFunctionGroup5() {
        DCCppMessage msg = DCCppMessage.makeFunctionGroup5SetMomMsg(this.getDccAddress(),
                f21Momentary, f22Momentary, f23Momentary, f24Momentary,
                f25Momentary, f26Momentary, f27Momentary, f28Momentary);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /* 
     * setSpeedSetting - notify listeners and send the new speed to the
     * command station.
     */
    synchronized public void setSpeedSetting(float speed) {
        if (log.isDebugEnabled()) {
            log.debug("set Speed to: " + speed
                    + " Current step mode is: " + this.speedStepMode);
        }
        super.setSpeedSetting(speed);
        if (speed < 0) {
            /* we're sending an emergency stop to this locomotive only */
            sendEmergencyStop();
        } else {
            if (speed > 1) {
                speed = (float) 1.0;
            }
            /* we're sending a speed to the locomotive */
            DCCppMessage msg = DCCppMessage.makeSpeedAndDirectionMsg(
								    getRegisterNum(),
								    getDccAddress(),
								    speed,
								    this.isForward);
            // now, queue the message for sending to the command station
            //queueMessage(msg, THROTTLESPEEDSENT);
            queueMessage(msg, THROTTLEIDLE);
        }
    }

    /* Since xpressnet has a seperate Opcode for emergency stop,
     * We're setting this up as a seperate protected function
     */
    protected void sendEmergencyStop() {
        /* Emergency stop sent */
        DCCppMessage msg = DCCppMessage.makeAddressedEmergencyStop(this.getRegisterNum(), this.getDccAddress());
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLESPEEDSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /* Since there is only one "throttle" command to the DCC++ base station,
     * when we change the direction, we must also re-set the speed.
     */
    public void setIsForward(boolean forward) {
        super.setIsForward(forward);
        setSpeedSetting(this.speedSetting);
    }

    /*
     * setSpeedStepMode - set the speed step value and the related
     *                    speedIncrement value.
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
     *
     * NOTE: DCC++ only supports 128-step mode.  So we ignore the speed
     * setting, even though we store it.
     */
    @Override
    public void setSpeedStepMode(int Mode) {
        super.setSpeedStepMode(Mode);
    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    protected void throttleDispose() {
        active = false;
        stopStatusTimer();
        finishRecord();
    }

    public int setDccAddress(int newaddress) {
        address = newaddress;
        return address;
    }

    public int getDccAddress() {
        return address;
    }


    protected int getDccAddressHigh() {
        return DCCppCommandStation.getDCCAddressHigh(this.address);
    }

    protected int getDccAddressLow() {
        return DCCppCommandStation.getDCCAddressLow(this.address);
    }


    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement() {
        return speedIncrement;
    }

    // Handle incoming messages for This throttle.
    public void message(DCCppReply l) {
        // First, we want to see if this throttle is waiting for a message 
        //or not.
        if (log.isDebugEnabled()) {
            log.debug("Throttle " + getDccAddress() + " - recieved message " + l.toString());
        }
        if (requestState == THROTTLEIDLE) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLEIDLE");
            }
            // We haven't sent anything, but we might be told someone else 
            // has taken over this address
	    // For now, do nothing.
        } else if ((requestState & THROTTLESPEEDSENT) == THROTTLESPEEDSENT) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLESPEEDSENT");
	    }
	    // This is a reply to a Throttle message, or to a Status message.
	    if (l.isThrottleReply()) {
		// Update our state with the register's information.
		handleThrottleReply(l);
	    }
	    // For a Throttle command ("t") we get back a Throttle Status.
	    if (log.isDebugEnabled()) {
		log.debug("Last Command processed successfully.");
	    }
	    setIsAvailable(true);
	    requestState = THROTTLEIDLE;
	    sendQueuedMessage();
		
	}
	if ((requestState & THROTTLEFUNCSENT) == THROTTLEFUNCSENT) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLEFUNCSENT. Ignoring Reply");
		log.debug("Reply: {}", l.toString());
            }
	}
        requestState=THROTTLEIDLE;
        sendQueuedMessage();
    }

    private void handleThrottleReply(DCCppReply l) {
	int reg, speed, dir;
	reg = l.getRegisterInt();
	speed = l.getSpeedInt();
	dir = l.getDirectionInt();

	// Check to see if register matches MY throttle.
	// If so, update my values to match the returned values.
	// Make (relatively) direct writes to the memories, so we don't
	// cause looped throttle messages.
	int regaddr = tc.getCommandStation().getRegisterAddress(reg);
	if ((regaddr == DCCppConstants.REGISTER_UNALLOCATED) ||
	    (regaddr != this.address)) {
	    // This register doesn't match anything.
	    // Or the assigned address doesn't match mine.
	    return;
	} else {
	    // The assigned address matches mine.  Update my info 
	    // to match the returned register info.
	    if (speed < 0) {
		//this.setSpeedSetting(0.0f);
		this.speedSetting = 0.0f;
	    }
	    else {
		//this.setSpeedSetting((speed * 1.0f)/126.0f);
		this.speedSetting = (speed * 1.0f)/126.0f;
	    }
	    this.isForward = (dir == 1 ? true : false);
	}
	
    }

	
    // listen for the messages to the LI100/LI101
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString() + " , " + msg.getRetries() + " retries available.");
        }
        if (msg.getRetries() > 0) {
            // If the message still has retries available, send it back to 
            // the traffic controller.
            tc.sendDCCppMessage(msg, this);
        } else {
            // Try to send the next queued message,  if one is available.
            sendQueuedMessage();
        }
    }

    // Status Information processing routines
    // Used for return values from Status requests.
    //Get SpeedStep and availability information



    /*
     * Set the internal isAvailable property
     */
    protected void setIsAvailable(boolean Available) {
        if (this.isAvailable != Available) {
            notifyPropertyChangeListener("IsAvailable",
                    Boolean.valueOf(this.isAvailable),
                    Boolean.valueOf(this.isAvailable = Available));
        }
        /* if we're setting this to true, stop the timer,
         otherwise start the timer. */
        if (Available == true) {
            stopStatusTimer();
        } else {
            startStatusTimer();
        }
    }

    /*
     * Set up the status timer, and start it.
     */
    protected void startStatusTimer() {
        if (log.isDebugEnabled()) {
            log.debug("Status Timer Started");
        }
        if (statusTask != null) {
            statusTask.cancel();
        }
        statusTask = new java.util.TimerTask() {
            public void run() {
                /* If the timer times out, just send a status 
                 request message */
		// TODO: how to do this for DCC++?
                //sendStatusInformationRequest();
            }
        };
        new java.util.Timer().schedule(statusTask, statTimeoutValue, statTimeoutValue);
    }

    /*
     * Stop the Status Timer 
     */
    protected void stopStatusTimer() {
        if (log.isDebugEnabled()) {
            log.debug("Status Timer Stopped");
        }
        if (statusTask != null) {
            statusTask.cancel();
        }
    }

    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, DCCppThrottleManager.isLongAddress(address));
    }

    //A queue to hold outstanding messages
    protected LinkedBlockingQueue<RequestMessage> requestList = null;

    //function to send message from queue.
    synchronized protected void sendQueuedMessage() {

        RequestMessage msg = null;
        // check to see if the queue has a message in it, and if it does,
        // remove the first message
        if (requestList.size() != 0) {
            if (log.isDebugEnabled()) {
                log.debug("sending message to traffic controller");
            }
            // if the queue is not empty, remove the first message
            // from the queue, send the message, and set the state machine 
            // to the requried state.
            try {
                msg = requestList.take();
            } catch (java.lang.InterruptedException ie) {
                return; // if there was an error, exit.
            }
            if (msg != null) {
                requestState = msg.getState();
                tc.sendDCCppMessage(msg.getMsg(), this);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("message queue empty");
            }
            // if the queue is empty, set the state to idle.
            requestState = THROTTLEIDLE;
        }
    }

    //function to queue a message
    synchronized protected void queueMessage(DCCppMessage m, int s) {
        if (log.isDebugEnabled()) {
            log.debug("adding message to message queue");
        }
        // put the message in the queue
        RequestMessage msg = new RequestMessage(m, s);
        try {
            requestList.put(msg);
        } catch (java.lang.InterruptedException ie) {
        }
        // if the state is idle, trigger the message send
        if (requestState == THROTTLEIDLE) {
            sendQueuedMessage();
        }
    }

    // internal class to hold a request message, along with the associated
    // throttle state.
    protected static class RequestMessage {

        private int state;
        private DCCppMessage msg;

        RequestMessage(DCCppMessage m, int s) {
            state = s;
            msg = m;
        }

        int getState() {
            return state;
        }

        DCCppMessage getMsg() {
            return msg;
        }

    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(DCCppThrottle.class.getName());
}

package jmri.jmrix.dccpp;

import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
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
     * Send the DCC++  message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup1OpsMsg(this.getDccAddress(),
                f0, f1, f2, f3, f4);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup2OpsMsg(this.getDccAddress(),
                f5, f6, f7, f8);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F9, F10, F11,
     * F12
     */
    @Override
    protected void sendFunctionGroup3() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup3OpsMsg(this.getDccAddress(),
                f9, f10, f11, f12);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup4OpsMsg(this.getDccAddress(),
                f13, f14, f15, f16, f17, f18, f19, f20);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup5OpsMsg(this.getDccAddress(),
                f21, f22, f23, f24, f25, f26, f27, f28);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4
     */
    protected void sendMomentaryFunctionGroup1() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup1SetMomMsg(this.getDccAddress(),
                f0Momentary, f1Momentary, f2Momentary, f3Momentary, f4Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F5,
     * F6, F7, F8
     */
    protected void sendMomentaryFunctionGroup2() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup2SetMomMsg(this.getDccAddress(),
                f5Momentary, f6Momentary, f7Momentary, f8Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F9,
     * F10, F11, F12
     */
    protected void sendMomentaryFunctionGroup3() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup2SetMomMsg(this.getDccAddress(),
                f9Momentary, f10Momentary, f11Momentary, f12Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F13,
     * F14, F15, F16 F17 F18 F19 F20
     */
    protected void sendMomentaryFunctionGroup4() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup4SetMomMsg(this.getDccAddress(),
                f13Momentary, f14Momentary, f15Momentary, f16Momentary,
                f17Momentary, f18Momentary, f19Momentary, f20Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F21,
     * F22, F23, F24 F25 F26 F27 F28
     */
    protected void sendMomentaryFunctionGroup5() {
        DCCppMessage msg = DCCppMessage.getFunctionGroup5SetMomMsg(this.getDccAddress(),
                f21Momentary, f22Momentary, f23Momentary, f24Momentary,
                f25Momentary, f26Momentary, f27Momentary, f28Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
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
            DCCppMessage msg = DCCppMessage.getSpeedAndDirectionMsg(getDccAddress(),
                    speed,
                    this.isForward);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLESPEEDSENT);
        }
    }

    /* Since xpressnet has a seperate Opcode for emergency stop,
     * We're setting this up as a seperate protected function
     */
    protected void sendEmergencyStop() {
        /* Emergency stop sent */
        DCCppMessage msg = DCCppMessage.getAddressedEmergencyStop(this.getDccAddress());
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLESPEEDSENT);
    }

    /* When we set the direction, we're going to set the speed to
     zero as well */
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
     */
    @Override
    public void setSpeedStepMode(int Mode) {
        super.setSpeedStepMode(Mode);
        // On a lenz system, we need to send the speed to make sure the 
        // command station knows about the change.
        setSpeedSetting(this.speedSetting);
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


    // TODO: DCC++ What if anything should these do?
    protected int getDccAddressHigh() {
        return DCCppCommandStation.getDCCAddressHigh(this.address);
    }

    // TODO: DCC++ What if anything should these do?
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
	    /*
            if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_RESPONSE ");
                }
                if (l.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE) {
                    // the address is in bytes 3 and 4
                    if (getDccAddressHigh() == l.getElement(2) && getDccAddressLow() == l.getElement(3)) {
                        if (isAvailable) {
                            //Set the Is available flag to Throttle.False
                            log.info("Loco " + getDccAddress() + " In use by another device");
                            setIsAvailable(false);
                            // popup a message box that will trigger a status request
                            //int select=JOptionPane.showConfirmDialog(null,"Throttle for address " +this.getDccAddress() + " Taken Over, reaquire?","Taken Over",JOptionPane.YES_NO_OPTION);
                            //if(select==JOptionPane.YES_OPTION)
                            //{
                            // Send a request for status
                            //sendStatusInformationRequest();
                            //return;
                            //} else {
                            // Remove the throttle
                            // TODO
                            //}
                        }
                    }
                }
            }
	    */
        } else 
	      if ((requestState & THROTTLESPEEDSENT) == THROTTLESPEEDSENT
                || (requestState & THROTTLEFUNCSENT) == THROTTLEFUNCSENT) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLESPEEDSENT or THROTTLEFUNCSENT");
            }

	    // For a Throttle command ("t") we get back a Throttle Status.
	    if (l.isOkMessage()) {
                if (log.isDebugEnabled()) {
                    log.debug("Last Command processed successfully.");
                }
                setIsAvailable(true);
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
	    } else {
                /* this is an unknown error */
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                log.warn("Received unhandled response: " + l);
            }
        }
        //requestState=THROTTLEIDLE;
        //sendQueuedMessage();
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
    static Logger log = LoggerFactory.getLogger(DCCppThrottle.class.getName());
}

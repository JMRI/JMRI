package jmri.jmrix.dccpp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
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
 *
 * Based on XNetThrottle by Paul Bender and Giorgio Terdina
 */
public class DCCppThrottle extends AbstractThrottle implements DCCppListener {

    protected DCCppTrafficController tc;

    // status of the throttle
    protected static final int THROTTLEIDLE = 0;  // Idle Throttle
    protected static final int THROTTLESPEEDSENT = 2;  // Sent speed/dir command to locomotive
    protected static final int THROTTLEFUNCSENT = 4;   // Sent a function command to locomotive.
    private final float speedMultiplier = 1.0f / 126.0f; //used to convert from integer speed to what JMRI expects

    public int requestState = THROTTLEIDLE;

    protected int address;

    /**
     * Constructor.
     * @param memo system connection.
     * @param controller system connection traffic controller.
     */
    public DCCppThrottle(DCCppSystemConnectionMemo memo, DCCppTrafficController controller) {
        super(memo);
        tc = controller;
        requestList = new LinkedBlockingQueue<RequestMessage>();
        this.isForward = true; //loco should default to forward
        log.debug("DCCppThrottle constructor");
    }

    /**
     * Constructor.
     * @param memo system connection.
     * @param address loco address to set on throttle
     * @param controller system connection traffic controller.
     */
    public DCCppThrottle(DCCppSystemConnectionMemo memo, LocoAddress address, DCCppTrafficController controller) {
        super(memo);
        tc = controller;
        if (address instanceof DccLocoAddress) {
            this.setDccAddress(address.getNumber());
        }
        else {
            log.error("LocoAddress {} is not a DccLocoAddress",address);
        }
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        requestList = new LinkedBlockingQueue<RequestMessage>();
        this.isForward = true; //loco should default to forward
        log.debug("DCCppThrottle constructor called for address {}", address);
    }

    /*
     *  Set the traffic controller used with this throttle
     */
    public void setDCCppTrafficController(DCCppTrafficController controller) {
        tc = controller;
    }

    /**
     * Get the Register Number for this Throttle's assigned address
     * @return register number currently
     */
    int getRegisterNum() {
        return (tc.getCommandStation().getRegisterNum(this.getDccAddress()));
    }

    /**
     * Send the DCC++  message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        log.debug("sendFunctionGroup1(): f0 {} f1 {} f2 {} f3 {} f4{}",
            getFunction(0), getFunction(1), getFunction(2), getFunction(3), getFunction(4));
        DCCppMessage msg = DCCppMessage.makeFunctionGroup1OpsMsg(this.getDccAddress(),
            getFunction(0), getFunction(1), getFunction(2), getFunction(3), getFunction(4));
        log.debug("sendFunctionGroup1(): Message: {}", msg);
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
            getFunction(5), getFunction(6), getFunction(7), getFunction(8));
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
            getFunction(9), getFunction(10), getFunction(11), getFunction(12));
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
            getFunction(13), getFunction(14), getFunction(15), getFunction(16),
            getFunction(17), getFunction(18), getFunction(19), getFunction(20));
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
            getFunction(21), getFunction(22), getFunction(23), getFunction(24),
            getFunction(25), getFunction(26), getFunction(27), getFunction(28));
        DCCppMessage msg = DCCppMessage.makeFunctionGroup5OpsMsg(this.getDccAddress(),
            getFunction(21), getFunction(22), getFunction(23), getFunction(24),
            getFunction(25), getFunction(26), getFunction(27), getFunction(28));
        log.debug("sendFunctionGroup5(): Message: '{}'", msg);
        // now, queue the message for sending to the command station
        //queueMessage(msg, THROTTLEFUNCSENT);
        queueMessage(msg, THROTTLEIDLE);
    }

    /* 
     * setSpeedSetting - notify listeners and send the new speed to the
     * command station.
     */
    @Override
    synchronized public void setSpeedSetting(float speed) {
        if (log.isDebugEnabled()) {
            log.debug("set Speed to: {} Current step mode is: {}", speed, this.speedStepMode);
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

    /* Since DCC++ has a seperate Opcode for emergency stop,
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
    @Override
    public void setIsForward(boolean forward) {
        super.setIsForward(forward);
        synchronized(this) {
            setSpeedSetting(this.speedSetting);
        }
    }

    /*
     * setSpeedStepMode - set the speed step value and the related
     *                    speedIncrement value.
     *
     * @param Mode  the current speed step mode - default should be 128
     *              speed step mode in most cases
     *
     * NOTE: DCC++ only supports 128-step mode.  So we ignore the speed
     * setting, even though we store it.
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        super.setSpeedStepMode(Mode);
    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    @Override
    protected void throttleDispose() {
        active = false;
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

    // Handle incoming messages for This throttle.
    @Override
    public void message(DCCppReply l) {
        // First, we want to see if this throttle is waiting for a message 
        //or not.
        if (log.isDebugEnabled()) {
            log.trace("Throttle {} - received message '{}'", getDccAddress(), l);
        }
        if (requestState == THROTTLEIDLE) {
            log.trace("Current throttle status is THROTTLEIDLE");
            // We haven't sent anything, but we might be told someone else 
            // has taken over this address
            // For now, do nothing.
        } else if ((requestState & THROTTLESPEEDSENT) == THROTTLESPEEDSENT) {
            log.debug("Current throttle status is THROTTLESPEEDSENT");
            // This is a reply to a Throttle message, or to a Status message.
            if (l.isThrottleReply()) {
                // Update our state with the register's information.
                handleThrottleReply(l);
            }
            // For a Throttle command ("t") we get back a Throttle Status.

            log.debug("Last Command processed successfully.");

            requestState = THROTTLEIDLE;
            sendQueuedMessage();
  
        }
        if ((requestState & THROTTLEFUNCSENT) == THROTTLEFUNCSENT) {
            log.debug("Current throttle status is THROTTLEFUNCSENT. Ignoring Reply: '{}'", l);
        }
        requestState=THROTTLEIDLE;
        sendQueuedMessage();
    }

    //check for any changes needed based on incoming LocoState reply for this throttle
    //then make those changes directly to the parent throttle to avoid a message loop
    protected void handleLocoState(DCCppReply r) {
        int cab = r.getCabInt();
        //insure this message belongs to this throttle (really shouldn't happen)        
        if (this.address != cab) {
            log.error("throttle {} incorrectly called for cab {}", this.address, cab);
            return;
        }

        boolean newForward = r.getIsForward();
        float newSpeedSetting = r.getSpeedInt() * speedMultiplier;
        String newFunctionsString = r.getFunctionsString();
        
        if (this.getIsForward() != newForward) {
            if (log.isDebugEnabled()) log.debug("changing forward from {} to {} for {}", this.getIsForward(), newForward, cab);
            super.setIsForward(newForward);
        }
        if (Math.abs(this.getSpeedSetting() - newSpeedSetting) > 0.0001) { //avoid possible float precision errors
            if (log.isDebugEnabled()) log.debug("changing speed from {} to {} for {}", this.getSpeedSetting(), newSpeedSetting, cab);
            super.setSpeedSetting(newSpeedSetting);
        }
        
        Map<Integer, Boolean> newStates = new HashMap<>();
        //check each function value for any changes, and update if so
        for (int i = 0; i <= 28; i++) {
            boolean newState = (newFunctionsString.charAt(i)=='1');
            if (this.getFunction(i) != newState) {
//                log.debug(r.toMonitorString());
                if (log.isDebugEnabled()) log.debug("changing F{} from {} to {} for {}", i, this.getFunction(i), newState, cab);                
                newStates.put(i, newState);
            }
        }
        if (!newStates.isEmpty()) super.setFunctions(newStates);
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
        } else {
            // The assigned address matches mine.  Update my info 
            // to match the returned register info.
            synchronized(this) {
                if (speed < 0) {
                    //this.setSpeedSetting(0.0f);
                    this.speedSetting = 0.0f;
                } else {
                    //this.setSpeedSetting((speed * 1.0f)/126.0f);
                    this.speedSetting = (speed * 1.0f) / 126.0f;
                }
            }
            this.isForward = (dir == 1);
         }
    }
 
    // Listen for the outgoing messages (to the command station)
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}' , {} retries available.", msg, msg.getRetries());
        if (msg.getRetries() > 0) {
            // If the message still has retries available, send it back to 
            // the traffic controller.
            tc.sendDCCppMessage(msg, this);
        } else {
            // Try to send the next queued message,  if one is available.
            sendQueuedMessage();
        }
    }

    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, DCCppThrottleManager.isLongAddress(address));
    }

    //A queue to hold outstanding messages
    protected LinkedBlockingQueue<RequestMessage> requestList;

    // function to send message from queue.
    synchronized protected void sendQueuedMessage() {
        RequestMessage msg;
        // check to see if the queue has a message in it, and if it does,
        // remove the first message
        if (!requestList.isEmpty()) {
            log.trace("sending message to traffic controller");
            // if the queue is not empty, remove the first message
            // from the queue, send the message, and set the state machine 
            // to the requeried state.
            try {
                msg = requestList.take();
            } catch (java.lang.InterruptedException ie) {
                return; // if there was an error, exit.
            }
            requestState = msg.getState();
            tc.sendDCCppMessage(msg.getMsg(), this);
        } else {
            log.trace("message queue empty");
            // if the queue is empty, set the state to idle.
            requestState = THROTTLEIDLE;
        }
    }

    //function to queue a message
    synchronized protected void queueMessage(DCCppMessage m, int s) {
        log.trace("adding message '{}' to message queue", m);
        // put the message in the queue
        RequestMessage msg = new RequestMessage(m, s);
        try {
            requestList.put(msg);
        } catch (java.lang.InterruptedException ignore) {
        }
        // if the state is idle, trigger the message send
        if (requestState == THROTTLEIDLE) {
            sendQueuedMessage();
        }
    }

    // internal class to hold a request message, along with the associated
    // throttle state.
    protected static class RequestMessage {

        private final int state;
        private final DCCppMessage msg;

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
    private final static Logger log = LoggerFactory.getLogger(DCCppThrottle.class);

}

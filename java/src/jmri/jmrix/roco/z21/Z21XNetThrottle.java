package jmri.jmrix.roco.z21;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a z21 XpressNet
 * connection.
 *
 * @author Paul Bender (C) 2015
 */
public class Z21XNetThrottle extends jmri.jmrix.roco.RocoXNetThrottle {

    /**
     * Constructor
     */
    public Z21XNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo,controller);
    }

    /**
     * Constructor
     */
    public Z21XNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo,address,controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void setSpeedSetting(float speed) {
        log.debug("set Speed to: {} Current step mode is: {}",speed,this.speedStepMode);
        this.speedSetting = speed;
        record(speed);
        if (speed < 0) {
            /* we're sending an emergency stop to this locomotive only */
            sendEmergencyStop();
        } else {
            if (speed > 1) {
                speed = (float) 1.0;
            }
            /* we're sending a speed to the locomotive */
            XNetMessage msg = Z21XNetMessage.getZ21LanXSetLocoDriveMsg(getDccAddress(),
                    this.speedStepMode,
                    speed,
                    this.isForward);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLEIDLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop) {
        this.speedSetting = speed;
        record(speed);
    }

    /**
     * Send a request to get the speed, direction and function status from
     * the command station.
     */
    @Override
    synchronized protected void sendStatusInformationRequest() {
        /* Send the request for status */
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveInfoRequestMsg(this.address);
        msg.setRetries(1); // Since we repeat this ourselves, don't ask the 
        // traffic controller to do this for us.
        // now, we queue the message for sending to the command station
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    /**
     * Send the XpressNet messages to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),0,f0);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEIDLE);
        XNetMessage msg1 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),1,f1);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEIDLE);
        XNetMessage msg2 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),2,f2);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEIDLE);
        XNetMessage msg3 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),3,f3);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEIDLE);
        XNetMessage msg4 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),4,f4);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEIDLE);
    }

    /**
     * Send the XpressNet message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),5,f5);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEIDLE);
        XNetMessage msg1 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),6,f6);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEIDLE);
        XNetMessage msg2 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),7,f7);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEIDLE);
        XNetMessage msg3 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),8,f8);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEIDLE);
    }

    /**
     * Send the XpressNet message to set the state of functions F9, F10, F11,
     * F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),9,f9);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEIDLE);
        XNetMessage msg1 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),10,f10);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEIDLE);
        XNetMessage msg2 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),11,f11);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEIDLE);
        XNetMessage msg3 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),12,f12);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEIDLE);
    }

    /**
     * Send the XpressNet message to set the state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20.
     */
    @Override
    protected void sendFunctionGroup4() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),13,f13);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEIDLE);
        XNetMessage msg1 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),14,f14);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEIDLE);
        XNetMessage msg2 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),15,f15);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEIDLE);
        XNetMessage msg3 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),16,f16);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEIDLE);
        XNetMessage msg4 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),17,f17);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEIDLE);
        XNetMessage msg5 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),18,f18);
        // now, queue the message for sending to the command station
        queueMessage(msg5, THROTTLEIDLE);
        XNetMessage msg6 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),19,f19);
        // now, queue the message for sending to the command station
        queueMessage(msg6, THROTTLEIDLE);
        XNetMessage msg7 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),20,f20);
        // now, queue the message for sending to the command station
        queueMessage(msg7, THROTTLEIDLE);
    }
    /**
     * Send the XpressNet message to set the state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28.
     */
    @Override
    protected void sendFunctionGroup5() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),21,f21);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEIDLE);
        XNetMessage msg1 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),22,f22);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEIDLE);
        XNetMessage msg2 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),23,f23);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEIDLE);
        XNetMessage msg3 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),24,f24);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEIDLE);
        XNetMessage msg4 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),25,f25);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEIDLE);
        XNetMessage msg5 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),26,f26);
        // now, queue the message for sending to the command station
        queueMessage(msg5, THROTTLEIDLE);
        XNetMessage msg6 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),27,f27);
        // now, queue the message for sending to the command station
        queueMessage(msg6, THROTTLEIDLE);
        XNetMessage msg7 = Z21XNetMessage.getZ21LocomotiveFunctionOperationMsg(this.getDccAddress(),28,f28);
        // now, queue the message for sending to the command station
        queueMessage(msg7, THROTTLEIDLE);
    }

    // The Roco Doesn't support the XpressNet directed emergency stop
    // instruction, so override sendEmergencyStop in the parent, and
    // just send speed step 0.
    @Override
    protected void sendEmergencyStop(){
       setSpeedSetting(0);
    }

    // Handle incoming messages for this throttle.
    @Override
    public void message(XNetReply l) {
        log.debug("Throttle {} - received message {}",getDccAddress(),l.toString());
        if((l.getElement(0)&0xE0)==0xE0 && ((l.getElement(0)&0x0f) >= 7 && (l.getElement(0)&0x0f) <=15 )){
            //This is a Roco specific throttle information message.
            //Data Byte 0 and 1 contain the locomotive address
            int messageaddress=((l.getElement(1)&0x3F) << 8)+l.getElement(2);
            if(messageaddress==getDccAddress()){
               //The message is for this throttle.
               int b2= l.getElement(3)&0xff; 
               int b3= l.getElement(4)&0xff; 
               int b4= l.getElement(5)&0xff; 
               int b5= l.getElement(6)&0xff; 
               int b6= l.getElement(7)&0xff; 
               int b7= l.getElement(8)&0xff; 
               // byte 2 contains the speed step mode and availability 
               // information.
               parseSpeedAndAvailability(b2);
               // byte 3 contains the direction and the speed information
               parseSpeedAndDirection(b3);
               // byte 4 contains flags for whether or not the locomotive
               // is in a double header and for smart search.  These aren't used
               // here.

               // byte 4 and 5 contain function information for F0-F12
               parseFunctionInformation(b4,b5);
               // byte 6 and 7 contain function information for F13-F28
               parseFunctionHighInformation(b6,b7);
              
               // set the request state to idle
               requestState = THROTTLEIDLE;
               // and send any queued messages.
               sendQueuedMessage();
           } 
        } else {
            // let the standard XpressNet Throttle have a chance to look
            // at the message.
            super.message(l);
        }
    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     * <p>
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    @Override
    protected void throttleDispose() {
        active = false;
        stopStatusTimer();
        finishRecord();
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(Z21XNetThrottle.class);

}

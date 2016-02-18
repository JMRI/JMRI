package jmri.jmrix.roco.z21;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a z21 XpressnetNet
 * connection.
 *
 * @author Paul Bender (C) 2015
 * @version $Revision$
 */
public class z21XNetThrottle extends jmri.jmrix.lenz.XNetThrottle {

    /**
     * Constructor
     */
    public z21XNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo,controller);
    }

    /**
     * Constructor
     */
    public z21XNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo,address,controller);
    }

    // sendStatusInformation sends a request to get the speed,direction
    // and function status from the command station
    @Override
    synchronized protected void sendStatusInformationRequest() {
        /* Send the request for status */
        XNetMessage msg = z21XNetMessage.getLocomotiveInfoRequestMsg(this.address);
        msg.setRetries(1); // Since we repeat this ourselves, don't ask the 
        // traffic controller to do this for us.
        // now, we queue the message for sending to the command station
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    /**
     * Send the XpressNet messages to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),0,f0);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
        XNetMessage msg1 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),1,f1);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEFUNCSENT);
        XNetMessage msg2 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),2,f2);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEFUNCSENT);
        XNetMessage msg3 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),3,f3);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEFUNCSENT);
        XNetMessage msg4 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),4,f4);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),5,f5);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
        XNetMessage msg1 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),6,f6);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEFUNCSENT);
        XNetMessage msg2 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),7,f7);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEFUNCSENT);
        XNetMessage msg3 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),8,f8);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F9, F10, F11,
     * F12
     */
    @Override
    protected void sendFunctionGroup3() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),9,f9);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
        XNetMessage msg1 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),10,f10);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEFUNCSENT);
        XNetMessage msg2 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),11,f11);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEFUNCSENT);
        XNetMessage msg3 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),12,f12);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),13,f13);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
        XNetMessage msg1 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),14,f14);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEFUNCSENT);
        XNetMessage msg2 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),15,f15);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEFUNCSENT);
        XNetMessage msg3 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),16,f16);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEFUNCSENT);
        XNetMessage msg4 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),17,f17);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEFUNCSENT);
        XNetMessage msg5 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),18,f18);
        // now, queue the message for sending to the command station
        queueMessage(msg5, THROTTLEFUNCSENT);
        XNetMessage msg6 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),19,f19);
        // now, queue the message for sending to the command station
        queueMessage(msg6, THROTTLEFUNCSENT);
        XNetMessage msg7 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),20,f20);
        // now, queue the message for sending to the command station
        queueMessage(msg7, THROTTLEFUNCSENT);
    }
    /**
     * Send the XpressNet message to set the state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        // because of the way the z21 wants to see the functions, we
        // send all the functions when there is a change in the group.
        XNetMessage msg = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),21,f21);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
        XNetMessage msg1 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),22,f22);
        // now, queue the message for sending to the command station
        queueMessage(msg1, THROTTLEFUNCSENT);
        XNetMessage msg2 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),23,f23);
        // now, queue the message for sending to the command station
        queueMessage(msg2, THROTTLEFUNCSENT);
        XNetMessage msg3 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),24,f24);
        // now, queue the message for sending to the command station
        queueMessage(msg3, THROTTLEFUNCSENT);
        XNetMessage msg4 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),25,f25);
        // now, queue the message for sending to the command station
        queueMessage(msg4, THROTTLEFUNCSENT);
        XNetMessage msg5 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),26,f26);
        // now, queue the message for sending to the command station
        queueMessage(msg5, THROTTLEFUNCSENT);
        XNetMessage msg6 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),27,f27);
        // now, queue the message for sending to the command station
        queueMessage(msg6, THROTTLEFUNCSENT);
        XNetMessage msg7 = z21XNetMessage.getLocomotiveFunctionOperationMsg(this.getDccAddress(),28,f28);
        // now, queue the message for sending to the command station
        queueMessage(msg7, THROTTLEFUNCSENT);
    }


    // Handle incoming messages for This throttle.
    @Override
    public void message(XNetReply l) {
        if (log.isDebugEnabled()) 
            log.debug("Throttle " + getDccAddress() + " - recieved message " + l.toString());
        if((l.getElement(0)&0xE0)==0xE0 && (l.getElement(0)&0x0f) >= 7 && (l.getElement(0)&0x0f) <=14 ){
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
               parseSpeedandAvailability(b2); 
               // byte 3 contains the direction and the speed information
               parseSpeedandDirection(b3);
               // byte 4 contains flags for whether or not the locomotive
               // is in a double header and for smart search.  These aren't used
               // here.

               // byte 4 and 5 contain function information for F0-F12
               parseFunctionInformation(b4,b5);
               // byte 6 and 7 contain function information for F13-F28
               parseFunctionHighInformation(b6,b7);
               
                // Always end by setting the state to idle
                // (z21 always responds with the same messge, regardless of
                // request).
                requestState = THROTTLEIDLE;
                // and send any queued messages.
                sendQueuedMessage();
           } 
        } else {
            // let the standard XPressNet Throttle have a chance to look 
            // at the message.
            super.message(l);
        }
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(z21XNetThrottle.class.getName());
}

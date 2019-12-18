package jmri.jmrix.lenz;

import java.util.concurrent.LinkedBlockingQueue;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an XpressNet
 * connection.
 *
 * @author Paul Bender (C) 2002-2019
 */
public class XNetThrottle extends AbstractThrottle implements XNetListener {

    protected boolean isAvailable;  // Flag  stating if the throttle is in use or not.

    protected java.util.TimerTask statusTask;   // Timer Task used to periodically get current
                                                // status of the throttle when throttle not available.
    protected static final int statTimeoutValue = 1000; // Interval to check the 
    protected XNetTrafficController tc = null;

    // status of the throttle
    protected static final int THROTTLEIDLE = 0;  // Idle Throttle
    protected static final int THROTTLESTATSENT = 1;  // Sent Status request
    protected static final int THROTTLESPEEDSENT = 2;  // Sent speed/dir command to locomotive
    protected static final int THROTTLEFUNCSENT = 4;   // Sent a function command to locomotive.
    protected static final int THROTTLEMOMSTATSENT = 8;  // Sent Momentary Status request for F0-F12
    protected static final int THROTTLEHIGHSTATSENT = 16;  // Sent Status request for F13-F28
    protected static final int THROTTLEHIGHMOMSTATSENT = 32;  // Sent Momentary Status request for F13-F28

    protected int requestState = THROTTLEIDLE;

    protected int address;

    /**
     * Constructor
     */
    public XNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo);
        tc = controller;
        requestList = new LinkedBlockingQueue<>();
        log.debug("XNetThrottle constructor");
    }

    /**
     * Constructor
     */
    public XNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo);
        this.tc = controller;
        this.setDccAddress(address.getNumber());
        this.speedStepMode = jmri.SpeedStepMode.NMRA_DCC_128;
        setIsAvailable(false);

        requestList = new LinkedBlockingQueue<>();
        sendStatusInformationRequest();
        log.debug("XNetThrottle constructor called for address {}",address);
    }

    /*
     * Set the traffic controller used with this throttle.
     */
    public void setXNetTrafficController(XNetTrafficController controller) {
        tc = controller;
    }

    /**
     * Send the XpressNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        XNetMessage msg = XNetMessage.getFunctionGroup1OpsMsg(this.getDccAddress(),
                f0, f1, f2, f3, f4);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        XNetMessage msg = XNetMessage.getFunctionGroup2OpsMsg(this.getDccAddress(),
                f5, f6, f7, f8);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F9, F10, F11,
     * F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        XNetMessage msg = XNetMessage.getFunctionGroup3OpsMsg(this.getDccAddress(),
                f9, f10, f11, f12);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    protected boolean csVersionSupportsHighFunctions(){
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version {}",
                    tc.getCommandStation().getCommandStationSoftwareVersion());
            return false;
        }
        return true;
    }

    /**
     * Send the XpressNet message to set the state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20.
     */
    @Override
    protected void sendFunctionGroup4() {
        if (csVersionSupportsHighFunctions()) {
            XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(this.getDccAddress(),
                    f13, f14, f15, f16, f17, f18, f19, f20);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLEFUNCSENT);
        }
    }

    /**
     * Send the XpressNet message to set the state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28.
     */
    @Override
    protected void sendFunctionGroup5() {
        if (csVersionSupportsHighFunctions()) {
            XNetMessage msg = XNetMessage.getFunctionGroup5OpsMsg(this.getDccAddress(),
                    f21, f22, f23, f24, f25, f26, f27, f28);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLEFUNCSENT);
        }
    }

    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendMomentaryFunctionGroup1() {
        XNetMessage msg = XNetMessage.getFunctionGroup1SetMomMsg(this.getDccAddress(),
                f0Momentary, f1Momentary, f2Momentary, f3Momentary, f4Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F5,
     * F6, F7, F8.
     */
    @Override
    protected void sendMomentaryFunctionGroup2() {
        XNetMessage msg = XNetMessage.getFunctionGroup2SetMomMsg(this.getDccAddress(),
                f5Momentary, f6Momentary, f7Momentary, f8Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F9,
     * F10, F11, F12.
     */
    @Override
    protected void sendMomentaryFunctionGroup3() {
        XNetMessage msg = XNetMessage.getFunctionGroup3SetMomMsg(this.getDccAddress(),
                f9Momentary, f10Momentary, f11Momentary, f12Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F13,
     * F14, F15, F16, F17, F18, F19, F20.
     */
    @Override
    protected void sendMomentaryFunctionGroup4() {
        if (csVersionSupportsHighFunctions()) {
            XNetMessage msg = XNetMessage.getFunctionGroup4SetMomMsg(this.getDccAddress(), f13Momentary, f14Momentary,
                    f15Momentary, f16Momentary, f17Momentary, f18Momentary, f19Momentary, f20Momentary);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLEFUNCSENT);
        }
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F21,
     * F22, F23, F24, F25, F26, F27, F28.
     */
    @Override
    protected void sendMomentaryFunctionGroup5() {
        if (csVersionSupportsHighFunctions()) {
            XNetMessage msg = XNetMessage.getFunctionGroup5SetMomMsg(this.getDccAddress(), f21Momentary, f22Momentary,
                    f23Momentary, f24Momentary, f25Momentary, f26Momentary, f27Momentary, f28Momentary);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLEFUNCSENT);
        }
    }

    /**
     * Notify listeners and send the new speed to the command station.
     */
    @Override
    public synchronized void setSpeedSetting(float speed) {
        log.debug("set Speed to: {} Current step mode is: {}",speed,this.speedStepMode);
        super.setSpeedSetting(speed);
        if (speed < 0) {
            /* we're sending an emergency stop to this locomotive only */
            sendEmergencyStop();
        } else {
            if (speed > 1) {
                speed = (float) 1.0;
            }
            /* we're sending a speed to the locomotive */
            XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(getDccAddress(),
                    this.speedStepMode,
                    speed,
                    this.isForward);
            // now, queue the message for sending to the command station
            queueMessage(msg, THROTTLESPEEDSENT);
        }
    }

    /**
     * Since XpressNet has a seperate Opcode for emergency stop,
     * we're setting this up as a seperate protected function.
     */
    protected void sendEmergencyStop() {
        /* Emergency stop sent */
        XNetMessage msg = XNetMessage.getAddressedEmergencyStop(this.getDccAddress());
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLESPEEDSENT);
    }

    /**
     *  When we set the direction, we're going to set the speed to zero as well.
     */
    @Override
    public void setIsForward(boolean forward) {
        super.setIsForward(forward);
        setSpeedSetting(this.speedSetting);
    }

    /**
     * Set the speed step value and the related speedIncrement value.
     *
     * @param Mode  the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        super.setSpeedStepMode(Mode);
        // On a lenz system, we need to send the speed to make sure the 
        // command station knows about the change.
        setSpeedSetting(this.speedSetting);
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

    public int setDccAddress(int newaddress) {
        address = newaddress;
        return address;
    }

    public int getDccAddress() {
        return address;
    }

    protected int getDccAddressHigh() {
        return LenzCommandStation.getDCCAddressHigh(this.address);
    }

    protected int getDccAddressLow() {
        return LenzCommandStation.getDCCAddressLow(this.address);
    }

    /**
     * Send a request to get the speed, direction and function status
     * from the command station.
     */
    protected synchronized void sendStatusInformationRequest() {
        /* Send the request for status */
        XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(this.address);
        msg.setRetries(1); // Since we repeat this ourselves, don't ask the 
        // traffic controller to do this for us.
        // now, we queue the message for sending to the command station
        queueMessage(msg, THROTTLESTATSENT);
    }

    /**
     * Send a request to get the status of functions from the command station.
     */
    protected synchronized void sendFunctionStatusInformationRequest() {
        log.debug("Throttle {} sending request for function momentary status.",address);
        /* Send the request for Function status */
        XNetMessage msg = XNetMessage.getLocomotiveFunctionStatusMsg(this.address);
        queueMessage(msg, ( THROTTLEMOMSTATSENT | THROTTLESTATSENT) );
    }

    /**
     * Send a request to get the on/off status of functions 13-28 from
     * the command station.
     */
    protected synchronized void sendFunctionHighInformationRequest() {
        if (csVersionSupportsHighFunctions()) {
            log.debug("Throttle {} sending request for high function momentary status.", address);
            /* Send the request for Function status */
            XNetMessage msg = XNetMessage.getLocomotiveFunctionHighOnStatusMsg(this.address);
            // now, we send the message to the command station
            queueMessage(msg, THROTTLEHIGHSTATSENT | THROTTLESTATSENT);
        }
    }

    /**
     * Send a request to get the status of functions from the command station.
     */
    protected synchronized void sendFunctionHighMomentaryStatusRequest() {
        if (csVersionSupportsHighFunctions()) {
            log.debug("Throttle {} sending request for function momentary status.", address);
            /* Send the request for Function status */
            XNetMessage msg = XNetMessage.getLocomotiveFunctionHighMomStatusMsg(this.address);
            // now, we send the message to the command station
            queueMessage(msg, (THROTTLEHIGHMOMSTATSENT | THROTTLESTATSENT));
        }
    }

    // Handle incoming messages for This throttle.
    @Override
    public void message(XNetReply l) {
        // First, we want to see if this throttle is waiting for a message 
        //or not.
        log.debug("Throttle {} - received message {}",getDccAddress(),l );
        if (requestState == THROTTLEIDLE) {
            log.trace("Current throttle status is THROTTLEIDLE");
            // We haven't sent anything, but we might be told someone else 
            // has taken over this address
            if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                log.trace("Throttle - message is LOCO_INFO_RESPONSE ");
                if (l.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE &&
                        getDccAddressHigh() == l.getElement(2) &&
                        getDccAddressLow() == l.getElement(3)) {
                        locoInUse();
                }
            }
        } else if ((requestState & THROTTLESPEEDSENT) == THROTTLESPEEDSENT
                || (requestState & THROTTLEFUNCSENT) == THROTTLEFUNCSENT) {
            log.trace("Current throttle status is THROTTLESPEEDSENT");
            // For a Throttle Command, we're just looking for a return 
            // acknowledgment, Either a Success or Failure message.
            if (l.isOkMessage()) {
                log.trace("Last Command processed successfully.");
                // Since we received an "ok",  we want to make sure
                // "isAvailable reflects we are in control
                setIsAvailable(true);
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
            } else if (l.isRetransmittableErrorMsg()) {
                /* this is a communications error */
                log.trace("Communications error occurred - message received was: {}", l);
            } else if (l.isUnsupportedError()) {
                /* The Command Station does not support this command */
                log.error("Unsupported Command Sent to command station");
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
            } else {
                /* this is an unknown error */
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                log.trace("Received unhandled response: {}", l);
            }
        } else if ( ( requestState & THROTTLESTATSENT ) == THROTTLESTATSENT) {
            log.trace("Current throttle status is THROTTLESTATSENT");
            // This throttle has requested status information, so we need 
            // to process those messages. 
            if (l.getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT) {
                if (l.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM) {
                    /* handle information response about F13-F28 Momentary 
                     Status*/
                    log.trace("Throttle - message is LOCO_FUNCTION_STATUS_HIGH_MOM");
                    int b3 = l.getElement(2);
                    int b4 = l.getElement(3);
                    parseFunctionHighMomentaryInformation(b3, b4);
                    //We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                } else {
                    log.trace("Throttle - message is LOCO_INFO_NORMAL_UNIT");
                    /* there is no address sent with this information */
                    int b1 = l.getElement(1);
                    int b2 = l.getElement(2);
                    int b3 = l.getElement(3);
                    int b4 = l.getElement(4);

                    parseSpeedAndAvailability(b1);
                    parseSpeedAndDirection(b2);
                    parseFunctionInformation(b3, b4);

                    //We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                    // And then we want to request the Function Momentary Status
                    sendFunctionStatusInformationRequest();
                }
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
                log.trace("Throttle - message is LOCO_INFO_MUED_UNIT ");
                /* there is no address sent with this information */
                int b1 = l.getElement(1);
                int b2 = l.getElement(2);
                int b3 = l.getElement(3);
                int b4 = l.getElement(4);
                // Element 5 is the consist address, it can only be in the 
                // range 1-99
                int b5 = l.getElement(5);

                log.trace("Locomotive {} inconsist {} ",getDccAddress(),b5);

                parseSpeedAndAvailability(b1);
                parseSpeedAndDirection(b2);
                parseFunctionInformation(b3, b4);

                // We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
                log.trace("Throttle - message is LOCO_INFO_DH_UNIT ");
                /* there is no address sent with this information */
                int b1 = l.getElement(1);
                int b2 = l.getElement(2);
                int b3 = l.getElement(3);
                int b4 = l.getElement(4);

                // elements 5 and 6 contain the address of the other unit 
                // in the DH
                int b5 = l.getElement(5);
                int b6 = l.getElement(6);

                if (log.isDebugEnabled()) {
                    int address2 = (b5 == 0x00) ? b6 : ((b5 * 256) & 0xFF00) + (b6 & 0xFF) - 0xC000;
                    log.trace("Locomotive {} in Double Header with {}",
                               getDccAddress(),address2);
                }

                parseSpeedAndAvailability(b1);
                parseSpeedAndDirection(b2);
                parseFunctionInformation(b3, b4);

                // We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
                log.trace("Throttle - message is LOCO_INFO_MU ADDRESS ");
                /* there is no address sent with this information */
                int b1 = l.getElement(1);
                int b2 = l.getElement(2);

                parseSpeedAndAvailability(b1);
                parseSpeedAndDirection(b2);

                //We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                log.trace("Throttle - message is LOCO_INFO_RESPONSE ");
                if (l.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE) {
                    /* the address is in bytes 3 and 4*/
                    if (getDccAddressHigh() == l.getElement(2) && getDccAddressLow() == l.getElement(3)) {
                        locoInUse();
                    }
                    // We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                } else if (l.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS) {
                    /* Bytes 3 and 4 contain function momentary status information */
                    int b3 = l.getElement(2);
                    int b4 = l.getElement(3);
                    parseFunctionMomentaryInformation(b3, b4);
                    // We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                    // And then we want to request the Function Status for F13-F28
                    sendFunctionHighInformationRequest();

                } else if (l.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH) {
                    /* Bytes 3 and 4 contain function status information for F13-F28*/
                    int b3 = l.getElement(2);
                    int b4 = l.getElement(3);
                    parseFunctionHighInformation(b3, b4);
                    //We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                    // And then we want to request the Function Momentary Status
                    // for functions F13-F28
                    sendFunctionHighMomentaryStatusRequest();
                }
            } else if (l.isRetransmittableErrorMsg()) {
                /* this is a communications error */
                log.trace("Communications error occurred - message received was: {}",l);
            } else if (l.isUnsupportedError()) {
                /* The Command Station does not support this command */
                log.error("Unsupported Command Sent to command station");
                if((requestState & THROTTLEMOMSTATSENT) == THROTTLEMOMSTATSENT){
                   // if momentaty is not supported, try requesting the
                   // high function state.
                   requestState = THROTTLEIDLE;
                   sendFunctionHighInformationRequest();
                } else {
                   requestState = THROTTLEIDLE;
                   sendQueuedMessage();
                }
            } else {
                /* this is an unknown error */
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                log.trace("Received unhandled response: {}",l);
            }
        }
    }

    private void locoInUse(){
        if (isAvailable) {
            //Set the Is available flag to Throttle.False
            log.info("Loco {} In use by another device",getDccAddress());
            setIsAvailable(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(XNetMessage l) {
        // no need to handle outgoing messages.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {} , {} retries available.",
                  msg,msg.getRetries());
        if (msg.getRetries() > 0) {
            // If the message still has retries available, send it back to 
            // the traffic controller.
            tc.sendXNetMessage(msg, this);
        } else {
            // Try to send the next queued message,  if one is available.
            sendQueuedMessage();
        }
    }

    // Status Information processing routines
    // Used for return values from Status requests.

    /**
     * Get SpeedStep and availability information.
     */
    protected void parseSpeedAndAvailability(int b1) {
        /* the first data bite indicates the speed step mode, and
         if the locomotive is being controlled by another throttle */

        if ((b1 & 0x08) == 0x08 && this.isAvailable) {
            locoInUse();
        } else if ((b1 & 0x08) == 0x00 && !this.isAvailable) {
            log.trace("Loco Is Available");
            setIsAvailable(true);
        }
        if ((b1 & 0x01) == 0x01) {
            log.trace("Speed Step setting 27");
            notifyNewSpeedStepMode(SpeedStepMode.NMRA_DCC_27);
        } else if ((b1 & 0x02) == 0x02) {
            log.trace("Speed Step setting 28");
            notifyNewSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        } else if ((b1 & 0x04) == 0x04) {
            log.trace("Speed Step setting 128");
            notifyNewSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
        } else {
            log.trace("Speed Step setting 14");
            notifyNewSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
        }
    }

    protected void notifyNewSpeedStepMode(SpeedStepMode mode){
        if (this.speedStepMode != mode) {
            notifyPropertyChangeListener(SPEEDSTEPS,
                this.speedStepMode,
                this.speedStepMode = mode);
        }
    }

    /**
     * Get Speed and Direction information.
     */
    protected void parseSpeedAndDirection(int b2) {
        /* the second byte indicates the speed and direction setting */

        if ((b2 & 0x80) == 0x80 && !this.isForward) {
            notifyNewDirection(true);
        } else if ((b2 & 0x80) == 0x00 && this.isForward) {
            notifyNewDirection(false);
        }

        if (this.speedStepMode == SpeedStepMode.NMRA_DCC_128) {
            // We're in 128 speed step mode
            int speedVal = b2 & 0x7f;
            // The first speed step used is actually at 2 for 128 
            // speed step mode.
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            if (java.lang.Math.abs(
                    this.getSpeedSetting() - ((float) speedVal / (float) 126)) >= 0.0079) {
                notifyPropertyChangeListener(SPEEDSETTING,
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 126));
            }
        } else if (this.speedStepMode == SpeedStepMode.NMRA_DCC_28) {
            // We're in 28 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            int speedVal = ((b2 & 0x0F) << 1)
                    + ((b2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 28 
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            if (java.lang.Math.abs(
                    this.getSpeedSetting() - ((float) speedVal / (float) 28)) >= 0.035) {
                notifyPropertyChangeListener(SPEEDSETTING,
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 28));
            }
        } else if (this.speedStepMode == SpeedStepMode.NMRA_DCC_27) {
            // We're in 27 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            int speedVal = ((b2 & 0x0F) << 1)
                    + ((b2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 27 
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            if (java.lang.Math.abs(
                    this.getSpeedSetting() - ((float) speedVal / (float) 27)) >= 0.037) {
                notifyPropertyChangeListener(SPEEDSETTING,
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 27));
            }
        } else {
            // Assume we're in 14 speed step mode.
            int speedVal = (b2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            if (java.lang.Math.abs(
                    this.getSpeedSetting() - ((float) speedVal / (float) 14)) >= 0.071) {
                notifyPropertyChangeListener(SPEEDSETTING,
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 14));
            }
        }
    }

    protected void notifyNewDirection(boolean forward){
        notifyPropertyChangeListener(ISFORWARD,
                Boolean.valueOf(this.isForward),
                Boolean.valueOf(this.isForward = forward));
        log.trace("Throttle - Changed direction to {} Locomotive: {}", forward?"forward":"reverse", getDccAddress());
    }


    protected void parseFunctionInformation(int b3, int b4) {
        log.trace("Parsing Function F0-F12 status, function bytes: {} and {}",
                  b3,b4);
        /* data byte 3 is the status of F0 F4 F3 F2 F1 */
        checkForFunctionValueChange(Throttle.F0,b3,0x10,getF0());
        checkForFunctionValueChange(Throttle.F1,b3,0x01,getF1());
        checkForFunctionValueChange(Throttle.F2,b3,0x02,getF2());
        checkForFunctionValueChange(Throttle.F3,b3,0x04,getF3());
        checkForFunctionValueChange(Throttle.F4,b3,0x08,getF4());
        /* data byte 4 is the status of F12 F11 F10 F9 F8 F7 F6 F5 */
        checkForFunctionValueChange(Throttle.F5,b4,0x01,getF5());
        checkForFunctionValueChange(Throttle.F6,b4,0x02,getF6());
        checkForFunctionValueChange(Throttle.F7,b4,0x04,getF7());
        checkForFunctionValueChange(Throttle.F8,b4,0x08,getF8());
        checkForFunctionValueChange(Throttle.F9,b4,0x10,getF9());
        checkForFunctionValueChange(Throttle.F10,b4,0x20,getF10());
        checkForFunctionValueChange(Throttle.F11,b4,0x40,getF11());
        checkForFunctionValueChange(Throttle.F12,b4,0x80,getF12());
    }

    protected void parseFunctionHighInformation(int b3, int b4) {
        log.trace("Parsing Function F13-F28 status, function bytes: {} and {}",
                b3,b4);
        /* data byte 3 is the status of F20 F19 F18 F17 F16 F15 F14 F13 */
        checkForFunctionValueChange(Throttle.F13,b3,0x01,getF13());
        checkForFunctionValueChange(Throttle.F14,b3,0x02,getF14());
        checkForFunctionValueChange(Throttle.F15,b3,0x04,getF15());
        checkForFunctionValueChange(Throttle.F16,b3,0x08,getF16());
        checkForFunctionValueChange(Throttle.F17,b3,0x10,getF17());
        checkForFunctionValueChange(Throttle.F18,b3,0x20,getF18());
        checkForFunctionValueChange(Throttle.F19,b3,0x40,getF19());
        checkForFunctionValueChange(Throttle.F20,b3,0x80,getF20());
        /* data byte 4 is the status of F28 F27 F26 F25 F24 F23 F22 F21 */
        checkForFunctionValueChange(Throttle.F21,b4,0x01,getF21());
        checkForFunctionValueChange(Throttle.F22,b4,0x02,getF22());
        checkForFunctionValueChange(Throttle.F23,b4,0x04,getF23());
        checkForFunctionValueChange(Throttle.F24,b4,0x08,getF24());
        checkForFunctionValueChange(Throttle.F25,b4,0x10,getF25());
        checkForFunctionValueChange(Throttle.F26,b4,0x20,getF26());
        checkForFunctionValueChange(Throttle.F27,b4,0x40,getF27());
        checkForFunctionValueChange(Throttle.F28,b4,0x80,getF28());
    }


    protected void checkForFunctionValueChange(String Function,int bytevalue,int bitmask,boolean currentValue){
        if ((bytevalue & bitmask) == bitmask && !currentValue) {
            notifyFunctionChanged(Function,true);
        } else if ((bytevalue & bitmask) == 0x00 && currentValue) {
            notifyFunctionChanged(Function,false);
        }
    }

    protected void notifyFunctionChanged(String function,boolean newValue){
        switch(function){
            case Throttle.F0:
                notifyPropertyChangeListener(Throttle.F0,
                        Boolean.valueOf(this.f0),
                        Boolean.valueOf(this.f0 = newValue));
                break;
            case Throttle.F1:
                notifyPropertyChangeListener(Throttle.F1,
                        Boolean.valueOf(this.f1),
                        Boolean.valueOf(this.f1 = newValue));
                break;
            case Throttle.F2:
                notifyPropertyChangeListener(Throttle.F2,
                        Boolean.valueOf(this.f2),
                        Boolean.valueOf(this.f2 = newValue));
                break;
            case Throttle.F3:
                notifyPropertyChangeListener(Throttle.F3,
                        Boolean.valueOf(this.f3),
                        Boolean.valueOf(this.f3 = newValue));
                break;
            case Throttle.F4:
                notifyPropertyChangeListener(Throttle.F4,
                        Boolean.valueOf(this.f4),
                        Boolean.valueOf(this.f4 = newValue));
                break;
            case Throttle.F5:
                notifyPropertyChangeListener(Throttle.F5,
                        Boolean.valueOf(this.f5),
                        Boolean.valueOf(this.f5 = newValue));
                break;
            case Throttle.F6:
                notifyPropertyChangeListener(Throttle.F6,
                        Boolean.valueOf(this.f6),
                        Boolean.valueOf(this.f6 = newValue));
                break;
            case Throttle.F7:
                notifyPropertyChangeListener(Throttle.F7,
                        Boolean.valueOf(this.f7),
                        Boolean.valueOf(this.f7 = newValue));
                break;
            case Throttle.F8:
                notifyPropertyChangeListener(Throttle.F8,
                        Boolean.valueOf(this.f8),
                        Boolean.valueOf(this.f8 = newValue));
                break;
            case Throttle.F9:
                notifyPropertyChangeListener(Throttle.F8,
                        Boolean.valueOf(this.f9),
                        Boolean.valueOf(this.f9 = newValue));
                break;
            case Throttle.F10:
                notifyPropertyChangeListener(Throttle.F10,
                        Boolean.valueOf(this.f10),
                        Boolean.valueOf(this.f10 = newValue));
                break;
            case Throttle.F11:
                notifyPropertyChangeListener(Throttle.F11,
                        Boolean.valueOf(this.f11),
                        Boolean.valueOf(this.f11 = newValue));
                break;
            case Throttle.F12:
                notifyPropertyChangeListener(Throttle.F12,
                        Boolean.valueOf(this.f12),
                        Boolean.valueOf(this.f12 = newValue));
                break;
            case Throttle.F13:
                notifyPropertyChangeListener(Throttle.F13,
                        Boolean.valueOf(this.f13),
                        Boolean.valueOf(this.f13 = newValue));
                break;
            case Throttle.F14:
                notifyPropertyChangeListener(Throttle.F14,
                        Boolean.valueOf(this.f14),
                        Boolean.valueOf(this.f14 = newValue));
                break;
            case Throttle.F15:
                notifyPropertyChangeListener(Throttle.F15,
                        Boolean.valueOf(this.f15),
                        Boolean.valueOf(this.f15 = newValue));
                break;
            case Throttle.F16:
                notifyPropertyChangeListener(Throttle.F16,
                        Boolean.valueOf(this.f16),
                        Boolean.valueOf(this.f16 = newValue));
                break;
            case Throttle.F17:
                notifyPropertyChangeListener(Throttle.F17,
                        Boolean.valueOf(this.f17),
                        Boolean.valueOf(this.f17 = newValue));
                break;
            case Throttle.F18:
                notifyPropertyChangeListener(Throttle.F18,
                        Boolean.valueOf(this.f18),
                        Boolean.valueOf(this.f18 = newValue));
                break;
            case Throttle.F19:
                notifyPropertyChangeListener(Throttle.F19,
                        Boolean.valueOf(this.f19),
                        Boolean.valueOf(this.f19 = newValue));
                break;
            case Throttle.F20:
                notifyPropertyChangeListener(Throttle.F20,
                        Boolean.valueOf(this.f20),
                        Boolean.valueOf(this.f20 = newValue));
                break;
            case Throttle.F21:
                notifyPropertyChangeListener(Throttle.F21,
                        Boolean.valueOf(this.f21),
                        Boolean.valueOf(this.f21 = newValue));
                break;
            case Throttle.F22:
                notifyPropertyChangeListener(Throttle.F22,
                        Boolean.valueOf(this.f22),
                        Boolean.valueOf(this.f22 = newValue));
                break;
            case Throttle.F23:
                notifyPropertyChangeListener(Throttle.F23,
                        Boolean.valueOf(this.f23),
                        Boolean.valueOf(this.f23 = newValue));
                break;
            case Throttle.F24:
                notifyPropertyChangeListener(Throttle.F24,
                        Boolean.valueOf(this.f24),
                        Boolean.valueOf(this.f24 = newValue));
                break;
            case Throttle.F25:
                notifyPropertyChangeListener(Throttle.F25,
                        Boolean.valueOf(this.f25),
                        Boolean.valueOf(this.f25 = newValue));
                break;
            case Throttle.F26:
                notifyPropertyChangeListener(Throttle.F26,
                        Boolean.valueOf(this.f26),
                        Boolean.valueOf(this.f26 = newValue));
                break;
            case Throttle.F27:
                notifyPropertyChangeListener(Throttle.F27,
                        Boolean.valueOf(this.f27),
                        Boolean.valueOf(this.f27 = newValue));
                break;
            case Throttle.F28:
                notifyPropertyChangeListener(Throttle.F28,
                        Boolean.valueOf(this.f28),
                        Boolean.valueOf(this.f28 = newValue));
                break;
            default:
                log.trace("Attempt to set unknonw function {} to {}",function,newValue);
        }
    }

    protected void parseFunctionMomentaryInformation(int b3, int b4) {
        log.trace("Parsing Function Momentary status, function bytes: {} and {}",
                  b3,b4);
        /* data byte 3 is the momentary status of F0 F4 F3 F2 F1 */
        checkForFunctionValueChange(Throttle.F0Momentary,b3,0x10,getF0Momentary());
        checkForFunctionValueChange(Throttle.F1Momentary,b3,0x01,getF1Momentary());
        checkForFunctionValueChange(Throttle.F2Momentary,b3,0x02,getF2Momentary());
        checkForFunctionValueChange(Throttle.F3Momentary,b3,0x04,getF3Momentary());
        checkForFunctionValueChange(Throttle.F4Momentary,b3,0x08,getF4Momentary());
        /* data byte 4 is the momentary status of F12 F11 F10 F9 F8 F7 F6 F5 */
        checkForFunctionValueChange(Throttle.F5Momentary,b4,0x01,getF5Momentary());
        checkForFunctionValueChange(Throttle.F6Momentary,b4,0x02,getF6Momentary());
        checkForFunctionValueChange(Throttle.F7Momentary,b4,0x04,getF7Momentary());
        checkForFunctionValueChange(Throttle.F8Momentary,b4,0x08,getF8Momentary());
        checkForFunctionValueChange(Throttle.F9Momentary,b4,0x10,getF9Momentary());
        checkForFunctionValueChange(Throttle.F10Momentary,b4,0x20,getF10Momentary());
        checkForFunctionValueChange(Throttle.F11Momentary,b4,0x40,getF11Momentary());
        checkForFunctionValueChange(Throttle.F12Momentary,b4,0x80,getF12Momentary());
    }

    protected void parseFunctionHighMomentaryInformation(int b3, int b4) {
        log.trace("Parsing Function F13-F28 Momentary status, function bytes: {} and {}",
                  b3,b4);
        /* data byte 3 is the momentary status of F20 F19 F17 F16 F15 F14 F13 */
        checkForFunctionValueChange(Throttle.F14Momentary,b3,0x02,getF14Momentary());
        checkForFunctionValueChange(Throttle.F15Momentary,b3,0x04,getF15Momentary());
        checkForFunctionValueChange(Throttle.F16Momentary,b3,0x08,getF16Momentary());
        checkForFunctionValueChange(Throttle.F17Momentary,b3,0x10,getF17Momentary());
        checkForFunctionValueChange(Throttle.F18Momentary,b3,0x20,getF18Momentary());
        checkForFunctionValueChange(Throttle.F19Momentary,b3,0x40,getF19Momentary());
        checkForFunctionValueChange(Throttle.F20Momentary,b3,0x80,getF20Momentary());
        /* data byte 4 is the momentary status of F28 F27 F26 F25 F24 F23 F22 F21 */
        checkForFunctionValueChange(Throttle.F21Momentary,b4,0x01,getF21Momentary());
        checkForFunctionValueChange(Throttle.F22Momentary,b4,0x02,getF22Momentary());
        checkForFunctionValueChange(Throttle.F23Momentary,b4,0x04,getF23Momentary());
        checkForFunctionValueChange(Throttle.F24Momentary,b4,0x08,getF24Momentary());
        checkForFunctionValueChange(Throttle.F25Momentary,b4,0x10,getF25Momentary());
        checkForFunctionValueChange(Throttle.F26Momentary,b4,0x20,getF26Momentary());
        checkForFunctionValueChange(Throttle.F27Momentary,b4,0x40,getF27Momentary());
        checkForFunctionValueChange(Throttle.F28Momentary,b4,0x80,getF28Momentary());
    }

    protected void checkForFunctionMomentaryValueChange(String Function,int bytevalue,int bitmask,boolean currentValue){
        if ((bytevalue & bitmask) == bitmask && !currentValue) {
            notifyFunctionMomentaryChanged(Function,true);
        } else if ((bytevalue & bitmask) == 0x00 && currentValue) {
            notifyFunctionMomentaryChanged(Function,false);
        }
    }

    protected void notifyFunctionMomentaryChanged(String function,boolean newValue) {
        switch (function) {
            case Throttle.F0Momentary:
                notifyPropertyChangeListener(Throttle.F0Momentary, Boolean.valueOf(this.f0Momentary), Boolean.valueOf(this.f0Momentary = newValue));
                break;
            case Throttle.F1Momentary:
                notifyPropertyChangeListener(Throttle.F1Momentary, Boolean.valueOf(this.f1Momentary), Boolean.valueOf(this.f1Momentary = newValue));
                break;
            case Throttle.F2Momentary:
                notifyPropertyChangeListener(Throttle.F2Momentary, Boolean.valueOf(this.f2Momentary), Boolean.valueOf(this.f2Momentary = newValue));
                break;
            case Throttle.F3Momentary:
                notifyPropertyChangeListener(Throttle.F3Momentary, Boolean.valueOf(this.f3Momentary), Boolean.valueOf(this.f3Momentary = newValue));
                break;
            case Throttle.F4Momentary:
                notifyPropertyChangeListener(Throttle.F4Momentary, Boolean.valueOf(this.f4Momentary), Boolean.valueOf(this.f4Momentary = newValue));
                break;
            case Throttle.F5Momentary:
                notifyPropertyChangeListener(Throttle.F5Momentary, Boolean.valueOf(this.f5Momentary), Boolean.valueOf(this.f5Momentary = newValue));
                break;
            case Throttle.F6Momentary:
                notifyPropertyChangeListener(Throttle.F6Momentary, Boolean.valueOf(this.f6Momentary), Boolean.valueOf(this.f6Momentary = newValue));
                break;
            case Throttle.F7Momentary:
                notifyPropertyChangeListener(Throttle.F7Momentary, Boolean.valueOf(this.f7Momentary), Boolean.valueOf(this.f7Momentary = newValue));
                break;
            case Throttle.F8Momentary:
                notifyPropertyChangeListener(Throttle.F8Momentary, Boolean.valueOf(this.f8Momentary), Boolean.valueOf(this.f8Momentary = newValue));
                break;
            case Throttle.F9Momentary:
                notifyPropertyChangeListener(Throttle.F8Momentary, Boolean.valueOf(this.f9Momentary), Boolean.valueOf(this.f9Momentary = newValue));
                break;
            case Throttle.F10Momentary:
                notifyPropertyChangeListener(Throttle.F10Momentary, Boolean.valueOf(this.f10Momentary), Boolean.valueOf(this.f10Momentary = newValue));
                break;
            case Throttle.F11Momentary:
                notifyPropertyChangeListener(Throttle.F11Momentary, Boolean.valueOf(this.f11Momentary), Boolean.valueOf(this.f11Momentary = newValue));
                break;
            case Throttle.F12Momentary:
                notifyPropertyChangeListener(Throttle.F12Momentary, Boolean.valueOf(this.f12Momentary), Boolean.valueOf(this.f12Momentary = newValue));
                break;
            case Throttle.F13Momentary:
                notifyPropertyChangeListener(Throttle.F13Momentary, Boolean.valueOf(this.f13Momentary), Boolean.valueOf(this.f13Momentary = newValue));
                break;
            case Throttle.F14Momentary:
                notifyPropertyChangeListener(Throttle.F14Momentary, Boolean.valueOf(this.f14Momentary), Boolean.valueOf(this.f14Momentary = newValue));
                break;
            case Throttle.F15Momentary:
                notifyPropertyChangeListener(Throttle.F15Momentary, Boolean.valueOf(this.f15Momentary), Boolean.valueOf(this.f15Momentary = newValue));
                break;
            case Throttle.F16Momentary:
                notifyPropertyChangeListener(Throttle.F16Momentary, Boolean.valueOf(this.f16Momentary), Boolean.valueOf(this.f16Momentary = newValue));
                break;
            case Throttle.F17Momentary:
                notifyPropertyChangeListener(Throttle.F17Momentary, Boolean.valueOf(this.f17Momentary), Boolean.valueOf(this.f17Momentary = newValue));
                break;
            case Throttle.F18Momentary:
                notifyPropertyChangeListener(Throttle.F18Momentary, Boolean.valueOf(this.f18Momentary), Boolean.valueOf(this.f18Momentary = newValue));
                break;
            case Throttle.F19Momentary:
                notifyPropertyChangeListener(Throttle.F19Momentary, Boolean.valueOf(this.f19Momentary), Boolean.valueOf(this.f19Momentary = newValue));
                break;
            case Throttle.F20Momentary:
                notifyPropertyChangeListener(Throttle.F20Momentary, Boolean.valueOf(this.f20Momentary), Boolean.valueOf(this.f20Momentary = newValue));
                break;
            case Throttle.F21Momentary:
                notifyPropertyChangeListener(Throttle.F21Momentary, Boolean.valueOf(this.f21Momentary), Boolean.valueOf(this.f21Momentary = newValue));
                break;
            case Throttle.F22Momentary:
                notifyPropertyChangeListener(Throttle.F22Momentary, Boolean.valueOf(this.f22Momentary), Boolean.valueOf(this.f22Momentary = newValue));
                break;
            case Throttle.F23Momentary:
                notifyPropertyChangeListener(Throttle.F23Momentary, Boolean.valueOf(this.f23Momentary), Boolean.valueOf(this.f23Momentary = newValue));
                break;
            case Throttle.F24Momentary:
                notifyPropertyChangeListener(Throttle.F24Momentary, Boolean.valueOf(this.f24Momentary), Boolean.valueOf(this.f24Momentary = newValue));
                break;
            case Throttle.F25Momentary:
                notifyPropertyChangeListener(Throttle.F25Momentary, Boolean.valueOf(this.f25Momentary), Boolean.valueOf(this.f25Momentary = newValue));
                break;
            case Throttle.F26Momentary:
                notifyPropertyChangeListener(Throttle.F26Momentary, Boolean.valueOf(this.f26Momentary), Boolean.valueOf(this.f26Momentary = newValue));
                break;
            case Throttle.F27Momentary:
                notifyPropertyChangeListener(Throttle.F27Momentary, Boolean.valueOf(this.f27Momentary), Boolean.valueOf(this.f27Momentary = newValue));
                break;
            case Throttle.F28Momentary:
                notifyPropertyChangeListener(Throttle.F28Momentary, Boolean.valueOf(this.f28Momentary), Boolean.valueOf(this.f28Momentary = newValue));
                break;
            default:
                log.trace("Attempt to set unknonw function {} to {}", function, newValue);
        }
    }

    /**
     * Set the internal isAvailable property.
     */
    protected void setIsAvailable(boolean Available) {
        if (this.isAvailable != Available) {
            notifyPropertyChangeListener("IsAvailable",
                    Boolean.valueOf(this.isAvailable),
                    Boolean.valueOf(this.isAvailable = Available));
        }
        /* if we're setting this to true, stop the timer,
         otherwise start the timer. */
        if (Available) {
            stopStatusTimer();
        } else {
            startStatusTimer();
        }
    }

    /**
     * Set up the status timer, and start it.
     */
    protected void startStatusTimer() {
        log.debug("Status Timer Started");

        if (statusTask != null) {
            statusTask.cancel();
            statusTask = null;
        }
        statusTask = new java.util.TimerTask() {
            @Override
            public void run() {
                /* If the timer times out, just send a status 
                 request message */
                sendStatusInformationRequest();
            }
        };
        
        jmri.util.TimerUtil.schedule(statusTask, statTimeoutValue, statTimeoutValue);
    }

    /**
     * Stop the Status Timer 
     */
    protected void stopStatusTimer() {
        log.debug("Status Timer Stopped");
        if (statusTask != null) {
            statusTask.cancel();
            statusTask = null;
        }
    }

    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, XNetThrottleManager.isLongAddress(address));
    }

    // A queue to hold outstanding messages
    protected LinkedBlockingQueue<RequestMessage> requestList = null;

    /**
     * Send message from queue.
     */
    protected synchronized void sendQueuedMessage() {

        RequestMessage msg = null;
        // check to see if the queue has a message in it, and if it does,
        // remove the first message
        if (!requestList.isEmpty()) {
            log.debug("sending message to traffic controller");
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
                tc.sendXNetMessage(msg.getMsg(), this);
            }
        } else {
            log.debug("message queue empty");
            // if the queue is empty, set the state to idle.
            requestState = THROTTLEIDLE;
        }
    }

    /**
     * Queue a message.
     */
    protected synchronized void queueMessage(XNetMessage m, int s) {
        log.debug("adding message to message queue");
        // put the message in the queue
        RequestMessage msg = new RequestMessage(m, s);
        try {
            requestList.put(msg);
        } catch (java.lang.InterruptedException ie) {
            log.trace("Interrupted while queueing message {}",msg);
        }
        // if the state is idle, trigger the message send
        if (requestState == THROTTLEIDLE) {
            sendQueuedMessage();
        }
    }

    /**
     * Internal class to hold a request message, along with the associated throttle state.
     */
    protected static class RequestMessage {

        private int state;
        private XNetMessage msg;

        RequestMessage(XNetMessage m, int s) {
            state = s;
            msg = m;
        }

        int getState() {
            return state;
        }

        XNetMessage getMsg() {
            return msg;
        }
    }

    // register for notification
    private static final Logger log = LoggerFactory.getLogger(XNetThrottle.class);

}

package jmri.jmrix.lenz;

import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a XpressnetNet
 * connection.
 *
 * @author Paul Bender (C) 2002-2010
 * @author Giorgio Terdina (C) 2007
 * @version $Revision$
 */
public class XNetThrottle extends AbstractThrottle implements XNetListener {

    protected boolean isAvailable;  // Flag  stating if the throttle is in 
    // use or not.
    protected java.util.TimerTask statusTask; // Timer Task used to 
    // periodically get 
    // current status of the 
    // throttle when throttle 
    // not available.
    protected static final int statTimeoutValue = 1000; // Interval to check the 
    protected XNetTrafficController tc = null;

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
    public XNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo);
        tc = controller;
        requestList = new LinkedBlockingQueue<RequestMessage>();
        if (log.isDebugEnabled()) {
            log.debug("XnetThrottle constructor");
        }
    }

    /**
     * Constructor
     */
    public XNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo);
        this.tc = controller;
        this.setDccAddress(address.getNumber());
        this.speedIncrement = SPEED_STEP_128_INCREMENT;
        this.speedStepMode = DccThrottle.SpeedStepMode128;
        //       this.isForward=true;
        setIsAvailable(false);

        f0Momentary = f1Momentary = f2Momentary = f3Momentary = f4Momentary
                = f5Momentary = f6Momentary = f7Momentary = f8Momentary = f9Momentary
                = f10Momentary = f11Momentary = f12Momentary = false;

        requestList = new LinkedBlockingQueue<RequestMessage>();
        sendStatusInformationRequest();
        if (log.isDebugEnabled()) {
            log.debug("XnetThrottle constructor called for address " + address);
        }
    }

    /*
     *  Set the traffic controller used with this throttle
     */
    public void setXNetTrafficController(XNetTrafficController controller) {
        tc = controller;
    }

    /**
     * Send the XpressNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        XNetMessage msg = XNetMessage.getFunctionGroup1OpsMsg(this.getDccAddress(),
                f0, f1, f2, f3, f4);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the state of functions F5, F6, F7, F8
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
     * F12
     */
    @Override
    protected void sendFunctionGroup3() {
        XNetMessage msg = XNetMessage.getFunctionGroup3OpsMsg(this.getDccAddress(),
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
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(this.getDccAddress(),
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
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup5OpsMsg(this.getDccAddress(),
                f21, f22, f23, f24, f25, f26, f27, f28);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4
     */
    protected void sendMomentaryFunctionGroup1() {
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup1SetMomMsg(this.getDccAddress(),
                f0Momentary, f1Momentary, f2Momentary, f3Momentary, f4Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F5,
     * F6, F7, F8
     */
    protected void sendMomentaryFunctionGroup2() {
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup2SetMomMsg(this.getDccAddress(),
                f5Momentary, f6Momentary, f7Momentary, f8Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F9,
     * F10, F11, F12
     */
    protected void sendMomentaryFunctionGroup3() {
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup2SetMomMsg(this.getDccAddress(),
                f9Momentary, f10Momentary, f11Momentary, f12Momentary);
        // now, queue the message for sending to the command station
        queueMessage(msg, THROTTLEFUNCSENT);
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F13,
     * F14, F15, F16 F17 F18 F19 F20
     */
    protected void sendMomentaryFunctionGroup4() {
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup4SetMomMsg(this.getDccAddress(),
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
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        XNetMessage msg = XNetMessage.getFunctionGroup5SetMomMsg(this.getDccAddress(),
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
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // MultiMaus doesn't support emergency off.  When -1 is
            // sent, set the speed to 0 instead.
            if (speed < 0) {
                speed = 0;
            }
        }
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

    /* Since xpressnet has a seperate Opcode for emergency stop,
     * We're setting this up as a seperate protected function
     */
    protected void sendEmergencyStop() {
        /* Emergency stop sent */
        XNetMessage msg = XNetMessage.getAddressedEmergencyStop(this.getDccAddress());
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

    protected int getDccAddressHigh() {
        return LenzCommandStation.getDCCAddressHigh(this.address);
    }

    protected int getDccAddressLow() {
        return LenzCommandStation.getDCCAddressLow(this.address);
    }

    // sendStatusInformation sends a request to get the speed,direction
    // and function status from the command station
    synchronized protected void sendStatusInformationRequest() {
        /* Send the request for status */
        XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(this.address);
        msg.setRetries(1); // Since we repeat this ourselves, don't ask the 
        // traffic controller to do this for us.
        // now, we queue the message for sending to the command station
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    // sendFunctionStatusInformation sends a request to get the status
    // of functions from the command station
    synchronized protected void sendFunctionStatusInformationRequest() {
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Throttle " + address + " sending request for function momentary status.");
        }
        /* Send the request for Function status */
        XNetMessage msg = XNetMessage.getLocomotiveFunctionStatusMsg(this.address);
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    // sendFunctionHighInformationRequest sends a request to get the on/off
    // status of functions 13-28 from the command station
    synchronized protected void sendFunctionHighInformationRequest() {
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Throttle " + address + " sending request for function momentary status.");
        }
        /* Send the request for Function status */
        XNetMessage msg = XNetMessage.getLocomotiveFunctionHighOnStatusMsg(this.address);
        // now, we send the message to the command station
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    // sendFunctionHighomentaryStatusRequest sends a request to get the status
    // of functions from the command station
    synchronized protected void sendFunctionHighMomentaryStatusRequest() {
        if (tc.getCommandStation().getCommandStationSoftwareVersionBCD() < 0x36) {
            log.info("Functions F13-F28 unavailable in CS software version "
                    + tc.getCommandStation().getCommandStationSoftwareVersion());
            return;
        }
        if (tc.getCommandStation().getCommandStationType() == 0x10) {
            // if the command station is multimouse, ignore
            if (log.isDebugEnabled()) {
                log.debug("Command station does not support Momentary functions");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Throttle " + address + " sending request for function momentary status.");
        }
        /* Send the request for Function status */
        XNetMessage msg = XNetMessage.getLocomotiveFunctionHighMomStatusMsg(this.address);
        // now, we send the message to the command station
        queueMessage(msg, THROTTLESTATSENT);
        return;
    }

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement() {
        return speedIncrement;
    }

    // Handle incoming messages for This throttle.
    public void message(XNetReply l) {
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
            if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_RESPONSE ");
                }
                if (l.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE) {
                    /* the address is in bytes 3 and 4*/
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
        } else if ((requestState & THROTTLESPEEDSENT) == THROTTLESPEEDSENT
                || (requestState & THROTTLEFUNCSENT) == THROTTLEFUNCSENT) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLESPEEDSENT");
            }
            // For a Throttle Command, we're just looking for a return 
            // acknowledgment, Either a Success or Failure message.
            if (l.isOkMessage()) {
                if (log.isDebugEnabled()) {
                    log.debug("Last Command processed successfully.");
                }
                // Since we recieved an "ok",  we want to make sure 
                // "isAvailable reflects we are in control
                setIsAvailable(true);
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
            } else if (l.isRetransmittableErrorMsg()) {
                /* this is a communications error */
                log.debug("Communications error occured - message recieved was: " + l);
            } else if (l.getElement(0) == XNetConstants.CS_INFO
                    && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                /* The Command Station does not support this command */
                log.error("Unsupported Command Sent to command station");
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
            } else {
                /* this is an unknown error */
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                log.debug("Received unhandled response: " + l);
            }
        } else if (requestState == THROTTLESTATSENT) {
            if (log.isDebugEnabled()) {
                log.debug("Current throttle status is THROTTLESTATSENT");
            }
            // This throttle has requested status information, so we need 
            // to process those messages.	
            if (l.getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT) {
                if (l.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM) {
                    /* handle information response about F13-F28 Momentary 
                     Status*/
                    if (log.isDebugEnabled()) {
                        log.debug("Throttle - message is LOCO_FUNCTION_STATUS_HIGH_MOM");
                    }
                    int b3 = l.getElement(2);
                    int b4 = l.getElement(3);
                    parseFunctionHighMomentaryInformation(b3, b4);
                    //We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Throttle - message is LOCO_INFO_NORMAL_UNIT");
                    }
                    /* there is no address sent with this information */
                    int b1 = l.getElement(1);
                    int b2 = l.getElement(2);
                    int b3 = l.getElement(3);
                    int b4 = l.getElement(4);

                    parseSpeedandAvailability(b1);
                    parseSpeedandDirection(b2);
                    parseFunctionInformation(b3, b4);

                    //We've processed this request, so set the status to Idle.
                    requestState = THROTTLEIDLE;
                    sendQueuedMessage();
                    // And then we want to request the Function Momentary Status
                    sendFunctionStatusInformationRequest();
                    return;
                }
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_MUED_UNIT ");
                }
                /* there is no address sent with this information */
                int b1 = l.getElement(1);
                int b2 = l.getElement(2);
                int b3 = l.getElement(3);
                int b4 = l.getElement(4);
                // Element 5 is the consist address, it can only be in the 
                // range 1-99
                int b5 = l.getElement(5);

                if (log.isDebugEnabled()) {
                    log.debug("Locomotive " + getDccAddress() + "in consist " + b5);
                }

                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);
                parseFunctionInformation(b3, b4);

                // We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_DH_UNIT ");
                }
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
                    log.debug("Locomotive " + getDccAddress()
                            + "in Double Header with " + address2);
                }

                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);
                parseFunctionInformation(b3, b4);

                // We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_MU ADDRESS ");
                }
                /* there is no address sent with this information */
                int b1 = l.getElement(1);
                int b2 = l.getElement(2);

                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);

                //We've processed this request, so set the status to Idle.
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
            } else if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - message is LOCO_INFO_RESPONSE ");
                }
                if (l.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE) {
                    /* the address is in bytes 3 and 4*/
                    if (getDccAddressHigh() == l.getElement(2) && getDccAddressLow() == l.getElement(3)) {
                        //Set the Is available flag to Throttle.False
                        log.info("Loco " + getDccAddress() + " In use by another device");
                        setIsAvailable(false);
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
                log.debug("Communications error occured - message received was: " + l);
            } else if (l.getElement(0) == XNetConstants.CS_INFO
                    && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                /* The Command Station does not support this command */
                log.error("Unsupported Command Sent to command station");
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
            } else {
                /* this is an unknown error */
                requestState = THROTTLEIDLE;
                sendQueuedMessage();
                log.debug("Received unhandled response: " + l);
            }
        }
        //requestState=THROTTLEIDLE;
        //sendQueuedMessage();
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString() + " , " + msg.getRetries() + " retries available.");
        }
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
    //Get SpeedStep and availability information
    protected void parseSpeedandAvailability(int b1) {
        /* the first data bite indicates the speed step mode, and
         if the locomotive is being controlled by another throttle */

        if ((b1 & 0x08) == 0x08 && this.isAvailable) {
            log.info("Loco " + getDccAddress() + " In use by another device");
            setIsAvailable(false);
        } else if ((b1 & 0x08) == 0x00 && !this.isAvailable) {
            if (log.isDebugEnabled()) {
                log.debug("Loco Is Available");
            }
            setIsAvailable(true);
        }
        if ((b1 & 0x01) == 0x01) {
            if (log.isDebugEnabled()) {
                log.debug("Speed Step setting 27");
            }
            this.speedIncrement = SPEED_STEP_27_INCREMENT;
            if (this.speedStepMode != DccThrottle.SpeedStepMode27) {
                notifyPropertyChangeListener("SpeedSteps",
                        Integer.valueOf(this.speedStepMode),
                        Integer.valueOf(this.speedStepMode = DccThrottle.SpeedStepMode27));
            }
        } else if ((b1 & 0x02) == 0x02) {
            if (log.isDebugEnabled()) {
                log.debug("Speed Step setting 28");
            }
            this.speedIncrement = SPEED_STEP_28_INCREMENT;
            if (this.speedStepMode != DccThrottle.SpeedStepMode28) {
                notifyPropertyChangeListener("SpeedSteps",
                        Integer.valueOf(this.speedStepMode),
                        Integer.valueOf(this.speedStepMode = DccThrottle.SpeedStepMode28));
            }
        } else if ((b1 & 0x04) == 0x04) {
            if (log.isDebugEnabled()) {
                log.debug("Speed Step setting 128");
            }
            this.speedIncrement = SPEED_STEP_128_INCREMENT;
            if (this.speedStepMode != DccThrottle.SpeedStepMode128) {
                notifyPropertyChangeListener("SpeedSteps",
                        Integer.valueOf(this.speedStepMode),
                        Integer.valueOf(this.speedStepMode = DccThrottle.SpeedStepMode128));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Speed Step setting 14");
            }
            this.speedIncrement = SPEED_STEP_14_INCREMENT;
            if (this.speedStepMode != DccThrottle.SpeedStepMode14) {
                notifyPropertyChangeListener("SpeedSteps",
                        Integer.valueOf(this.speedStepMode),
                        Integer.valueOf(this.speedStepMode = DccThrottle.SpeedStepMode14));
            }
        }
    }

    //Get Speed and Direction information
    protected void parseSpeedandDirection(int b2) {
        /* the second byte indicates the speed and direction setting */

        if ((b2 & 0x80) == 0x80 && this.isForward == false) {
            if (log.isDebugEnabled()) {
                log.debug("Throttle - Direction Forward Locomotive:" + address);
            }
            notifyPropertyChangeListener("IsForward",
                    Boolean.valueOf(this.isForward),
                    Boolean.valueOf(this.isForward = true));
            if (this.isForward == true) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - Changed direction to Forward Locomotive:" + address);
                }
            }
        } else if ((b2 & 0x80) == 0x00 && this.isForward == true) {
            if (log.isDebugEnabled()) {
                log.debug("Throttle - Direction Reverse Locomotive:" + address);
            }
            notifyPropertyChangeListener("IsForward",
                    Boolean.valueOf(this.isForward),
                    Boolean.valueOf(this.isForward = false));
            if (this.isForward == false) {
                if (log.isDebugEnabled()) {
                    log.debug("Throttle - Changed direction to Reverse Locomotive:" + address);
                }
            }
        }

        if (this.speedStepMode == DccThrottle.SpeedStepMode128) {
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
                notifyPropertyChangeListener("SpeedSetting",
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 126));
            }
        } else if (this.speedStepMode == DccThrottle.SpeedStepMode28) {
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
                notifyPropertyChangeListener("SpeedSetting",
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 28));
            }
        } else if (this.speedStepMode == DccThrottle.SpeedStepMode27) {
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
                notifyPropertyChangeListener("SpeedSetting",
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
                notifyPropertyChangeListener("SpeedSetting",
                        Float.valueOf(this.speedSetting),
                        Float.valueOf(this.speedSetting
                                = (float) speedVal / (float) 14));
            }
        }
    }

    protected void parseFunctionInformation(int b3, int b4) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing Function F0-F12 status, function bytes: " + b3 + " and " + b4);
        }
        /* data byte 3 is the status of F0 F4 F3 F2 F1 */
        if ((b3 & 0x10) == 0x10 && getF0() == false) {
            notifyPropertyChangeListener(Throttle.F0,
                    Boolean.valueOf(this.f0),
                    Boolean.valueOf(this.f0 = true));
        } else if ((b3 & 0x10) == 0x00 && getF0() == true) {
            notifyPropertyChangeListener(Throttle.F0,
                    Boolean.valueOf(this.f0),
                    Boolean.valueOf(this.f0 = false));
        }

        if ((b3 & 0x01) == 0x01 && getF1() == false) {
            notifyPropertyChangeListener(Throttle.F1,
                    Boolean.valueOf(this.f1),
                    Boolean.valueOf(this.f1 = true));
        } else if ((b3 & 0x01) == 0x00 && getF1() == true) {
            notifyPropertyChangeListener(Throttle.F1,
                    Boolean.valueOf(this.f1),
                    Boolean.valueOf(this.f1 = false));
        }

        if ((b3 & 0x02) == 0x02 && getF2() == false) {
            notifyPropertyChangeListener(Throttle.F2,
                    Boolean.valueOf(this.f2),
                    Boolean.valueOf(this.f2 = true));
        } else if ((b3 & 0x02) == 0x00 && getF2() == true) {
            notifyPropertyChangeListener(Throttle.F2,
                    Boolean.valueOf(this.f2),
                    Boolean.valueOf(this.f2 = false));
        }

        if ((b3 & 0x04) == 0x04 && getF3() == false) {
            notifyPropertyChangeListener(Throttle.F3,
                    Boolean.valueOf(this.f3),
                    Boolean.valueOf(this.f3 = true));
        } else if ((b3 & 0x04) == 0x00 && getF3() == true) {
            notifyPropertyChangeListener(Throttle.F3,
                    Boolean.valueOf(this.f3),
                    Boolean.valueOf(this.f3 = false));
        }

        if ((b3 & 0x08) == 0x08 && getF4() == false) {
            notifyPropertyChangeListener(Throttle.F4,
                    Boolean.valueOf(this.f4),
                    Boolean.valueOf(this.f4 = true));
        } else if ((b3 & 0x08) == 0x00 && getF4() == true) {
            notifyPropertyChangeListener(Throttle.F4,
                    Boolean.valueOf(this.f4),
                    Boolean.valueOf(this.f4 = false));
        }

        /* data byte 4 is the status of F12 F11 F10 F9 F8 F7 F6 F5 */
        if ((b4 & 0x01) == 0x01 && getF5() == false) {
            notifyPropertyChangeListener(Throttle.F5,
                    Boolean.valueOf(this.f5),
                    Boolean.valueOf(this.f5 = true));
        } else if ((b4 & 0x01) == 0x00 && getF5() == true) {
            notifyPropertyChangeListener(Throttle.F5,
                    Boolean.valueOf(this.f5),
                    Boolean.valueOf(this.f5 = false));
        }

        if ((b4 & 0x02) == 0x02 && getF6() == false) {
            notifyPropertyChangeListener(Throttle.F6,
                    Boolean.valueOf(this.f6),
                    Boolean.valueOf(this.f6 = true));
        } else if ((b4 & 0x02) == 0x00 && getF6() == true) {
            notifyPropertyChangeListener(Throttle.F6,
                    Boolean.valueOf(this.f6),
                    Boolean.valueOf(this.f6 = false));
        }

        if ((b4 & 0x04) == 0x04 && getF7() == false) {
            notifyPropertyChangeListener(Throttle.F7,
                    Boolean.valueOf(this.f7),
                    Boolean.valueOf(this.f7 = true));
        } else if ((b4 & 0x04) == 0x00 && getF7() == true) {
            notifyPropertyChangeListener(Throttle.F7,
                    Boolean.valueOf(this.f7),
                    Boolean.valueOf(this.f7 = false));
        }

        if ((b4 & 0x08) == 0x08 && getF8() == false) {
            notifyPropertyChangeListener(Throttle.F8,
                    Boolean.valueOf(this.f8),
                    Boolean.valueOf(this.f8 = true));
        } else if ((b4 & 0x08) == 0x00 && getF8() == true) {
            notifyPropertyChangeListener(Throttle.F8,
                    Boolean.valueOf(this.f8),
                    Boolean.valueOf(this.f8 = false));
        }

        if ((b4 & 0x10) == 0x10 && getF9() == false) {
            notifyPropertyChangeListener(Throttle.F9,
                    Boolean.valueOf(this.f9),
                    Boolean.valueOf(this.f9 = true));
        } else if ((b4 & 0x10) == 0x00 && getF9() == true) {
            notifyPropertyChangeListener(Throttle.F9,
                    Boolean.valueOf(this.f9),
                    Boolean.valueOf(this.f9 = false));
        }

        if ((b4 & 0x20) == 0x20 && getF10() == false) {
            notifyPropertyChangeListener(Throttle.F10,
                    Boolean.valueOf(this.f10),
                    Boolean.valueOf(this.f10 = true));
        } else if ((b4 & 0x20) == 0x00 && getF10() == true) {
            notifyPropertyChangeListener(Throttle.F10,
                    Boolean.valueOf(this.f10),
                    Boolean.valueOf(this.f10 = false));
        }

        if ((b4 & 0x40) == 0x40 && getF11() == false) {
            notifyPropertyChangeListener(Throttle.F11,
                    Boolean.valueOf(this.f11),
                    Boolean.valueOf(this.f11 = true));
        } else if ((b4 & 0x40) == 0x00 && getF11() == true) {
            notifyPropertyChangeListener(Throttle.F11,
                    Boolean.valueOf(this.f11),
                    Boolean.valueOf(this.f11 = false));
        }

        if ((b4 & 0x80) == 0x80 && getF12() == false) {
            notifyPropertyChangeListener(Throttle.F12,
                    Boolean.valueOf(this.f12),
                    Boolean.valueOf(this.f12 = true));
        } else if ((b4 & 0x80) == 0x00 && getF12() == true) {
            notifyPropertyChangeListener(Throttle.F12,
                    Boolean.valueOf(this.f12),
                    Boolean.valueOf(this.f12 = false));
        }
    }

    protected void parseFunctionHighInformation(int b3, int b4) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing Function F13-F28 status, function bytes: " + b3 + " and " + b4);
        }
        /* data byte 3 is the status of F20 F19 F18 F17 F16 F15 F14 F13 */
        if ((b3 & 0x01) == 0x01 && getF13() == false) {
            notifyPropertyChangeListener(Throttle.F13,
                    Boolean.valueOf(this.f13),
                    Boolean.valueOf(this.f13 = true));
        } else if ((b3 & 0x01) == 0x00 && getF13() == true) {
            notifyPropertyChangeListener(Throttle.F13,
                    Boolean.valueOf(this.f13),
                    Boolean.valueOf(this.f13 = false));
        }

        if ((b3 & 0x02) == 0x02 && getF14() == false) {
            notifyPropertyChangeListener(Throttle.F14,
                    Boolean.valueOf(this.f14),
                    Boolean.valueOf(this.f14 = true));
        } else if ((b3 & 0x02) == 0x00 && getF14() == true) {
            notifyPropertyChangeListener(Throttle.F14,
                    Boolean.valueOf(this.f14),
                    Boolean.valueOf(this.f14 = false));
        }

        if ((b3 & 0x04) == 0x04 && getF15() == false) {
            notifyPropertyChangeListener(Throttle.F15,
                    Boolean.valueOf(this.f15),
                    Boolean.valueOf(this.f15 = true));
        } else if ((b3 & 0x04) == 0x00 && getF15() == true) {
            notifyPropertyChangeListener(Throttle.F15,
                    Boolean.valueOf(this.f15),
                    Boolean.valueOf(this.f15 = false));
        }

        if ((b3 & 0x08) == 0x08 && getF16() == false) {
            notifyPropertyChangeListener(Throttle.F16,
                    Boolean.valueOf(this.f16),
                    Boolean.valueOf(this.f16 = true));
        } else if ((b3 & 0x08) == 0x00 && getF16() == true) {
            notifyPropertyChangeListener(Throttle.F16,
                    Boolean.valueOf(this.f16),
                    Boolean.valueOf(this.f16 = false));
        }

        if ((b3 & 0x10) == 0x10 && getF17() == false) {
            notifyPropertyChangeListener(Throttle.F17,
                    Boolean.valueOf(this.f17),
                    Boolean.valueOf(this.f17 = true));
        } else if ((b3 & 0x10) == 0x00 && getF17() == true) {
            notifyPropertyChangeListener(Throttle.F17,
                    Boolean.valueOf(this.f17),
                    Boolean.valueOf(this.f17 = false));
        }

        if ((b3 & 0x20) == 0x20 && getF18() == false) {
            notifyPropertyChangeListener(Throttle.F18,
                    Boolean.valueOf(this.f18),
                    Boolean.valueOf(this.f18 = true));
        } else if ((b3 & 0x20) == 0x00 && getF18() == true) {
            notifyPropertyChangeListener(Throttle.F18,
                    Boolean.valueOf(this.f18),
                    Boolean.valueOf(this.f18 = false));
        }

        if ((b3 & 0x40) == 0x40 && getF19() == false) {
            notifyPropertyChangeListener(Throttle.F19,
                    Boolean.valueOf(this.f19),
                    Boolean.valueOf(this.f19 = true));
        } else if ((b3 & 0x40) == 0x00 && getF19() == true) {
            notifyPropertyChangeListener(Throttle.F19,
                    Boolean.valueOf(this.f19),
                    Boolean.valueOf(this.f19 = false));
        }

        if ((b3 & 0x80) == 0x80 && getF20() == false) {
            notifyPropertyChangeListener(Throttle.F20,
                    Boolean.valueOf(this.f20),
                    Boolean.valueOf(this.f20 = true));
        } else if ((b3 & 0x80) == 0x00 && getF20() == true) {
            notifyPropertyChangeListener(Throttle.F20,
                    Boolean.valueOf(this.f20),
                    Boolean.valueOf(this.f20 = false));
        }
        /* data byte 4 is the status of F28 F27 F26 F25 F24 F23 F22 F21 */

        if ((b4 & 0x01) == 0x01 && getF21() == false) {
            notifyPropertyChangeListener(Throttle.F21,
                    Boolean.valueOf(this.f21),
                    Boolean.valueOf(this.f21 = true));
        } else if ((b4 & 0x01) == 0x00 && getF21() == true) {
            notifyPropertyChangeListener(Throttle.F21,
                    Boolean.valueOf(this.f21),
                    Boolean.valueOf(this.f21 = false));
        }

        if ((b4 & 0x02) == 0x02 && getF22() == false) {
            notifyPropertyChangeListener(Throttle.F22,
                    Boolean.valueOf(this.f22),
                    Boolean.valueOf(this.f22 = true));
        } else if ((b4 & 0x02) == 0x00 && getF22() == true) {
            notifyPropertyChangeListener(Throttle.F22,
                    Boolean.valueOf(this.f22),
                    Boolean.valueOf(this.f22 = false));
        }

        if ((b4 & 0x04) == 0x04 && getF23() == false) {
            notifyPropertyChangeListener(Throttle.F23,
                    Boolean.valueOf(this.f23),
                    Boolean.valueOf(this.f23 = true));
        } else if ((b4 & 0x04) == 0x00 && getF23() == true) {
            notifyPropertyChangeListener(Throttle.F23,
                    Boolean.valueOf(this.f23),
                    Boolean.valueOf(this.f23 = false));
        }

        if ((b4 & 0x08) == 0x08 && getF24() == false) {
            notifyPropertyChangeListener(Throttle.F24,
                    Boolean.valueOf(this.f24),
                    Boolean.valueOf(this.f24 = true));
        } else if ((b4 & 0x08) == 0x00 && getF24() == true) {
            notifyPropertyChangeListener(Throttle.F24,
                    Boolean.valueOf(this.f24),
                    Boolean.valueOf(this.f24 = false));
        }

        if ((b4 & 0x10) == 0x10 && getF25() == false) {
            notifyPropertyChangeListener(Throttle.F25,
                    Boolean.valueOf(this.f25),
                    Boolean.valueOf(this.f25 = true));
        } else if ((b4 & 0x10) == 0x00 && getF25() == true) {
            notifyPropertyChangeListener(Throttle.F25,
                    Boolean.valueOf(this.f25),
                    Boolean.valueOf(this.f25 = false));
        }

        if ((b4 & 0x20) == 0x20 && getF26() == false) {
            notifyPropertyChangeListener(Throttle.F26,
                    Boolean.valueOf(this.f26),
                    Boolean.valueOf(this.f26 = true));
        } else if ((b4 & 0x20) == 0x00 && getF26() == true) {
            notifyPropertyChangeListener(Throttle.F26,
                    Boolean.valueOf(this.f26),
                    Boolean.valueOf(this.f26 = false));
        }

        if ((b4 & 0x40) == 0x40 && getF27() == false) {
            notifyPropertyChangeListener(Throttle.F27,
                    Boolean.valueOf(this.f27),
                    Boolean.valueOf(this.f27 = true));
        } else if ((b4 & 0x40) == 0x00 && getF27() == true) {
            notifyPropertyChangeListener(Throttle.F27,
                    Boolean.valueOf(this.f27),
                    Boolean.valueOf(this.f27 = false));
        }

        if ((b4 & 0x80) == 0x80 && getF28() == false) {
            notifyPropertyChangeListener(Throttle.F28,
                    Boolean.valueOf(this.f28),
                    Boolean.valueOf(this.f28 = true));
        } else if ((b4 & 0x80) == 0x00 && getF28() == true) {
            notifyPropertyChangeListener(Throttle.F28,
                    Boolean.valueOf(this.f28),
                    Boolean.valueOf(this.f28 = false));
        }
    }

    protected void parseFunctionMomentaryInformation(int b3, int b4) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing Function Momentary status, function bytes: " + b3 + " and " + b4);
        }
        /* data byte 3 is the momentary status of F0 F4 F3 F2 F1 */
        if ((b3 & 0x10) == 0x10 && this.f0Momentary == false) {
            notifyPropertyChangeListener(Throttle.F0Momentary,
                    Boolean.valueOf(this.f0Momentary),
                    Boolean.valueOf(this.f0Momentary = true));
        } else if ((b3 & 0x10) == 0x00 && this.f0Momentary == true) {
            notifyPropertyChangeListener(Throttle.F0Momentary,
                    Boolean.valueOf(this.f0Momentary),
                    Boolean.valueOf(this.f0Momentary = false));
        }

        if ((b3 & 0x01) == 0x01 && this.f1Momentary == false) {
            notifyPropertyChangeListener(Throttle.F1Momentary,
                    Boolean.valueOf(this.f1Momentary),
                    Boolean.valueOf(this.f1Momentary = true));
        } else if ((b3 & 0x01) == 0x00 && this.f1Momentary == true) {
            notifyPropertyChangeListener(Throttle.F1Momentary,
                    Boolean.valueOf(this.f1Momentary),
                    Boolean.valueOf(this.f1Momentary = false));
        }

        if ((b3 & 0x02) == 0x02 && this.f2Momentary == false) {
            notifyPropertyChangeListener(Throttle.F2Momentary,
                    Boolean.valueOf(this.f2Momentary),
                    Boolean.valueOf(this.f2Momentary = true));
        } else if ((b3 & 0x02) == 0x00 && this.f2Momentary == true) {
            notifyPropertyChangeListener(Throttle.F2Momentary,
                    Boolean.valueOf(this.f2Momentary),
                    Boolean.valueOf(this.f2Momentary = false));
        }

        if ((b3 & 0x04) == 0x04 && this.f3Momentary == false) {
            notifyPropertyChangeListener(Throttle.F3Momentary,
                    Boolean.valueOf(this.f3Momentary),
                    Boolean.valueOf(this.f3Momentary = true));
        } else if ((b3 & 0x04) == 0x00 && this.f3Momentary == true) {
            notifyPropertyChangeListener(Throttle.F3Momentary,
                    Boolean.valueOf(this.f3Momentary),
                    Boolean.valueOf(this.f3Momentary = false));
        }

        if ((b3 & 0x08) == 0x08 && this.f4Momentary == false) {
            notifyPropertyChangeListener(Throttle.F4Momentary,
                    Boolean.valueOf(this.f4Momentary),
                    Boolean.valueOf(this.f4Momentary = true));
        } else if ((b3 & 0x08) == 0x00 && this.f4Momentary == true) {
            notifyPropertyChangeListener(Throttle.F4Momentary,
                    Boolean.valueOf(this.f4Momentary),
                    Boolean.valueOf(this.f4Momentary = false));
        }

        /* data byte 4 is the momentary status of F12 F11 F10 F9 F8 F7 F6 F5 */
        if ((b4 & 0x01) == 0x01 && this.f5Momentary == false) {
            notifyPropertyChangeListener(Throttle.F5Momentary,
                    Boolean.valueOf(this.f5Momentary),
                    Boolean.valueOf(this.f5Momentary = true));
        } else if ((b4 & 0x01) == 0x00 && this.f5Momentary == true) {
            notifyPropertyChangeListener(Throttle.F5Momentary,
                    Boolean.valueOf(this.f5Momentary),
                    Boolean.valueOf(this.f5Momentary = false));
        }

        if ((b4 & 0x02) == 0x02 && this.f6Momentary == false) {
            notifyPropertyChangeListener(Throttle.F6Momentary,
                    Boolean.valueOf(this.f6Momentary),
                    Boolean.valueOf(this.f6Momentary = true));
        } else if ((b4 & 0x02) == 0x00 && this.f6Momentary == true) {
            notifyPropertyChangeListener(Throttle.F6Momentary,
                    Boolean.valueOf(this.f6Momentary),
                    Boolean.valueOf(this.f6Momentary = false));
        }

        if ((b4 & 0x04) == 0x04 && this.f7Momentary == false) {
            notifyPropertyChangeListener(Throttle.F7Momentary,
                    Boolean.valueOf(this.f7Momentary),
                    Boolean.valueOf(this.f7Momentary = true));
        } else if ((b4 & 0x04) == 0x00 && this.f7Momentary == true) {
            notifyPropertyChangeListener(Throttle.F7Momentary,
                    Boolean.valueOf(this.f7Momentary),
                    Boolean.valueOf(this.f7Momentary = false));
        }

        if ((b4 & 0x08) == 0x08 && this.f8Momentary == false) {
            notifyPropertyChangeListener(Throttle.F8Momentary,
                    Boolean.valueOf(this.f8Momentary),
                    Boolean.valueOf(this.f8Momentary = true));
        } else if ((b4 & 0x08) == 0x00 && this.f8Momentary == true) {
            notifyPropertyChangeListener(Throttle.F8Momentary,
                    Boolean.valueOf(this.f8Momentary),
                    Boolean.valueOf(this.f8Momentary = false));
        }

        if ((b4 & 0x10) == 0x10 && this.f9Momentary == false) {
            notifyPropertyChangeListener(Throttle.F9Momentary,
                    Boolean.valueOf(this.f9Momentary),
                    Boolean.valueOf(this.f9Momentary = true));
        } else if ((b4 & 0x10) == 0x00 && this.f9Momentary == true) {
            notifyPropertyChangeListener(Throttle.F9Momentary,
                    Boolean.valueOf(this.f9Momentary),
                    Boolean.valueOf(this.f9Momentary = false));
        }

        if ((b4 & 0x20) == 0x20 && this.f10Momentary == false) {
            notifyPropertyChangeListener(Throttle.F10Momentary,
                    Boolean.valueOf(this.f10Momentary),
                    Boolean.valueOf(this.f10Momentary = true));
        } else if ((b4 & 0x20) == 0x00 && this.f10Momentary == true) {
            notifyPropertyChangeListener(Throttle.F10Momentary,
                    Boolean.valueOf(this.f10Momentary),
                    Boolean.valueOf(this.f10Momentary = false));
        }

        if ((b4 & 0x40) == 0x40 && this.f11Momentary == false) {
            notifyPropertyChangeListener(Throttle.F11Momentary,
                    Boolean.valueOf(this.f11Momentary),
                    Boolean.valueOf(this.f11Momentary = true));
        } else if ((b4 & 0x40) == 0x00 && this.f11Momentary == true) {
            notifyPropertyChangeListener(Throttle.F11Momentary,
                    Boolean.valueOf(this.f11Momentary),
                    Boolean.valueOf(this.f11Momentary = false));
        }

        if ((b4 & 0x80) == 0x80 && this.f12Momentary == false) {
            notifyPropertyChangeListener(Throttle.F12Momentary,
                    Boolean.valueOf(this.f12Momentary),
                    Boolean.valueOf(this.f12Momentary = true));
        } else if ((b4 & 0x80) == 0x00 && this.f12Momentary == true) {
            notifyPropertyChangeListener(Throttle.F12Momentary,
                    Boolean.valueOf(this.f12Momentary),
                    Boolean.valueOf(this.f12Momentary = false));
        }
    }

    protected void parseFunctionHighMomentaryInformation(int b3, int b4) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing Function F13-F28 Momentary status, function bytes: " + b3 + " and " + b4);
        }
        /* data byte 3 is the momentary status of F20 F19 F17 F16 F15 F14 F13 */

        if ((b3 & 0x01) == 0x01 && this.f13Momentary == false) {
            notifyPropertyChangeListener(Throttle.F13Momentary,
                    Boolean.valueOf(this.f13Momentary),
                    Boolean.valueOf(this.f13Momentary = true));
        } else if ((b3 & 0x01) == 0x00 && this.f13Momentary == true) {
            notifyPropertyChangeListener(Throttle.F13Momentary,
                    Boolean.valueOf(this.f13Momentary),
                    Boolean.valueOf(this.f13Momentary = false));
        }

        if ((b3 & 0x02) == 0x02 && this.f2Momentary == false) {
            notifyPropertyChangeListener(Throttle.F14Momentary,
                    Boolean.valueOf(this.f14Momentary),
                    Boolean.valueOf(this.f14Momentary = true));
        } else if ((b3 & 0x02) == 0x00 && this.f14Momentary == true) {
            notifyPropertyChangeListener(Throttle.F14Momentary,
                    Boolean.valueOf(this.f14Momentary),
                    Boolean.valueOf(this.f14Momentary = false));
        }

        if ((b3 & 0x04) == 0x04 && this.f15Momentary == false) {
            notifyPropertyChangeListener(Throttle.F15Momentary,
                    Boolean.valueOf(this.f15Momentary),
                    Boolean.valueOf(this.f15Momentary = true));
        } else if ((b3 & 0x04) == 0x00 && this.f15Momentary == true) {
            notifyPropertyChangeListener(Throttle.F15Momentary,
                    Boolean.valueOf(this.f15Momentary),
                    Boolean.valueOf(this.f15Momentary = false));
        }

        if ((b3 & 0x08) == 0x08 && this.f16Momentary == false) {
            notifyPropertyChangeListener(Throttle.F16Momentary,
                    Boolean.valueOf(this.f16Momentary),
                    Boolean.valueOf(this.f16Momentary = true));
        } else if ((b3 & 0x08) == 0x00 && this.f16Momentary == true) {
            notifyPropertyChangeListener(Throttle.F16Momentary,
                    Boolean.valueOf(this.f16Momentary),
                    Boolean.valueOf(this.f16Momentary = false));
        }

        if ((b3 & 0x10) == 0x10 && this.f17Momentary == false) {
            notifyPropertyChangeListener(Throttle.F17Momentary,
                    Boolean.valueOf(this.f17Momentary),
                    Boolean.valueOf(this.f17Momentary = true));
        } else if ((b3 & 0x10) == 0x00 && this.f17Momentary == true) {
            notifyPropertyChangeListener(Throttle.F17Momentary,
                    Boolean.valueOf(this.f17Momentary),
                    Boolean.valueOf(this.f17Momentary = false));
        }

        if ((b3 & 0x20) == 0x20 && this.f18Momentary == false) {
            notifyPropertyChangeListener(Throttle.F18Momentary,
                    Boolean.valueOf(this.f18Momentary),
                    Boolean.valueOf(this.f18Momentary = true));
        } else if ((b3 & 0x20) == 0x00 && this.f18Momentary == true) {
            notifyPropertyChangeListener(Throttle.F18Momentary,
                    Boolean.valueOf(this.f18Momentary),
                    Boolean.valueOf(this.f18Momentary = false));
        }

        if ((b3 & 0x40) == 0x40 && this.f19Momentary == false) {
            notifyPropertyChangeListener(Throttle.F19Momentary,
                    Boolean.valueOf(this.f19Momentary),
                    Boolean.valueOf(this.f19Momentary = true));
        } else if ((b3 & 0x40) == 0x00 && this.f19Momentary == true) {
            notifyPropertyChangeListener(Throttle.F19Momentary,
                    Boolean.valueOf(this.f19Momentary),
                    Boolean.valueOf(this.f19Momentary = false));
        }

        if ((b3 & 0x80) == 0x80 && this.f20Momentary == false) {
            notifyPropertyChangeListener(Throttle.F20Momentary,
                    Boolean.valueOf(this.f20Momentary),
                    Boolean.valueOf(this.f20Momentary = true));
        } else if ((b3 & 0x80) == 0x00 && this.f20Momentary == true) {
            notifyPropertyChangeListener(Throttle.F20Momentary,
                    Boolean.valueOf(this.f20Momentary),
                    Boolean.valueOf(this.f20Momentary = false));
        }

        /* data byte 4 is the momentary status of F28 F27 F26 F25 F24 F23 F22 F21 */
        if ((b4 & 0x01) == 0x01 && this.f21Momentary == false) {
            notifyPropertyChangeListener(Throttle.F5Momentary,
                    Boolean.valueOf(this.f21Momentary),
                    Boolean.valueOf(this.f21Momentary = true));
        } else if ((b4 & 0x01) == 0x00 && this.f21Momentary == true) {
            notifyPropertyChangeListener(Throttle.F21Momentary,
                    Boolean.valueOf(this.f21Momentary),
                    Boolean.valueOf(this.f21Momentary = false));
        }

        if ((b4 & 0x02) == 0x02 && this.f22Momentary == false) {
            notifyPropertyChangeListener(Throttle.F22Momentary,
                    Boolean.valueOf(this.f22Momentary),
                    Boolean.valueOf(this.f22Momentary = true));
        } else if ((b4 & 0x02) == 0x00 && this.f22Momentary == true) {
            notifyPropertyChangeListener(Throttle.F22Momentary,
                    Boolean.valueOf(this.f22Momentary),
                    Boolean.valueOf(this.f22Momentary = false));
        }

        if ((b4 & 0x04) == 0x04 && this.f23Momentary == false) {
            notifyPropertyChangeListener(Throttle.F23Momentary,
                    Boolean.valueOf(this.f23Momentary),
                    Boolean.valueOf(this.f23Momentary = true));
        } else if ((b4 & 0x04) == 0x00 && this.f23Momentary == true) {
            notifyPropertyChangeListener(Throttle.F23Momentary,
                    Boolean.valueOf(this.f23Momentary),
                    Boolean.valueOf(this.f23Momentary = false));
        }

        if ((b4 & 0x08) == 0x08 && this.f24Momentary == false) {
            notifyPropertyChangeListener(Throttle.F24Momentary,
                    Boolean.valueOf(this.f24Momentary),
                    Boolean.valueOf(this.f24Momentary = true));
        } else if ((b4 & 0x08) == 0x00 && this.f24Momentary == true) {
            notifyPropertyChangeListener(Throttle.F24Momentary,
                    Boolean.valueOf(this.f24Momentary),
                    Boolean.valueOf(this.f24Momentary = false));
        }

        if ((b4 & 0x10) == 0x10 && this.f25Momentary == false) {
            notifyPropertyChangeListener(Throttle.F25Momentary,
                    Boolean.valueOf(this.f25Momentary),
                    Boolean.valueOf(this.f25Momentary = true));
        } else if ((b4 & 0x10) == 0x00 && this.f25Momentary == true) {
            notifyPropertyChangeListener(Throttle.F25Momentary,
                    Boolean.valueOf(this.f25Momentary),
                    Boolean.valueOf(this.f25Momentary = false));
        }

        if ((b4 & 0x20) == 0x20 && this.f26Momentary == false) {
            notifyPropertyChangeListener(Throttle.F26Momentary,
                    Boolean.valueOf(this.f26Momentary),
                    Boolean.valueOf(this.f26Momentary = true));
        } else if ((b4 & 0x20) == 0x00 && this.f26Momentary == true) {
            notifyPropertyChangeListener(Throttle.F26Momentary,
                    Boolean.valueOf(this.f26Momentary),
                    Boolean.valueOf(this.f26Momentary = false));
        }

        if ((b4 & 0x40) == 0x40 && this.f27Momentary == false) {
            notifyPropertyChangeListener(Throttle.F27Momentary,
                    Boolean.valueOf(this.f27Momentary),
                    Boolean.valueOf(this.f27Momentary = true));
        } else if ((b4 & 0x40) == 0x00 && this.f27Momentary == true) {
            notifyPropertyChangeListener(Throttle.F27Momentary,
                    Boolean.valueOf(this.f27Momentary),
                    Boolean.valueOf(this.f27Momentary = false));
        }

        if ((b4 & 0x80) == 0x80 && this.f28Momentary == false) {
            notifyPropertyChangeListener(Throttle.F28Momentary,
                    Boolean.valueOf(this.f28Momentary),
                    Boolean.valueOf(this.f28Momentary = true));
        } else if ((b4 & 0x80) == 0x00 && this.f28Momentary == true) {
            notifyPropertyChangeListener(Throttle.F28Momentary,
                    Boolean.valueOf(this.f28Momentary),
                    Boolean.valueOf(this.f28Momentary = false));
        }
    }

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
                sendStatusInformationRequest();
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
        return new DccLocoAddress(address, XNetThrottleManager.isLongAddress(address));
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
                tc.sendXNetMessage(msg.getMsg(), this);
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
    synchronized protected void queueMessage(XNetMessage m, int s) {
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
    private final static Logger log = LoggerFactory.getLogger(XNetThrottle.class.getName());
}

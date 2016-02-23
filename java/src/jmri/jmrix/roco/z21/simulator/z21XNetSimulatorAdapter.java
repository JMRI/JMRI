// z21XNetSimulatorAdapter.java
package jmri.jmrix.roco.z21.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.roco.z21.z21Constants;

/**
 * Provide access to a simulated z21 XPressNet sub-system.
 * This shares some code with the XPressNet simulator, but it's 
 * not a derived class because it isn't a real connection.
 *
 * @author	Paul Bender, Copyright (C) 2015
 * @version	$Revision$
 */
public class z21XNetSimulatorAdapter {

    private int csStatus;
    // status values from the z21 Documentation.
    private final static int csEmergencyStop = 0x01;
    // 0x00 means normal mode.
    private final static int csNormalMode = 0x00;

    public z21XNetSimulatorAdapter() {
       csStatus = csNormalMode;
    }

    // generateReply is the heart of the simulation.  It translates an 
    // incoming XNetMessage into an outgoing XNetReply.
    @SuppressWarnings("fallthrough")
    XNetReply generateReply(XNetMessage m) {
        log.debug("Generating Reply");
        XNetReply reply;
        switch (m.getElement(0)&0xff) {
            case XNetConstants.CS_REQUEST:
                log.debug("CS Request Received");
                switch (m.getElement(1)) {
                    case XNetConstants.CS_VERSION:
                        reply=xNetVersionReply();
                        break;
                    case XNetConstants.RESUME_OPS:
                        csStatus = csNormalMode;
                        reply=normalOpsReply();
                        break;
                    case XNetConstants.EMERGENCY_OFF:
                        csStatus = csEmergencyStop;
                        reply=everythingOffReply();
                        break;
                    case XNetConstants.CS_STATUS:
                        reply=csStatusReply();
                        break;
                    case XNetConstants.SERVICE_MODE_CSRESULT:
                    default:
                        log.debug("Unsupoorted requested received: {}", m.toString());
                        reply=notSupportedReply();
                }
                break;
            case XNetConstants.LI_VERSION_REQUEST:
                log.debug("LI Version Request Received");
                reply=new XNetReply();
                reply.setOpCode(XNetConstants.LI_VERSION_RESPONSE);
                reply.setElement(1, 0x00);  // set the hardware type to 0
                reply.setElement(2, 0x00);  // set the firmware version to 0
                reply.setElement(3, 0x00);  // set the parity byte to 0
                reply.setParity();
                break;
            case XNetConstants.LOCO_OPER_REQ:
                log.debug("Locomotive Operations Request received");
                switch(m.getElement(1)&0xff) {
                     case XNetConstants.LOCO_SPEED_14:
                        // z21 specific locomotive information reply is expected.
                        reply=new XNetReply();
                        reply.setOpCode(z21Constants.LAN_X_LOCO_INFO_RESPONSE);
                        reply.setElement(1,m.getElement(2)); // address msb from
                                                            // message.
                        reply.setElement(2,m.getElement(3)); // address lsb from
                                                            // message.
                        reply.setElement(3,0x00);  // set speed step mode to 14
                        reply.setElement(4,m.getElement(4)&0xff);  // set the speed and direction to the sent value.
                        reply.setElement(5, 0x00);  // set function group a off
                        reply.setElement(6, 0x00);  // set function group b off
                        reply.setElement(7, 0x00);  // set F13-F20 off
                        reply.setElement(8, 0x00);  // set F21-F28 off
                        reply.setElement(9, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                     case XNetConstants.LOCO_SPEED_27:
                        log.debug("Unsupoorted requested received: {}", m.toString());
                        reply = notSupportedReply();
                        break;
                     case XNetConstants.LOCO_SPEED_28:
                        // z21 specific locomotive information reply is expected.
                        reply=new XNetReply();
                        reply.setOpCode(z21Constants.LAN_X_LOCO_INFO_RESPONSE);
                        reply.setElement(1,m.getElement(2)); // address msb from
                                                            // message.
                        reply.setElement(2,m.getElement(3)); // address lsb from
                                                            // message.
                        reply.setElement(3,0x02);  // set speed step mode to 28
                        reply.setElement(4,m.getElement(4));  // set the speed and direction to the sent value.
                        reply.setElement(5, 0x00);  // set function group a off
                        reply.setElement(6, 0x00);  // set function group b off
                        reply.setElement(7, 0x00);  // set F13-F20 off
                        reply.setElement(8, 0x00);  // set F21-F28 off
                        reply.setElement(9, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                     case XNetConstants.LOCO_SPEED_128:
                        // z21 specific locomotive information reply is expected.
                        reply=new XNetReply();
                        reply.setOpCode(z21Constants.LAN_X_LOCO_INFO_RESPONSE);
                        reply.setElement(1,m.getElement(2)); // address msb from
                                                            // message.
                        reply.setElement(2,m.getElement(3)); // address lsb from
                                                            // message.
                        reply.setElement(3,0x04);  // set speed step mode to 128
                        reply.setElement(4,m.getElement(4));  // set the speed and direction to the sent value.
                        reply.setElement(5, 0x00);  // set function group a off
                        reply.setElement(6, 0x00);  // set function group b off
                        reply.setElement(7, 0x00);  // set F13-F20 off
                        reply.setElement(8, 0x00);  // set F21-F28 off
                        reply.setElement(9, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                     case z21Constants.LAN_X_SET_LOCO_FUNCTION:
                        // z21 specific locomotive information reply is expected.
                        reply=new XNetReply();
                        reply.setOpCode(z21Constants.LAN_X_LOCO_INFO_RESPONSE);
                        reply.setElement(1,m.getElement(2)); // address msb from
                                                            // message.
                        reply.setElement(2,m.getElement(3)); // address lsb from
                                                            // message.
                        reply.setElement(3,0x04);  // set speed step mode to 128
                        reply.setElement(4,0x00);  // set the speed and direction to the sent value.
                        reply.setElement(5, 0x00);  // set function group a off
                        reply.setElement(6, 0x00);  // set function group b off
                        reply.setElement(7, 0x00);  // set F13-F20 off
                        reply.setElement(8, 0x00);  // set F21-F28 off
                        reply.setElement(9, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                     case XNetConstants.LOCO_SET_FUNC_GROUP1:
                     case XNetConstants.LOCO_SET_FUNC_GROUP2:
                     case XNetConstants.LOCO_SET_FUNC_GROUP3:
                     case XNetConstants.LOCO_SET_FUNC_GROUP4:
                     case XNetConstants.LOCO_SET_FUNC_GROUP5:
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_SET_FUNC_Group1:
                     case XNetConstants.LOCO_SET_FUNC_Group2:
                     case XNetConstants.LOCO_SET_FUNC_Group3:
                     case XNetConstants.LOCO_SET_FUNC_Group4:
                     case XNetConstants.LOCO_SET_FUNC_Group5:
                          reply = okReply();
                          break;
                     case XNetConstants.LOCO_ADD_MULTI_UNIT_REQ:
                     case XNetConstants.LOCO_REM_MULTI_UNIT_REQ:
                     case XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD:
                     case XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD:
                     default:
                        log.debug("Unsupoorted requested received: {}", m.toString());
                        reply = notSupportedReply();
                        break;
                }
                break;
            case XNetConstants.EMERGENCY_STOP:
                log.debug("Emergency Stop Received");
                reply = emergencyStopReply();
                break;
           case XNetConstants.ACC_OPER_REQ:
                log.debug("Accessory Operations Request Received");
                reply = okReply();
                break;
            case XNetConstants.LOCO_STATUS_REQ:
                log.debug("Locomotive Status Request received");
                switch (m.getElement(1)&0xff) {
                    case XNetConstants.LOCO_INFO_REQ_V3:
                        reply=new XNetReply();
                        reply.setOpCode(XNetConstants.LOCO_INFO_NORMAL_UNIT);
                        reply.setElement(1, 0x04);  // set to 128 speed step mode
                        reply.setElement(2, 0x00);  // set the speed to 0 
                        // direction reverse
                        reply.setElement(3, 0x00);  // set function group a off
                        reply.setElement(4, 0x00);  // set function group b off
                        reply.setElement(5, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case z21Constants.LAN_X_LOCO_INFO_REQUEST_Z21:
                        // z21 specific locomotive information request.
                        reply=new XNetReply();
                        reply.setOpCode(z21Constants.LAN_X_LOCO_INFO_RESPONSE);
                        reply.setElement(1,m.getElement(2)); // address msb from
                                                            // message.
                        reply.setElement(2,m.getElement(3)); // address lsb from
                                                            // message.
                        reply.setElement(3,0x04);  // set speed step mode to 128
                        reply.setElement(4,0x00);  // set the speed and direction to 0 and reverse.
                        reply.setElement(5, 0x00);  // set function group a off
                        reply.setElement(6, 0x00);  // set function group b off
                        reply.setElement(7, 0x00);  // set F13-F20 off
                        reply.setElement(8, 0x00);  // set F21-F28 off
                        reply.setElement(9, 0x00);  // set the parity byte to 0
                        reply.setParity();         // set the parity correctly.
                        break;
                    case XNetConstants.LOCO_INFO_REQ_FUNC:
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                    case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                    default:
                        log.debug("Unsupoorted requested received: {}", m.toString());
                        reply=notSupportedReply();
                }
                break;
            case XNetConstants.ACC_INFO_REQ:
                log.debug("Accessory Information Request Received");
                reply=new XNetReply();
                reply.setOpCode(XNetConstants.ACC_INFO_RESPONSE);
                reply.setElement(1, m.getElement(1));
                if (m.getElement(1) < 64) {
                    // treat as turnout feedback request.
                    if (m.getElement(2) == 0x80) {
                        reply.setElement(2, 0x00);
                    } else {
                        reply.setElement(2, 0x10);
                    }
                } else {
                    // treat as feedback encoder request.
                    if (m.getElement(2) == 0x80) {
                        reply.setElement(2, 0x40);
                    } else {
                        reply.setElement(2, 0x50);
                    }
                }
                reply.setElement(3, 0x00);
                reply.setParity();
                break;
            case XNetConstants.LI101_REQUEST:
            case XNetConstants.CS_SET_POWERMODE:
            //case XNetConstants.PROG_READ_REQUEST:  //PROG_READ_REQUEST 
            //and CS_SET_POWERMODE 
            //have the same value
            case XNetConstants.PROG_WRITE_REQUEST:
            case XNetConstants.OPS_MODE_PROG_REQ:
            case XNetConstants.LOCO_DOUBLEHEAD:
            default:
                log.debug("Unsupoorted requested received: {}", m.toString());
                reply=notSupportedReply();
        }
        log.debug("generated reply {}",reply);
        return reply;
    }

    // We have a few canned response messages.
    // Create an Unsupported XNetReply message
    private XNetReply notSupportedReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.CS_NOT_SUPPORTED);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create an OK XNetReply message
    private XNetReply okReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.LI_MESSAGE_RESPONSE_HEADER);
        r.setElement(1, XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a "Normal Operations Resumed" message
    private XNetReply normalOpsReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.BC_NORMAL_OPERATIONS);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a broadcast "Everything Off" reply
    private XNetReply everythingOffReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.CS_INFO);
        r.setElement(1, XNetConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }

    // Create a broadcast "Emergecy Stop" reply
    private XNetReply emergencyStopReply() {
        XNetReply r = new XNetReply();
        r.setOpCode(XNetConstants.BC_EMERGENCY_STOP);
        r.setElement(1, XNetConstants.BC_EVERYTHING_OFF);
        r.setElement(2, 0x00); // set the parity byte to 0
        r.setParity();
        return r;
    }
   
    // Create a reply to a request for the XPressNet Version
    private XNetReply xNetVersionReply(){
        XNetReply reply=new XNetReply();
        reply.setOpCode(XNetConstants.CS_SERVICE_MODE_RESPONSE);
        reply.setElement(1, XNetConstants.CS_SOFTWARE_VERSION);
        reply.setElement(2, 0x30 & 0xff); // indicate we are version 3.0
        reply.setElement(3, 0x12 & 0xff); // indicate we are a Z21;
        reply.setElement(4, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    // Create a reply to a request for the Command Station Status
    private XNetReply csStatusReply(){
        XNetReply reply=new XNetReply();
        reply.setOpCode(XNetConstants.CS_REQUEST_RESPONSE);
        reply.setElement(1, XNetConstants.CS_STATUS_RESPONSE);
        reply.setElement(2, csStatus);
        reply.setElement(3, 0x00); // set the parity byte to 0
        reply.setParity();
        return reply;
    }

    private final static Logger log = LoggerFactory.getLogger(z21XNetSimulatorAdapter.class.getName());

}

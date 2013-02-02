// XNetMonFrame.java

package jmri.jmrix.lenz.swing.mon;

import org.apache.log4j.Logger;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Frame displaying (and logging) XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @author          Paul Bender Copyright (C) 2004-2010
 * @author          Giorgio Terdina Copyright (C) 2007
 * @version         $Revision$
 */
 public class XNetMonFrame extends jmri.jmrix.AbstractMonFrame implements XNetListener {

        protected XNetTrafficController tc = null;

        public XNetMonFrame() {
               super();
               // If there is no system memo given, assume the system memo
               // is the first one in the instance list.
                  jmri.jmrix.lenz.XNetSystemConnectionMemo memo;
                    memo=(jmri.jmrix.lenz.XNetSystemConnectionMemo)(jmri.InstanceManager.
                    getList(jmri.jmrix.lenz.XNetSystemConnectionMemo.class).get(0));
                 tc=memo.getXNetTrafficController();
        }


	public XNetMonFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
		super();
                tc=memo.getXNetTrafficController();
	}

	protected String title() { return "XpressNet Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		tc.removeXNetListener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		tc.addXNetListener(~0, this);
	}

	public synchronized void message(XNetReply l) {  // receive a XpressNet message and log it
		// display the raw data if requested
		StringBuilder raw = new StringBuilder();
		if ( rawCheckBox.isSelected() ) {
			raw.append(l.toString());
		}

		// display the decoded data
		String text;
		// First, Decode anything that is sent by the LI10x, and
                // not the command station
		if(l.isOkMessage()) {
		   text="Command Successfully Sent/Normal Operations Resumed after timeout";
		} else if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER) {
		  switch(l.getElement(1)) {
		  case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR:
					text="Error Occurred between the interface and the PC";
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR:
					text="Error Occurred between the interface and the command station";
					break;
	          case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR:
					text="Unknown Communications Error";
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR:
					text="Command Station no longer providing a timeslot for communications";
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW:
					text="Buffer Overflow in interface";
					break;
		  case XNetConstants.LIUSB_WRONG_SYNTAX:
					text="Wrong Message Syntax";
					break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.LI_VERSION_RESPONSE) {
			text = "LI10x hardware Version:  " +
					    (l.getElementBCD(1).floatValue())/10 +
					  " Software Version: " +
					    l.getElementBCD(2);
		} else if(l.getElement(0)==XNetConstants.LI101_REQUEST) {
		  // The request and response for baud rate look the same,
		  // so we need this for both incoming and outgoing directions
		  switch(l.getElement(1)) {
		  case XNetConstants.LI101_REQUEST_ADDRESS:
				text= "RESPONSE LI101 Address " +l.getElement(2);
				break;
		  case XNetConstants.LI101_REQUEST_BAUD:
				text= "RESPONSE LI101 Baud Rate: ";
				switch(l.getElement(2)){
				   case 1: text += "19200bps (default)";
					break;
				   case 2: text += "38400bps";
					break;
				   case 3: text += "57600bps";
					break;
				   case 4: text += "115200bps";
					break;
				   default: text += "<undefined>";
				}
				break;
		  default:
			text = l.toString();
		  }
		/* Next, check the "CS Info" messages */
		} else if(l.getElement(0)==XNetConstants.CS_INFO) {
		  switch(l.getElement(1)) {
		  case XNetConstants.BC_NORMAL_OPERATIONS:
				text= "Broadcast: Normal Operations Resumed";
				break;
		  case XNetConstants.BC_EVERYTHING_OFF:
				text= "Broadcast: Emergency Off (short circuit)";
				break;
		  case XNetConstants.BC_SERVICE_MODE_ENTRY:
				text= "Broadcast: Service Mode Entry";
				break;
		  case XNetConstants.PROG_SHORT_CIRCUIT:
				text= "Service Mode: Short Circuit";
				break;
		  case XNetConstants.PROG_BYTE_NOT_FOUND:
				text= "Service Mode: Data Byte Not Found";
				break;
		  case XNetConstants.PROG_CS_BUSY:
				text= "Service Mode: Command Station Busy";
				break;
		  case XNetConstants.PROG_CS_READY:
				text= "Service Mode: Command Station Ready";
				break;
		  case XNetConstants.CS_BUSY:
				text= "Command Station Busy";
				break;
		  case XNetConstants.CS_NOT_SUPPORTED:
				text= "XPressNet Instruction not supported by Command Station";
				break;
		  case XNetConstants.CS_TRANSFER_ERROR:
				text= "Command Station Reported Transfer Error";
				break;
		  /* The remaining cases are for a Double Header or MU Error */
		  case 0x83: 
			text = "XBus V1 and V2 MU+DH error: ";
			text = text+ "Selected Locomotive has not been operated by this XPressNet device or address 0 selected";
				break;
		  case 0x84:
 			text = "XBus V1 and V2 MU+DH error: ";
			text = text+ "Selected Locomotive is being operated by another XPressNet device";
		        break;
		  case 0x85: 
 			text = "XBus V1 and V2 MU+DH error: " ;
			text = text+ "Selected Locomotive already in MU or DH";
		        break;
		  case 0x86: 
 			text = "XBus V1 and V2 MU+DH error: " ;
			text = text+ "Unit selected for MU or DH has speed setting other than 0";
		        break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.BC_EMERGENCY_STOP &&
			  l.getElement(1)==XNetConstants.BC_EVERYTHING_STOP) {
				text= "Broadcast: Emergency Stop (track power on)";
                /* Followed by Service Mode responses */
		} else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE) {
		  if(l.isDirectModeResponse()) {
				text = "Service Mode: Direct Programming Response: CV:" +
				       l.getServiceModeCVNumber() +
				       " Value: " +
				       l.getServiceModeCVValue();
                  } else if (l.isPagedModeResponse()) {
				text = "Service Mode: Register or Paged Mode Response: CV:" +
				       l.getServiceModeCVNumber() +
				       " Value: " +
				       l.getServiceModeCVValue();
                  } else if (l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION) {
				text = "Command Station Software Version: " + (l.getElementBCD(2).floatValue())/10 + " Type: ";
				switch(l.getElement(3)) {
				    case 0x00: text = text+ "LZ100/LZV100";
				               break;
				    case 0x01: text = text+ "LH200";
				               break;
				    case 0x02: text = text+ "Compact or Other";
				               break;
					// GT 2007/11/6 - Added multiMaus
				    case 0x10: text = text+ "multiMaus";
				               break;
				    default:
					text = text + l.getElement(3);
				}
                     } else {
                        text = l.toString();
                     }
		  //}
		/* We want to look at responses to specific requests made to the Command Station */
		} else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
              	    if (l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
			text = "Command Station Status:";
                        int statusByte=l.getElement(2);
                        if((statusByte&0x01)==0x01) {
                           // Command station is in Emergency Off Mode
                           text = text + " Emergency Off";
			}
                	if ((statusByte&0x02)==0x02){
                   	   // Command station is in Emergency Stop Mode
                           text= text + " Emergency Stop";
                	}
			if ((statusByte&0x08)==0x08){
                           // Command station is in Service Mode
                           text = text + " Service Mode";
                	}
			if ((statusByte&0x40)==0x40){
                   	   // Command station is in Power Up Mode
			   text = text + " Powering up";
			}
                        if((statusByte&0x04)==0x04)
				   text = text + " Auto power-up Mode";
                                else text = text + " Manual power-up Mode";
			if ((statusByte&0x80)==0x80){
                   	   // Command station has a experienced a ram check error
                           text=text + " RAM check error!";
                	}
		   } else if (l.getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
			/* This is a Software version response for XPressNet
                           Version 1 or 2 */
				text = "Command Station Software Version: " + (l.getElementBCD(2).floatValue())/10 + "Type: Unknown (X-Bus V1 or V2)";
		   } else text = l.toString();

		// MU and Double Header Related Responses
		} else if (l.getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
			text = "XpressNet MU+DH error: ";
			switch(l.getElement(1)) {
			case 0x81: text = text+ "Selected Locomotive has not been operated by this XPressNet device or address 0 selected";
				break;
			case 0x82: text = text+ "Selected Locomotive is being operated by another XPressNet device";
				break;
			case 0x83: text = text+ "Selected Locomotive already in MU or DH";
		               break;
			case 0x84: text = text+ "Unit selected for MU or DH has speed setting other than 0";
		               break;
			case 0x85: text = text+ "Locomotive not in a MU";
		               break;
			case 0x86: text = text+ "Locomotive address not a multi-unit base address";
		               break;
			case 0x87: text = text+ "It is not possible to delete the locomotive";
		               break;
			case 0x88: text = text+ "The Command Station Stack is Full";
		               break;
			default:
				text = text + l.getElement(1);
			}
		// Loco Information Response Messages
		} else if (l.getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT) {
                     if(l.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM) {
		          text = "Locomotive F13-F28 Momentary Status: ";
                          // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
			  int element3 = l.getElement(2);
                          // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
			  int element4 = l.getElement(3);
			  text += parseFunctionHighMomentaryStatus(element3,element4);
                     } else {
		        text = "Locomotive Information Response: Normal Unit ";
		        text += parseSpeedandDirection(l.getElement(1),l.getElement(2)) + " ";
                        // message byte 4, contains F0,F1,F2,F3,F4
	                int element3 = l.getElement(3);
                        // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
	                int element4 = l.getElement(4);
	                text += parseFunctionStatus(element3,element4);
                     }
		} else if (l.getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
                     if(l.getElement(1)==0xF8) {
                     // This message is a Hornby addition to the protocol
                     // indicating the speed and direction of a locomoitve
                     // controlled by the elite's built in throttles
		     text = "Elite Speed/Direction Information: Locomotive ";
	             text += calcLocoAddress(l.getElement(2),l.getElement(3));
		     text += parseSpeedandDirection(l.getElement(4),l.getElement(5)) + " ";
                     } else if(l.getElement(1)==0xF9) {
                     // This message is a Hornby addition to the protocol
                     // indicating the function on/off status of a locomoitve
                     // controlled by the elite's built in throttles
		     text = "Elite Function Information: Locomotive ";
	             text += calcLocoAddress(l.getElement(2),l.getElement(3));
                     // message byte 5, contains F0,F1,F2,F3,F4
	             int element4 = l.getElement(4);
                     // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
	             int element5 = l.getElement(5);
	             text += parseFunctionStatus(element4,element5);
                     } else {
		        text = "Locomotive Information Response: Locomotive in Multiple Unit ";
		        text += parseSpeedandDirection(l.getElement(1),l.getElement(2)) + " ";
                        // message byte 4, contains F0,F1,F2,F3,F4
	                int element3 = l.getElement(3);
                        // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
	                int element4 = l.getElement(4);
	                text += parseFunctionStatus(element3,element4);
                     }
		} else if (l.getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
		     text = "Locomotive Information Response: Multi Unit Base Address";
		     text += parseSpeedandDirection(l.getElement(1),l.getElement(2)) + " ";
		} else if (l.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
		     text = "Locomotive Information Response: Locomotive in Double Header ";
		     text += parseSpeedandDirection(l.getElement(1),l.getElement(2)) + " ";
                     // message byte 4, contains F0,F1,F2,F3,F4
	             int element3 = l.getElement(3);
                     // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                     int element4 = l.getElement(4);
	             text += parseFunctionStatus(element3,element4);
		     text += " Second Locomotive in Double Header is: ";
	             text += calcLocoAddress(l.getElement(5),l.getElement(6));
		} else if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
		    text = "Locomotive Information Response: ";
                    switch(l.getElement(1)) {
                       case XNetConstants.LOCO_SEARCH_RESPONSE_N:
			  text += "Search Response, Normal Locomotive: ";
			  text += l.getThrottleMsgAddr();
                          break;
                       case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
			  text += "Search Response, Loco in Double Header: ";
			  text += l.getThrottleMsgAddr();
                          break;
                       case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
			  text += "Search Response, MU Base Address: ";
			  text += l.getThrottleMsgAddr();
                          break;
                       case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
			  text += "Search Response, Loco in MU: ";
			  text += l.getThrottleMsgAddr();
                          break;
                       case XNetConstants.LOCO_SEARCH_NO_RESULT:
			  text += "Search Response, Search failed for: ";
			  text += l.getThrottleMsgAddr();
                          break;
		       case XNetConstants.LOCO_NOT_AVAILABLE:
			  text += "Locomotive ";
			  text += l.getThrottleMsgAddr();
			  text += " is being operated by another device.";
                          break;
		       case XNetConstants.LOCO_FUNCTION_STATUS: {
			  text += "Locomotive ";
			  text += " Function Status: ";
                          // message byte 3, contains F0,F1,F2,F3,F4
			  int element3 = l.getElement(2);
                          // message byte 4, contains F12,F11,F10,F9,F8,F7,F6,F5
			  int element4 = l.getElement(3);
			  text += parseFunctionMomentaryStatus(element3,element4);
                          break;
                       }
		       case XNetConstants.LOCO_FUNCTION_STATUS_HIGH:{
			  text += "Locomotive ";
			  text += "F13-F28 Status: ";
                          // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
			  int element3 = l.getElement(2);
                          // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
			  int element4 = l.getElement(3);
			  text += parseFunctionHighStatus(element3,element4);
                          break;
                       }
		       default: text = l.toString();
                   }
		// Feedback Response Messages
		} else if (l.isFeedbackBroadcastMessage() ) {
		    text = "Feedback Response:";
		    int numDataBytes = l.getElement(0)&0x0f;
		    for( int i=1;i<numDataBytes;i+=2) {
			switch(l.getFeedbackMessageType(i)) {
				case 0:
					text = text + "Turnout with out Feedback "+
					" Turnout: " +l.getTurnoutMsgAddr(i) +
					" State: ";
					if((l.getElement(i+1)&0x03)==0x00) {
						text = text + "Not Operated";
					} else if((l.getElement(i+1)&0x03)==0x01) {
						text = text + "Thrown Left";
					} else if((l.getElement(i+1)&0x03)==0x02){
						text = text + "Thrown Right";
					} else if((l.getElement(i+1)&0x03)==0x03){
						text = text + "<Invalid>";
					} else text = text + "<Unknown>";
					text = text + "; Turnout: " +(l.getTurnoutMsgAddr(i) +1) +
					" State: ";
					if((l.getElement(i+1)&0x0C)==0x00) {
						text = text + "Not Operated";
					} else if((l.getElement(i+1)&0x0C)==0x04) {
						text = text + "Thrown Left";
					} else if((l.getElement(i+1)&0x0C)==0x08){
						text = text + "Thrown Right";
					} else if((l.getElement(i+1)&0x0C)==0x0C){
						text = text + "<Invalid>";
					} else text = text + "<Unknown>";
					break;
				case 1:
					text = text + "Turnout with Feedback "+
					" Turnout: " +l.getTurnoutMsgAddr(i) +
					" State: ";
					if((l.getElement(i+1)&0x03)==0x00) {
						text = text + "Not Operated";
					} else if((l.getElement(i+1)&0x03)==0x01) {
						text = text + "Thrown Left";
					} else if((l.getElement(i+1)&0x03)==0x02){
						text = text + "Thrown Right";
					} else if((l.getElement(i+1)&0x03)==0x03){
						text = text + "<Invalid>";
					} else text = text + "<Unknown>";
					text = text + "; Turnout: " +(l.getTurnoutMsgAddr() +1) +
					" State: ";
					if((l.getElement(i+1)&0x0C)==0x00) {
						text = text + "Not Operated";
					} else if((l.getElement(i+1)&0x0C)==0x04) {
						text = text + "Thrown Left";
					} else if((l.getElement(i+1)&0x0C)==0x08){
						text = text + "Thrown Right";
					} else if((l.getElement(i+1)&0x0C)==0x0C){
						text = text + "<Invalid>";
					} else text = text + "<Unknown>";
					break;
				case 2:
					text = text + "Feedback Encoder " +
					"Base Address: " + 
					(l.getFeedbackEncoderMsgAddr(i)+1);
					boolean highnibble = ((l.getElement(i+1) &0x10)==0x10);
					text = text + " Contact: " + (highnibble?5:1);
					text = text + " State: " + (((l.getElement(i+1) &0x01)==0x01)?"On;":"Off;");
					text = text + " Contact: " + (highnibble?6:2);
					text = text + " State: " + (((l.getElement(i+1) &0x02)==0x02)?"On;":"Off;");
					text = text + " Contact: " + (highnibble?7:3);
					text = text + " State: " + (((l.getElement(i+1) &0x04)==0x04)?"On;":"Off;");
					text = text + " Contact: " + (highnibble?8:4);
					text = text + " State: " + (((l.getElement(i+1) &0x08)==0x08)?"On;":"Off;");
					break;
				default:
					text = text + l.getElement(i) + " " + l.getElement(i+1);
			}
		   }
		} else {
		     text = l.toString();
		}
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(text+"\n", new String(raw));

	}

 	// listen for the messages to the LI100/LI101
    	@SuppressWarnings("fallthrough")
		public synchronized void message(XNetMessage l) {
		// display the raw data if requested
		StringBuilder raw = new StringBuilder("packet: ");
		if ( rawCheckBox.isSelected() ) {
				raw.append(l.toString());
		}

		// display the decoded data
		String text;
                /* Start decoding messages sent by the computer */
		/* Start with LI101F requests */
		if(l.getElement(0)==XNetConstants.LI_VERSION_REQUEST) {
			text = "REQUEST LI10x hardware/software version";
		} else if(l.getElement(0)==XNetConstants.LI101_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.LI101_REQUEST_ADDRESS:
				text= "REQUEST LI101 Address " +l.getElement(2);
				break;
		  case XNetConstants.LI101_REQUEST_BAUD:
				text= "REQUEST LI101 Baud Rate ";
				switch(l.getElement(2)){
				   case 1: text += "19200bps (default)";
					break;
				   case 2: text += "38400bps";
					break;
				   case 3: text += "57600bps";
					break;
				   case 4: text += "115200bps";
					break;
				   default: text += "<undefined>";
				}
				break;
		  default:
			text = l.toString();
		  }
		/* Next, we have generic requests */
		} else if(l.getElement(0)==XNetConstants.CS_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.EMERGENCY_OFF:
				text = "REQUEST: Emergency Off";
				break;
		  case XNetConstants.RESUME_OPS:
				text = "REQUEST: Normal Operations Resumed";
				break;
		  case XNetConstants.SERVICE_MODE_CSRESULT:
				text = "REQUEST: Service Mode Results";
				break;
		  case XNetConstants.CS_VERSION:
				text = "REQUEST: Command Station Version";
				break;
		  case XNetConstants.CS_STATUS:
				text = "REQUEST: Command Station Status";
				break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(1)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(2)==XNetConstants.CS_POWERMODE_AUTO ) {
			  text= "REQUEST: Set Power-up Mode to Automatic";
		} else if(l.getElement(0)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(1)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(2)==XNetConstants.CS_POWERMODE_MANUAL ) {
			  text= "REQUEST: Set Power-up Mode to Manual";
		/* Next, we have Programming Requests */
		} else if(l.getElement(0)==XNetConstants.PROG_READ_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.PROG_READ_MODE_REGISTER:
				text = "Service Mode Request: Read Register " + l.getElement(2);
				break;
		  case XNetConstants.PROG_READ_MODE_CV:
				text = "Service Mode Request: Read CV " + l.getElement(2) + " in Direct Mode";
				break;
		  case XNetConstants.PROG_READ_MODE_PAGED:
				text = "Service Mode Request: Read CV " + l.getElement(2) + " in Paged Mode";
				break;
                  case XNetConstants.PROG_READ_MODE_CV_V36:
		        text = "Service Mode Request (V3.6): Read CV " + (l.getElement(2)==0?1024:l.getElement(2)) + " in Direct Mode";
                               break;
                  case XNetConstants.PROG_READ_MODE_CV_V36+1:
		        text = "Service Mode Request (V3.6): Read CV " + (256+l.getElement(2)) + " in Direct Mode";
                               break;
                  case XNetConstants.PROG_READ_MODE_CV_V36+2:
		        text = "Service Mode Request (V3.6): Read CV " + (512+l.getElement(2)) + " in Direct Mode";
                               break;
                  case XNetConstants.PROG_READ_MODE_CV_V36+3:
		        text = "Service Mode Request (V3.6): Read CV " + (768+l.getElement(2)) + " in Direct Mode";
                               break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.PROG_WRITE_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.PROG_WRITE_MODE_REGISTER:
				text = "Service Mode Request: Write " + l.getElement(3) +" to Register " + l.getElement(2);
				break;
		  case XNetConstants.PROG_WRITE_MODE_CV:
				text = "Service Mode Request: Write " + l.getElement(3) +" to CV " + l.getElement(2) + " in Direct Mode";
				break;
		  case XNetConstants.PROG_WRITE_MODE_PAGED:
				text = "Service Mode Request: Write " + l.getElement(3) +" to CV " + l.getElement(2) + " in Paged Mode";
				break;
                  case XNetConstants.PROG_WRITE_MODE_CV_V36:
				text = "Service Mode Request (V3.6): Write " + l.getElement(3) +" to CV " + (l.getElement(2)==0?1024:l.getElement(2)) + " in Direct Mode";
                               break;
                  case (XNetConstants.PROG_WRITE_MODE_CV_V36+1):
				text = "Service Mode Request (V3.6): Write " + l.getElement(3) +" to CV " + (256+l.getElement(2)) + " in Direct Mode";
                               break;
                  case (XNetConstants.PROG_WRITE_MODE_CV_V36+2):
				text = "Service Mode Request (V3.6): Write " + l.getElement(3) +" to CV " + (512+l.getElement(2)) + " in Direct Mode";
                               break;
                  case (XNetConstants.PROG_WRITE_MODE_CV_V36+3):
				text = "Service Mode Request (V3.6): Write " + l.getElement(3) +" to CV " + (768+l.getElement(2)) + " in Direct Mode";
                               break;
		  default:
			text = l.toString();
		  }
                } else if(l.getElement(0)==XNetConstants.OPS_MODE_PROG_REQ) {
		  switch(l.getElement(1)) {
		  case XNetConstants.OPS_MODE_PROG_WRITE_REQ:
				text = "Operations Mode Programming Request: ";
				if((l.getElement(4) & 0xEC)==0xEC || (l.getElement(4) & 0xE4)==0xE4) {
				   if((l.getElement(4) & 0xEC)==0xEC) {
					text = text + "Byte Mode Write: ";
				   } else if((l.getElement(4) & 0xE4)==0xE4) {
					text = text + "Byte Mode Verify: ";
				   }
				   text = text + l.getElement(6)
					+" to CV "
					+ (1+l.getElement(5)+((l.getElement(4)&0x03)<<8))
					+" For Decoder Address "
					+calcLocoAddress(l.getElement(2),l.getElement(3));
				break;
 				} else if((l.getElement(4) & 0xE8)==0xE8) {
					if((l.getElement(6) & 0x10) == 0x10) {
					   text = text + "Bit Mode Write: ";
					} else {
					   text = text + "Bit Mode Verify: ";
					}
					text = text + ((l.getElement(6) &0x08)>>3)
					   +" to CV "
					   + (1+l.getElement(5)+((l.getElement(4)&0x03)<<8))
					   +" bit " 
					   + (l.getElement(6)&0x07)
					   +" For Decoder Address "
					   +calcLocoAddress(l.getElement(2),l.getElement(3));
				break;
				}
				//fall through
		  default:
			text = l.toString();
		  }
		// Next, decode the locomotive operation requests
                } else if(l.getElement(0)==XNetConstants.LOCO_OPER_REQ) {
		  text = "Mobile Decoder Operations Request: ";
		  int speed;
		  switch(l.getElement(1)) {
		  case XNetConstants.LOCO_SPEED_14: 
						text = text 
						+"Set Address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3))
						+" To Speed Step "
						+ (l.getElement(4)&0x0f)
						+ " and direction ";	
					if((l.getElement(4)&0x80)!=0) 
						text+="Forward"; 
					else text+="Reverse";
						text+= " In 14 speed step mode.";
						break;
		  case XNetConstants.LOCO_SPEED_27:
						text = text 
						+"Set Address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3))
						+" To Speed Step ";
					speed=(((l.getElement(4)&0x10)>> 4) + ((l.getElement(4)&0x0F) << 1));
					if(speed>=3){ speed -=3; }
					text += speed;
					if((l.getElement(4)&0x80)!=0) 
						text+=" and direction Forward"; 
					else text+="and direction Reverse";
						text+= " In 27 speed step mode.";
						break;
		  case XNetConstants.LOCO_SPEED_28:
						text = text 
						+"Set Address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3))
						+" To Speed Step ";
					speed=(((l.getElement(4)&0x10)>> 4) + ((l.getElement(4)&0x0F) << 1));
					if(speed>=3){ speed -=3; }
						text += speed;
					if((l.getElement(4)&0x80)!=0) 
						text+=" and direction Forward"; 
					else text+="and direction Reverse";
						text+= " In 28 speed step mode.";
						break;
		  case XNetConstants.LOCO_SPEED_128:
						text = text 
						+"Set Address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3))
						+" To Speed Step "
						+ (l.getElement(4) & 0x7F)
						+ " and direction ";
					if((l.getElement(4)&0x80)!=0) 
						text+="Forward"; 
					else text+="Reverse";
						text+= " In 128 speed step mode.";
						break;
		  case XNetConstants.LOCO_SET_FUNC_GROUP1: {
						text = text 
						+"Set Function Group 1 for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x10)!=0) 
						text += "F0 on ";
					else text += "F0 off ";
					if((element4 & 0x01)!=0) 
						text += "F1 on ";
					else text += "F1 off ";
					if((element4 & 0x02)!=0)
						text += "F2 on ";
					else text += "F2 off ";
					if((element4 & 0x04)!=0)
						text += "F3 on ";
					else text += "F3 off ";
					if((element4 & 0x08)!=0)
						text += "F4 on ";
					else text += "F4 off ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_GROUP2: {
						text = text 
						+"Set Function Group 2 for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)!=0)
						text += "F5 on ";
					else text += "F5 off ";
					if((element4 & 0x02)!=0)
						text += "F6 on ";
					else text += "F6 off ";
					if((element4 & 0x04)!=0)
						text += "F7 on ";
					else text += "F7 off ";
					if((element4 & 0x08)!=0)
						text += "F8 on ";
					else text += "F8 off ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_GROUP3: {
						text = text 
						+"Set Function Group 3 for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)!=0)
						text += "F9 on ";
					else text += "F9 off ";
					if((element4 & 0x02)!=0)
						text += "F10 on ";
					else text += "F10 off ";
					if((element4 & 0x04)!=0)
						text += "F11 on ";
					else text += "F11 off ";
					if((element4 & 0x08)!=0)
						text += "F12 on ";
					else text += "F12 off ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_GROUP4: {
						text = text 
						+"Set Function Group 4 for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)!=0)
						text += "F13 on ";
					else text += "F13 off ";
					if((element4 & 0x02)!=0)
						text += "F14 on ";
					else text += "F14 off ";
					if((element4 & 0x04)!=0)
						text += "F15 on ";
					else text += "F15 off ";
					if((element4 & 0x08)!=0)
						text += "F16 on ";
					else text += "F16 off ";
					if((element4 & 0x10)!=0)
						text += "F17 on ";
					else text += "F17 off ";
					if((element4 & 0x20)!=0)
						text += "F18 on ";
					else text += "F18 off ";
					if((element4 & 0x40)!=0)
						text += "F19 on ";
					else text += "F19 off ";
					if((element4 & 0x80)!=0)
						text += "F20 on ";
					else text += "F20 off ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_GROUP5: {
						text = text 
						+"Set Function Group 5 for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)!=0)
						text += "F21 on ";
					else text += "F21 off ";
					if((element4 & 0x02)!=0)
						text += "F22 on ";
					else text += "F22 off ";
					if((element4 & 0x04)!=0)
						text += "F23 on ";
					else text += "F23 off ";
					if((element4 & 0x08)!=0)
						text += "F24 on ";
					else text += "F24 off ";
					if((element4 & 0x10)!=0)
						text += "F25 on ";
					else text += "F25 off ";
					if((element4 & 0x20)!=0)
						text += "F26 on ";
					else text += "F26 off ";
					if((element4 & 0x40)!=0)
						text += "F27 on ";
					else text += "F27 off ";
					if((element4 & 0x80)!=0)
						text += "F28 on ";
					else text += "F28 off ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_Group1: {
						text = text 
						+"Set Function Group 1 Momentary Status for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x10)==0)
						text += "F0 continuous ";
					else text += "F0 momentary ";
					if((element4 & 0x01)==0)
						text += "F1 continuous ";
					else text += "F1 momentary ";
					if((element4 & 0x02)==0)
						text += "F2 continuous ";
					else text += "F2 momentary ";
					if((element4 & 0x04)==0)
						text += "F3 continous ";
					else text += "F3 momentary ";
					if((element4 & 0x08)==0) 
						text += "F4 continuous ";
					else text += "F4 momentary ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_Group2: {
						text = text 
						+"Set Function Group 2 Momentary Status for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)==0)
						text += "F5 continuous ";
					else text += "F5 momentary ";
					if((element4 & 0x02)==0)
						text += "F6 continuous ";
					else text += "F6 momentary ";
					if((element4 & 0x04)==0)
						text += "F7 continuous ";
					else text += "F7 momentary ";
					if((element4 & 0x08)==0)
						text += "F8 continuous ";
					else text += "F8 momentary ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_Group3: {
						text = text 
						+"Set Function Group 3 Momentary Status for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)==0)
						text += "F9 continuous ";
					else text += "F9 momentary ";
					if((element4 & 0x02)==0)
						text += "F10 continuous ";
					else text += "F10 momentary ";
					if((element4 & 0x04)==0)
						text += "F11 continuous ";
					else text += "F11 momentary ";
					if((element4 & 0x08)==0) 
						text += "F12 continuous ";
					else text += "F12 momentary ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_Group4: {
						text = text 
						+"Set Function Group 4 Momentary Status for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)==0)
						text += "F13 continuous ";
					else text += "F13 momentary ";
					if((element4 & 0x02)==0)
						text += "F14 continuous ";
					else text += "F14 momentary ";
					if((element4 & 0x04)==0)
						text += "F15 continuous ";
					else text += "F15 momentary ";
					if((element4 & 0x08)==0) 
						text += "F16 continuous ";
					else text += "F16 momentary ";
					if((element4 & 0x10)==0)
						text += "F17 continuous ";
					else text += "F17 momentary ";
					if((element4 & 0x20)==0)
						text += "F18 continuous ";
					else text += "F18 momentary ";
					if((element4 & 0x40)==0)
						text += "F19 continuous ";
					else text += "F19 momentary ";
					if((element4 & 0x80)==0) 
						text += "F20 continuous ";
					else text += "F20 momentary ";
						break;
					}
		  case XNetConstants.LOCO_SET_FUNC_Group5: {
						text = text 
						+"Set Function Group 5 Momentary Status for address: "
						+calcLocoAddress(l.getElement(2),l.getElement(3)) + " ";
					int element4 = l.getElement(4);
					if((element4 & 0x01)==0)
						text += "F21 continuous ";
					else text += "F21 momentary ";
					if((element4 & 0x02)==0)
						text += "F22 continuous ";
					else text += "F22 momentary ";
					if((element4 & 0x04)==0)
						text += "F23 continuous ";
					else text += "F23 momentary ";
					if((element4 & 0x08)==0) 
						text += "F24 continuous ";
					else text += "F24 momentary ";
					if((element4 & 0x10)==0)
						text += "F25 continuous ";
					else text += "F25 momentary ";
					if((element4 & 0x20)==0)
						text += "F26 continuous ";
					else text += "F26 momentary ";
					if((element4 & 0x40)==0)
						text += "F27 continuous ";
					else text += "F27 momentary ";
					if((element4 & 0x80)==0) 
						text += "F28 continuous ";
					else text += "F28 momentary ";
						break;
					}
		  case XNetConstants.LOCO_ADD_MULTI_UNIT_REQ: text = text 
				+ "Add Locomotive:" 
					+ calcLocoAddress(l.getElement(2),l.getElement(3))
					+ " To Multi Unit Consist: "
					+ l.getElement(4)
					+ " With Loco Direction Normal";
					break;
		  case (XNetConstants.LOCO_ADD_MULTI_UNIT_REQ | 0x01): 
				text = text + "Add Locomotive:" 
					+ calcLocoAddress(l.getElement(2),l.getElement(3))
					+ " To Multi Unit Consist: "
					+ l.getElement(4)
					+ " With Loco Direction Reversed";
					break;
		  case (XNetConstants.LOCO_REM_MULTI_UNIT_REQ):
				text = text + "Remove Locomotive:" 
					+ calcLocoAddress(l.getElement(2),l.getElement(3))
					+ " From Multi Unit Consist: "
					+ l.getElement(4);
					break;
		  default:
			text = l.toString();
		  }
                // Emergency Stop a locomotive
		} else if(l.getElement(0)==XNetConstants.EMERGENCY_STOP){
                         text = "Emergency Stop " + 
			 calcLocoAddress(l.getElement(1),l.getElement(2)); // GT 2007/11/6 Corrected calculation
		// Disolve or Establish a Double Header
		} else if(l.getElement(0)==XNetConstants.LOCO_DOUBLEHEAD &&
			  l.getElement(1)==XNetConstants.LOCO_DOUBLEHEAD_BYTE2){
		        text = "Double Header Request: ";
			int loco1=calcLocoAddress(l.getElement(2),l.getElement(3));
			int loco2=calcLocoAddress(l.getElement(4),l.getElement(5));
			if(loco2 == 0) {
			   text = text + "Disolve Double Header that includes mobile decoder " + loco1;
			} else {
			   text = text + "Establish Double Header with " + loco1 + " and " + loco2;
			}
		// Locomotive Status Request messages
                } else if(l.getElement(0)==XNetConstants.LOCO_STATUS_REQ) {
		       switch(l.getElement(1)) {
		          case XNetConstants.LOCO_INFO_REQ_FUNC:
			      text="Request for Address " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3)) + 
				  " function momentary/continuous status.";
                              break;
		          case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
			      text="Request for Address " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3)) + 
				  " F13-F28 on/off status.";
                              break;
		          case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
			      text="Request for Address " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3)) + 
				  " F13-F28 momentary/continuous status.";
                              break;
                          case XNetConstants.LOCO_INFO_REQ_V3:
			      text="Request for Address " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3)) + 
				  " speed/direction/function on/off status.";
			      break;
                          case XNetConstants.LOCO_STACK_SEARCH_FWD:
			      text="Search Command Station Stack Forward - Start Address: " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3));
			      break;
                          case XNetConstants.LOCO_STACK_SEARCH_BKWD:
			      text="Search Command Station Stack Backward - Start Address: " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3));
			      break;
                          case XNetConstants.LOCO_STACK_DELETE:
			      text="Delete Address " +
                                  calcLocoAddress(l.getElement(2),l.getElement(3)) +
                                  " from Command Station Stack.";
			      break;
		          default: text=l.toString();
                      }
		// Accessory Info Request message
		} else if(l.getElement(0)==XNetConstants.ACC_INFO_REQ){
			text = "Accessory Decoder/Feedback Encoder Status Request: "+
			       "Base Address " + l.getElement(1) + ",";
			text = text + (((l.getElement(2)&0x01)==0x01)?"Upper":"Lower") + " Nibble.";
		} else if(l.getElement(0)==XNetConstants.ACC_OPER_REQ){
			 text = "Accessory Decoder Operations Request: ";
			 int baseaddress = l.getElement(1);
			 int subaddress = ((l.getElement(2)&0x06)>>1);
			 int address = (baseaddress*4)+subaddress+1;
			 text = text + "Turnout Address " + address + "(" +
			       "Base Address " + l.getElement(1) + "," +
			       "Sub Address " + ((l.getElement(2)&0x06)>>1) + ") ";
			 text = text + "Turn Output " + (l.getElement(2)&0x01) + 
			       " " + (((l.getElement(2)&0x08)==0x08)?"On.":"Off.");
		} else {
		     text = l.toString();
		}
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(text+"\n", new String(raw));

    	}

        // Handle a timeout notification
        public void notifyTimeout(XNetMessage msg)
        {
           if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
        }

	/**
  	 *  We need to calculate the locomotive address when doing the
	 *  translations back to text.
	 *  XPressNet Messages will have these as two elements, which need
         *  to get translated back into a single address by reversing the
	 *  formulas used to calculate them in the first place.
	 */
	private int calcLocoAddress(int AH,int AL) {
		if(AH==0x00) {
			/* if AH is 0, this is a short address */
			return(AL);
		} else {
			/* This must be a long address */
			int address = 0;
			address = ( (AH*256) &0xFF00);
			address += (AL &0xFF);
			address -= 0xC000;
			return(address);
	   	}
	}

        /* parse the speed step and the direction information for a locomotive
         * element1 contains the speed step mode designation and 
         * availability information
         * element2 contains the data byte including the step mode and 
         * availability information 
         */

	private String parseSpeedandDirection(int element1,int element2){
		String text = "";
                int speedVal = 0;
                if ((element2 & 0x80)==0x80)
                    text+= "Direction Forward,";
		else text+= "Direction Reverse,";
            
                if((element1&0x04)==0x04) {
                   // We're in 128 speed step mode
                   speedVal=element2 & 0x7f;
                   // The first speed step used is actually at 2 for 128
                   // speed step mode.
                   if(speedVal>=1) { speedVal-=1; }
                        else speedVal=0;
		   text += "128 Speed Step Mode,";
                } else if((element1&0x02)==0x02) { 
                   // We're in 28 speed step mode
                   // We have to re-arange the bits, since bit 4 is the LSB,
                   // but other bits are in order from 0-3
                   speedVal =((element2 & 0x0F)<<1) + ((element2 & 0x10) >>4);
                   // The first speed step used is actually at 4 for 28  
                   // speed step mode.
                   if(speedVal>=3) { speedVal-=3; }
                        else speedVal=0;
		   text += "28 Speed Step Mode,";           
                } else if((element1&0x01)==0x01) { 
                   // We're in 27 speed step mode
                   // We have to re-arange the bits, since bit 4 is the LSB,
                   // but other bits are in order from 0-3
                   speedVal =((element2 & 0x0F)<<1) + ((element2 & 0x10) >>4);
                   // The first speed step used is actually at 4 for 27
                   // speed step mode.
                   if(speedVal>=3) { speedVal-=3; }
                       else speedVal=0;
		   text += "27 Speed Step Mode,";
                } else {
                   // Assume we're in 14 speed step mode.
                   speedVal=(element2 & 0x0F);
                   if(speedVal>=1) { speedVal-=1; }
                        else speedVal=0;
		   text += "14 Speed Step Mode,";
                }

		text += "Speed Step " + speedVal +". ";

                if((element1 & 0x08)==0x08) 
                   text += " Address in use by another device.";
		else text += " Address is Free for Operation.";
		return(text);
        }

        /* Parse the status of functions.
         * element3 contains the data byte including F0,F1,F2,F3,F4
         * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
         */

	private String parseFunctionStatus(int element3,int element4){
		String text = "";
		if((element3 & 0x10)!=0) 
		   text += "F0 on ";
		else text += "F0 off ";
		if((element3 & 0x01)!=0) 
		   text += "F1 on ";
		else text += "F1 off ";
		if((element3 & 0x02)!=0)
	           text += "F2 on ";
		else text += "F2 off ";
		if((element3 & 0x04)!=0)
		   text += "F3 on ";
	        else text += "F3 off ";
		if((element3 & 0x08)!=0)
		   text += "F4 on ";
	        else text += "F4 off ";
	        if((element4 & 0x01)!=0) 
		   text += "F5 on ";
		else text += "F5 off ";
		if((element4 & 0x02)!=0)
		   text += "F6 on ";
		else text += "F6 off ";
		if((element4 & 0x04)!=0)
		   text += "F7 on ";
		else text += "F7 off ";
		if((element4 & 0x08)!=0)
		   text += "F8 on ";
		else text += "F8 off ";
		if((element4 & 0x10)!=0) 
		   text += "F9 on ";
		else text += "F9 off ";
		if((element4 & 0x20)!=0)
	 	   text += "F10 on ";
		else text += "F10 off ";
		if((element4 & 0x40)!=0)
		   text += "F11 on ";
		else text += "F11 off ";
		if((element4 & 0x80)!=0)
		   text += "F12 on ";
		else text += "F12 off ";
		return(text);
        }

        /* Parse the status of functions functions F13-F28.
         * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
         * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
         */

	private String parseFunctionHighStatus(int element3,int element4){
		String text = "";
		if((element3 & 0x01)!=0) 
		   text += "F13 on ";
		else text += "F13 off ";
		if((element3 & 0x02)!=0)
	           text += "F14 on ";
		else text += "F14 off ";
		if((element3 & 0x04)!=0)
		   text += "F15 on ";
	        else text += "F15 off ";
		if((element3 & 0x08)!=0)
		   text += "F16 on ";
	        else text += "F16 off ";
		if((element3 & 0x10)!=0) 
		   text += "F17 on ";
		else text += "F17 off ";
		if((element3 & 0x20)!=0)
	 	   text += "F18 on ";
		else text += "F18 off ";
		if((element3 & 0x40)!=0)
		   text += "F19 on ";
		else text += "F19 off ";
		if((element3 & 0x80)!=0)
		   text += "F20 on ";
		else text += "F20 off ";
	        if((element4 & 0x01)!=0) 
		   text += "F21 on ";
		else text += "F21 off ";
		if((element4 & 0x02)!=0)
		   text += "F22 on ";
		else text += "F22 off ";
		if((element4 & 0x04)!=0)
		   text += "F23 on ";
		else text += "F23 off ";
		if((element4 & 0x08)!=0)
		   text += "F24 on ";
		else text += "F24 off ";
		if((element4 & 0x10)!=0) 
		   text += "F25 on ";
		else text += "F25 off ";
		if((element4 & 0x20)!=0)
	 	   text += "F26 on ";
		else text += "F26 off ";
		if((element4 & 0x40)!=0)
		   text += "F27 on ";
		else text += "F27 off ";
		if((element4 & 0x80)!=0)
		   text += "F28 on ";
		else text += "F28 off ";
		return(text);
        }
        /* Parse the Momentary sytatus of functions.
         * element3 contains the data byte including F0,F1,F2,F3,F4
         * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
         */

	private String parseFunctionMomentaryStatus(int element3,int element4){
		String text = "";
		if((element3 & 0x10)!=0) 
		   text += "F0 Momentary ";
		else text += "F0 Continuous ";
		if((element3 & 0x01)!=0) 
		   text += "F1 Momentary ";
		else text += "F1 Continuous ";
		if((element3 & 0x02)!=0)
	           text += "F2 Momentary ";
		else text += "F2 Continuous ";
		if((element3 & 0x04)!=0)
		   text += "F3 Momentary ";
	        else text += "F3 Continuous ";
		if((element3 & 0x08)!=0)
		   text += "F4 Momentary ";
	        else text += "F4 Continuous ";
	        if((element4 & 0x01)!=0) 
		   text += "F5 Momentary ";
		else text += "F5 Continuous ";
		if((element4 & 0x02)!=0)
		   text += "F6 Momentary ";
		else text += "F6 Continuous ";
		if((element4 & 0x04)!=0)
		   text += "F7 Momentary ";
		else text += "F7 Continuous ";
		if((element4 & 0x08)!=0)
		   text += "F8 Momentary ";
		else text += "F8 Continuous ";
		if((element4 & 0x10)!=0) 
		   text += "F9 Momentary ";
		else text += "F9 Continuous ";
		if((element4 & 0x20)!=0)
	 	   text += "F10 Momentary ";
		else text += "F10 Continuous ";
		if((element4 & 0x40)!=0)
		   text += "F11 Momentary ";
		else text += "F11 Continuous ";
		if((element4 & 0x80)!=0)
		   text += "F12 Momentary ";
		else text += "F12 Continuous ";
		return(text);
        }
  
        /* Parse the Momentary sytatus of functions F13-F28.
         * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
         * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
         */

	private String parseFunctionHighMomentaryStatus(int element3,int element4){
		String text = "";
		if((element3 & 0x01)!=0) 
		   text += "F13 Momentary ";
		else text += "F13 Continuous ";
		if((element3 & 0x02)!=0)
	           text += "F14 Momentary ";
		else text += "F14 Continuous ";
		if((element3 & 0x04)!=0)
		   text += "F15 Momentary ";
	        else text += "F15 Continuous ";
		if((element3 & 0x08)!=0)
		   text += "F16 Momentary ";
	        else text += "F16 Continuous ";
		if((element3 & 0x10)!=0) 
		   text += "F17 Momentary ";
		else text += "F17 Continuous ";
		if((element3 & 0x20)!=0)
	 	   text += "F18 Momentary ";
		else text += "F18 Continuous ";
		if((element3 & 0x40)!=0)
		   text += "F19 Momentary ";
		else text += "F19 Continuous ";
		if((element3 & 0x80)!=0)
		   text += "F20 Momentary ";
		else text += "F20 Continuous ";
		if((element4 & 0x01)!=0) 
		   text += "F21 Momentary ";
		else text += "F21 Continuous ";
		if((element4 & 0x02)!=0)
	 	   text += "F22 Momentary ";
		else text += "F22 Continuous ";
		if((element4 & 0x04)!=0)
		   text += "F23 Momentary ";
		else text += "F23 Continuous ";
		if((element4 & 0x08)!=0)
		   text += "F24 Momentary ";
		else text += "F24 Continuous ";
		if((element4 & 0x10)!=0) 
		   text += "F25 Momentary ";
		else text += "F25 Continuous ";
		if((element4 & 0x20)!=0)
	 	   text += "F26 Momentary ";
		else text += "F26 Continuous ";
		if((element4 & 0x40)!=0)
		   text += "F27 Momentary ";
		else text += "F27 Continuous ";
		if((element4 & 0x80)!=0)
		   text += "F28 Momentary ";
		else text += "F28 Continuous ";
		return(text);
        }

	static Logger log = Logger.getLogger(XNetMonFrame.class.getName());

}

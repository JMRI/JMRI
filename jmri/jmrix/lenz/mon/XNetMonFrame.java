// XNetMonFrame.java

package jmri.jmrix.lenz.mon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Frame displaying (and logging) XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version         $Revision: 2.6 $
 */
 public class XNetMonFrame extends jmri.jmrix.AbstractMonFrame implements XNetListener {

	public XNetMonFrame() {
		super();
	}

	protected String title() { return "XpressNet Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		XNetTrafficController.instance().removeXNetListener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		XNetTrafficController.instance().addXNetListener(~0, this);
	}

	public synchronized void message(XNetReply l) {  // receive a XpressNet message and log it
		// display the raw data if requested
		String raw = "packet: ";
		if ( rawCheckBox.isSelected() ) {
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				raw += Integer.toHexString(l.getElement(i))+" ";
			raw+="\n";
		}

		// display the decoded data
		String text;
		// First, Decode anything that is sent by the LI10x, and 
                // not the command station 
		if(l.isOkMessage()) {
		   text=new String("Command Successfully Sent/Normal Operations Resumed after timeout");
		} else if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER) {
		  switch(l.getElement(1)) {
		  case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR: 
					text=new String("Error Occurred between the interface and the PC");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR: 
					text=new String("Error Occurred between the interface and the command station");
					break;		  
	          case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR: 
					text=new String("Unknown Communications Error");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR: 
					text=new String("Command Station no longer providing a timeslot for communications");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW: 
					text=new String("Buffer Overflow in interface");
					break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.LI_VERSION_RESPONSE) {
			text = new String("LI10x hardware Version:  " + 
					    (l.getElementBCD(1).floatValue())/10 + 
					  " Software Version: " +
					    l.getElementBCD(2));
		} else if(l.getElement(0)==XNetConstants.LI101_REQUEST) {
		  // The request and response for baud rate look the same, 
		  // so we need this for both incoming and outgoing directions
		  switch(l.getElement(1)) {
		  case XNetConstants.LI101_REQUEST_ADDRESS:
				text= new String("RESPONSE LI101 Address " +l.getElement(2));
				break;
		  case XNetConstants.LI101_REQUEST_BAUD:
				text= new String("RESPONSE LI101 Baud Rate: ");
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
				text= new String("Broadcast: Normal Operations Resumed");
				break;
		  case XNetConstants.BC_EVERYTHING_OFF:
				text= new String("Broadcast: Emergency Off (short circuit)");
				break;
		  case XNetConstants.BC_SERVICE_MODE_ENTRY:
				text= new String("Broadcast: Service Mode Entry");
				break;
		  case XNetConstants.PROG_SHORT_CIRCUIT:
				text= new String("Service Mode: Short Circuit");
				break;
		  case XNetConstants.PROG_BYTE_NOT_FOUND:
				text= new String("Service Mode: Data Byte Not Found");
				break;
		  case XNetConstants.PROG_CS_BUSY:
				text= new String("Service Mode: Command Station Busy");
				break;
		  case XNetConstants.PROG_CS_READY:
				text= new String("Service Mode: Command Station Ready");
				break;
		  case XNetConstants.CS_BUSY:
				text= new String("Command Station Busy");
				break;
		  case XNetConstants.CS_NOT_SUPPORTED:
				text= new String("XPressNet Instruction not supported by Command Station");
				break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.BC_EMERGENCY_STOP &&
			  l.getElement(1)==XNetConstants.BC_EVERYTHING_STOP) {
				text= new String("Broadcast: Emergency Stop (track power on)");
                /* Followed by Service Mode responces */
		} else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE) {
		  switch(l.getElement(1)) {
		  case XNetConstants.CS_SERVICE_DIRECT_RESPONSE:
				text = new String("Service Mode: Direct Programming Responce: CV:" +
				       l.getElement(2) +
				       " Value: " +
				       l.getElement(3));
				break;
		  case XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE:
				text = new String("Service Mode: Register or Paged Mode Responce: CV:" +
				       l.getElement(2) +
				       " Value: " +
				       l.getElement(3));
				break;
		  case XNetConstants.CS_SOFTWARE_VERSION:
				text = new String("Command Station Software Version: " + (l.getElementBCD(2).floatValue())/10 + "Type: ") ;
				switch(l.getElement(3)) {
				    case 0x00: text = text+ "LZ100/LZV100";
				               break;
				    case 0x01: text = text+ "LH200";
				               break;
				    case 0x02: text = text+ "Compact or Other";
				               break;
				    default:
					text = text + l.getElement(3);
				}
		  default:
			text = l.toString();
		  }
		/* We want to look at responses to specific requests made to the Command Station */
		} else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
              	    if (l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
			text = new String("Command Station Status:") ;
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
				text = new String("Command Station Software Version: " + (l.getElementBCD(2).floatValue())/10 + "Type: Unknown (X-Bus V1 or V2)") ;			
		   } else text = l.toString(); 

		} else { 
		     text = l.toString(); 
		}
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(text+"\n", raw);

	}

 	// listen for the messages to the LI100/LI101
    	public synchronized void message(XNetMessage l) {
		// display the raw data if requested
		String raw = "packet: ";
		if ( rawCheckBox.isSelected() ) {
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				raw += Integer.toHexString(l.getElement(i))+" ";
			raw+="\n";
		}

		// display the decoded data
		String text;
                /* Start decoding messages sent by the computer */
		/* Start with LI101F requests */
		if(l.getElement(0)==XNetConstants.LI_VERSION_REQUEST) {
			text = new String("Request LI10x hardware/software version");
		} else if(l.getElement(0)==XNetConstants.LI101_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.LI101_REQUEST_ADDRESS:
				text= new String("REQUEST LI101 Address " +l.getElement(2));
				break;
		  case XNetConstants.LI101_REQUEST_BAUD:
				text= new String("REQUEST LI101 Baud Rate ");
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
				text = new String("REQUEST: Emergency Off");
				break;
		  case XNetConstants.RESUME_OPS: 
				text = new String("REQUEST: Normal Operations Resumed");
				break;
		  case XNetConstants.SERVICE_MODE_CSRESULT: 
				text = new String("REQUEST: Service Mode Results");
				break;
		  case XNetConstants.CS_VERSION: 
				text = new String("REQUEST: Command Station Version");
				break;
		  case XNetConstants.CS_STATUS: 
				text = new String("REQUEST: Command Station Status");
				break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(1)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(2)==XNetConstants.CS_POWERMODE_AUTO ) {
			  text= new String("REQUEST: Set Power-up Mode to Automatic");
		} else if(l.getElement(0)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(1)==XNetConstants.CS_SET_POWERMODE &&
			  l.getElement(2)==XNetConstants.CS_POWERMODE_MANUAL ) {
			  text= new String("REQUEST: Set Power-up Mode to Manual");
		/* Next, we have Programming Requests */
		} else if(l.getElement(0)==XNetConstants.PROG_READ_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.PROG_READ_MODE_REGISTER: 
				text = new String("Service Mode Request: Read Register " + l.getElement(2));
				break;
		  case XNetConstants.PROG_READ_MODE_CV: 
				text = new String("Service Mode Request: Read CV " + l.getElement(2) + " in Direct Mode");
				break;
		  case XNetConstants.PROG_READ_MODE_PAGED: 
				text = new String("Service Mode Request: Read CV " + l.getElement(2) + " in Paged Mode");
				break;
		  default:
			text = l.toString();
		  }
		} else if(l.getElement(0)==XNetConstants.PROG_WRITE_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.PROG_WRITE_MODE_REGISTER: 
				text = new String("Service Mode Request: Write " + l.getElement(3) +" to Register " + l.getElement(2));
				break;
		  case XNetConstants.PROG_WRITE_MODE_CV: 
				text = new String("Service Mode Request: Write " + l.getElement(3) +" to CV " + l.getElement(2) + " in Direct Mode");
				break;
		  case XNetConstants.PROG_WRITE_MODE_PAGED: 
				text = new String("Service Mode Request: Write " + l.getElement(3) +" to CV " + l.getElement(2) + " in Paged Mode");
				break;
		  default:
			text = l.toString();
		  }
                } else if(l.getElement(0)==XNetConstants.OPS_MODE_PROG_REQ) {
		  switch(l.getElement(1)) {
		  case XNetConstants.OPS_MODE_PROG_WRITE_REQ: 
				text = new String("Operations Mode Programming Request: ");
				if((l.getElement(4) & 0xEC)==0xEC) {
					text = text + new String("Byte Mode Write: ");
 				} else if((l.getElement(4) & 0xE8)==0xE8) {
					text = text + new String("Bit Mode Write: ");
				}
				text = text + new String(l.getElement(6) 
					+" to Register " 
					+ (1+l.getElement(5)+((l.getElement(4)&0x03)<<8))
					+" For Decoder Address "
					+calcLocoAddress(l.getElement(2),l.getElement(3)));
				break;
		  default:
			text = l.toString();
		  }
		} else { 
		     text = l.toString(); 
		}
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(text+"\n", raw);

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
			address += AL;
			address += ( (AH<<6) -0xC000 );
			return(address);
	   	}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMonFrame.class.getName());

}

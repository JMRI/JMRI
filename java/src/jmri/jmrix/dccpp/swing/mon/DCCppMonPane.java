// DCCppMonPane.java
package jmri.jmrix.dccpp.swing.mon;

import jmri.jmrix.dccpp.DCCppConstants;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying (and logging) DCC++ messages derived from DCCppMonFrame.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author      Paul Bender Copyright (C) 2004-2014
 * @author      Giorgio Terdina Copyright (C) 2007
 * @author      Mark Underwood Copyright (C) 2015
 * @version $Revision$
 */
public class DCCppMonPane extends jmri.jmrix.AbstractMonPane implements DCCppListener {

    final java.util.ResourceBundle rb
            = java.util.ResourceBundle.
            getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle");

    protected DCCppTrafficController tc = null;
    protected DCCppSystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return (rb.getString("DCCppMonFrameTitle"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof DCCppSystemConnectionMemo) {
            memo = (DCCppSystemConnectionMemo) context;
            tc = memo.getDCCppTrafficController();
            // connect to the TrafficController
            tc.addDCCppListener(~0, this);
        }
    }

    /**
     * Initialize the data source.
     */
    @Override
    protected void init() {
    }

    @Override
    public void dispose() {
        // disconnect from the LnTrafficController
        tc.removeDCCppListener(~0, this);
        // and unwind swing
        super.dispose();
    }

    public synchronized void message(DCCppReply l) {				// receive a DCC++ message and log it
        // display the raw data if requested
	// Since DCC++ is text-based traffic, this is good enough for now.
	// TODO: Provide "beautified" output later.
        StringBuilder raw = new StringBuilder();
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        } 
	
	// Beautify and display
	String text = new String();

	switch(l.getElement(0)) {
	    case DCCppConstants.THROTTLE_REPLY:
		text = "Throttle Reply: \n";
		text += "\tRegister: " + l.getRegisterString() + "\n";
		text += "\tSpeed: " + l.getSpeedString() + "\n";
		text += "\tDirection: " + l.getDirectionString() + "\n";
		break;
	    case DCCppConstants.TURNOUT_REPLY:
		text = "Turnout Reply: \n";
		text += "\tT/O Number: " + l.getTOIDString()  + "\n";
		text += "\tDirection: " + l.getTOStateString() + "\n";
		break;
	    case DCCppConstants.PROGRAM_REPLY:
		text = "Program Reply: \n";
		text += "\tCallback Num: " + l.getCallbackNumString()  + "\n";
		text += "\tCallback Sub: " + l.getCallbackSubString()  + "\n";
		text += "\tCV Value: " + l.getCVString()  + "\n";
		break;
	    case DCCppConstants.VERSION_REPLY:
		text = "Base Station Status: ";
		break;
	    case DCCppConstants.POWER_REPLY:
		text = "Power Status: ";
		text += ((char)(l.getElement(1) & 0x00FF) == '1' ? "ON" : "OFF");
		break;
	    default:
		text += "Unregonized reply: ";
	    }

	// we use Llnmon to format, expect it to provide consistent \n after each line
	nextLine(text + "\n", new String(raw));
    }

    // listen for the messages to the LI100/LI101
    @SuppressWarnings("fallthrough")
    public synchronized void message(DCCppMessage l) {
        // display the raw data if requested  
	// Since DCC++ is text-based traffic, this is good enough for now.
	// TODO: Provide "beautified" output later.
        StringBuilder raw = new StringBuilder("packet: ");
        if (rawCheckBox.isSelected()) {
            raw.append(l.toString());
        }

	// Beautify and display
	String text = new String();

	switch(l.getElement(0)) {
	case DCCppConstants.THROTTLE_CMD:
	    text = "Throttle Cmd: ";
	    text += "\n\tRegister: " + l.getRegisterString();
	    text += "\n\tAddress: " + l.getAddressString();
	    text += "\n\tSpeed: " + l.getSpeedString();
	    text += "\n\t:Direction: " + l.getDirectionString();
	    text += "\n";
	    break;
	case DCCppConstants.FUNCTION_CMD:
	    text = "Function Cmd: ";
	    break;
	case DCCppConstants.STATIONARY_DECODER_CMD:
	    text = "Stationary Decoder Cmd: ";
	    break;
	case DCCppConstants.TURNOUT_CMD:
	    text = "Turnout Cmd: ";
	    text += "\n\tT/O ID: " + l.getTOIDString();
	    text += "\n\t:State: " + l.getTOStateString();
	    text += "\n";
	    break;
	case DCCppConstants.OPS_WRITE_CV_BYTE:
	    text = "Ops Write Byte Cmd: ";
	    break;
	case DCCppConstants.OPS_WRITE_CV_BIT:
	    text = "Ops Write Bit Cmd: ";
	    break;
	case DCCppConstants.PROG_WRITE_CV_BYTE:
	    text = "Prog Write Byte Cmd: ";
	    text += "\n\tCV : " + l.getCVString();
	    text += "\n\tValue: " + l.getValueString();
	    text += "\n\tCallback Num: " + l.getCallbackNumString();
	    text += "\n\tCallback Sub: " + l.getCallbackSubString() + "\n";
	    break;
	    
	case DCCppConstants.PROG_WRITE_CV_BIT:
	    text = "Prog Write Bit Cmd: ";
	    text += "\n\tCV : " + l.getCVString();
	    text += "\n\tBit : " + l.getBitString();
	    text += "\n\tValue: " + l.getValueString();
	    text += "\n\tCallback Num: " + l.getCallbackNumString();
	    text += "\n\tCallback Sub: " + l.getCallbackSubString() + "\n";
	    break;
	case DCCppConstants.PROG_READ_CV:
	    text = "Prog Read Cmd: ";
	    text += "\n\tCV: " + l.getCVString();
	    text += "\n\tCallback Num: " + l.getCallbackNumString();
	    text += "\n\tCallback Sub: " + l.getCallbackSubString() + "\n";
	    break;
	case DCCppConstants.TRACK_POWER_ON:
	    text = "Track Power On Cmd: ";
	    break;
	case DCCppConstants.TRACK_POWER_OFF:
	    text = "Track Power Off Cmd: ";
	    break;
	case DCCppConstants.READ_TRACK_CURRENT:
	    text = "Read Track Current Cmd: ";
	    break;
	case DCCppConstants.READ_CS_STATUS:
	    text = "Status Cmd: ";
	    break;
	case DCCppConstants.WRITE_DCC_PACKET_MAIN:
		text = "Write DCC Packet Main Cmd: ";
		break;
	case DCCppConstants.WRITE_DCC_PACKET_PROG:
	    text = "Write DCC Packet Prog Cmd: ";
	    break;
	case DCCppConstants.GET_FREE_MEMORY:
	    text = "Get Free Memory Cmd: ";
	    break;
	case DCCppConstants.LIST_REGISTER_CONTENTS:
	    text = "List Register Contents Cmd: ";
	    break;
	default:
	    text = "Unknown Message: ";
	}
	
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(text + "\n", new String(raw));

    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * We need to calculate the locomotive address when doing the translations
     * back to text. XPressNet Messages will have these as two elements, which
     * need to get translated back into a single address by reversing the
     * formulas used to calculate them in the first place.
     */
    private int calcLocoAddress(int AH, int AL) {
        if (AH == 0x00) {
            /* if AH is 0, this is a short address */
            return (AL);
        } else {
            /* This must be a long address */
            int address = 0;
            address = ((AH * 256) & 0xFF00);
            address += (AL & 0xFF);
            address -= 0xC000;
            return (address);
        }
    }

    /* parse the speed step and the direction information for a locomotive
     * element1 contains the speed step mode designation and 
     * availability information
     * element2 contains the data byte including the step mode and 
     * availability information 
     */
    private String parseSpeedandDirection(int element1, int element2) {
        String text = "";
        int speedVal = 0;
        if ((element2 & 0x80) == 0x80) {
            text += "Direction Forward,";
        } else {
            text += "Direction Reverse,";
        }

        if ((element1 & 0x04) == 0x04) {
            // We're in 128 speed step mode
            speedVal = element2 & 0x7f;
            // The first speed step used is actually at 2 for 128
            // speed step mode.
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += "128 Speed Step Mode,";
        } else if ((element1 & 0x02) == 0x02) {
            // We're in 28 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 28  
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += "28 Speed Step Mode,";
        } else if ((element1 & 0x01) == 0x01) {
            // We're in 27 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 27
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += "27 Speed Step Mode,";
        } else {
            // Assume we're in 14 speed step mode.
            speedVal = (element2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += "14 Speed Step Mode,";
        }

        text += "Speed Step " + speedVal + ". ";

        if ((element1 & 0x08) == 0x08) {
            text += " Address in use by another device.";
        } else {
            text += " Address is Free for Operation.";
        }
        return (text);
    }

    /* Parse the status of functions.
     * element3 contains the data byte including F0,F1,F2,F3,F4
     * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */
    private String parseFunctionStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 on ";
        } else {
            text += "F0 off ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 on ";
        } else {
            text += "F1 off ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 on ";
        } else {
            text += "F2 off ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 on ";
        } else {
            text += "F3 off ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 on ";
        } else {
            text += "F4 off ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 on ";
        } else {
            text += "F5 off ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 on ";
        } else {
            text += "F6 off ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 on ";
        } else {
            text += "F7 off ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 on ";
        } else {
            text += "F8 off ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 on ";
        } else {
            text += "F9 off ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 on ";
        } else {
            text += "F10 off ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 on ";
        } else {
            text += "F11 off ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 on ";
        } else {
            text += "F12 off ";
        }
        return (text);
    }

    /* Parse the status of functions functions F13-F28.
     * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    private String parseFunctionHighStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 on ";
        } else {
            text += "F13 off ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 on ";
        } else {
            text += "F14 off ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 on ";
        } else {
            text += "F15 off ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 on ";
        } else {
            text += "F16 off ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 on ";
        } else {
            text += "F17 off ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 on ";
        } else {
            text += "F18 off ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 on ";
        } else {
            text += "F19 off ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 on ";
        } else {
            text += "F20 off ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 on ";
        } else {
            text += "F21 off ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 on ";
        } else {
            text += "F22 off ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 on ";
        } else {
            text += "F23 off ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 on ";
        } else {
            text += "F24 off ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 on ";
        } else {
            text += "F25 off ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 on ";
        } else {
            text += "F26 off ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 on ";
        } else {
            text += "F27 off ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 on ";
        } else {
            text += "F28 off ";
        }
        return (text);
    }
    /* Parse the Momentary sytatus of functions.
     * element3 contains the data byte including F0,F1,F2,F3,F4
     * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */

    private String parseFunctionMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 Momentary ";
        } else {
            text += "F0 Continuous ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 Momentary ";
        } else {
            text += "F1 Continuous ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 Momentary ";
        } else {
            text += "F2 Continuous ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 Momentary ";
        } else {
            text += "F3 Continuous ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 Momentary ";
        } else {
            text += "F4 Continuous ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 Momentary ";
        } else {
            text += "F5 Continuous ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 Momentary ";
        } else {
            text += "F6 Continuous ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 Momentary ";
        } else {
            text += "F7 Continuous ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 Momentary ";
        } else {
            text += "F8 Continuous ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 Momentary ";
        } else {
            text += "F9 Continuous ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 Momentary ";
        } else {
            text += "F10 Continuous ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 Momentary ";
        } else {
            text += "F11 Continuous ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 Momentary ";
        } else {
            text += "F12 Continuous ";
        }
        return (text);
    }

    /* Parse the Momentary sytatus of functions F13-F28.
     * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    private String parseFunctionHighMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 Momentary ";
        } else {
            text += "F13 Continuous ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 Momentary ";
        } else {
            text += "F14 Continuous ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 Momentary ";
        } else {
            text += "F15 Continuous ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 Momentary ";
        } else {
            text += "F16 Continuous ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 Momentary ";
        } else {
            text += "F17 Continuous ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 Momentary ";
        } else {
            text += "F18 Continuous ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 Momentary ";
        } else {
            text += "F19 Continuous ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 Momentary ";
        } else {
            text += "F20 Continuous ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 Momentary ";
        } else {
            text += "F21 Continuous ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 Momentary ";
        } else {
            text += "F22 Continuous ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 Momentary ";
        } else {
            text += "F23 Continuous ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 Momentary ";
        } else {
            text += "F24 Continuous ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 Momentary ";
        } else {
            text += "F25 Continuous ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 Momentary ";
        } else {
            text += "F26 Continuous ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 Momentary ";
        } else {
            text += "F27 Continuous ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 Momentary ";
        } else {
            text += "F28 Continuous ";
        }
        return (text);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(java.util.ResourceBundle.
                    getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle").
                    getString("DCCppMonFrameTitle"), DCCppMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(DCCppSystemConnectionMemo.class));
        }
    }

    static Logger log = LoggerFactory.getLogger(DCCppMonPane.class.getName());

}

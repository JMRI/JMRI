// SerialTurnout.java

package jmri.jmrix.cmri.serial;

import jmri.AbstractTurnout;
import jmri.Turnout;

/**
 * SerialTurnout.java
 *
 *  This object doesn't listen to the C/MRI communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 *  Turnouts may be controlled by one or two output bits.  If a turnout is 
 *  controlled by two output bits, the output bits must be on the same node,
 *  the address must point to the first output bit, and the second output bit
 *  must follow the output bit in the address.  Valid states for the two bits
 *  controlling the two-bit turnout are:  ON OFF, and OFF ON for the two bits.
 *
 * Description:		extend jmri.AbstractTurnout for C/MRI serial layouts
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.7 $
 */
public class SerialTurnout extends AbstractTurnout {

    /**
     * Create a Turnout object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialTurnoutManager
     */
    public SerialTurnout(String systemName, String userName) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName);
    }

    /**
     * Handle a request to change state by sending a turnout command
     */
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //				                Object oldValue,
        //						Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true);
            }
        } else {
            // send a THROWN command
            sendMessage(false);
        }
    }

    public void dispose() {}  // no connections need to be broken

    // data members
    String tSystemName; // System Name of this turnout
    int tBit;          // bit number of turnout control in Serial node

    protected void sendMessage(boolean closed) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName);
        if (tNode == null) {
            // node does not exist, ignore call
            return;
        }
		if (getNumberOutputBits() == 1) {
			tNode.setOutputBit(tBit, closed);
		} 
		else if (getNumberOutputBits() == 2) {
			tNode.setOutputBit(tBit,closed);
			tNode.setOutputBit(tBit+1,!closed);
		}
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnout.class.getName());
}

/* @(#)SerialTurnout.java */

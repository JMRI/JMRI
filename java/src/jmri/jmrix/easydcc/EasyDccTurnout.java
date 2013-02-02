// EasyDccTurnout.java

package jmri.jmrix.easydcc;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;
import jmri.NmraPacket;

/**
 * EasyDcc implementation of the Turnout interface.
 * <P>
 *  This object doesn't listen to the EasyDcc communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * Description:		extend jmri.AbstractTurnout for EasyDcc layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public class EasyDccTurnout extends AbstractTurnout {

	/**
	 * EasyDcc turnouts use the NMRA number (0-511) as their numerical identification.
	 */
	public EasyDccTurnout(int number) {
            super("ET"+number);
            _number = number;
            // At construction, register for messages
	}

	public int getNumber() { return _number; }

    // Turnouts do support inversion
    @Override
    public boolean canInvert(){return true;}

	// Handle a request to change state by sending a formatted DCC packet
	protected void forwardCommandChangeToLayout(int s) {
		// sort out states
		if ( (s & Turnout.CLOSED) > 0) {
			// first look for the double case, which we can't handle
			if ( (s & Turnout.THROWN) > 0) {
				// this is the disaster case!
				log.error("Cannot command both CLOSED and THROWN "+s);
				return;
			} else {
				// send a CLOSED command
				sendMessage(true^getInverted());
			}
		} else {
			// send a THROWN command
			sendMessage(false^getInverted());
		}
	}
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout){
		if (log.isDebugEnabled()) log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock")+ " Pushbutton ET"+_number);
    }

	// data members
	int _number;   // turnout number

	protected void sendMessage(boolean closed) {
		// get the packet
		byte[] bl = NmraPacket.accDecoderPkt(_number, closed);
		if (log.isDebugEnabled()) log.debug("packet: "
											+Integer.toHexString(0xFF & bl[0])
											+" "+Integer.toHexString(0xFF & bl[1])
											+" "+Integer.toHexString(0xFF & bl[2]));

		EasyDccMessage m = new EasyDccMessage(13);
		int i = 0; // counter to make it easier to format the message
		m.setElement(i++, 'S');  // "S02 " means send it twice
		m.setElement(i++, ' ');
		m.setElement(i++, '0');
		m.setElement(i++, '2');
		m.setElement(i++, ' ');
		String s = Integer.toHexString(bl[0]&0xFF).toUpperCase();
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		s = Integer.toHexString(bl[1]&0xFF).toUpperCase();
		m.setElement(i++, ' ');
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		s = Integer.toHexString(bl[2]&0xFF).toUpperCase();
		m.setElement(i++, ' ');
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}

		EasyDccTrafficController.instance().sendEasyDccMessage(m, null);

	}

	static Logger log = Logger.getLogger(EasyDccTurnout.class.getName());

}


/* @(#)EasyDccTurnout.java */

// SprogTurnout.java

package jmri.jmrix.sprog;

import jmri.AbstractTurnout;
import jmri.Turnout;
import jmri.NmraPacket;

/**
 * Sprog implementation of the Turnout interface.
 * <P>
 *  This object doesn't listen to the Sprog communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * Description:		extend jmri.AbstractTurnout for Sprog layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class SprogTurnout extends AbstractTurnout {

    /**
     * Sprog turnouts use the NMRA number (0-511) as their numerical identification.
     */
    public SprogTurnout(int number) {
        super("ST"+number);
        _number = number;
    }

	public int getNumber() { return _number; }

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
				sendMessage(true);
			}
		} else {
			// send a THROWN command
			sendMessage(false);
		}
	}

	public void dispose() {}  // no connections need to be broken

	// data members
	int _number;   // turnout number

	protected void sendMessage(boolean closed) {
		// The address space in the packet starts with zero, not one

		// dBit is the "channel" info, least 7 bits, for the packet
		// The lowest channel bit represents CLOSED (1) and THROWN (0)
		int dBits = (( (_number-1) & 0x03) << 1 );  // without the low CLOSED vs THROWN bit
		dBits = closed ? (dBits | 1) : dBits;

		// aBits is the "address" part of the nmra packet, which starts with 1
		int aBits = (( (_number-1) & 0x1FC) >> 2 )+1;

		// cBit is the control bit, we're always setting it active
		int cBit = 1;

		// get the packet
		if (log.isDebugEnabled()) log.debug("build packet from (addr, control, channel): "+aBits+" "+cBit+" "+dBits);
		byte[] bl = NmraPacket.accDecoderPkt(aBits, cBit, dBits);
		if (log.isDebugEnabled()) log.debug("packet: "
											+Integer.toHexString(0xFF & bl[0])
											+" "+Integer.toHexString(0xFF & bl[1])
											+" "+Integer.toHexString(0xFF & bl[2]));

		SprogMessage m = new SprogMessage(9);
		int i = 0; // counter to make it easier to format the message
		m.setElement(i++, 'S');  // "S02 " means send it twice
		m.setElement(i++, '0');
		m.setElement(i++, '2');
		String s = Integer.toHexString((int)bl[0]&0xFF).toUpperCase();
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		s = Integer.toHexString((int)bl[1]&0xFF).toUpperCase();
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		s = Integer.toHexString((int)bl[2]&0xFF).toUpperCase();
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}

		SprogTrafficController.instance().sendSprogMessage(m, null);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTurnout.class.getName());

}


/* @(#)SprogTurnout.java */

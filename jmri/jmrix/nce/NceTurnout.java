/**
 * NceTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for NCE layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */

/**
 *  This object doesn't listen to the NCE communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 */

package jmri.jmrix.nce;

import jmri.AbstractTurnout;
import jmri.Turnout;
import jmri.NmraPacket;

public class NceTurnout extends AbstractTurnout {

	/**
	 * NCE turnouts use the NMRA number (0-511) as their numerical identification.
	 */

	public NceTurnout(int number) {
		_number = number;
		// At construction, register for messages
	}

	public int getNumber() { return _number; }
	public String getSystemName() { return "NT"+getNumber(); }

	// Handle a request to change state by sending a turnout command
	protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException {
		// implementing classes will typically have a function/listener to get
		// updates from the layout, which will then call
		//		public void firePropertyChange(String propertyName,
		//										Object oldValue,
		//										Object newValue)
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

		NceMessage m = new NceMessage(14);
		int i = 0; // counter to make it easier to format the message
		m.setElement(i++, 'S');  // "S C02 " means sent it twice
		m.setElement(i++, ' ');
		m.setElement(i++, 'C');
		m.setElement(i++, '0');
		m.setElement(i++, '2');
		m.setElement(i++, ' ');
		String s = Integer.toHexString((int)bl[0]&0xFF);
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		m.setElement(i++, ' ');
		s = Integer.toHexString((int)bl[1]&0xFF);
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}
		m.setElement(i++, ' ');
		s = Integer.toHexString((int)bl[2]&0xFF);
		if (s.length() == 1) {
			m.setElement(i++, '0');
			m.setElement(i++, s.charAt(0));
		} else {
			m.setElement(i++, s.charAt(0));
			m.setElement(i++, s.charAt(1));
		}

		NceTrafficController.instance().sendNceMessage(m, null);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnout.class.getName());

}


/* @(#)NceTurnout.java */

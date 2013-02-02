// SRCPCommandStation.java

package jmri.jmrix.srcp;

import org.apache.log4j.Logger;
import jmri.CommandStation;

/**
 * SRCP implementation of the CommandStation interface.
 *
 * @author			Bob Jacobsen Copyright (C) 2007, 2008
 * @version			$Revision$
 */
public class SRCPCommandStation implements CommandStation {

	public SRCPCommandStation(SRCPSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats Number of times to repeat the transmission,
     * capped at 9
     */
    public void sendPacket(byte[] packet, int repeats) {

        if (repeats>9) repeats = 9;
        if (repeats<0) {
            log.error("repeat count out of range: "+repeats);
            repeats = 1;
        }
        
		SRCPMessage m = new SRCPMessage(4+3*packet.length);
		int i = 0; // counter to make it easier to format the message
		m.setElement(i++, 'S');  // "S 02 " means send it twice
		m.setElement(i++, ' ');
		m.setElement(i++, '0');
		m.setElement(i++, '0'+repeats);
		
		for (int j=0; j<packet.length; j++) {
		    m.setElement(i++, ' ');
            String s = Integer.toHexString(packet[j]&0xFF).toUpperCase();
            if (s.length() == 1) {
                m.setElement(i++, '0');
                m.setElement(i++, s.charAt(0));
            } else {
                m.setElement(i++, s.charAt(0));
                m.setElement(i++, s.charAt(1));
            }
        }

		SRCPTrafficController.instance().sendSRCPMessage(m, null);

	}
    
    SRCPSystemConnectionMemo adaptermemo;
   
    public String getUserName() { 
        if(adaptermemo==null) return "SRCP";
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "D";
        return adaptermemo.getSystemPrefix();
    }

	static Logger log = Logger.getLogger(SRCPCommandStation.class.getName());

}


/* @(#)SRCPCommandStation.java */

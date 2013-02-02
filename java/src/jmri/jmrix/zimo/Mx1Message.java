// Mx1Message.java

package jmri.jmrix.zimo;

import org.apache.log4j.Logger;
import java.io.Serializable;

/**
 * Represents a single command or response on the XpressNet.
 *<P>
 * Content is represented with ints to avoid the problems with
 * sign-extension that bytes have, and because a Java char is
 * actually a variable number of bytes in Unicode.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version			$Revision$
 *
 * Adapted by Sip Bosch for use with zimo MX-1
 *
 */
public class Mx1Message extends jmri.jmrix.NetMessage implements Serializable {

	/** Create a new object, representing a specific-length message.
	 * @param len Total bytes in message, including opcode and error-detection byte.
	 */
	public Mx1Message(int len) {
        super(len);
        if (len>15||len<0) log.error("Invalid length in ctor: "+len);
	}

        /**
	 * check whether the message has a valid parity
         * in fact check for CR or LF as end of message
	 */
	public boolean checkParity() {
          //javax.swing.JOptionPane.showMessageDialog(null, "A-Programma komt tot hier!");
		int len = getNumDataElements();
		return (getElement(len-1) == (0x0D | 0x0A));
                }
// programma komt hier volgens mij nooit
        // in fact set CR as end of message
        public void setParity() {
          javax.swing.JOptionPane.showMessageDialog(null, "B-Programma komt tot hier!");
                int len = getNumDataElements();
	        setElement(len-1, 0x0D);
                }

    // decode messages of a particular form

    // create messages of a particular form

	// initialize logging
    static Logger log = Logger.getLogger(Mx1Message.class.getName());

}

/* @(#)Mx1Message.java */

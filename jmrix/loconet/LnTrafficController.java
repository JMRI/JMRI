/** 
 * LnTrafficController.java
 *
 * Description:		Converts Stream-based I/O to LocoNet messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package LocoNet;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import ErrLoggerJ.ErrLog;

public class LnTrafficController implements LocoNetInterface, Runnable {


// The methods to implement the LocoNetInterface

	public boolean status() { return (ostream != null & istream != null); 
		}

	public void notifyLocoNetEvents(int mask, LocoNetListener l) { //!! only one!
		oneListener = l;
		}

	public void endLocoNetEvents(int mask, LocoNetListener l) {  //!! nothing yet
		}

	public void sendLocoNetMessage(LocoNetMessage m) {
		// set the error correcting code byte
		int len = m.getNumDataElements();
		int chksum = 0xff;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= m.getElement(loop);
        }
		m.setElement(len-1, chksum);  // checksum is last element of message
		
		// stream to port in single write, as that's needed by serial
		byte msg[] = new byte[len];
		for (int i=0; i< len; i++)
			msg[i] = (byte) m.getElement(i);
		try {
			ostream.write(msg);
			}
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnTrafficController", "", "Exception: "+e.toString());
			}
		}

// methods to connect to a source of data in a LnPortController
	public void connectToPort(LnPortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			// ErrLog.msg(ErrLog.routine,"LnTrafficController","connectToPort","went OK");
		}
			
// the methods to connect to the streams from a port
	void setInputStream(InputStream s) { 
			istream = new DataInputStream(s);
		}
		
	void setOutputStream(OutputStream o) { 
			ostream = o;
		}
		
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

// data members to hold contact with the listeners
	LocoNetListener oneListener = null;  // no protection, no provision for more than one!!
	private void dispatch(LocoNetMessage m) {
		oneListener.message(m);
		}
	
// main running member function
	public void run() {
			int opCode;
			try {
			 while (true /* istream.available() > 0 */ ) {   // loop permanently, not right!!
				// ErrLog.msg(ErrLog.debugging,"LnTrafficController","run","looking at data");
				// start by looking for command
				while ( ((opCode = (istream.readByte()&0xFF)) & 0x80) ==0 )  {};  // skip if bit not set
				// here opCode is OK. Create output message
				LocoNetMessage msg = null;
				// Capture 2nd byte, always present
				int byte2 = istream.readByte()&0xFF;
				// Decide length
				switch((opCode & 0x60) >> 5)
                    {
                        case 0:     /* 2 byte message */
                            msg = new LocoNetMessage(2);
                            break;

                        case 1:     /* 4 byte message */
                            msg = new LocoNetMessage(4);
                            break;

                        case 2:     /* 6 byte message */
                            msg = new LocoNetMessage(4);
                            break;

                        case 3:     /* N byte message */
                            msg = new LocoNetMessage(byte2);
                            break;
                    }
             	// message exists, now fill it
             	msg.setOpCode(opCode);
             	msg.setElement(1, byte2);
             	int len = msg.getNumDataElements();
             	for (int i = 2; i < len; i++) msg.setElement(i, istream.readByte()&0xFF);
             	// confirm you've got the message right...
             	// ErrLog.msg(ErrLog.debugging,"LnTrafficController", "dispatch msg:", msg.toString());
             	
             	// message is complete, dispatch it !!
             	dispatch(msg);
             	
             	// done with this one
            	}  // end loop until no data available
			} // end of try
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnTrafficController", "", "Exception: "+e.toString());
			}
		}
}


/* @(#)LnTrafficController.java */


/** 
 * LnTrafficController.java
 *
 * Description:		Converts Stream-based I/O to LocoNet messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package loconet;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import ErrLoggerJ.ErrLog;

public class LnTrafficController implements LocoNetInterface, Runnable {

	public LnTrafficController() {self=this;}
	

// The methods to implement the LocoNetInterface

	private Vector listeners = new Vector();
	
	public boolean status() { return (ostream != null & istream != null); 
		}

	public synchronized void addLocoNetListener(int mask, LocoNetListener l) { 
			// add only if not already registered
			if (l == null) throw new java.lang.NullPointerException();
			if (!listeners.contains(l)) {
					listeners.addElement(l);
				}
		}

	public synchronized void removeLocoNetListener(int mask, LocoNetListener l) {
			if (listeners.contains(l)) {
					listeners.removeElement(l);
				}
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
			if (ostream != null)
				ostream.write(msg);
			else {
				// no stream connected
				ErrLog.msg(ErrLog.warning, "LnTrafficController", "sendLocoNetMessage", "no connection established");
				}
			}
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnTrafficController", "sendLocoNetMessage", "Exception: "+e.toString());
			}
		}

// methods to connect/disconnect to a source of data in a LnPortController
	private LnPortController controller = null;
	
	public void connectPort(LnPortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			if (controller != null)
				ErrLog.msg(ErrLog.error,"LnTrafficController", "connectPort", 
							"connect called while connected");
			controller = p;
		}
	public void disconnectPort(LnPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				ErrLog.msg(ErrLog.error,"LnTrafficController", "disconnectPort", 
							"disconnect called from non-connected LnPortController");
			controller = null;
		}
		
// the methods to connect to the streams from a port
	//void setInputStream(InputStream s) { 
	//		istream = new DataInputStream(s);
	//	}
		
	//void setOutputStream(OutputStream o) { 
	//		ostream = o;
	//	}
		
// static function to find object
	static public LnTrafficController instance() { return self;}
	static private LnTrafficController self = null;
	
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

// data members to hold contact with the listeners
	protected void notify(LocoNetMessage m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) listeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			LocoNetListener client = (LocoNetListener) listeners.elementAt(i);
			try {
				client.message(m);
				}
			catch (Exception e)
				{
					ErrLog.msg(ErrLog.error,"LnTrafficController", 
								"notify", "During dispatch to "+client+"\nException "+e);
				}
			}
	}
	
// main running member function
	public void run() {
			int opCode;
			try {
			 while (true) {   // loop permanently, stream close will exit
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
             	notify(msg);
             	
             	// done with this one
            	}  // end loop until no data available
            // at this point, input stream is not available
            // so we just fall off end to stop running
            
			} // end of try
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnTrafficController", "run", "Exception: "+e.toString());
			}
		}
}


/* @(#)LnTrafficController.java */


/** 
 * LnTrafficController.java
 *
 * Description:		Converts Stream-based I/O to/from LocoNet messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/** 
 * Converts Stream-based I/O to/from LocoNet messages.  The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects.  The connection to 
 * a LnPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is 
 * handled in an independent thread.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 */
public class LnTrafficController implements LocoNetInterface, Runnable {

	public LnTrafficController() {self=this;}
	

// The methods to implement the LocoNetInterface

	protected Vector listeners = new Vector();
	
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

	/**
	 * Forward a preformatted LocoNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here.
	 */
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
		if (log.isDebugEnabled()) log.debug("send LocoNet packet: "+m.toString());
		try {
			if (ostream != null)
				ostream.write(msg);
			else {
				// no stream connected
				log.warn("sendLocoNetMessage: no connection established");
				}
			}
		catch (Exception e) {
			log.warn("sendLocoNetMessage: Exception: "+e.toString());
			}
		}

// methods to connect/disconnect to a source of data in a LnPortController
	private LnPortController controller = null;
	
	/**
	 * Make connection to existing LnPortController object.
	 */
	public void connectPort(LnPortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			if (controller != null)
				log.warn("connectPort: connect called while connected");
			controller = p;
		}
		
	/**
	 * Break connection to existing LnPortController object. Once broken,
	 * attempts to send via "message" member will fail.
	 */
	public void disconnectPort(LnPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			controller = null;
		}
				
	/**
	 * static function returning the LnTrafficController instance to use.
	 * @return The registered LnTrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public LnTrafficController instance() { 
		if (self == null) self = new LnTrafficController();
		return self;
	}
	
	static protected LnTrafficController self = null;
	
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

	/**
	 * Forward a LocoNetMessage to all registered listeners.
	 */
	protected void notify(LocoNetMessage m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) listeners.clone();
			}
		if (log.isDebugEnabled()) log.debug("notify of incoming LocoNet packet: "+m.toString());
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			LocoNetListener client = (LocoNetListener) listeners.elementAt(i);
			client.message(m);
		}
	}
	
	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the 
	 * stream connected to the LnPortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
		int opCode;
		while (true) {   // loop permanently, program close will exit
			try {
				// start by looking for command -  skip if bit not set
				while ( ((opCode = (istream.readByte()&0xFF)) & 0x80) == 0 )  {
					//log.debug("Skipping: "+Integer.toHexString(opCode));
				} 
				// here opCode is OK. Create output message
				// log.debug("Start message with opcode: "+Integer.toHexString(opCode));
				LocoNetMessage msg = null;
				while (msg == null) {
					try {
						// Capture 2nd byte, always present
						int byte2 = istream.readByte()&0xFF;
						//log.debug("Byte2: "+Integer.toHexString(byte2));
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
    	                        	msg = new LocoNetMessage(6);
         		                   	break;
	
    	                    	case 3:     /* N byte message */
    	                    		if (byte2<2) log.error("LocoNet message length invalid: "+byte2
    	                    						+" opcode: "+Integer.toHexString(opCode));
        	                    	msg = new LocoNetMessage(byte2);
            	                	break;
                	    	}
             			// message exists, now fill it
             			msg.setOpCode(opCode);
             			msg.setElement(1, byte2);
             			int len = msg.getNumDataElements();
						//log.debug("len: "+len);
             			for (int i = 2; i < len; i++)  {
             				// check for message-blocking error
             				int b = istream.readByte()&0xFF;
 							//log.debug("char "+i+" is: "+Integer.toHexString(b));
            				if ( (b&0x80) != 0) {
             					log.warn("LocoNet message with opCode: "
             							+Integer.toHexString(opCode)
             							+" ended early. Expected length: "+len
             							+" seen length: "+i
             							+" unexpected byte: "
             							+Integer.toHexString(b));
             					opCode = b;
             					throw new LocoNetMessageException();
             				}
             				msg.setElement(i, b);
             			}
 					}
 					catch (LocoNetMessageException e) {
 						// retry by destroying the existing message
 						// opCode is set for the newly-started packet
 						msg = null;
 					}       
 				}     	
				// check parity
				if (!msg.checkParity()) {
					log.warn("Ignore Loconet packet with bad checksum: "+msg.toString());
					throw new LocoNetMessageException();
				}		
             	// message is complete, dispatch it !!
             	{ 
             		final LocoNetMessage thisMsg = msg;
             		final LnTrafficController thisTC = this;
 					// return a notification via the queue to ensure end
					Runnable r = new Runnable() {
						LocoNetMessage msgForLater = thisMsg;
						LnTrafficController myTC = thisTC;
						public void run() { 
           					myTC.notify(msgForLater);
						}
					};
					javax.swing.SwingUtilities.invokeLater(r);
				}
              	
             	// done with this one
            }
 			catch (LocoNetMessageException e) {
				// just let it ride for now
			}
 			catch (java.io.EOFException e) {
				// posted from idle port when enableReceiveTimeout used
				log.debug("EOFException, is LocoNet serial I/O using timeouts?");
			}
 			catch (java.io.IOException e) {
				// fired when write-end of HexFile reaches end
				log.debug("IOException, should only happen with HexFIle: "+e);
				log.info("End of file");
				disconnectPort(controller);
				return;
			}
 			catch (Exception e) {
				log.warn("run: unexpected exception: "+e);
			}
		} // end of permanent loop
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTrafficController.class.getName());
}


/* @(#)LnTrafficController.java */


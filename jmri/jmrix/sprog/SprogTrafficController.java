// SprogTrafficController.java

package jmri.jmrix.sprog;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from Sprog messages.  The "SprogInterface"
 * side sends/receives message objects.  The connection to
 * a SprogPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.5 $
 */
public class SprogTrafficController implements SprogInterface, Runnable {

	public SprogTrafficController() {
		if (log.isDebugEnabled()) log.debug("setting instance: "+this);
		self=this;
	}


// The methods to implement the SprogInterface

	protected Vector cmdListeners = new Vector();

	public boolean status() { return (ostream != null & istream != null);
		}

	public synchronized void addSprogListener(SprogListener l) {
			// add only if not already registered
			if (l == null) throw new java.lang.NullPointerException();
			if (!cmdListeners.contains(l)) {
					cmdListeners.addElement(l);
				}
		}

	public synchronized void removeSprogListener(SprogListener l) {
			if (cmdListeners.contains(l)) {
					cmdListeners.removeElement(l);
				}
		}


	/**
	 * Forward a SprogMessage to all registered SprogInterface listeners.
	 */
	protected void notifyMessage(SprogMessage m, SprogListener notMe) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) cmdListeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			SprogListener client = (SprogListener) v.elementAt(i);
			if (notMe != client) {
				if (log.isDebugEnabled()) log.debug("notify client: "+client);
				try {
					client.message(m);
					}
				catch (Exception e)
					{
						log.warn("notify: During dispatch to "+client+"\nException "+e);
					}
				}
			}
	}

	SprogListener lastSender = null;

	protected void notifyReply(SprogReply r) {
          // make a copy of the listener vector to synchronized (not needed for transmit?)
          Vector v;
          synchronized(this)
          {
            v = (Vector) cmdListeners.clone();
          }
          // forward to all listeners
          int cnt = v.size();
          for (int i=0; i < cnt; i++) {
            SprogListener client = (SprogListener) v.elementAt(i);
            if (log.isDebugEnabled()) log.debug("notify client: "+client);
            try {
              // skip forwarding to the last sender for now, we'll get them later
              if (lastSender != client)
                client.reply(r);
            }
            catch (Exception e)
            {
              log.warn("notify: During dispatch to "+client+"\nException "+e);
            }
          }

          // forward to the last listener who send a message
          // this is done _second_ so monitoring can have already stored the reply
          // before a response is sent
          if (lastSender != null) lastSender.reply(r);
        }


	/**
	 * Forward a preformatted message to the actual interface.
	 */
        public void sendSprogMessage(SprogMessage m, SprogListener reply) {
          if (log.isDebugEnabled()) log.debug("sendSprogMessage message: ["+m+"]");
          // remember who sent this
          lastSender = reply;

          // notify all _other_ listeners
          notifyMessage(m, reply);

		// stream to port in single write, as that's needed by serial
          int len = m.getNumDataElements();
          int cr = 1;  // space for carriage return linefeed

          byte msg[] = new byte[len+cr];

          for (int i=0; i< len; i++)
            msg[i] = (byte) m.getElement(i);
          msg[len] = 0x0d;

          try {
            if (ostream != null) {
              if (log.isDebugEnabled()) log.debug("write message: "+msg);
              ostream.write(msg);
            }
            else {
              // no stream connected
              log.warn("sendMessage: no connection established");
            }
			}
                        catch (Exception e) {
                          log.warn("sendMessage: Exception: "+e.toString());
                        }
        }

        // methods to connect/disconnect to a source of data in a LnPortController
	private SprogPortController controller = null;

	/**
	 * Make connection to existing PortController object.
	 */
	public void connectPort(SprogPortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			if (controller != null)
				log.warn("connectPort: connect called while connected");
			controller = p;
		}

	/**
	 * Break connection to existing SprogPortController object. Once broken,
	 * attempts to send via "message" member will fail.
	 */
	public void disconnectPort(SprogPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			controller = null;
		}

	/**
	 * static function returning the SprogTrafficController instance to use.
	 * @return The registered SprogTrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public SprogTrafficController instance() {
		if (self == null) {
			if (log.isDebugEnabled()) log.debug("creating a new SprogTrafficController object");
			self = new SprogTrafficController();
		}
		return self;
	}

	static protected SprogTrafficController self = null;

        // data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;


	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the
	 * stream connected to the PortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
          while (true) {   // loop permanently, stream close will exit via exception
            try {
              handleOneIncomingReply();
            }
            catch (java.io.IOException e) {
              log.warn("run: Exception: "+e.toString());
            }
          }
        }

        void handleOneIncomingReply() throws java.io.IOException {
          // we sit in this until the message is complete, relying on
          // threading to let other stuff happen

          // Create output message
          SprogReply msg = new SprogReply();
          // message exists, now fill it
          int i;
          for (i = 0; i < SprogReply.maxSize; i++) {
            byte char1 = istream.readByte();
            msg.setElement(i, char1);
            if (endReply(msg)) break;
          }

          // message is complete, dispatch it !!
          if (log.isDebugEnabled()) log.debug("dispatch reply of length "+i);
          {
            final SprogReply thisMsg = msg;
            final SprogTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
              SprogReply msgForLater = thisMsg;
              SprogTrafficController myTC = thisTC;
              public void run() {
                log.debug("Delayed notify starts");
                myTC.notifyReply(msgForLater);
              }
            };
            javax.swing.SwingUtilities.invokeLater(r);
          }
        }

        /*
         * SPROG replies will end with the prompt for the next command
        */
        boolean endReply(SprogReply msg) {
          // detect that the reply buffer ends with "P> " or "R> " (note ending space)
          int num = msg.getNumDataElements();
          if ( num >= 3) {
            // ptr is offset of last element in NceReply
            int ptr = num-1;
            if (msg.getElement(ptr)   != ' ') return false;
            if (msg.getElement(ptr-1) != '>') return false;
            if ((msg.getElement(ptr-2) != 'P')&&(msg.getElement(ptr-2) != 'R')) return false;
            return true;
          }
          else return false;
        }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTrafficController.class.getName());
}


/* @(#)SprogTrafficController.java */


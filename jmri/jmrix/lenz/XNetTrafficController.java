// XNetTrafficController.java

package jmri.jmrix.lenz;

import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRListener;

/**
 * Abstract base class for implementations of XNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version 		$Revision: 2.1 $
 *
 */
public abstract class XNetTrafficController extends AbstractMRTrafficController implements XNetInterface {

    /**
	 * static function returning the TrafficController instance to use.
	 * @return The registered TrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public XNetTrafficController instance() {
		return self;
	}

    /**
	 * static function setting this object as the TrafficController 
         * instance to use.
	 */
	protected void setInstance() {
		if(self==null) self=this;
	}

	static protected XNetTrafficController self = null;

    /**
     * Must provide a LenzCommandStation reference at creation time
     * @param pCommandStation reference to associated command station object,
     *          preserved for later.
     */
    XNetTrafficController(LenzCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
    }

    // Abstract methods for the XNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted XNetMessage to the actual interface.
     * @param m Message to send; will be updated with CRC
     */
   abstract public void sendXNetMessage(XNetMessage m, XNetListener reply);

    /**
     * Forward a preformatted XNetMessage to the actual interface.
     * @param m Message to send; will be updated with CRC
     */
   public void forwardMessage(AbstractMRListener reply,AbstractMRMessage m){
          ((XNetListener)reply).message(new XNetReply((XNetMessage)m));
   }

   public synchronized void addXNetListener(int mask, XNetListener l) {
	addListener(l);
    }

    public synchronized void removeXNetListener(int mask, XNetListener l) {
	removeListener(l);
    }

	/**
	 * Forward a message to all registered listeners.
     * @param m Message to forward. Listeners should not modify it!
     * @param replyTo Listener for the reply to this message, doesn't get
     *                the echo of it.
	 */
	protected void notify(XNetMessage m, XNetListener replyTo) {
		notifyMessage((AbstractMRMessage)m,(AbstractMRListener) replyTo);
        }

	protected void notify(XNetReply m, XNetListener replyTo) {
		notifyMessage(new XNetMessage(m),(AbstractMRListener)replyTo);
        }

    /** Reference to the command station in communication here */
    LenzCommandStation mCommandStation;

    /**
     * Get access to communicating command station object
     * @return associated Command Station object
     */
    public LenzCommandStation getCommandStation() { return mCommandStation; }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTrafficController.class.getName());
}


/* @(#)XNetTrafficController.java */


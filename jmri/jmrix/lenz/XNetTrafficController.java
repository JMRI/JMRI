// XNetTrafficController.java

package jmri.jmrix.lenz;

import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRListener;

import com.sun.java.util.collections.Hashtable;

/**
 * Abstract base class for implementations of XNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @author			Paul Bender  Copyright (C) 2004,2005
 * @version 		$Revision: 2.7 $
 *
 */
public abstract class XNetTrafficController extends AbstractMRTrafficController implements XNetInterface {

    protected Hashtable mListenerMasks;

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
	setAllowUnexpectedReply(true);
	mListenerMasks = new Hashtable();
    }

    // Abstract methods for the XNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted XNetMessage to the actual interface.
     * @param m Message to send; will be updated with CRC
     */
   abstract public void sendXNetMessage(XNetMessage m, XNetListener reply);

    /**
     * Forward a preformatted XNetMessage to a specific listener interface.
     * @param m Message to send; 
     */
   public void forwardMessage(AbstractMRListener reply,AbstractMRMessage m){
          ((XNetListener)reply).message((XNetMessage)m);
   }

        /**
         * Forward a preformatted XNetMessage to the registered
         * XNetListeners.
         * NOTE: this drops the packet if the checksum is bad.
         * 
         * @param m Message to send
         # @parm client is the client getting the message
         */
        public void forwardReply(AbstractMRListener client,AbstractMRReply m) {
	 	// check parity
                if (!((XNetReply)m).checkParity()) {
                    log.warn("Ignore packet with bad checksum: "+((XNetReply)m).toString());
		} else {
		   int mask = ((Integer)mListenerMasks.get((XNetListener)client)).intValue();
		   if(mask==XNetInterface.ALL) {
		   	((XNetListener)client).message((XNetReply)m);		   
		   } else if ((mask&XNetInterface.COMMINFO)==
					XNetInterface.COMMINFO &&
			(((XNetReply)m).getElement(0)== 
				XNetConstants.LI_MESSAGE_RESPONSE_HEADER)) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   } else if ((mask&XNetInterface.CS_INFO)==
					XNetInterface.CS_INFO &&
			      (((XNetReply)m).getElement(0)== 
						XNetConstants.CS_INFO ||
			       ((XNetReply)m).getElement(0)== 
						XNetConstants.CS_SERVICE_MODE_RESPONSE ||
			       ((XNetReply)m).getElement(0)== 
					XNetConstants.CS_REQUEST_RESPONSE ||
                               ((XNetReply)m).getElement(0)== 
					XNetConstants.BC_EMERGENCY_STOP )) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   } else if ((mask&XNetInterface.FEEDBACK)==
					XNetInterface.FEEDBACK &&
			      (((XNetReply)m).isFeedbackMessage() || 
		   	       ((XNetReply)m).isFeedbackBroadcastMessage())) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   }else if ((mask&XNetInterface.THROTTLE)==
					XNetInterface.THROTTLE &&
			     ((XNetReply)m).isThrottleMessage()) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   }else if ((mask&XNetInterface.CONSIST)==
					XNetInterface.CONSIST &&
			     ((XNetReply)m).isConsistMessage()) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   }else if ((mask&XNetInterface.INTERFACE)==
					XNetInterface.INTERFACE &&
			     (((XNetReply)m).getElement(0)==
					XNetConstants.LI_VERSION_RESPONSE ||
			      ((XNetReply)m).getElement(0)==
					XNetConstants.LI101_REQUEST)) {
  		   	   ((XNetListener)client).message((XNetReply)m);
		   } 
		}
        }

 	protected AbstractMRMessage pollMessage() { return null; }
    	protected AbstractMRListener pollReplyHandler() { return null; }

   public synchronized void addXNetListener(int mask, XNetListener l) {
	addListener(l);
        // This is adds all the mask information.  A better way to do
        // this would be to allow updating individual bits
	mListenerMasks.put(l,new Integer(mask));
    }

    public synchronized void removeXNetListener(int mask, XNetListener l) {
	removeListener(l);
	// This is removes all the mask information.  A better way to do 
	// this would be to allow updating of individual bits
	mListenerMasks.remove(l);
    }

    /**
      * enterProgMode(); has to be available, even though it doesn't do 
      * anything on lenz
      */
    protected AbstractMRMessage enterProgMode() { return null; }

    /**
      * enterNormalMode() returns the value of getExitProgModeMsg();
      */
    protected AbstractMRMessage enterNormalMode() { 
		return XNetMessage.getExitProgModeMsg();
	}

    /**
      * enterNormalMode() returns the value of getExitProgModeMsg();
      */
    protected boolean programmerIdle() { 
	  return !(XNetProgrammer.instance().programmerBusy());
	}

    protected boolean endOfMessage(AbstractMRReply msg) { 
           int len = (((XNetReply)msg).getElement(0)&0x0f)+2;  // opCode+Nbytes+ECC
           log.debug("Message Length " +len +" Current Size " +msg.getNumDataElements());
           if(msg.getNumDataElements()<len)
		return false;                                 
	   else {
		return true; 
	   }
    }

    protected AbstractMRReply newReply() { return new XNetReply(); }


    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread. 
     *
     * @param msg message to fill
     * @param istream character source.  
     * @throws IOException when presented by the input source.
     */
    protected void loadChars(AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize; i++) { 
            byte char1 = istream.readByte();
            msg.setElement(i, char1 &0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
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


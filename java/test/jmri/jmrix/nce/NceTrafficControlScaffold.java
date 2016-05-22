// NceInterfaceScaffold.java

package jmri.jmrix.nce;

import org.apache.log4j.Logger;
import java.util.Vector;

/** 
 * Stands in for the NceTrafficController class
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class NceTrafficControlScaffold extends NceTrafficController {
	public NceTrafficControlScaffold() {
		if (log.isDebugEnabled()) log.debug("setting instance: "+this);
		self = this;
	}

	// override some NceTrafficController methods for test purposes
	
	public boolean status() { 
		return true; 
	}

	/**
	 * record messages sent, provide access for making sure they are OK
	 */
	public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class
	public void sendNceMessage(NceMessage m, NceListener reply) {
		if (log.isDebugEnabled()) log.debug("sendNceMessage ["+m+"]");
		// save a copy
		outbound.addElement(m);
		// we don't return an echo so that the processing before the echo can be
		// separately tested
	}

	// test control member functions
	
	/** 
	 * forward a message to the listeners, e.g. test receipt
	 */
	protected void sendTestMessage(NceMessage m, NceListener l) {
		// forward a test message to NceListeners
		if (log.isDebugEnabled()) log.debug("sendTestMessage    ["+m+"]");
		notifyMessage(m, l);
		return;
	}
	
	/*
	* Check number of listeners, used for testing dispose()
	*/
	
	public int numListeners() {
		return cmdListeners.size();
	}
	
	static Logger log = Logger.getLogger(NceTrafficControlScaffold.class.getName());

}

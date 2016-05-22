// EasyDccListenerScaffold.java

package jmri.jmrix.easydcc;

import org.apache.log4j.Logger;

/** 
 * Stands in for the EasyDccTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */
class EasyDccListenerScaffold implements EasyDccListener {
	public EasyDccListenerScaffold() {
		rcvdReply = null;
		rcvdMsg = null;
	}
	public void message(EasyDccMessage m) {rcvdMsg = m;}
	public void reply(EasyDccReply r) {rcvdReply = r;}

	EasyDccReply rcvdReply;
	EasyDccMessage rcvdMsg;
	
	static Logger log = Logger.getLogger(EasyDccListenerScaffold.class.getName());

}

// EasyDccListenerScaffold.java

package jmri.jmrix.easydcc;

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
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EasyDccListenerScaffold.class.getName());

}

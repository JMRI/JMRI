/** 
 * NceInterfaceScaffold.java
 *
 * Description:	    Stands in for the NceTrafficController class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.3 $
 */

package jmri.jmrix.nce;

import jmri.*;

class NceListenerScaffold implements jmri.jmrix.nce.NceListener {
	public NceListenerScaffold() {
		rcvdReply = null;
		rcvdMsg = null;
	}
	public void message(NceMessage m) {rcvdMsg = m;}
	public void reply(NceReply r) {rcvdReply = r;}

	NceReply rcvdReply;
	NceMessage rcvdMsg;
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceListenerScaffold.class.getName());

}

/** 
 * NceInterfaceScaffold.java
 *
 * Description:	    Stands in for the NceTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NcePortController;

class NceInterfaceScaffold implements jmri.jmrix.nce.NceListener {
	public NceInterfaceScaffold() {
		rcvdReply = null;
		rcvdMsg = null;
	}
	public void message(NceMessage m) {rcvdMsg = m;}
	public void reply(NceReply r) {rcvdReply = r;}

	NceReply rcvdReply;
	NceMessage rcvdMsg;
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceInterfaceScaffold.class.getName());

}

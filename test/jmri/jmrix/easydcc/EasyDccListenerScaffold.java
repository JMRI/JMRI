/** 
 * EasyDccInterfaceScaffold.java
 *
 * Description:	    Stands in for the EasyDccTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.easydcc;

import jmri.*;

class EasyDccInterfaceScaffold implements EasyDccListener {
	public EasyDccInterfaceScaffold() {
		rcvdReply = null;
		rcvdMsg = null;
	}
	public void message(EasyDccMessage m) {rcvdMsg = m;}
	public void reply(EasyDccReply r) {rcvdReply = r;}

	EasyDccReply rcvdReply;
	EasyDccMessage rcvdMsg;
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccInterfaceScaffold.class.getName());

}

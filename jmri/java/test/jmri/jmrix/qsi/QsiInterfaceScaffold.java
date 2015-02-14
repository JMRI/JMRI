/** 
 * QsiInterfaceScaffold.java
 *
 * Description:	    Stands in for the QsiTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.qsi;

import org.apache.log4j.Logger;

class QsiInterfaceScaffold implements QsiListener {
	public QsiInterfaceScaffold() {
		rcvdReply = null;
		rcvdMsg = null;
	}
	public void message(QsiMessage m) {rcvdMsg = m;}
	public void reply(QsiReply r) {rcvdReply = r;}

	QsiReply rcvdReply;
	QsiMessage rcvdMsg;
	
	static Logger log = Logger.getLogger(QsiInterfaceScaffold.class.getName());

}

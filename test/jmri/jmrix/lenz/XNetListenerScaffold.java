/**
 * XNetListenerScaffold.java
 *
 * Description:	    test implementation of XNetListener
 * @author			Bob Jacobsen
 * @version         $Revision: 2.2 $
 */

package jmri.jmrix.lenz;

class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {
		public XNetListenerScaffold() {
			rcvdMsg = null;
			rcvdRply = null;
		}
		public void message(XNetReply m) {rcvdRply = m;}
		public void message(XNetMessage m) {rcvdMsg = m;}

	    static XNetReply rcvdRply;
	    private static XNetMessage rcvdMsg;
}

/**
 * XNetListenerScaffold.java
 *
 * Description:	    test implementation of XNetListener
 * @author			Bob Jacobsen
 * @version         $Revision: 2.4 $
 */

package jmri.jmrix.lenz;

public class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {
		public XNetListenerScaffold() {
			rcvdMsg = null;
			rcvdRply = null;
		}
		public void message(XNetReply m) {rcvdRply = m;}
		public void message(XNetMessage m) {rcvdMsg = m;}

	    static XNetReply rcvdRply;
	    @SuppressWarnings("unused")
		private static XNetMessage rcvdMsg;
}

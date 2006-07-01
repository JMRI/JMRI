/**
 * XNetListenerScaffold.java
 *
 * Description:	    test implementation of XNetListener
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */

package jmri.jmrix.lenz;

import jmri.*;

class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {
		public XNetListenerScaffold() {
			rcvdMsg = null;
		}
		public void message(XNetMessage m) {rcvdMsg = m;}

	    static XNetMessage rcvdMsg;
}

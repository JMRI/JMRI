/**
 * XNetListenerScaffold.java
 *
 * Description:	    test implementation of XNetListener
 * @author			Bob Jacobsen
 * @version         $Revision: 2.0 $
 */

package jmri.jmrix.lenz;

import jmri.*;

class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {
		public XNetListenerScaffold() {
			rcvdMsg = null;
		}
		public void message(XNetReply m) {rcvdMsg = m;}

	    static XNetReply rcvdMsg;
}

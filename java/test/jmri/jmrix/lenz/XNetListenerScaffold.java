/**
 * XNetListenerScaffold.java
 *
 * Description:	test implementation of XNetListener
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.lenz;

public class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {

    public XNetListenerScaffold() {
        rcvdMsg = null;
        rcvdRply = null;
        timeOutMsg = null;
        rcvCount = 0;
    }

    public void message(XNetReply m) {
        rcvdRply = m;
        rcvCount++;
    }

    public void message(XNetMessage m) {
        rcvdMsg = m;
    }

    public void notifyTimeout(XNetMessage m) {
        timeOutMsg = m;
    }

    int rcvCount;
    XNetReply rcvdRply;
    XNetMessage timeOutMsg;
    @SuppressWarnings("unused")
    private static XNetMessage rcvdMsg;
}

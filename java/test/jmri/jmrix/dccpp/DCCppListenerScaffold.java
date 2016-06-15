/**
 * DCCppListenerScaffold.java
 *
 * Description:	test implementation of DCCppListener
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 */
package jmri.jmrix.dccpp;

public class DCCppListenerScaffold implements jmri.jmrix.dccpp.DCCppListener {

    public DCCppListenerScaffold() {
        rcvdMsg = null;
        rcvdRply = null;
        timeOutMsg = null;
        rcvCount = 0;
    }

    public void message(DCCppReply m) {
        rcvdRply = m;
        rcvCount++;
    }

    public void message(DCCppMessage m) {
        rcvdMsg = m;
    }

    public void notifyTimeout(DCCppMessage m) {
        timeOutMsg = m;
    }

    int rcvCount;
    DCCppReply rcvdRply;
    DCCppMessage timeOutMsg;
    @SuppressWarnings("unused")
    private static DCCppMessage rcvdMsg;
}

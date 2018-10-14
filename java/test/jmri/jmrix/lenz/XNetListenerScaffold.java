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

    @Override
    public void message(XNetReply m) {
        rcvdRply = m;
        rcvCount++;
    }

    @Override
    public void message(XNetMessage m) {
        rcvdMsg = m;
    }

    @Override
    public void notifyTimeout(XNetMessage m) {
        timeOutMsg = m;
    }

    int rcvCount;
    XNetReply rcvdRply;
    XNetMessage timeOutMsg;
    @SuppressWarnings("unused")
    private static XNetMessage rcvdMsg;

    // required for access outside of package.
    public XNetReply getRcvdRply() {
       return rcvdRply; 
    }

    public void setRcvdRply(XNetReply r){
       rcvdRply = r;
    }

    public int getRcvCount(){
       return rcvCount;
    }

}

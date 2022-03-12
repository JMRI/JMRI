/**
 * XNetListenerScaffold.java
 *
 * test implementation of XNetListener
 *
 * @author Bob Jacobsen
 */
package jmri.jmrix.lenz;

public class XNetListenerScaffold implements jmri.jmrix.lenz.XNetListener {

    public XNetListenerScaffold() {
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
    }

    @Override
    public void notifyTimeout(XNetMessage m) {
        timeOutMsg = m;
    }

    int rcvCount;
    XNetReply rcvdRply;
    XNetMessage timeOutMsg;

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

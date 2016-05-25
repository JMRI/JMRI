/**
 * NceInterfaceScaffold.java
 *
 * Description:	Stands in for the NceTrafficController class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
package jmri.jmrix.nce;


class NceListenerScaffold implements jmri.jmrix.nce.NceListener {

    public NceListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    public void message(NceMessage m) {
        rcvdMsg = m;
    }

    public void reply(NceReply r) {
        rcvdReply = r;
    }

    NceReply rcvdReply;
    NceMessage rcvdMsg;

}

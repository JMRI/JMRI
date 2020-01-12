/**
 * Stands in for the NceTrafficController class.
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.nce;


class NceListenerScaffold implements jmri.jmrix.nce.NceListener {

    public NceListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    @Override
    public void message(NceMessage m) {
        rcvdMsg = m;
    }

    @Override
    public void reply(NceReply r) {
        rcvdReply = r;
    }

    NceReply rcvdReply;
    NceMessage rcvdMsg;

}

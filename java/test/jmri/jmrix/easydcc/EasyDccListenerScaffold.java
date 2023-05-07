package jmri.jmrix.easydcc;

/**
 * Stands in for the EasyDccTrafficController class
 *
 * @author Bob Jacobsen
 */
public class EasyDccListenerScaffold implements EasyDccListener {

    public EasyDccListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    @Override
    public void message(EasyDccMessage m) {
        rcvdMsg = m;
    }

    @Override
    public void reply(EasyDccReply r) {
        rcvdReply = r;
    }

    public EasyDccReply rcvdReply;
    public EasyDccMessage rcvdMsg;

}

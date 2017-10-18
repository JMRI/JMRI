/**
 * Stands in for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc;


class EasyDccInterfaceScaffold implements EasyDccListener {

    public EasyDccInterfaceScaffold() {
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

    EasyDccReply rcvdReply;
    EasyDccMessage rcvdMsg;

}

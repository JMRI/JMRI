/**
 * EasyDccInterfaceScaffold.java
 *
 * Description:	Stands in for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.easydcc;


class EasyDccInterfaceScaffold implements EasyDccListener {

    public EasyDccInterfaceScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    public void message(EasyDccMessage m) {
        rcvdMsg = m;
    }

    public void reply(EasyDccReply r) {
        rcvdReply = r;
    }

    EasyDccReply rcvdReply;
    EasyDccMessage rcvdMsg;

}

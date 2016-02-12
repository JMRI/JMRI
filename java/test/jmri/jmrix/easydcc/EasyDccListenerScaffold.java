// EasyDccListenerScaffold.java
package jmri.jmrix.easydcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen
 * @version
 */
class EasyDccListenerScaffold implements EasyDccListener {

    public EasyDccListenerScaffold() {
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

    private final static Logger log = LoggerFactory.getLogger(EasyDccListenerScaffold.class.getName());

}

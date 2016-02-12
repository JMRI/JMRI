// QsiListenerScaffold.java
package jmri.jmrix.qsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the QsiTrafficController class
 *
 * @author	Bob Jacobsen
 * @version
 */
class QsiListenerScaffold implements QsiListener {

    public QsiListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    public void message(QsiMessage m) {
        rcvdMsg = m;
    }

    public void reply(QsiReply r) {
        rcvdReply = r;
    }

    QsiReply rcvdReply;
    QsiMessage rcvdMsg;

    private final static Logger log = LoggerFactory.getLogger(QsiListenerScaffold.class.getName());

}

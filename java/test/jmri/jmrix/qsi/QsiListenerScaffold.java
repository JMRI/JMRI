package jmri.jmrix.qsi;

/**
 * Stands in for a QsiListener class
 *
 * @author Bob Jacobsen
 */
public class QsiListenerScaffold implements QsiListener {

    public QsiListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    @Override
    public void message(QsiMessage m) {
        rcvdMsg = m;
    }

    @Override
    public void reply(QsiReply r) {
        rcvdReply = r;
    }

    QsiReply rcvdReply;
    QsiMessage rcvdMsg;

}

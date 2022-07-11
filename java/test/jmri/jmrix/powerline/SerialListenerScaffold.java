package jmri.jmrix.powerline;

// class to simulate a Listener
public class SerialListenerScaffold implements SerialListener {

    public SerialReply rcvdReply;
    public SerialMessage rcvdMsg;

    public SerialListenerScaffold() {
        rcvdReply = null;
        rcvdMsg = null;
    }

    @Override
    public void message(SerialMessage m) {
        rcvdMsg = m;
    }

    @Override
    public void reply(SerialReply r) {
        rcvdReply = r;
    }

}

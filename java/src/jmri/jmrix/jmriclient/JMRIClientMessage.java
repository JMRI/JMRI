package jmri.jmrix.jmriclient;


/**
 * Encodes a message to an JMRIClient server. The JMRIClientReply class handles
 * the response from the server.
 * <p>
 * The {@link JMRIClientReply} class handles the response from the server.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientMessage extends jmri.jmrix.AbstractMRMessage {

    public JMRIClientMessage() {
        super();
    }

    // create a new one
    public JMRIClientMessage(int i) {
        super(i);
    }

    // copy one
    public JMRIClientMessage(JMRIClientMessage m) {
        super(m);
    }

    // from String
    public JMRIClientMessage(String m) {
        super(m);
    }

    // diagnose format
    public boolean isKillMain() {
        String s = toString();
        return s.contains("POWER OFF");
    }

    public boolean isEnableMain() {
        String s = toString();
        return s.contains("POWER ON");
    }

    // static methods to return a formatted message
    static public JMRIClientMessage getEnableMain() {
        JMRIClientMessage m = new JMRIClientMessage("POWER ON\n");
        m.setBinary(false);
        return m;
    }

    static public JMRIClientMessage getKillMain() {
        JMRIClientMessage m = new JMRIClientMessage("POWER OFF\n");
        m.setBinary(false);
        return m;
    }

    static public JMRIClientMessage getProgMode() {
        return null;
    }

    static public JMRIClientMessage getExitProgMode() {
        return null;
    }

    final static protected int LONG_TIMEOUT = 180000;  // e.g. for programming options

}

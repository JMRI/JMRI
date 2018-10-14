package jmri.jmrix.jmriclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an JMRIClientMessage.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
public class JMRIClientReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public JMRIClientReply() {
        super();
    }

    public JMRIClientReply(String s) {
        super(s);
    }

    public JMRIClientReply(JMRIClientReply l) {
        super(l);
    }

    public boolean isResponseOK() {
        return getResponseCode().charAt(0) == '1' || getResponseCode().charAt(0) == '2';
    }

    public String getResponseCode() {
        // split into 3 parts {TIMESTAMP, ResponseCode, Rest}
        // and use the second one (ResponseCode)
        String[] part = toString().split("\\s", 3);
        return part[1];
    }

    @Override
    protected int skipPrefix(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements() - 1
                && ((char) getElement(index) <= ' ')) {
            index++;
        }
        return index;
    }

    /**
     * Extracts Read-CV returned value from a message. Returns -1 if message
     * can't be parsed. Expects a message of the form 1264343601.156 100 INFO 1
     * SM -1 CV 8 99
     */
    @Override
    public int value() {
        String s = toString();
        String[] part = s.split("\\s", 10);
        int val = -1;

        try {
            int tmp = Integer.valueOf(part[8], 10).intValue();
            val = tmp;  // don't do this assign until now in case the conversion throws
        } catch (Exception e) {
            log.error("Unable to get number from reply: \"" + s + "\"");
        }
        return val;
    }

    @Override
    public boolean isUnsolicited() {
        String s = toString();
        // Split in 7 is enough for initial handshake 
        String[] part = s.split("\\s", 7);
        // Test for initial handshake message with key "JMRIClient".
        if (part[2].equals("JMRIClient")) {
            setUnsolicited();
            return true;
        } else {
            return false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientReply.class);

}




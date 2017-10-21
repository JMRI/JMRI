package jmri.jmrix.easydcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an EasyDccMessage.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004
 */
public class EasyDccReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public EasyDccReply() {
        super();
    }

    public EasyDccReply(String s) {
        super(s);
    }

    public EasyDccReply(EasyDccReply l) {
        super(l);
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
     * Extracts Read-CV returned value from a message.
     * Expects a message of the format "CVnnnvv" where vv is
     * the hexadecimal value or "Vnvv" where vv is the hexadecimal value.
     *
     * @return -1 if message can't be parsed
     */
    @Override
    public int value() {
        int index = 0;
        if ((char) getElement(index) == 'C') {
            // integer value of 6th, 7th digits in hex
            index = 5;  // 5th position is index 5
        } else if ((char) getElement(index) == 'V') {
            // integer value of 3rd, 4th digits in hex
            index = 2;  // 2nd position is index 2
        } else {
            log.warn("Did not find recognizable format: {}", this.toString());
        }
        String s1 = "" + (char) getElement(index);
        String s2 = "" + (char) getElement(index + 1);
        int val = -1;
        try {
            int sum = Integer.valueOf(s2, 16).intValue();
            sum += 16 * Integer.valueOf(s1, 16).intValue();
            val = sum;  // don't do this assign until now in case the conversion throws
        } catch (RuntimeException e) {
            log.error("Unable to get number from reply: \"" + s1 + s2 + "\" index: " + index
                    + " message: \"" + toString() + "\"");
        }
        return val;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccReply.class);

}

package jmri.jmrix.oaktree;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;


/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006
 */
@API(status = EXPERIMENTAL)
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public SerialReply() {
        super();
        setBinary(true);
    }

    public SerialReply(String s) {
        super(s);
        setBinary(true);
    }

    public SerialReply(SerialReply l) {
        super(l);
        setBinary(true);
    }

    /**
     * Is reply to poll message.
     * @return element 0.
     */
    public int getAddr() {
        return getElement(0);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}

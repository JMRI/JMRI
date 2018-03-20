package jmri.jmrix.acela;


/**
 * Contains the data payload of an Acela reply packet
 *
 * @author Bob Jacobsen Copyright (C) 2002
 *
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaReply extends jmri.jmrix.AbstractMRReply {

    // Create a new one
    public AcelaReply() {
        super();
    }

    public AcelaReply(String s) {
        super(s);
    }

    public AcelaReply(AcelaReply l) {
        super(l);
        l.setBinary(true);
    }

    //  Must be here since it is declared abstract
    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }
}

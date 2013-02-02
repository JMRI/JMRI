// AcelaReply.java

package jmri.jmrix.acela;

import org.apache.log4j.Logger;

/**
 * Contains the data payload of a Acela reply packet
 *
 * @author	Bob Jacobsen  Copyright (C) 2002
 * @version     $Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaReply extends jmri.jmrix.AbstractMRReply {

    // Create a new one
    public  AcelaReply() {
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
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }
    
    static Logger log = Logger.getLogger(AcelaReply.class.getName());
}

/* @(#)AcelaReply.java */

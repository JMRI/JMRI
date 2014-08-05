// EcosMessage.java
package jmri.jmrix.ecos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an Ecos command station.
 * <P>
 * The {@link EcosReply}
 * class handles the response from the command station.
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2001, 2008
 * @author Daniel Boudreau Copyright (C) 2007
 * @version     $Revision$
 */
public class EcosMessage extends jmri.jmrix.AbstractMRMessage {
	
    
    public EcosMessage() {
        super();
    }
    
    // create a new one
    public  EcosMessage(int i) {
        super(i);
    }

    // copy one
    public  EcosMessage(EcosMessage m) {
        super(m);
    }

    // from String
    public  EcosMessage(String m) {
        super(m);
    }

    static public EcosMessage getProgMode() {
		EcosMessage m = new EcosMessage();
		return m;
	}

    static public EcosMessage getExitProgMode() {
        EcosMessage m = new EcosMessage();
        return m;
    }
    
    static Logger log = LoggerFactory.getLogger(EcosMessage.class.getName());
}
/* @(#)EcosMessage.java */
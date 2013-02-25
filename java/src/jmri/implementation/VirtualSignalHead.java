// VirtualSignalHead.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A signal head that exists only within the program.
 * <P>
 * This can be useful e.g. as part of a more complex signal calculation.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision$
 */
public class VirtualSignalHead extends DefaultSignalHead {

    public VirtualSignalHead(String sys, String user) {
        super(sys, user);
    }

    public VirtualSignalHead(String sys) {
        super(sys);
    }
	
	protected void updateOutput() {
	}
	
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        super.dispose();
    }

    static Logger log = LoggerFactory.getLogger(VirtualSignalHead.class.getName());
}

/* @(#)VirtualSignalHead.java */

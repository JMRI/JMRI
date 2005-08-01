// VirtualSignalHead.java

package jmri;

/**
 * A signal head that exists only within the program.
 * <P>
 * This can be useful e.g. as part of a more complex signal calculation.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision: 1.1 $
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VirtualSignalHead.class.getName());
}

/* @(#)VirtualSignalHead.java */

// LnPacketizer.java

package jmri.jmrix.loconet.pr2;

/**
 * Special LnPacketizer implementation for PR2.
 * 
 * Differs only in handling PR2's non-echo
 *
 * @author			Bob Jacobsen  Copyright (C) 2006
 * @version 		$Revision: 1.3 $
 *
 */
public class LnPacketizer extends jmri.jmrix.loconet.LnPacketizer {

    final static boolean fulldebug = false;
  
  	boolean debug = false;
  	
    public LnPacketizer() {
        super();
    	self=this;
    	echo = true;
    	debug = log.isDebugEnabled();
   	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnPacketizer.class.getName());
}

/* @(#)LnPacketizer.java */

// LocoNetListener.java

package jmri.jmrix.loconet;

/** 
 * LocoNetListener provides the call-back interface for notification when a 
 * new LocoNet message arrives from the layout.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: LocoNetListener.java,v 1.4 2001-10-26 16:29:43 jacobsen Exp $		
 */
public interface LocoNetListener extends java.util.EventListener{

	/*
	 * Member function that will be invoked by a LocoNetInterface implementation
	 * to forward a LocoNet message from the layout.
	 *<P>
	 * Note that the LocoNetListener implementation cannot assume that this
	 * will be invoked in any particular thread.
	 *
	 * @param msg  The received LocoNet message.  Note that this same object
	 *             may be presented to multiple users. It should not be 
	 *             modified here.
	 */
	public void message(LocoNetMessage msg);
}


/* @(#)LocoNetListener.java */

/** 
 * LocoNetInterface.java
 *
 * Description:		<describe the LocoNetInterface class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;


public interface LocoNetInterface {
// the event notification contains the received message as source, not this object,
// so that we can notify of an incoming message to multiple places and then move on.
// "mask" is the OR of the key values of messages to be reported (to reduce traffic,
// provide for listeners interested in different things)

// request notification of things happening on the LocoNet. The same listener
// can register multiple times with different masks.  (Multiple registrations with
// a single mask value are equivalent to a single registration)
void addLocoNetListener(int mask, LocoNetListener l);

// stop notification of things happening on the LocoNet. Note that mask and LocoNetListener
// must match a previous request exactly.
void removeLocoNetListener(int mask, LocoNetListener l);


public boolean status();   // true if the implementation is operational

public void sendLocoNetMessage(LocoNetMessage m);
}


/* @(#)LocoNetInterface.java */

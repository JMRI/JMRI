/** 
 * SlotManager.java
 *
 * Description:		<describe the SlotManager class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */
 
 // This is a collection of LocoNetSlots, plus support for coordinating
 // them with the controller

package loconet;

import loconet.LocoNetSlot;
import loconet.SlotManager;
import loconet.LocoNetException;

import java.util.Vector;

public class SlotManager {

	public SlotManager() { 
	System.out.println("ctor with this = "+this);
	self = this; }

	// obtain a slot for a particular loco address
	// this will actually require a delayed return value - what impact does that have?
	LocoNetSlot fromAddress(int i) { return null; }
	
// method to find the existing SlotManager object
	static public final SlotManager instance() { return self;}
	static private SlotManager self = null;

// data members to hold contact with the listeners
	private Vector listeners = new Vector();
	
	public synchronized void addSlotListener(SlotListener l) { 
			// add only if not already registered
			if (!listeners.contains(l)) {
					listeners.addElement(l);
				}
		}

	public synchronized void removeSlotListener(SlotListener l) {
			if (listeners.contains(l)) {
					listeners.removeElement(l);
				}
		}

	protected void notify(LocoNetSlot s) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) listeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			SlotListener client = (SlotListener) listeners.elementAt(i);
			client.notifyChangedSlot(s);
						}
	}
	

}


/* @(#)SlotManager.java */

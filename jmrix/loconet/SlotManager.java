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

import ErrLoggerJ.ErrLog;

import java.util.Vector;

public class SlotManager implements LocoNetListener {

	private LocoNetSlot _slots[] = new LocoNetSlot[128];

	public LocoNetSlot slot(int i) {return _slots[i];}
	
	public SlotManager() { 
		// error if more than one constructed?
		if (self != null) 
			ErrLog.msg(ErrLog.error, "SlotManager", 
						"ctor", "Creating too many SlotManager objects");

		// initialize slot array
		for (int i=0; i<=127; i++) _slots[i] = new LocoNetSlot();
		
		// register this as the default
		self = this; 
			
		// listen to the LocoNet
		LnTrafficController.instance().addLocoNetListener(~0, this);	
		}

	// obtain a slot for a particular loco address
	// this will actually require a delayed return value - what impact does that have?
	LocoNetSlot fromLocoAddress(int i) { return null; }
	
// method to find the existing SlotManager object
	static public final SlotManager instance() { 
		if (self == null) self = new SlotManager();
		return self;
		}
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
	

	// listen to the LocoNet
	public void message(LocoNetMessage m) {
		int i = 0;

		switch (m.getOpCode()) {
			case LnConstants.OPC_WR_SL_DATA:
			case LnConstants.OPC_SL_RD_DATA:
				i = m.getElement(2);
				break;
							
			case LnConstants.OPC_LOCO_DIRF:
			case LnConstants.OPC_LOCO_SND:
			case LnConstants.OPC_LOCO_SPD: 
			case LnConstants.OPC_SLOT_STAT1: 
				i = m.getElement(1);
				break;

			case LnConstants.OPC_MOVE_SLOTS:  // handle the follow-on message when it comes
			default: 
				// nothing here for us
				return;
			}	
			
		// if here, i holds the slot number, and we expect to be able to parse		
		// and have the slot handle the message
		try {
			_slots[i].setSlot(m);
			}
		catch (LocoNetException e) {
			// must not have been interesting, or at least routed right
			ErrLog.msg(ErrLog.error, "SlotManager", 
						"message", "slot rejected LocoNetMessage"+m);
			return;
			}
		// notify listeners that slots may have changed
		notify(_slots[i]);
	}
	
}


/* @(#)SlotManager.java */

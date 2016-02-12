/* 
 * DCCppRegisterManager.java
 */

package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines and Manages the Registers (~ slots) for DCC++ Base Station
 *
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 */

/* A few notes on implementation
 *
 * This class is used by the DCCppCommandStation to allocate/free and keep
 * track of the registers in the BaseStation.  This is assuming the BaseStation
 * doesn't provide its own method of allocating registers.  It would be better
 * if the BaseStation handled this, since there may be more than just JMRI
 * asking for slots.
*/

public class DCCppRegisterManager {

    final protected DCCppRegister registers[] = new DCCppRegister[DCCppConstants.MAX_MAIN_REGISTERS];

    public DCCppRegisterManager() { 
	for (int i = 0; i < DCCppConstants.MAX_MAIN_REGISTERS; i++) {
	    registers[i] = new DCCppRegister();
	}
    }
    
    public int requestRegister(int addr) {
	for (int i = 0; i < DCCppConstants.MAX_MAIN_REGISTERS; i++) {
	    if (registers[i].getAddress() == addr) {
		registers[i].allocate();
		return(i);
	    }
	}
	// If we've made it here, there isn't a register that already matches.
	// Loop back through and find a free slot.
	for (int i = 0; i < DCCppConstants.MAX_MAIN_REGISTERS; i++) {
	    if (registers[i].isFree()) {
		registers[i].allocate();
		registers[i].setAddress(addr);
		return(i);
	    }
	}
	// If we've made it here, there is no available slot.  Bummer.
	return(DCCppConstants.NO_REGISTER_FREE);
    }

    public void releaseRegister(int addr) {
	for (int i = 0; i < DCCppConstants.MAX_MAIN_REGISTERS; i++) {
	    if (registers[i].getAddress() == addr) {
		registers[i].release();
	    }
	}
    }

    // NOTE: queryRegisterNum does not increment the use count.
    public int getRegisterNum(int addr) {
	for (int i = 0; i < DCCppConstants.MAX_MAIN_REGISTERS; i++) {
	    if (registers[i].getAddress() == addr) {
		return(i+1);
	    }
	}
	// Optional:  If a nonexistant register is requested, create one?
	return(DCCppConstants.NO_REGISTER_FREE);
    }

    public int getRegisterAddress(int num) {
	return(registers[num-1].getAddress());
    }

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(DCCppRegisterManager.class.getName());

}

class DCCppRegister {
    private int user_count;
    private int address;

    public DCCppRegister() {
	user_count = 0;
	address = DCCppConstants.REGISTER_UNALLOCATED;
    }

    public int getUserCount() { return(user_count); }
    public void setUserCount(int i) { user_count = i; } // Don't use this...
    public void incUserCount() { user_count++;}

    public void decUserCount() { 
	if (user_count > 0) { 
	    user_count--;
	}
	if (user_count == 0) {
	    address = DCCppConstants.REGISTER_UNALLOCATED;
	}
    }
    public int getAddress() { return(address); }
    public void setAddress(int a) { address = a; }
    public boolean isAllocated() { return(user_count > 0); }
    public boolean isFree() { return(user_count == 0); }
    public void allocate() { this.incUserCount(); }
    public void release() { this.decUserCount(); }
}
/* @(#)DCCppRegisterManager.java */

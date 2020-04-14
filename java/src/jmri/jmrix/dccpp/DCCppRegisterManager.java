/* 
 * DCCppRegisterManager.java
 */

package jmri.jmrix.dccpp;


/**
 * Defines and Manages the Registers (~ slots) for DCC++ Base Station
 *
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
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

    protected int maxMainRegisters = 0;
    protected DCCppRegister registers[];

    // Constuctors
    public DCCppRegisterManager(int maxMainRegisters) { 
	this.maxMainRegisters = maxMainRegisters;
	registers = new DCCppRegister[maxMainRegisters];
	for (int i = 0; i < maxMainRegisters; i++) {
	    registers[i] = new DCCppRegister();
	}
    }
    public DCCppRegisterManager() { 
	this(DCCppConstants.MAX_MAIN_REGISTERS);
    }
    
    // Member functions
    public int requestRegister(int addr) {
	int free = DCCppConstants.NO_REGISTER_FREE;

	for (int i = 0; i < maxMainRegisters; i++) {
	    if (registers[i].getAddress() == addr) {
		registers[i].allocate();
		return(i);
	    }
	    // This might be a free spot
	    if (free == DCCppConstants.NO_REGISTER_FREE && registers[i].isFree()) {
		free = i;
	    }
	}
	// If we've made it here, there isn't a register that already matches.
	// Look if we found a free one on the way through the list above
	// if not, there is no available slot.  Bummer.
	if (free != DCCppConstants.NO_REGISTER_FREE) {
	    registers[free].allocate();
	    registers[free].setAddress(addr);
	}
	return(free);
    }

    public void releaseRegister(int addr) {
	for (int i = 0; i < maxMainRegisters; i++) {
	    if (registers[i].getAddress() == addr) {
		registers[i].release();
	    }
	}
    }

    // NOTE: queryRegisterNum does not increment the use count.
    public int getRegisterNum(int addr) {
	for (int i = 0; i < maxMainRegisters; i++) {
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


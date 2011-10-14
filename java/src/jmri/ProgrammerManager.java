/* ProgrammerManager.java */

package jmri;

/**
 * Get access to available {@link Programmer} objects.
 * <P>
 * Programmers come in two types:
 * <UL>
 * <LI>Global, previously Service Mode, e.g. on a programming track
 * <LI>Addressed, previously Ops Mode, e.g. "programming on the main"
 * </UL>
 * You get a {@link Programmer} object from a ProgrammerManager, which in turn can be located
 * from the {@link InstanceManager}.
 * <P>
 * The ProgramerManager also provides a reserve/release
 * system for tools that want to pretend they have exclusive use of a Programmer.
 * This is a cooperative reservation; both tools (first and second reserver) must
 * be using the reserve/release interface.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public interface ProgrammerManager  {

    /**
     * Gain access to the Global Mode Programmer without reservation.
     * @return null only if there isn't a Global Mode Programmer available 
     * via this Manager.
     */
    public Programmer getGlobalProgrammer();
    
    /**
     * Gain access to a Addressed Mode Programmer without reservation.
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress Specific decoder address to use.
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress);
    
    /**
     * Gain access to the Global Mode Programmer, in the process reserving it
     * for yourself.
     * @return null if the existing Global Mode programmer is in use
     */
    public Programmer reserveGlobalProgrammer();
    
    /**
     * Return access to the Global Mode Programmer, so that it can
     * be used elsewhere.
     */
    public void releaseGlobalProgrammer(Programmer p);

    /**
     * Gain access to a (the) Addressed Mode Programmer, in the process
     * reserving it for yourself.
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress Specific decoder address to use.
     * @return null if the address is in use by a reserved programmer
     */
    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress);
    
    /**
     * Return access to the Global Mode Programmer, so that it can
     * be used elsewhere.
     */
    public void releaseAddressedProgrammer(Programmer p);

    /**
     * Convenience method to check whether you'll be able to get
     * a Global Mode programmer.
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable();
    
    /**
     * Convenience method to check whether you'll be able to get
     * an Addressed Mode programmer.
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible();
    
    public String getUserName();
    
}


/* @(#)Programmer.java */

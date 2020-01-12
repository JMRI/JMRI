package jmri.jmrix.openlcb;

import java.util.List;
import jmri.AddressedProgrammer;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;

/**
 * Get access to available {@link Programmer} objects.
 * <p>
 * Programmers come in two types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track. Request
 * these from an instance of {@link GlobalProgrammerManager}.
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main". Request
 * these from an instance of this interface.
 * </ul>
 * You get a {@link Programmer} object from a ProgrammerManager, which in turn
 * can be located from the {@link InstanceManager}.
 * <p>
 * This interface also provides a reserve/release system for tools that want to
 * pretend they have exclusive use of a Programmer. This is a cooperative
 * reservation; both tools (first and second reserver) must be using the
 * reserve/release interface.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2015
 * @since 4.1.1
 */
public class OlcbProgrammerManager extends jmri.managers.DefaultProgrammerManager {

    public OlcbProgrammerManager(Programmer pProgrammer) {
        super(pProgrammer);
    }

    public OlcbProgrammerManager(Programmer pProgrammer, jmri.jmrix.SystemConnectionMemo memo) {
        super(pProgrammer, memo);
    }


    public static final ProgrammingMode OPENLCBMODE = new ProgrammingMode("OPENLCBMODE", Bundle.getMessage("OPENLCBMODE"));
    
    /**
     * Gain access to a Addressed Mode Programmer without reservation.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     Specific decoder address to use.
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) { return null; }

    /**
     * Gain access to a (the) Addressed Mode Programmer, in the process
     * reserving it for yourself.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     Specific decoder address to use.
     * @return null if the address is in use by a reserved programmer
     */
    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) { return null; }

    /**
     * Return access to an Addressed Mode Programmer, so that it can be used
     * elsewhere.
     */
    @Override
    public void releaseAddressedProgrammer(AddressedProgrammer p) {}

    /**
     * Convenience method to check whether you'll be able to get an Addressed
     * Mode programmer.
     *
     * @return false if there's no chance of getting one
     */
    @Override
    public boolean isAddressedModePossible() { return false; }

    /**
     * Get the list of {@link ProgrammingMode} (generally) supported by
     * Programmers provided by this Manager.
     * <p>
     * Use this to enquire about modes before you're ready to request a specific
     * programmer.
     * <p>
     * If the order is significant, earlier modes are better.
     */
    @Override
    public List<ProgrammingMode> getDefaultModes() { return new java.util.ArrayList<>(); }

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should return a
     * user-provided name for this particular one.
     */
    @Override
    public String getUserName() { return "OpenLCB"; }

    /**
     * toString() provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should return a
     * user-provided name for this particular one.
     */
    @Override
    public String toString() { return "OlcbProgrammerManager"; }
}

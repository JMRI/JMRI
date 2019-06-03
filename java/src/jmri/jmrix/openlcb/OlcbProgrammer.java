package jmri.jmrix.openlcb;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provide access to the hardware DCC decoder programming capability.
 * <p>
 * Programmers come in multiple types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main"
 * </ul>
 * Different equipment may also require different programmers:
 * <ul>
 * <li>DCC CV programming, on service mode track or on the main
 * <li>CBUS Node Variable programmers
 * <li>LocoNet System Variable programmers
 * <li>LocoNet Op Switch programmers
 * <li>etc
 * </ul>
 * Depending on which type you have, only certain modes can be set. Valid modes
 * are specified by the class static constants.
 * <p>
 * You get a Programmer object from a {@link jmri.AddressedProgrammer}, which in turn
 * can be located from the {@link jmri.InstanceManager}.
 * <p>
 * Starting in JMRI 3.5.5, the CV addresses are Strings for generality. The
 * methods that use ints for CV addresses will later be deprecated.
 * <hr>
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
 * @see jmri.AddressedProgrammer
 * @author Bob Jacobsen Copyright (C) 2015
 * @since 4.1.1
 */
public class OlcbProgrammer extends jmri.jmrix.AbstractProgrammer implements jmri.AddressedProgrammer  {
    
    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> retval = new ArrayList<ProgrammingMode>();
        retval.add(OlcbProgrammerManager.OPENLCBMODE);
        return retval;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void timeout() {}
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() { return false;}

    /** 
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {return 0;}

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getAddress() { return "";}

}

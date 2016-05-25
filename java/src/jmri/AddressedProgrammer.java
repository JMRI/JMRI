package jmri;

/**
 * Provide access to the hardware DCC decoder programming capability.
 * <P>
 * Programmers come in multiple types:
 * <UL>
 * <LI>Global, previously Service Mode, e.g. on a programming track
 * <LI>Addressed, previously Ops Mode, e.g. "programming on the main"
 * </UL>
 * Different equipment may also require different programmers:
 * <ul>
 * <LI>DCC CV programming, on service mode track or on the main
 * <LI>CBUS Node Variable programmers
 * <LI>LocoNet System Variable programmers
 * <LI>LocoNet Op Switch programmers
 * <li>etc
 * </UL>
 * Depending on which type you have, only certain modes can be set. Valid modes
 * are specified by the class static constants.
 * <P>
 * You get a Programmer object from a {@link ProgrammerManager}, which in turn
 * can be located from the {@link InstanceManager}.
 * <p>
 * Starting in JMRI 3.5.5, the CV addresses are Strings for generality. The
 * methods that use ints for CV addresses will later be deprecated.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @see jmri.ProgrammerManager
 * @see jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2013, 2014
 */
public interface AddressedProgrammer extends Programmer {

    public boolean getLongAddress();

    public int getAddressNumber();

    public String getAddress();
}

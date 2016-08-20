package jmri;

/**
 * Get access to available {@link Programmer} objects.
 * <P>
 * Programmers come in two types:
 * <UL>
 * <LI>Global, previously Service Mode, e.g. on a programming track
 * <LI>Addressed, previously Ops Mode, e.g. "programming on the main"
 * </UL>
 * You get a {@link Programmer} object from a ProgrammerManager, which in turn
 * can be located from the {@link InstanceManager}.
 * <P>
 * This class forms the union of the {@link GlobalProgrammerManager} and
 * {@link AddressedProgrammerManager} interfaces that older code which looks for
 * a combined implementation can use. But it's deprecated, because the default
 * {@link GlobalProgrammerManager} and {@link AddressedProgrammerManager} are
 * likely to be different objects, so code should request the one it needs from
 * the instance manager.
 * <P>
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
 * @see jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @deprecated 3.9.6
 */
@Deprecated
public interface ProgrammerManager extends AddressedProgrammerManager, GlobalProgrammerManager {

}


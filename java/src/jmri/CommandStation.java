package jmri;

import javax.annotation.Nonnull;

/**
 * Provide a DCC command station's basic ability: Sending DCC packets to the
 * rails.
 * <p>
 * Note that this is separate from higher-level things like access to
 * {@link jmri.Throttle} capability (e.g. via {@link jmri.ThrottleManager}),
 * more convenient sending of accessory command messages via JMRI
 * {@link jmri.Turnout} objects, programming via service mode
 * ({@link jmri.Programmer}) or on-main programmers
 * ({@link jmri.AddressedProgrammer}) etc.
 * <P>
 * System-specific implementations can be obtained via the
 * {@link jmri.InstanceManager} class.
 *
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
 * @author Bob Jacobsen Copyright (C) 2003
 */
public interface CommandStation {

    /**
     * Send a specific packet to the rails.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte.
     * @param repeats Number of times to repeat the transmission.
     */
    public void sendPacket(@Nonnull byte[] packet, int repeats);

    public String getUserName();

    public String getSystemPrefix();

}

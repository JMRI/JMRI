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
 * <p>
 * System-specific implementations can be obtained via the
 * {@link jmri.InstanceManager} class.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public interface CommandStation {

    /**
     * Send a specific packet to the rails.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte.
     * @param repeats Number of times to repeat the transmission.
     *
     * @return {@code true} if the operation succeeds, {@code false} otherwise.
     */
    public boolean sendPacket(@Nonnull byte[] packet, int repeats);

    public String getUserName();

    @Nonnull
    public String getSystemPrefix();

}

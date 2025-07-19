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
 * @author Bob Jacobsen Copyright (C) 2003, 2024
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
    boolean sendPacket(@Nonnull byte[] packet, int repeats);

    String getUserName();

    @Nonnull
    String getSystemPrefix();

    /**
     * As a shortcut, and to allow for command station types
     * that cannot sent generic packets to the rails, we
     * provide this method to specifically send
     * Accessory Signal Decoder Packets.
     * <p>
     * It's equivalent to calling 
     * {@link NmraPacket#accSignalDecoderPkt}
     * and sending the resulting packet to the rails
     * @param address The DCC signal decoder address to use
     * @param aspect The signal aspect to send
     * @param count the number of times to repeat the send
     */
    default void sendAccSignalDecoderPkt(int address, int aspect, int count) {
        var packet = NmraPacket.accSignalDecoderPkt(address, aspect);
        sendPacket(packet, count);
    }

    /**
     * As a shortcut, and to allow for command station types
     * that cannot sent generic packets to the rails, we
     * provide this method to specifically send
     * the alternate form of Accessory Signal Decoder Packets.
     * <p>
     * It's equivalent to calling 
     * {@link NmraPacket#altAccSignalDecoderPkt}
     * and sending the resulting packet to the rails
     * @param address The DCC signal decoder address to use
     * @param aspect The signal aspect to send
     * @param count the number of times to repeat the send
     */
    default void sendAltAccSignalDecoderPkt(int address, int aspect, int count) {
        var packet = NmraPacket.altAccSignalDecoderPkt(address, aspect);
        sendPacket(packet, count);
    }
}

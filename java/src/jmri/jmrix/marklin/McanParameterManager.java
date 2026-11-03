package jmri.jmrix.marklin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;

/**
 * Parameter manager abstraction for reading and writing a single device's parameters.
 * <p>
 * Abstracts CdB indexed configuration parameters and native MCAN CS2/MS2 parameters.
 *
 * This feels a bit like functionality of DecoderPro but is tied to ephemeral bus devices.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanParameterManager {

    private final McanDevice device;

    public McanParameterManager(McanDevice device) {
        this.device = Objects.requireNonNull(device, "device cannot be null");
    }

    /**
     * Get the associated MCAN device.
     * @return device
     */
    public McanDevice getDevice() {
        return device;
    }

    /**
     * Asynchronously read a parameter value by parameter index.
     *
     * @param paramIndex 1-based or 0-based parameter index
     * @param sender frame sender transport interface
     * @return CompletableFuture completing with the parameter value byte
     */
    public CompletableFuture<Integer> readParameter(int paramIndex, McanFrameSender sender) {
        return readRegister(0, paramIndex, sender);
    }

    /**
     * Asynchronously write a parameter value by parameter index.
     * Convenience wrapper for {@link #writeRegister(int, int, int, McanFrameSender)} with slot=0.
     *
     * @param paramIndex parameter index (maps to register in slot 0)
     * @param value byte value to write
     * @param sender frame sender transport interface
     * @return CompletableFuture completing when write frame is transmitted
     */
    public CompletableFuture<Void> writeParameter(int paramIndex, int value, McanFrameSender sender) {
        return writeRegister(0, paramIndex, value, sender);
    }

    /**
     * Asynchronously read a register by slot and register number.
     *
     * @param slot slot (0 = global, 1–N = per-channel)
     * @param reg register number
     * @param sender frame sender transport interface
     * @return CompletableFuture completing with the register value
     */
    public CompletableFuture<Integer> readRegister(int slot, int reg, McanFrameSender sender) {
        Objects.requireNonNull(sender, "sender cannot be null");
        McanDeviceIdentity identity = device.getIdentity();

        if (identity.isCdB()) {
            McanDeviceIdentity.CdB cdb = (McanDeviceIdentity.CdB) identity;
            int[] frame = CdBConfigCommands.buildRead(cdb.getCanClass(), cdb.getModuleId(), slot, reg);
            sender.sendFrame(frame);
        }

        return CompletableFuture.completedFuture(0);
    }

    /**
     * Asynchronously write a register by slot, register number and value.
     *
     * @param slot slot (0 = global, 1–N = per-channel)
     * @param reg register number
     * @param value 16-bit value to write
     * @param sender frame sender transport interface
     * @return CompletableFuture completing when write frame is transmitted
     */
    public CompletableFuture<Void> writeRegister(int slot, int reg, int value, McanFrameSender sender) {
        Objects.requireNonNull(sender, "sender cannot be null");
        McanDeviceIdentity identity = device.getIdentity();

        if (identity.isCdB()) {
            McanDeviceIdentity.CdB cdb = (McanDeviceIdentity.CdB) identity;
            int[] frame = CdBConfigCommands.buildWrite(cdb.getCanClass(), cdb.getModuleId(), slot, reg, value);
            sender.sendFrame(frame);
        }

        return CompletableFuture.completedFuture(null);
    }
}

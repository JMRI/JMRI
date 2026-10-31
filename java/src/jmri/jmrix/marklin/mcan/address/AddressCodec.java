package jmri.jmrix.marklin.mcan.address;

/**
 * Converts between user-visible accessory addresses (format + 1-based number) and the
 * raw wire values stored in CdB configuration registers.
 * <p>
 * Two channel output types use different encoding schemes:
 * <ul>
 *   <li>Type 2 (solenoid drive): DCC, MM and CAN formats</li>
 *   <li>Type 3 (motor drive with end-stop evaluation): DCC and DCC ext. formats only</li>
 * </ul>
 */
public interface AddressCodec {

    /**
     * Encode a user address for a type-2 (solenoid drive) output channel.
     *
     * @param format  {@code "DCC"}, {@code "MM"} or {@code "CAN"}
     * @param address 1-based user address (raw wire value for CAN format)
     * @return raw wire value for storage in the CdB configuration register
     * @throws IllegalArgumentException for unknown formats
     */
    int encodeType2(String format, int address);

    /**
     * Decode a raw wire value for a type-2 (solenoid drive) output channel.
     *
     * @param rawAddr raw wire value from the CdB configuration register
     * @return decoded format and address
     */
    AddressEncoding decodeType2(int rawAddr);

    /**
     * Encode a user address for a type-3 (motor drive) output channel.
     *
     * @param format  {@code "DCC"} or {@code "DCC ext."}
     * @param address 1-based user address
     * @return raw wire value
     * @throws IllegalArgumentException for unsupported formats
     */
    int encodeType3(String format, int address);

    /**
     * Decode a raw wire value for a type-3 (motor drive) output channel.
     *
     * @param rawAddr raw wire value from the CdB configuration register
     * @return decoded format and address
     */
    AddressEncoding decodeType3(int rawAddr);
}

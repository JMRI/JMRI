package jmri.jmrix.marklin.mcan.address;

/**
 * CdB protocol accessory address codec, ported from {@code CdBMessage.encodeAddress} and
 * {@code CdBMessage.decodeAddress}.
 * <p>
 * Type-2 (solenoid drive) offsets:
 * <pre>
 *   DCC  →  raw = 14336 + address - 1
 *   MM   →  raw = 12288 + address - 1
 *   CAN  →  raw = address  (no offset; raw IS the address)
 * </pre>
 * Type-3 (motor drive with end-stop evaluation) offsets:
 * <pre>
 *   DCC      →  raw = 12288 + address - 1
 *   DCC ext. →  raw = 12800 + address + 3
 * </pre>
 * Do not confuse with the operational-command address offsets used by the CS2 CAN ID
 * Local-ID sub-range encoding (not a configuration register).
 */
public final class CdBAddressCodec implements AddressCodec {

    public static final CdBAddressCodec INSTANCE = new CdBAddressCodec();

    // Type-2 base offsets
    static final int BASE_DCC_TYPE2 = 14336;
    static final int BASE_MM_TYPE2  = 12288;

    // Type-3 base offsets
    static final int BASE_DCC_TYPE3     = 12288;
    static final int BASE_DCC_EXT_TYPE3 = 12800;

    private CdBAddressCodec() {}

    @Override
    public int encodeType2(String format, int address) {
        switch (format) {
            case "DCC": return BASE_DCC_TYPE2 + address - 1;
            case "MM":  return BASE_MM_TYPE2  + address - 1;
            case "CAN": return address;
            default: throw new IllegalArgumentException("Unknown type-2 address format: " + format);
        }
    }

    @Override
    public AddressEncoding decodeType2(int rawAddr) {
        if (rawAddr >= BASE_DCC_TYPE2) {
            return new AddressEncoding("DCC", rawAddr - BASE_DCC_TYPE2 + 1);
        } else if (rawAddr >= BASE_MM_TYPE2) {
            return new AddressEncoding("MM",  rawAddr - BASE_MM_TYPE2  + 1);
        } else {
            return new AddressEncoding("CAN", rawAddr);
        }
    }

    @Override
    public int encodeType3(String format, int address) {
        if ("DCC ext.".equals(format)) {
            return BASE_DCC_EXT_TYPE3 + address + 3;
        } else if ("DCC".equals(format)) {
            return BASE_DCC_TYPE3 + address - 1;
        }
        throw new IllegalArgumentException("Unsupported type-3 address format: " + format);
    }

    @Override
    public AddressEncoding decodeType3(int rawAddr) {
        if (rawAddr >= BASE_DCC_EXT_TYPE3) {
            return new AddressEncoding("DCC ext.", rawAddr - BASE_DCC_EXT_TYPE3 - 3);
        }
        return new AddressEncoding("DCC", rawAddr - BASE_DCC_TYPE3 + 1);
    }
}
